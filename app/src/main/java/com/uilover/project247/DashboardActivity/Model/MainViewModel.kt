package com.uilover.project247.DashboardActivity.Model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.models.Level
import com.uilover.project247.data.models.Topic
import com.uilover.project247.data.repository.FirebaseRepository
import com.uilover.project247.data.repository.PlacementTestManager
import com.uilover.project247.data.repository.UserProgressManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TopicWithStatus(
    val topic: Topic,
    val isCompleted: Boolean,
    val isLocked: Boolean,
    val progress: Float // 0-100
)

data class MainUiState(
    val isLoading: Boolean = true,
    val levels: List<Level> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val topicsWithStatus: List<TopicWithStatus> = emptyList(),
    val selectedLevelId: String? = null,
    val currentLevel: Level? = null,
    val levelProgress: Float = 0f, // 0-100
    val errorMessage: String? = null,
    val completedTopics: Set<String> = emptySet()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()
    private val progressManager = UserProgressManager(application)
    private val placementTestManager = PlacementTestManager(application)

    init {
        loadLevels()
        loadCompletedTopics()
    }

    /**
     * Refresh data khi quay lại màn hình (onResume)
     */
    fun refreshData() {
        loadCompletedTopics()
        // Reload topics của level hiện tại để cập nhật trạng thái lock/unlock
        val currentLevelId = _uiState.value.selectedLevelId
        if (currentLevelId != null) {
            loadTopicsByLevel(currentLevelId)
        }
    }

    fun refreshCompletedTopics() {
        loadCompletedTopics()
    }

    private fun loadCompletedTopics() {
        // CHỈ lấy topics có isCompleted = true
        val completed = progressManager.getCompletedTopics()
            .filter { it.value.isCompleted }
            .keys
        _uiState.update { it.copy(completedTopics = completed) }
    }

    fun isTopicCompleted(topicId: String): Boolean {
        return _uiState.value.completedTopics.contains(topicId)
    }

    private fun loadLevels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val levels = firebaseRepository.getLevels()
                
                // Lấy level từ placement test result nếu có
                val recommendedLevelId = placementTestManager.getRecommendedLevel()
                val defaultLevel = if (recommendedLevelId != null) {
                    // Map level name từ placement test sang Firebase
                    val levelName = when (recommendedLevelId) {
                        "beginner" -> "Beginner"
                        "elementary" -> "Elementary"
                        "intermediate" -> "Intermediate"
                        "advanced" -> "Advanced"
                        else -> "Beginner"
                    }
                    levels.find { it.name == levelName } ?: levels.firstOrNull()
                } else {
                    levels.find { it.name == "Beginner" } ?: levels.firstOrNull()
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        levels = levels,
                        selectedLevelId = defaultLevel?.id,
                        currentLevel = defaultLevel,
                        errorMessage = if (levels.isEmpty()) "Không có dữ liệu levels" else null
                    )
                }
                
                Log.d("MainViewModel", "Loaded ${levels.size} levels, selected: ${defaultLevel?.name}")
                
                if (defaultLevel != null) {
                    loadTopicsByLevel(defaultLevel.id)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading levels", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối Firebase: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun retryLoadLevels() {
        loadLevels()
    }
    
    fun loadTopicsByLevel(levelId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val topics = firebaseRepository.getTopicsByLevel(levelId)
                
                // Tính toán trạng thái unlock cho từng topic
                val topicsWithStatus = topics.mapIndexed { index, topic ->
                    val isCompleted = isTopicCompleted(topic.id)
                    val progress = progressManager.getTopicFlashcardProgress(topic.id, topic.totalWords)
                    
                    // Logic unlock:
                    // - Topic đầu tiên luôn mở
                    // - Topic tiếp theo mở khi topic trước đã HOÀN THÀNH (làm xong 1 lần với accuracy >= 60%)
                    val isLocked = if (index == 0) {
                        false
                    } else {
                        val previousTopic = topics[index - 1]
                        !progressManager.hasReachedUnlockThreshold(previousTopic.id)
                    }
                    
                    TopicWithStatus(
                        topic = topic,
                        isCompleted = isCompleted,
                        isLocked = isLocked,
                        progress = progress
                    )
                }
                
                // Tính progress của level = số topic hoàn thành / tổng số topic
                val completedCount = topicsWithStatus.count { it.isCompleted }
                val levelProgress = if (topicsWithStatus.isNotEmpty()) {
                    (completedCount.toFloat() / topicsWithStatus.size.toFloat()) * 100f
                } else {
                    0f
                }
                
                val currentLevel = _uiState.value.levels.find { it.id == levelId }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        topics = topics,
                        topicsWithStatus = topicsWithStatus,
                        selectedLevelId = levelId,
                        currentLevel = currentLevel,
                        levelProgress = levelProgress,
                        errorMessage = if (topics.isEmpty()) "Không có chủ đề nào" else null
                    )
                }
                
                Log.d("MainViewModel", "Loaded ${topics.size} topics for level $levelId")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading topics for level $levelId", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi khi tải topics: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Kiểm tra xem có thể mở topic không
     */
    fun canOpenTopic(topicId: String): Boolean {
        val topicStatus = _uiState.value.topicsWithStatus.find { it.topic.id == topicId }
        return topicStatus?.isLocked == false
    }
}
