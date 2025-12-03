package com.uilover.project247.StatisticsActivity.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.models.DailyStats
import com.uilover.project247.data.models.LearningStreak
import com.uilover.project247.data.models.MonthlyHeatmapData
import com.uilover.project247.data.models.WeeklyStats
import com.uilover.project247.data.repository.UserProgressManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val weeklyStats: WeeklyStats? = null,
    val monthlyHeatmap: MonthlyHeatmapData? = null,
    val learningStreak: LearningStreak = LearningStreak(0, 0, null),
    val totalWordsLearned: Int = 0,
    val totalStudyTime: Int = 0,
    val errorMessage: String? = null
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val progressManager = UserProgressManager(application)
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val studyHistory = progressManager.getStudyHistory()
                
                // Tính toán Weekly Stats (7 ngày gần nhất)
                val weeklyStats = calculateWeeklyStats(studyHistory)
                
                // Tính toán Monthly Heatmap (tháng hiện tại)
                val monthlyHeatmap = calculateMonthlyHeatmap(studyHistory)
                
                // Tính toán Learning Streak
                val streak = calculateLearningStreak(studyHistory)
                
                // Tính tổng số từ đã học
                val totalWords = studyHistory.sumOf { it.totalItems }
                
                // Tính tổng thời gian học (phút)
                val totalTime = (studyHistory.sumOf { it.timeSpent } / 60000).toInt()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        weeklyStats = weeklyStats,
                        monthlyHeatmap = monthlyHeatmap,
                        learningStreak = streak,
                        totalWordsLearned = totalWords,
                        totalStudyTime = totalTime
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    private fun calculateWeeklyStats(history: List<com.uilover.project247.data.repository.StudyResult>): WeeklyStats {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        // Lấy 7 ngày gần nhất
        val dailyStatsMap = mutableMapOf<String, DailyStats>()
        
        for (i in 6 downTo 0) {
            calendar.timeInMillis = today
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val dayStart = calendar.timeInMillis
            val dateKey = getDayKey(dayStart)
            
            dailyStatsMap[dateKey] = DailyStats(
                date = dayStart,
                wordsReviewed = 0,
                studyTimeMinutes = 0,
                accuracy = 0f
            )
        }
        
        // Fill data from history
        history.forEach { result ->
            val dateKey = getDayKey(result.completedDate)
            
            if (dailyStatsMap.containsKey(dateKey)) {
                val existing = dailyStatsMap[dateKey]!!
                dailyStatsMap[dateKey] = existing.copy(
                    wordsReviewed = existing.wordsReviewed + result.totalItems,
                    studyTimeMinutes = existing.studyTimeMinutes + (result.timeSpent / 60000).toInt(),
                    accuracy = if (existing.wordsReviewed == 0) result.accuracy
                               else (existing.accuracy * existing.wordsReviewed + result.accuracy * result.totalItems) /
                                    (existing.wordsReviewed + result.totalItems)
                )
            }
        }
        
        val dailyStatsList = dailyStatsMap.values.sortedBy { it.date }
        return WeeklyStats(dailyStatsList)
    }

    private fun calculateMonthlyHeatmap(history: List<com.uilover.project247.data.repository.StudyResult>): MonthlyHeatmapData {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        val dailyActivityMap = mutableMapOf<Int, Int>()
        
        history.forEach { result ->
            calendar.timeInMillis = result.completedDate
            
            if (calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month) {
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                dailyActivityMap[dayOfMonth] = (dailyActivityMap[dayOfMonth] ?: 0) + result.totalItems
            }
        }
        
        return MonthlyHeatmapData(year, month, dailyActivityMap)
    }

    private fun calculateLearningStreak(history: List<com.uilover.project247.data.repository.StudyResult>): LearningStreak {
        if (history.isEmpty()) {
            return LearningStreak(0, 0, null)
        }
        
        val sortedHistory = history.sortedByDescending { it.completedDate }
        val calendar = Calendar.getInstance()
        
        // Lấy ngày học gần nhất
        val lastStudyDate = sortedHistory.first().completedDate
        
        // Group by date
        val studyDates = sortedHistory
            .map { result ->
                calendar.timeInMillis = result.completedDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            .distinct()
            .sorted()
            .reversed()
        
        // Tính current streak
        var currentStreak = 0
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        var checkDate = calendar.timeInMillis
        
        for (studyDate in studyDates) {
            if (studyDate == checkDate || studyDate == checkDate - 86400000) { // 86400000 = 1 day in millis
                currentStreak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                checkDate = calendar.timeInMillis
            } else {
                break
            }
        }
        
        // Tính longest streak
        var longestStreak = 0
        var tempStreak = 1
        
        for (i in 0 until studyDates.size - 1) {
            val diff = (studyDates[i] - studyDates[i + 1]) / 86400000
            if (diff <= 1) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
        }
        longestStreak = maxOf(longestStreak, tempStreak)
        
        return LearningStreak(currentStreak, longestStreak, lastStudyDate)
    }

    private fun getDayKey(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }
}
