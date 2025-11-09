package com.uilover.project247.DashboardActivity.Model

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

data class MainUiState(
    val isLoading: Boolean = true,
    val topics: List<Topic> = emptyList(),
    val errorMessage: String? = null
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()

    init {
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Lấy topics từ Firebase Realtime Database
                val topics = firebaseRepository.getTopics()
                
                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        topics = topics,
                        errorMessage = if (topics.isEmpty()) "Không có dữ liệu topics" else null
                    )
                }
                
                Log.d("MainViewModel", "Loaded ${topics.size} topics from Firebase")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading topics", e)
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