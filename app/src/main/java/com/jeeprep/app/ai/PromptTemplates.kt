package com.jeeprep.app.ai

object PromptTemplates {

    const val DEFAULT_SYSTEM = """You are a JEE tutor. Give short, clear answers. Use markdown: **bold**, bullet points, numbered lists. Use LaTeX for math: ${'$'}x^2${'$'}, ${'$'}\frac{a}{b}${'$'}, ${'$'}\sqrt{x}${'$'}, ${'$'}\Delta p${'$'}. Use dollar-dollar for important equations on their own line. Keep answers concise and exam-focused."""

    /** Strip any LaTeX that the model still generates */
    fun stripLatex(text: String): String {
        return text
            .replace(Regex("""\$\$(.+?)\$\$""", RegexOption.DOT_MATCHES_ALL)) { it.groupValues[1].trim() }
            .replace(Regex("""\$(.+?)\$""")) { it.groupValues[1].trim() }
            .replace(Regex("""\\text\{(.+?)}""")) { it.groupValues[1] }
            .replace(Regex("""\\frac\{(.+?)}\{(.+?)}""")) { "(${it.groupValues[1]})/(${it.groupValues[2]})" }
            .replace("\\Delta", "Δ")
            .replace("\\pi", "π")
            .replace("\\times", "×")
            .replace("\\cdot", "·")
            .replace("\\sqrt", "sqrt")
            .replace("\\infty", "∞")
            .replace("\\leq", "≤")
            .replace("\\geq", "≥")
            .replace("\\neq", "≠")
            .replace("\\approx", "≈")
            .replace("\\rightarrow", "→")
            .replace("\\alpha", "α")
            .replace("\\beta", "β")
            .replace("\\theta", "θ")
            .replace("\\omega", "ω")
            .replace("\\lambda", "λ")
            .replace("\\mu", "μ")
    }

    fun quickExplain(question: String, options: List<String>, correctAnswer: String): String {
        return """Explain this JEE question in 3-4 short numbered steps.

Question: $question
Options: A) ${options[0]}, B) ${options[1]}, C) ${options[2]}, D) ${options[3]}
Correct Answer: $correctAnswer

Give ONLY the solution steps. Each step should be one line. Start each step with the step number. Keep it under 60 words total."""
    }

    fun detailedExplain(question: String, options: List<String>, correctAnswer: String): String {
        return """Give a detailed explanation for this JEE question.

Question: $question
Options: A) ${options[0]}, B) ${options[1]}, C) ${options[2]}, D) ${options[3]}
Correct Answer: $correctAnswer

Include:
- The underlying concept/formula
- Full step-by-step solution
- Why each wrong option is incorrect
- Related concepts to revise

Use markdown formatting. Keep it under 200 words."""
    }

    fun trickForQuestion(question: String, options: List<String>, correctAnswer: String): String {
        return """Is there a shortcut or trick to solve this JEE question quickly?

Question: $question
Options: A) ${options[0]}, B) ${options[1]}, C) ${options[2]}, D) ${options[3]}
Correct Answer: $correctAnswer

If yes, explain the shortcut in 2-3 lines. If no shortcut exists, say "No special trick — solve step by step" and give the fastest approach in 2 lines. Keep it under 50 words."""
    }

    // Keep old method for backwards compat (used in QuestionRepository)
    fun explainAnswer(question: String, options: List<String>, correctAnswer: String): String {
        return quickExplain(question, options, correctAnswer)
    }

    fun generateMCQ(subject: String, topic: String, difficulty: String = "Medium"): String {
        return """Generate exactly 1 JEE Mains level MCQ on $subject - $topic.
Difficulty: $difficulty

You MUST respond with ONLY valid JSON in this exact format, no other text:
{
  "question": "question text here",
  "optionA": "first option",
  "optionB": "second option",
  "optionC": "third option",
  "optionD": "fourth option",
  "correctAnswer": "A",
  "explanation": "brief explanation of why the answer is correct"
}"""
    }

    fun generateNotes(subject: String, topic: String): String {
        return """Write ultra-detailed JEE revision notes for $subject - $topic.

Structure the notes with these sections using markdown headings:

## 1. Core Concepts
- Explain every key concept in this topic thoroughly
- Include definitions, physical significance, and intuition
- Use LaTeX (${'$'}...${'$'}) for all formulas and equations

## 2. Important Formulas & Derivations
- List ALL important formulas with brief derivation hints
- Mention when/where each formula applies
- Highlight frequently tested formulas with **bold**

## 3. Solved Examples
- Provide 2-3 solved examples covering different difficulty levels
- Show step-by-step solution for each

## 4. Common Mistakes & Pitfalls
- List 4-5 common errors students make
- Explain why each is wrong and how to avoid it

## 5. Tips & Tricks for JEE
- Quick-solve techniques and shortcuts
- Dimensional analysis tricks
- Elimination strategies for MCQs

## 6. Previous Year Questions (PYQ) Highlights
- Mention 3-4 types of questions asked from this topic in JEE Mains/Advanced
- Describe the pattern and approach for each type

## 7. Quick Revision Checklist
- Bullet points for last-minute revision

Use markdown formatting throughout. Use LaTeX for math: ${'$'}F = ma${'$'}, ${'$'}\\frac{d}{dx}${'$'}, etc. Be thorough — this should be a complete study resource. Minimum 800 words."""
    }

    fun generateMemoryTrick(concept: String, subject: String): String {
        return """Create a fun, memorable mnemonic or memory trick for this JEE concept:

Subject: $subject
Concept: $concept

Requirements:
- Make it catchy and easy to remember
- Use acronyms, stories, or visual associations
- Include what each part of the mnemonic refers to
- Keep it short (students should memorize it in under 30 seconds)"""
    }

    fun analyzeWeakness(topicStats: String): String {
        return """Based on this student's performance data, provide a brief analysis:

$topicStats

Give:
1. Top 3 weak areas that need immediate attention
2. One specific tip for each weak area
3. Suggested practice order (what to study first)

Keep it encouraging and actionable. Max 150 words."""
    }

    fun chatSystemPrompt(questionText: String, correctAnswer: String, explanation: String): String {
        return """You are a JEE tutor having a conversation about this question:

Question: $questionText
Correct Answer: $correctAnswer
Explanation: $explanation

Help the student understand this question deeply. Answer follow-up questions, explain related concepts, and suggest similar problems. Use markdown formatting. Write math as x^2, sqrt(x). Be encouraging and concise (under 100 words per response)."""
    }
}
