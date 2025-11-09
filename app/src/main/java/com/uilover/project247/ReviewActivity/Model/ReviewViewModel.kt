package com.uilover.project247.ReviewActivity.Model

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

data class ReviewUiState(
    val isLoading: Boolean = true,
    val reviewTopics: List<ReviewTopic> = emptyList(),
    val errorMessage: String? = null
)

class ReviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()

    init {
        loadReviewTopics()
    }

    private fun loadReviewTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Lấy topics từ Firebase
                val allTopics = firebaseRepository.getTopics()

                // --- DỮ LIỆU GIẢ (STUB) CHO TIẾN ĐỘ ---
                // TODO: Thay thế bằng logic tải từ Firebase UserProgress
                val fakedProgressList = listOf(0.8f, 1.0f, 0.3f) // 80%, 100%, 30%

                // Ghép 2 danh sách lại với nhau
                val fakeReviewList = allTopics.mapIndexed { index, topic ->
                    // Lấy tiến độ giả tương ứng, nếu không có thì mặc định là 0%
                    val progress = fakedProgressList.getOrNull(index) ?: 0.0f
                    ReviewTopic(topic, progress)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        reviewTopics = fakeReviewList,
                        errorMessage = if (fakeReviewList.isEmpty()) "Không có dữ liệu" else null
                    )
                }
                
                Log.d("ReviewViewModel", "Loaded ${fakeReviewList.size} review topics from Firebase")
            } catch (e: Exception) {
                Log.e("ReviewViewModel", "Error loading topics", e)
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
        loadReviewTopics()
    }
}