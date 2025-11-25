package com.uilover.project247.TopicListActivity.Model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TopicListViewModelFactory(
    private val application: Application,
    private val levelId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopicListViewModel(application, levelId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
