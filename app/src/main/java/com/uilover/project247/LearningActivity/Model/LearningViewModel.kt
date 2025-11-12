package com.uilover.project247.LearningActivity.Model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.repository.FirebaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LearningViewModel(private val topicId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()

    init {
        loadFlashcardsForTopic()
    }

    private fun loadFlashcardsForTopic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load flashcards từ Firebase theo topicId
                val flashcards = firebaseRepository.getFlashcardsByTopic(topicId)
                
                _uiState.update {
                    it.copy(
                        flashcards = flashcards,
                        isLoading = false
                    )
                }
                
                Log.d("LearningViewModel", "Loaded ${flashcards.size} flashcards for topic $topicId")
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Error loading flashcards for topic $topicId", e)
                _uiState.update {
                    it.copy(
                        flashcards = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    // (Toàn bộ các hàm bên dưới đã chính xác, giữ nguyên)

    fun onActionCompleted() {
        val currentState = _uiState.value
        when (currentState.currentStudyMode) {
            StudyMode.FLASHCARD -> {
                _uiState.update { it.copy(
                    currentStudyMode = StudyMode.WRITE_WORD,
                    checkResult = CheckResult.NEUTRAL
                ) }
            }
            StudyMode.WRITE_WORD -> { /* Chờ checkWrittenAnswer */ }
            StudyMode.MULTIPLE_CHOICE -> {
                goToNextCard()
            }
        }
    }

    fun checkWrittenAnswer(userAnswer: String) {
        val correctWord = _uiState.value.currentCard?.word ?: return

        if (userAnswer.equals(correctWord, ignoreCase = true)) {
            // NẾU ĐÚNG: Chỉ set trạng thái, KHÔNG tự động chuyển
            _uiState.update { it.copy(checkResult = CheckResult.CORRECT) }
        } else {
            // NẾU SAI: Chỉ set trạng thái
            _uiState.update { it.copy(checkResult = CheckResult.INCORRECT) }
        }
    }
    fun onQuizContinue() {
        val currentState = _uiState.value

        // Sau khi xem kết quả (Dù Đúng hay Sai), chuyển sang bước tiếp theo
        // (Trong ví dụ này, ta chuyển sang MULTIPLE_CHOICE)
        if (currentState.currentStudyMode == StudyMode.WRITE_WORD) {
            _uiState.update {
                it.copy(
                    currentStudyMode = StudyMode.MULTIPLE_CHOICE,
                    checkResult = CheckResult.NEUTRAL // Reset
                )
            }
        }
        // (Nếu đang ở MULTIPLE_CHOICE thì gọi goToNextWord())
        else if (currentState.currentStudyMode == StudyMode.MULTIPLE_CHOICE) {
            goToNextCard()
        }
    }
    fun clearCheckResult() {
        if (_uiState.value.checkResult == CheckResult.INCORRECT) {
            _uiState.update { it.copy(checkResult = CheckResult.NEUTRAL) }
        }
    }

    fun goToNextCard() {
        val currentState = _uiState.value
        if (currentState.currentCardIndex < currentState.flashcards.size - 1) {
            _uiState.update {
                it.copy(
                    currentCardIndex = it.currentCardIndex + 1,
                    currentStudyMode = StudyMode.FLASHCARD,
                    checkResult = CheckResult.NEUTRAL
                )
            }
        } else {
            _uiState.update { it.copy(isTopicComplete = true) }
        }
    }
}