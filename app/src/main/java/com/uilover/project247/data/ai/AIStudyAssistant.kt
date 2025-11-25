package com.uilover.project247.data.ai

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.uilover.project247.data.repository.StudyResult
import com.uilover.project247.data.repository.UserProgressManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class StudyAnalysis(
    val overallScore: Int, // 0-100
    val strengthAreas: List<String>,
    val weaknessAreas: List<String>,
    val recommendation: String,
    val motivationalMessage: String
)

data class WordRecommendation(
    val topicId: String,
    val topicName: String,
    val reason: String,
    val priority: Int // 1-5
)

data class AIQuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String,
    val difficulty: String // "easy", "medium", "hard"
)

class AIStudyAssistant(private val context: Context) {
    
    private val progressManager = UserProgressManager(context)
    
    // TODO: Replace with your Gemini API key
    private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    /**
     * Phân tích tiến trình học tập của user
     */
    suspend fun analyzeStudyProgress(): Result<StudyAnalysis> = withContext(Dispatchers.IO) {
        try {
            val history = progressManager.getStudyHistory()
            val completedTopics = progressManager.getCompletedTopics()
            
            if (history.isEmpty()) {
                return@withContext Result.success(
                    StudyAnalysis(
                        overallScore = 0,
                        strengthAreas = emptyList(),
                        weaknessAreas = emptyList(),
                        recommendation = "Bắt đầu học từ vựng để nhận được phân tích từ AI!",
                        motivationalMessage = "Hãy bắt đầu hành trình học tập của bạn! 🚀"
                    )
                )
            }

            val prompt = buildAnalysisPrompt(history, completedTopics.values.toList())
            
            val response = generativeModel.generateContent(prompt)
            val analysis = parseAnalysisResponse(response.text ?: "")
            
            Result.success(analysis)
        } catch (e: Exception) {
            Log.e("AIStudyAssistant", "Error analyzing progress", e)
            Result.failure(e)
        }
    }

