package com.uilover.project247.TopicListActivity.Model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.models.Topic
import com.uilover.project247.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TopicListUiState(
    val isLoading: Boolean = true,
    val topics: List<Topic> = emptyList(),
    val errorMessage: String? = null
)

class TopicListViewModel(private val levelId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicListUiState())
    val uiState: StateFlow<TopicListUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()

    init {
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Lấy topics theo levelId từ Firebase
                val topics = firebaseRepository.getTopicsByLevel(levelId)
                
                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        topics = topics,
                        errorMessage = if (topics.isEmpty()) "Không có chủ đề nào" else null
                    )
                }
                
                Log.d("TopicListViewModel", "Loaded ${topics.size} topics for level $levelId")
            } catch (e: Exception) {
                Log.e("TopicListViewModel", "Error loading topics for level $levelId", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối Firebase: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun retryLoadTopics() {
        loadTopics()
    }
}
