package com.uilover.project247.DashboardActivity.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.MockData
import com.uilover.project247.data.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = true,
    val topics: List<Topic> = emptyList()
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Lấy topics trực tiếp từ MockData
            val allTopics = MockData.allTopics

            _uiState.update {
                it.copy(isLoading = false, topics = allTopics)
            }
        }
    }
}