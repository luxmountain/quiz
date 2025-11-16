package com.uilover.project247.ConversationActivity.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uilover.project247.data.repository.FirebaseRepository

class ConversationDetailViewModelFactory(
    private val conversationId: String,
    private val repository: FirebaseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConversationDetailViewModel(conversationId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}