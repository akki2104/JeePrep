package com.jeeprep.app.data

import android.content.Context
import com.jeeprep.app.data.db.dao.QuestionDao
import com.jeeprep.app.data.db.dao.SubjectDao
import com.jeeprep.app.data.db.dao.TopicDao
import com.jeeprep.app.data.db.entity.QuestionEntity
import com.jeeprep.app.data.db.entity.SubjectEntity
import com.jeeprep.app.data.db.entity.TopicEntity
import org.json.JSONObject

object SeedData {

    val subjects = listOf(
        SubjectEntity(id = 1, name = "Physics", iconName = "physics"),
        SubjectEntity(id = 2, name = "Chemistry", iconName = "chemistry"),
        SubjectEntity(id = 3, name = "Mathematics", iconName = "math")
    )

    val topics = listOf(
        // Physics (1-20)
        TopicEntity(id = 1, subjectId = 1, name = "Mechanics", displayOrder = 1),
        TopicEntity(id = 2, subjectId = 1, name = "Kinematics", displayOrder = 2),
        TopicEntity(id = 3, subjectId = 1, name = "Laws of Motion", displayOrder = 3),
        TopicEntity(id = 4, subjectId = 1, name = "Work, Energy & Power", displayOrder = 4),
        TopicEntity(id = 5, subjectId = 1, name = "Rotational Motion", displayOrder = 5),
        TopicEntity(id = 6, subjectId = 1, name = "Gravitation", displayOrder = 6),
        TopicEntity(id = 7, subjectId = 1, name = "Properties of Matter", displayOrder = 7),
        TopicEntity(id = 8, subjectId = 1, name = "Thermodynamics", displayOrder = 8),
        TopicEntity(id = 9, subjectId = 1, name = "Kinetic Theory of Gases", displayOrder = 9),
        TopicEntity(id = 10, subjectId = 1, name = "Oscillations & Waves", displayOrder = 10),
        TopicEntity(id = 11, subjectId = 1, name = "Electrostatics", displayOrder = 11),
        TopicEntity(id = 12, subjectId = 1, name = "Current Electricity", displayOrder = 12),
        TopicEntity(id = 13, subjectId = 1, name = "Magnetic Effects of Current", displayOrder = 13),
        TopicEntity(id = 14, subjectId = 1, name = "Electromagnetic Induction", displayOrder = 14),
        TopicEntity(id = 15, subjectId = 1, name = "Optics", displayOrder = 15),
        TopicEntity(id = 16, subjectId = 1, name = "Modern Physics", displayOrder = 16),
        TopicEntity(id = 17, subjectId = 1, name = "Semiconductors", displayOrder = 17),

        // Chemistry (101-120)
        TopicEntity(id = 101, subjectId = 2, name = "Atomic Structure", displayOrder = 1),
        TopicEntity(id = 102, subjectId = 2, name = "Chemical Bonding", displayOrder = 2),
        TopicEntity(id = 103, subjectId = 2, name = "Periodic Table", displayOrder = 3),
        TopicEntity(id = 104, subjectId = 2, name = "States of Matter", displayOrder = 4),
        TopicEntity(id = 105, subjectId = 2, name = "Thermodynamics (Chem)", displayOrder = 5),
        TopicEntity(id = 106, subjectId = 2, name = "Equilibrium", displayOrder = 6),
        TopicEntity(id = 107, subjectId = 2, name = "Redox Reactions", displayOrder = 7),
        TopicEntity(id = 108, subjectId = 2, name = "Organic Chemistry Basics", displayOrder = 8),
        TopicEntity(id = 109, subjectId = 2, name = "Hydrocarbons", displayOrder = 9),
        TopicEntity(id = 110, subjectId = 2, name = "Alcohols, Phenols & Ethers", displayOrder = 10),
        TopicEntity(id = 111, subjectId = 2, name = "Aldehydes, Ketones & Acids", displayOrder = 11),
        TopicEntity(id = 112, subjectId = 2, name = "Coordination Compounds", displayOrder = 12),
        TopicEntity(id = 113, subjectId = 2, name = "Electrochemistry", displayOrder = 13),
        TopicEntity(id = 114, subjectId = 2, name = "Chemical Kinetics", displayOrder = 14),
        TopicEntity(id = 115, subjectId = 2, name = "Surface Chemistry", displayOrder = 15),
        TopicEntity(id = 116, subjectId = 2, name = "p-Block Elements", displayOrder = 16),
        TopicEntity(id = 117, subjectId = 2, name = "d & f Block Elements", displayOrder = 17),
        TopicEntity(id = 118, subjectId = 2, name = "Biomolecules & Polymers", displayOrder = 18),

        // Mathematics (201-220)
        TopicEntity(id = 201, subjectId = 3, name = "Sets, Relations & Functions", displayOrder = 1),
        TopicEntity(id = 202, subjectId = 3, name = "Complex Numbers", displayOrder = 2),
        TopicEntity(id = 203, subjectId = 3, name = "Quadratic Equations", displayOrder = 3),
        TopicEntity(id = 204, subjectId = 3, name = "Permutations & Combinations", displayOrder = 4),
        TopicEntity(id = 205, subjectId = 3, name = "Binomial Theorem", displayOrder = 5),
        TopicEntity(id = 206, subjectId = 3, name = "Sequences & Series", displayOrder = 6),
        TopicEntity(id = 207, subjectId = 3, name = "Matrices & Determinants", displayOrder = 7),
        TopicEntity(id = 208, subjectId = 3, name = "Limits & Continuity", displayOrder = 8),
        TopicEntity(id = 209, subjectId = 3, name = "Differentiation", displayOrder = 9),
        TopicEntity(id = 210, subjectId = 3, name = "Integration", displayOrder = 10),
        TopicEntity(id = 211, subjectId = 3, name = "Differential Equations", displayOrder = 11),
        TopicEntity(id = 212, subjectId = 3, name = "Coordinate Geometry", displayOrder = 12),
        TopicEntity(id = 213, subjectId = 3, name = "Straight Lines", displayOrder = 13),
        TopicEntity(id = 214, subjectId = 3, name = "Conic Sections", displayOrder = 14),
        TopicEntity(id = 215, subjectId = 3, name = "3D Geometry", displayOrder = 15),
        TopicEntity(id = 216, subjectId = 3, name = "Vectors", displayOrder = 16),
        TopicEntity(id = 217, subjectId = 3, name = "Probability", displayOrder = 17),
        TopicEntity(id = 218, subjectId = 3, name = "Statistics", displayOrder = 18),
        TopicEntity(id = 219, subjectId = 3, name = "Trigonometry", displayOrder = 19)
    )