    /**
     * Gợi ý từ/topic cần ôn tập
     */
    suspend fun getReviewRecommendations(): Result<List<WordRecommendation>> = withContext(Dispatchers.IO) {
        try {
            val history = progressManager.getStudyHistory()
            val completedTopics = progressManager.getCompletedTopics()
            
            if (history.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val prompt = buildReviewPrompt(history, completedTopics.values.toList())
            
            val response = generativeModel.generateContent(prompt)
            val recommendations = parseReviewResponse(response.text ?: "")
            
            Result.success(recommendations)
        } catch (e: Exception) {
            Log.e("AIStudyAssistant", "Error getting recommendations", e)
            Result.failure(e)
        }
    }

    /**
     * Tạo quiz cá nhân hóa dựa trên lịch sử học
     */
    suspend fun generatePersonalizedQuiz(
        topicId: String,
        numberOfQuestions: Int = 5
    ): Result<List<AIQuizQuestion>> = withContext(Dispatchers.IO) {
        try {
            val history = progressManager.getStudyHistory()
                .filter { it.topicId == topicId }
            
            val topicCompletion = progressManager.getTopicCompletion(topicId)
            
            val prompt = buildQuizPrompt(topicId, topicCompletion, numberOfQuestions)
            
            val response = generativeModel.generateContent(prompt)
            val questions = parseQuizResponse(response.text ?: "")
            
            Result.success(questions)
        } catch (e: Exception) {
            Log.e("AIStudyAssistant", "Error generating quiz", e)
            Result.failure(e)
        }
    }

    // ============ PROMPT BUILDERS ============

    private fun buildAnalysisPrompt(
        history: List<StudyResult>,
        completedTopics: List<com.uilover.project247.data.repository.TopicCompletionStatus>
    ): String {
        val totalStudyTime = history.sumOf { it.timeSpent } / 60000 // minutes
        val avgAccuracy = if (history.isNotEmpty()) 
            history.map { it.accuracy }.average().toInt() else 0
        val totalWords = history.sumOf { it.totalItems }
        val totalCorrect = history.sumOf { it.correctCount }

        return """
You are an AI English learning coach. Analyze this student's learning data and provide insights in Vietnamese.

STUDY DATA:
- Total study sessions: ${history.size}
- Total study time: $totalStudyTime minutes
- Average accuracy: $avgAccuracy%
- Total words studied: $totalWords
- Total correct answers: $totalCorrect
- Completed topics: ${completedTopics.size}

Recent performance (last 5 sessions):
${history.take(5).joinToString("\n") { 
    "- ${it.topicName}: ${it.accuracy.toInt()}% (${it.correctCount}/${it.totalItems})"
}}

Provide analysis in JSON format:
{
  "overallScore": <0-100>,
  "strengthAreas": ["area1", "area2"],
  "weaknessAreas": ["area1", "area2"],
  "recommendation": "detailed recommendation in Vietnamese",
  "motivationalMessage": "encouraging message in Vietnamese"
}

Only return valid JSON, no other text.
        """.trimIndent()
    }

    private fun buildReviewPrompt(
        history: List<StudyResult>,
        completedTopics: List<com.uilover.project247.data.repository.TopicCompletionStatus>
    ): String {
        val topicsNeedingReview = completedTopics
            .filter { 
                System.currentTimeMillis() - it.lastStudyDate > 7 * 24 * 60 * 60 * 1000 // 7 days
            }

        return """
You are an AI English learning coach. Based on the student's learning history, recommend topics/words that need review.

COMPLETED TOPICS:
${completedTopics.joinToString("\n") {
    "- ${it.topicId}: Best accuracy ${it.bestAccuracy.toInt()}%, Last studied ${daysAgo(it.lastStudyDate)} days ago"
}}

TOPICS NEEDING REVIEW (not studied in 7+ days):
${topicsNeedingReview.joinToString("\n") { "- ${it.topicId}" }}

LOW ACCURACY TOPICS (< 70%):
${completedTopics.filter { it.bestAccuracy < 70 }.joinToString("\n") { 
    "- ${it.topicId}: ${it.bestAccuracy.toInt()}%" 
}}

Recommend 3-5 topics to review. Return as JSON array:
[
  {
    "topicId": "topic_id",
    "topicName": "topic name",
    "reason": "reason in Vietnamese",
    "priority": 1-5
  }
]

Only return valid JSON array, no other text.
        """.trimIndent()
    }

    private fun buildQuizPrompt(
        topicId: String,
        topicCompletion: com.uilover.project247.data.repository.TopicCompletionStatus?,
        numberOfQuestions: Int
    ): String {
        val accuracy = topicCompletion?.bestAccuracy?.toInt() ?: 0
        val difficulty = when {
            accuracy < 60 -> "easy"
            accuracy < 80 -> "medium"
            else -> "hard"
        }

        return """
You are an AI quiz generator for English vocabulary learning. Generate $numberOfQuestions quiz questions.

TOPIC: $topicId
STUDENT ACCURACY: $accuracy%
DIFFICULTY LEVEL: $difficulty

Generate questions that test:
- Vocabulary meaning
- Word usage in context
- Synonyms/antonyms
- Fill in the blank

Return as JSON array:
[
  {
    "question": "question text in Vietnamese/English",
    "options": ["option1", "option2", "option3", "option4"],
    "correctAnswer": 0-3,
    "explanation": "why this is correct in Vietnamese",
    "difficulty": "easy|medium|hard"
  }
]

Only return valid JSON array, no other text.
        """.trimIndent()
    }

    // ============ RESPONSE PARSERS ============

    private fun parseAnalysisResponse(response: String): StudyAnalysis {
        return try {
            val json = JSONObject(response.trim())
            StudyAnalysis(
                overallScore = json.optInt("overallScore", 50),
                strengthAreas = json.optJSONArray("strengthAreas")?.toStringList() ?: emptyList(),
                weaknessAreas = json.optJSONArray("weaknessAreas")?.toStringList() ?: emptyList(),
                recommendation = json.optString("recommendation", "Tiếp tục học tập đều đặn"),
                motivationalMessage = json.optString("motivationalMessage", "Bạn làm rất tốt! Tiếp tục phát huy!")
            )
        } catch (e: Exception) {
            Log.e("AIStudyAssistant", "Error parsing analysis", e)
            StudyAnalysis(
                overallScore = 50,
                strengthAreas = listOf("Kiên trì học tập"),
                weaknessAreas = listOf("Cần luyện tập thêm"),
                recommendation = "Hãy tiếp tục học đều đặn mỗi ngày",
                motivationalMessage = "Bạn đang trên con đường đúng! 💪"
            )
        }
    }

    private fun parseReviewResponse(response: String): List<WordRecommendation> {
        return try {
            val jsonArray = JSONArray(response.trim())
            (0 until jsonArray.length()).map { i ->
                val json = jsonArray.getJSONObject(i)
                WordRecommendation(
                    topicId = json.getString("topicId"),
                    topicName = json.getString("topicName"),
                    reason = json.getString("reason"),
                    priority = json.getInt("priority")
                )
            }
        } catch (e: Exception) {
            Log.e("AIStudyAssistant", "Error parsing recommendations", e)
            emptyList()
        }
    }

    private fun parseQuizResponse(response: String): List<AIQuizQuestion> {
        return try {
            val jsonArray = JSONArray(response.trim())
            (0 until jsonArray.length()).map { i ->
                val json = jsonArray.getJSONObject(i)
                AIQuizQuestion(
                    question = json.getString("question"),
                    options = json.getJSONArray("options").toStringList(),
                    correctAnswer = json.getInt("correctAnswer"),
                    explanation = json.getString("explanation"),
                    difficulty = json.optString("difficulty", "medium")
                )
            }
        } catch (e: Exception) {
            Log.e("AIStudyAssistant", "Error parsing quiz", e)
            emptyList()
        }
    }

    // ============ HELPERS ============

    private fun JSONArray.toStringList(): List<String> {
        return (0 until length()).map { getString(it) }
    }

    private fun daysAgo(timestamp: Long): Int {
        val diff = System.currentTimeMillis() - timestamp
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }
}
