package com.uilover.project247.data.models

data class DailyStats(
    val date: Long, // Timestamp của ngày (00:00:00)
    val wordsReviewed: Int,
    val studyTimeMinutes: Int,
    val correctCount: Int,
    val totalCount: Int
) {
    val accuracy: Float
        get() = if (totalCount > 0) (correctCount.toFloat() / totalCount.toFloat()) * 100f else 0f
}

data class WeeklyStats(
    val dailyStats: List<DailyStats>
) {
    fun getTotalWordsReviewed(): Int = dailyStats.sumOf { it.wordsReviewed }
    fun getTotalStudyTime(): Int = dailyStats.sumOf { it.studyTimeMinutes }
    fun getAverageAccuracy(): Float {
        val totalCorrect = dailyStats.sumOf { it.correctCount }
        val totalQuestions = dailyStats.sumOf { it.totalCount }
        return if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions.toFloat()) * 100f else 0f
    }
}

data class MonthlyHeatmapData(
    val year: Int,
    val month: Int,
    val dailyActivityMap: Map<Int, Int> // day of month -> words reviewed count
) {
    fun getMaxActivity(): Int = dailyActivityMap.values.maxOrNull() ?: 0
}

data class LearningStreak(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastStudyDate: Long?
)
