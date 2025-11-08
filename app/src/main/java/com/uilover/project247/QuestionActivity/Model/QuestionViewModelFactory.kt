package com.uilover.project247.QuestionActivity.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuestionViewModelFactory(
    private val topicId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionViewModel(topicId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}