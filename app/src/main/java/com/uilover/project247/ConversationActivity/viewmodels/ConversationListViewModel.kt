package com.uilover.project247.ConversationActivity.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.models.Conversation
import com.uilover.project247.data.repository.FirebaseRepository
import com.uilover.project247.data.repository.UserProgressManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversationListUiState(
    val isLoading: Boolean = true,
    val conversations: List<Conversation> = emptyList(),
    val completedConversationIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)

class ConversationListViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()
    private val firebaseRepository = FirebaseRepository()
    private val progressManager = UserProgressManager(application)

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val conversations = firebaseRepository.getAllConversations()
                
                // Lấy danh sách conversation đã hoàn thành
                val completedTopics = progressManager.getCompletedTopics()
                val completedIds = conversations
                    .filter { conv -> completedTopics[conv.id]?.isCompleted == true }
                    .map { it.id }
                    .toSet()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        conversations = conversations,
                        completedConversationIds = completedIds,
                        errorMessage = if (conversations.isEmpty()) "Không có dữ liệu hội thoại" else null
                    )
                }
                Log.d("ConvListViewModel", "Loaded ${conversations.size} conversations, ${completedIds.size} completed")
            } catch (e: Exception) {
                Log.e("ConvListViewModel", "Error loading conversations", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi kết nối: ${e.message}"
                    )
                }
            }
        }
    }

    fun retry() {
        loadConversations()
    }
}