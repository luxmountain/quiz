package com.uilover.project247.ReviewActivity.Model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.models.Topic
import com.uilover.project247.data.repository.FirebaseRepository
import com.uilover.project247.utils.AnkiScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val isLoading: Boolean = true,
    val reviewTopics: List<ReviewTopic> = emptyList(),
    val errorMessage: String? = null,
    val newCardsCount: Int = 0,
    val learningCardsCount: Int = 0,
    val reviewCardsCount: Int = 0
)

class ReviewViewModel(
    private val userId: String = "demo_user" // TODO: Lấy từ Firebase Auth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()
    private val ankiScheduler = AnkiScheduler()

    init {
        loadReviewTopics()
    }

    private fun loadReviewTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Lấy topics từ Firebase
                val allTopics = firebaseRepository.getTopics()
                
                // Lấy tất cả flashcard results của user
                val allResults = firebaseRepository.getFlashcardResults(userId)
                
                // Tính toán thống kê cards
                val newCards = ankiScheduler.getNewCardsCount(allResults)
                val learningCards = ankiScheduler.getLearningCardsCount(allResults)
                val reviewCards = ankiScheduler.getReviewCardsCount(allResults)
                
                // Tạo ReviewTopic cho mỗi topic với tiến độ thực
                val reviewTopics = allTopics.map { topic ->
                    // Lấy flashcards của topic này
                    val topicFlashcards = firebaseRepository.getFlashcardsByTopic(topic.id)
                    val topicFlashcardIds = topicFlashcards.map { it.id }.toSet()
                    
                    // Filter results của topic này
                    val topicResults = allResults.filter { it.value.flashcardId in topicFlashcardIds }
                    
                    // Tính số cards cần review trong topic
                    val dueCount = ankiScheduler.getDueCards(topicResults).size
                    
                    // Tính progress: số cards đã learned / tổng số cards
                    val learnedCount = topicResults.values.count { it.learned }
                    val progress = if (topicFlashcards.isEmpty()) 0f 
                                  else (learnedCount.toFloat() / topicFlashcards.size.toFloat())
                    
                    ReviewTopic(
                        topic = topic,
                        progress = progress,
                        dueCount = dueCount,
                        totalCards = topicFlashcards.size
                    )
                }
                
                // Filter chỉ hiển thị topics có cards cần ôn hoặc đang học
                val topicsToReview = reviewTopics.filter { 
                    it.dueCount > 0 || it.progress > 0f 
                }

                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        reviewTopics = topicsToReview,
                        newCardsCount = newCards,
                        learningCardsCount = learningCards,
                        reviewCardsCount = reviewCards,
                        errorMessage = if (topicsToReview.isEmpty()) "Chưa có từ vựng cần ôn tập" else null
                    )
                }
                
                Log.d("ReviewViewModel", "Loaded ${topicsToReview.size} topics to review")
                Log.d("ReviewViewModel", "Stats - New: $newCards, Learning: $learningCards, Review: $reviewCards")
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