package com.uilover.project247.ConversationActivity.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ConversationDetailViewModelFactory(
    private val conversationId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra xem class được yêu cầu có phải là ConversationDetailViewModel không
        if (modelClass.isAssignableFrom(ConversationDetailViewModel::class.java)) {
            // Nếu đúng, tạo và trả về một instance
            @Suppress("UNCHECKED_CAST")
            return ConversationDetailViewModel(conversationId) as T
        }
        // Nếu không, báo lỗi
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}