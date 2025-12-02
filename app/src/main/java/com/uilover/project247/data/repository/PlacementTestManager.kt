package com.uilover.project247.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.uilover.project247.data.models.PlacementTestResult

class PlacementTestManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "placement_test_prefs",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val KEY_TEST_COMPLETED = "test_completed"
        private const val KEY_TEST_RESULT = "test_result"
        private const val KEY_RECOMMENDED_LEVEL = "recommended_level"
        private const val KEY_COMPLETED_DATE = "completed_date"
    }

    /**
     * Kiểm tra user đã làm placement test chưa
     */
    fun hasCompletedTest(): Boolean {
        return prefs.getBoolean(KEY_TEST_COMPLETED, false)
    }

    /**
     * Lưu kết quả placement test
     */
    fun saveTestResult(result: PlacementTestResult) {
        val resultJson = gson.toJson(result)
        prefs.edit().apply {
            putBoolean(KEY_TEST_COMPLETED, true)
            putString(KEY_TEST_RESULT, resultJson)
            putString(KEY_RECOMMENDED_LEVEL, result.recommendedLevel)
            putLong(KEY_COMPLETED_DATE, result.completedDate)
            apply()
        }
    }

    /**
     * Lấy kết quả placement test đã lưu
     */
    fun getTestResult(): PlacementTestResult? {
        val resultJson = prefs.getString(KEY_TEST_RESULT, null) ?: return null
        return try {
            gson.fromJson(resultJson, PlacementTestResult::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lấy level được recommend từ test
     */
    fun getRecommendedLevel(): String? {
        return prefs.getString(KEY_RECOMMENDED_LEVEL, null)
    }

    /**
     * Reset test (cho phép làm lại)
     */
    fun resetTest() {
        prefs.edit().clear().apply()
    }

    /**
     * Tính toán level dựa trên số câu đúng
     */
    fun calculateLevel(
        correctAnswers: Int,
        totalQuestions: Int,
        passingScores: Map<String, Int>
    ): Pair<String, String> {
        val beginner = passingScores["beginner"] ?: 0
        val elementary = passingScores["elementary"] ?: 6
        val intermediate = passingScores["intermediate"] ?: 12
        val advanced = passingScores["advanced"] ?: 16

        return when {
            correctAnswers >= advanced -> "advanced" to "Nâng cao"
            correctAnswers >= intermediate -> "intermediate" to "Trung cấp"
            correctAnswers >= elementary -> "elementary" to "Sơ cấp"
            else -> "beginner" to "Cơ bản"
        }
    }
}
