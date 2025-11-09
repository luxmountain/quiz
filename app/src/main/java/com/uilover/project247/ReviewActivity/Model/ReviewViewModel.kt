package com.uilover.project247.ReviewActivity.Model

import android.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.Topic
import com.uilover.project247.data.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val isLoading: Boolean = true,
    val reviewTopics: List<ReviewTopic> = emptyList()
)

class ReviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadReviewTopics()
    }

    private fun loadReviewTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // --- DỮ LIỆU GIẢ (STUB) ---
            // TODO: Thay thế bằng logic tải từ Database (Room)
            val allTopics= MockData.allTopics

            // --- DỮ LIỆU GIẢ (STUB) CHO TIẾN ĐỘ ---
            // Chúng ta vẫn cần làm giả tiến độ,
            // vì MockData không chứa thông tin này.
            val fakedProgressList = listOf(0.8f, 1.0f, 0.3f) // 80%, 100%, 30%

            // Ghép 2 danh sách lại với nhau
            val fakeReviewList = allTopics.mapIndexed { index, topic ->
                // Lấy tiến độ giả tương ứng, nếu không có thì mặc định là 0%
                val progress = fakedProgressList.getOrNull(index) ?: 0.0f
                ReviewTopic(topic, progress)
            }

            _uiState.update {
                it.copy(isLoading = false, reviewTopics = fakeReviewList)
            }
        }
    }
}