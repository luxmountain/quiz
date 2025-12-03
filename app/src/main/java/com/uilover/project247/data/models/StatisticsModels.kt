package com.uilover.project247.data.models

data class DailyStats(
    val date: Long, // Timestamp của ngày (00:00:00)
    val wordsReviewed: Int,
    val studyTimeMinutes: Int,
    val accuracy: Float
)

data class WeeklyStats(
    val dailyStats: List<DailyStats>
) {
    fun getTotalWordsReviewed(): Int = dailyStats.sumOf { it.wordsReviewed }
    fun getAverageAccuracy(): Float = if (dailyStats.isEmpty()) 0f else dailyStats.map { it.accuracy }.average().toFloat()
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
