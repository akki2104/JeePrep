package com.jeeprep.app.ai

import android.util.Log
import com.jeeprep.app.BuildConfig
import com.jeeprep.app.data.db.entity.QuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiApiService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        // Gemini 2.5 Flash Lite — free tier available
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"
        private const val TAG = "GeminiApi"
    }

    val isConfigured: Boolean get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    /**
     * Generate JEE-level MCQ questions for a given topic using Gemini Flash API.
     */
    suspend fun generateQuestions(
        subject: String,
        topic: String,
        count: Int = 3,
        difficulty: String = "Mixed"
    ): Result<List<QuestionEntity>> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(Exception("Download not available"))
        }

        try {
            val prompt = buildPrompt(subject, topic, count, difficulty)
            val responseText = callGemini(prompt)
            val questions = parseQuestions(responseText)
            Log.d(TAG, "Generated ${questions.size} questions for $topic")
            Result.success(questions)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating questions", e)
            Result.failure(e)
        }
    }

    private fun buildPrompt(subject: String, topic: String, count: Int, difficulty: String): String = """
You are a JEE Mains/Advanced exam question generator. Generate exactly $count unique MCQ questions for:

Subject: $subject
Topic: $topic
Difficulty: $difficulty (if Mixed, include Easy, Medium, and Hard questions)

IMPORTANT RULES:
- Questions must be JEE Mains/Advanced level
- Use LaTeX notation for math: \frac{}{}, \sqrt{}, ^{}, _{}, \vec{}, \int, \sum, etc.
- Each question must have exactly 4 options (A, B, C, D)
- Exactly one correct answer per question
- Include numerical problems, not just theory
- Vary the difficulty

Return ONLY a JSON array (no markdown, no explanation), where each object has:
{
  "question": "question text with LaTeX",
  "optionA": "option A text",
  "optionB": "option B text",
  "optionC": "option C text",
  "optionD": "option D text",
  "correctAnswer": "A" or "B" or "C" or "D",
  "difficulty": "Easy" or "Medium" or "Hard",
  "explanation": "brief solution explanation with LaTeX"
}

Return ONLY the JSON array, starting with [ and ending with ].
""".trimIndent()

    private fun callGemini(prompt: String): String {
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.8)
                put("maxOutputTokens", 4096)
            })
        }

        val url = "$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}"
        val body = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            Log.e(TAG, "API error ${response.code}: $responseBody")
            throw Exception("API error ${response.code}: ${extractErrorMessage(responseBody)}")
        }

        // Extract text from Gemini response
        // Gemini 2.5 models may have multiple parts (thinking + response)
        val json = JSONObject(responseBody)
        val candidates = json.getJSONArray("candidates")
        if (candidates.length() == 0) throw Exception("No candidates in response")

        val content = candidates.getJSONObject(0).getJSONObject("content")
        val parts = content.getJSONArray("parts")
        if (parts.length() == 0) throw Exception("No parts in response")

        // Find the last text part (skip thinking parts)
        var resultText = ""
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            if (part.has("text")) {
                resultText = part.getString("text")
            }
        }
        if (resultText.isBlank()) throw Exception("No text in response parts")

        Log.d(TAG, "Raw response (first 500 chars): ${resultText.take(500)}")
        return resultText
    }

    private fun parseQuestions(responseText: String): List<QuestionEntity> {
        // Strip markdown code fences if present
        val cleaned = responseText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val jsonArray = try {
            JSONArray(cleaned)
        } catch (e: Exception) {
            // Try to find JSON array in the response
            val start = cleaned.indexOf('[')
            val end = cleaned.lastIndexOf(']')
            if (start >= 0 && end > start) {
                JSONArray(cleaned.substring(start, end + 1))
            } else {
                throw Exception("Could not parse JSON array from response")
            }
        }

        val questions = mutableListOf<QuestionEntity>()
        for (i in 0 until jsonArray.length()) {
            try {
                val obj = jsonArray.getJSONObject(i)
                val correctAnswer = obj.getString("correctAnswer").uppercase().trim()
                if (correctAnswer !in listOf("A", "B", "C", "D")) continue

                questions.add(
                    QuestionEntity(
                        topicId = 0, // Caller sets this
                        questionText = obj.getString("question"),
                        optionA = obj.getString("optionA"),
                        optionB = obj.getString("optionB"),
                        optionC = obj.getString("optionC"),
                        optionD = obj.getString("optionD"),
                        correctAnswer = correctAnswer,
                        explanation = obj.optString("explanation", null),
                        difficulty = obj.optString("difficulty", "Medium"),
                        isAiGenerated = true
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Skipping malformed question at index $i: ${e.message}")
            }
        }

        return questions
    }

    private fun extractErrorMessage(body: String): String {
        return try {
            val json = JSONObject(body)
            json.getJSONObject("error").getString("message")
        } catch (e: Exception) {
            body.take(200)
        }
    }
}
