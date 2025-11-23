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


class LearningViewModel(private val levelId: String, private val topicId: String) : ViewModel() {

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
                // Load topic từ Firebase theo levelId và topicId
                val topic = firebaseRepository.getTopic(levelId, topicId)
                val flashcards = topic?.flashcards ?: emptyList()
                
                _uiState.update {
                    it.copy(
                        flashcards = flashcards,
                        isLoading = false
                    )
                }
                
                Log.d("LearningViewModel", "Loaded ${flashcards.size} flashcards for topic $topicId in level $levelId")
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Error loading flashcards for topic $topicId in level $levelId", e)
                _uiState.update {
                    it.copy(
                        flashcards = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }


    // Dùng cho Flashcard
    fun onActionCompleted() {
        val currentState = _uiState.value
        if (currentState.currentStudyMode == StudyMode.FLASHCARD) {
            _uiState.update { it.copy(
                currentStudyMode = StudyMode.WRITE_WORD, // 1. Chuyển sang Điền từ
                checkResult = CheckResult.NEUTRAL
            ) }
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
    fun checkListenAnswer(userAnswer: String) {
        val correctWord = _uiState.value.currentCard?.word ?: return
        if (userAnswer.equals(correctWord, ignoreCase = true)) {
            _uiState.update { it.copy(checkResult = CheckResult.CORRECT) }
        } else {
            _uiState.update { it.copy(checkResult = CheckResult.INCORRECT) }
        }
    }

    // --- 3. SỬA LẠI LOGIC "TIẾP TỤC" ---
    fun onQuizContinue() {
        val currentState = _uiState.value
        if (currentState.checkResult == CheckResult.INCORRECT) {
            // Nếu sai, cho thử lại
            _uiState.update { it.copy(checkResult = CheckResult.NEUTRAL) }
            return
        }

        // Nếu đúng:
        when (currentState.currentStudyMode) {
            // 2. Nếu xong WRITE_WORD -> Chuyển sang LISTEN_AND_WRITE
            StudyMode.WRITE_WORD -> {
                _uiState.update {
                    it.copy(
                        currentStudyMode = StudyMode.LISTEN_AND_WRITE,
                        checkResult = CheckResult.NEUTRAL
                    )
                }
            }
            // 3. Nếu xong LISTEN_AND_WRITE -> Chuyển sang từ tiếp theo
            StudyMode.LISTEN_AND_WRITE -> {
                goToNextCard()
            }
            else -> {}
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