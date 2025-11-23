package com.uilover.project247.LearningActivity.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory này là BẮT BUỘC.
 * Vì LearningViewModel cần `levelId` và `topicId` trong constructor của nó,
 * chúng ta không thể dùng cách tạo ViewModel mặc định.
 * Factory này sẽ nhận `levelId` và `topicId` từ Activity và "nhét" nó vào ViewModel.
 */
class LearningViewModelFactory(
    private val levelId: String,
    private val topicId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra xem class được yêu cầu có phải là LearningViewModel không
        if (modelClass.isAssignableFrom(LearningViewModel::class.java)) {
            // Nếu đúng, tạo và trả về một instance
            @Suppress("UNCHECKED_CAST")
            return LearningViewModel(levelId, topicId) as T
        }
        // Nếu không, báo lỗi
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}