    // Sample seed questions — in production, load ~3000 PYQs from a JSON asset file
    private fun loadQuestionsFromAssets(context: Context): List<QuestionEntity> {
        val json = context.assets.open("questions.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val arr = root.getJSONArray("questions")
        val list = mutableListOf<QuestionEntity>()
        for (i in 0 until arr.length()) {
            val q = arr.getJSONObject(i)
            list.add(
                QuestionEntity(
                    topicId = q.getInt("topicId"),
                    questionText = q.getString("questionText"),
                    optionA = q.getString("optionA"),
                    optionB = q.getString("optionB"),
                    optionC = q.getString("optionC"),
                    optionD = q.getString("optionD"),
                    correctAnswer = q.getString("correctAnswer"),
                    difficulty = q.optString("difficulty", "Medium"),
                    year = if (q.has("year")) q.getInt("year") else null,
                    examType = q.optString("examType", "Mains")
                )
            )
        }
        return list
    }

    private fun getJsonVersion(context: Context): Int {
        val json = context.assets.open("questions.json").bufferedReader().use { it.readText() }
        return JSONObject(json).optInt("version", 1)
    }

    suspend fun seedDatabase(context: Context, subjectDao: SubjectDao, topicDao: TopicDao, questionDao: QuestionDao) {
        val prefs = context.getSharedPreferences("jeeprep_seed", Context.MODE_PRIVATE)
        val lastVersion = prefs.getInt("question_bank_version", 0)
        val currentVersion = getJsonVersion(context)

        if (lastVersion >= currentVersion) return

        // Clear old questions and re-insert from JSON
        questionDao.deleteAll()
        subjectDao.insertAll(subjects)
        topicDao.insertAll(topics)
        questionDao.insertAll(loadQuestionsFromAssets(context))

        prefs.edit().putInt("question_bank_version", currentVersion).apply()
    }
}
