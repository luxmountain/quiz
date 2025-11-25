package com.uilover.project247.DashboardActivity.Model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.models.Level
import com.uilover.project247.data.models.Topic
import com.uilover.project247.data.repository.FirebaseRepository
import com.uilover.project247.data.repository.UserProgressManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = true,
    val levels: List<Level> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val selectedLevelId: String? = null,
    val errorMessage: String? = null,
    val completedTopics: Set<String> = emptySet()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()
    private val progressManager = UserProgressManager(application)

    init {
        loadLevels()
        loadCompletedTopics()
    }

    fun refreshCompletedTopics() {
        loadCompletedTopics()
    }

    private fun loadCompletedTopics() {
        val completed = progressManager.getCompletedTopics().keys
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
                val defaultLevel = levels.find { it.name == "Beginner" } ?: levels.firstOrNull()
                
                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        levels = levels,
                        selectedLevelId = defaultLevel?.id,
                        errorMessage = if (levels.isEmpty()) "Không có dữ liệu levels" else null
                    )
                }
                
                Log.d("MainViewModel", "Loaded ${levels.size} levels from Firebase")
                
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
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        topics = topics,
                        selectedLevelId = levelId,
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
}
