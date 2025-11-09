package com.uilover.project247.LearningActivity.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.MockData
import com.uilover.project247.data.VocabularyWord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LearningViewModel(private val topicId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        loadWordsForTopic()
    }

    private fun loadWordsForTopic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // *** SỬA 4: THAY THẾ LOGIC FAKEWORDS BẰNG MOCKDATA ***
            // Lấy `topicId` (ví dụ: "topic1_animals") từ constructor
            // và dùng nó làm "chìa khóa" (key) để tra cứu trong Map.
            val wordsFromMock = MockData.wordsByTopicId[topicId] ?: emptyList()
            // ******************************************************

            _uiState.update {
                // Tải danh sách từ vựng từ MockData vào state
                it.copy(words = wordsFromMock, isLoading = false)
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
                goToNextWord()
            }
        }
    }

    fun checkWrittenAnswer(userAnswer: String) {
        val correctWord = _uiState.value.currentWord?.word ?: return
        if (userAnswer.equals(correctWord, ignoreCase = true)) {
            _uiState.update { it.copy(checkResult = CheckResult.CORRECT) }
            viewModelScope.launch {
                delay(1000)
                goToNextWord()
            }
        } else {
            _uiState.update { it.copy(checkResult = CheckResult.INCORRECT) }
        }
    }

    fun clearCheckResult() {
        if (_uiState.value.checkResult == CheckResult.INCORRECT) {
            _uiState.update { it.copy(checkResult = CheckResult.NEUTRAL) }
        }
    }

    private fun goToNextWord() {
        val currentState = _uiState.value
        if (currentState.currentWordIndex < currentState.words.size - 1) {
            _uiState.update {
                it.copy(
                    currentWordIndex = it.currentWordIndex + 1,
                    currentStudyMode = StudyMode.FLASHCARD,
                    checkResult = CheckResult.NEUTRAL
                )
            }
        } else {
            _uiState.update { it.copy(isTopicComplete = true) }
        }
    }
}