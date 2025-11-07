package com.uilover.project247.LearningActivity.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.DashboardActivity.components.VocabularyWord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Thêm Enum để biểu diễn trạng thái kiểm tra

// 2. Cập nhật UI State để lưu trạng thái kiểm tra và tiến trình
data class LearningUiState(
    val words: List<VocabularyWord> = emptyList(),
    val currentWordIndex: Int = 0,
    val currentStudyMode: StudyMode = StudyMode.FLASHCARD,
    val isLoading: Boolean = true,
    val isTopicComplete: Boolean = false,

    // -- THÊM DÒNG NÀY --
    val checkResult: CheckResult = CheckResult.NEUTRAL // Trạng thái kiểm tra
) {
    val currentWord: VocabularyWord?
        get() = words.getOrNull(currentWordIndex)
}

// 4. ViewModel (Cập nhật logic)
class LearningViewModel(private val topicId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        loadWordsForTopic()
    }

    private fun loadWordsForTopic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Dùng dữ liệu giả với từ "character"
            val fakeWords = listOf(
                VocabularyWord("w1", "character", "Tính cách, cá tính (n)", "/ˈkerəktər/", "His father has a strong impact on his character."),
                VocabularyWord("w2", "Student", "Học sinh", "/ˈstuːdnt/", "He is a student."),
                VocabularyWord("w3", "Teacher", "Giáo viên", "/ˈtiːtʃər/", "She is a teacher.")
            )
            _uiState.update {
                it.copy(words = fakeWords, isLoading = false)
            }
        }
    }

    // Khi người dùng hoàn thành 1 màn (Flashcard, MultipleChoice)
    fun onActionCompleted() {
        val currentState = _uiState.value
        when (currentState.currentStudyMode) {
            // Khi xong Flashcard, chuyển sang Viết, reset checkResult
            StudyMode.FLASHCARD -> {
                _uiState.update { it.copy(
                    currentStudyMode = StudyMode.WRITE_WORD,
                    checkResult = CheckResult.NEUTRAL
                ) }
            }
            StudyMode.WRITE_WORD -> {
                // Không làm gì, chờ hàm checkWrittenAnswer
            }
            StudyMode.MULTIPLE_CHOICE -> {
                goToNextWord()
            }
        }
    }

    // --- HÀM MỚI ĐỂ KIỂM TRA TỪ VỰNG (WriteWordView gọi hàm này) ---
    fun checkWrittenAnswer(userAnswer: String) {
        val correctWord = _uiState.value.currentWord?.word ?: return

        // So sánh câu trả lời (không phân biệt hoa thường)
        if (userAnswer.equals(correctWord, ignoreCase = true)) {
            // NẾU ĐÚNG
            _uiState.update { it.copy(checkResult = CheckResult.CORRECT) }

            // Tự động chuyển sang từ tiếp theo sau 1 giây
            viewModelScope.launch {
                delay(1000) // Đợi 1 giây để người dùng thấy màu xanh
                goToNextWord()
            }
        } else {
            // NẾU SAI
            _uiState.update { it.copy(checkResult = CheckResult.INCORRECT) }
        }
    }

    // --- HÀM MỚI ĐỂ RESET TRẠNG THÁI "SAI" ---
    // (Gọi khi người dùng bắt đầu gõ lại)
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
                    currentStudyMode = StudyMode.FLASHCARD, // Bắt đầu lại với Flashcard
                    checkResult = CheckResult.NEUTRAL // Reset trạng thái
                )
            }
        } else {
            // Hết từ!
            _uiState.update { it.copy(isTopicComplete = true) }
        }
    }
}