package com.uilover.project247.ReviewActivity.Model

import android.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.DashboardActivity.components.Topic
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
            val fakeTopics = listOf(
                Topic(1, "Schools", "Trường học", R.drawable.ic_menu_gallery),
                Topic(2, "Examination", "Kì thi", android.R.drawable.ic_menu_gallery),
                Topic(3, "Activities", "Hoạt động", android.R.drawable.ic_menu_gallery),
                Topic(4, "Stationery", "Dụng cụ", android.R.drawable.ic_menu_gallery)
            )

            val fakeReviewList = listOf(
                ReviewTopic(fakeTopics[0], 0.8f), // 80%
                ReviewTopic(fakeTopics[1], 1.0f), // 100%
                ReviewTopic(fakeTopics[2], 0.3f), // 30%
                ReviewTopic(fakeTopics[3], 0.0f)  // 0%
            )
            // --- KẾT THÚC DỮ LIỆU GIẢ ---

            _uiState.update {
                it.copy(isLoading = false, reviewTopics = fakeReviewList)
            }
        }
    }
}