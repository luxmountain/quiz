package com.uilover.project247.ConversationActivity.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.data.models.Conversation
import com.uilover.project247.data.models.DialogueLine
import com.uilover.project247.data.models.QuizOption
import com.uilover.project247.data.models.VocabularyWordInfo
import com.uilover.project247.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ConversationStep {
    LOADING,
    CONTEXT,          // Đang đọc ngữ cảnh
    SHOWING_DIALOGUE, // Đang đọc 1 câu thoại
    QUIZ_CHOICE,      // Đang làm quiz 1 (Chọn nghĩa)
    QUIZ_WRITE,       // Đang làm quiz 2 (Viết từ)
    FINISHED          // Hoàn thành
}
data class ConversationDetailUiState(
    val isLoading: Boolean = true,
    val conversation: Conversation? = null,
    val errorMessage: String? = null,

    val currentStep: ConversationStep = ConversationStep.LOADING,
    val currentDialogueIndex: Int = 0, // Theo dõi đang ở câu thoại thứ mấy
    val visibleDialogueLines: List<DialogueLine> = emptyList(),
    val currentSpokenText: String? = null,

    val checkResult: CheckResult = CheckResult.NEUTRAL,
    val selectedOptionId: String? = null
){
    val currentDialogue: DialogueLine?
        get() = conversation?.dialogue?.getOrNull(currentDialogueIndex)

    // Helper lấy thông tin từ vựng đầy đủ
    val currentWordInfo: VocabularyWordInfo?
        get() = conversation?.vocabularyWords
            ?.find { it.word == currentDialogue?.vocabularyWord }

    // Helper tính tiến trình
    val progress: Float
        get() = if (conversation == null || conversation.dialogue.isEmpty()) 0f
        else (currentDialogueIndex + 1) / conversation.dialogue.size.toFloat()
}

class ConversationDetailViewModel(
    private val conversationId: String,
    private val repository: FirebaseRepository // Nhận Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationDetailUiState())
    val uiState: StateFlow<ConversationDetailUiState> = _uiState.asStateFlow()

    init {
        loadConversation()
    }

    private fun loadConversation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val conversation = repository.getConversation(conversationId)
                if (conversation != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversation = conversation,
                            currentStep = ConversationStep.CONTEXT,
                            currentSpokenText = conversation.contextDescription // Đọc Context (tiếng Anh)
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy hội thoại.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: ${e.message}") }
            }
        }
    }
    fun onSpeechFinished() {
        val currentState = _uiState.value
        when (currentState.currentStep) {

            // 1. Đọc xong Context -> Chuyển sang câu thoại đầu tiên
            ConversationStep.CONTEXT -> {
                goToDialogueStep(0)
            }

            // 2. Đọc xong câu thoại -> Chuyển sang Quiz 1 (Chọn nghĩa)
            ConversationStep.SHOWING_DIALOGUE -> {
                _uiState.update {
                    it.copy(
                        currentStep = ConversationStep.QUIZ_CHOICE,
                        currentSpokenText = it.currentDialogue?.question, // Đọc câu hỏi
                        checkResult = CheckResult.NEUTRAL,
                        selectedOptionId = null
                    )
                }
            }

            // 3. Đọc xong câu hỏi -> Dừng lại, chờ người dùng
            ConversationStep.QUIZ_CHOICE, ConversationStep.QUIZ_WRITE -> {
                _uiState.update { it.copy(currentSpokenText = null) }
            }
            else -> {}
        }
    }
    fun checkChoiceAnswer(selectedOption: QuizOption) {
        if (_uiState.value.checkResult != CheckResult.NEUTRAL) return

        if (selectedOption.isCorrect) {
            _uiState.update {
                it.copy(
                    checkResult = CheckResult.CORRECT,
                    selectedOptionId = selectedOption.id,

                )
            }
        } else {
            _uiState.update {
                it.copy(
                    checkResult = CheckResult.INCORRECT,
                    selectedOptionId = selectedOption.id
                )
            }
        }
    }
    fun checkWriteAnswer(userAnswer: String) {
        if (_uiState.value.checkResult != CheckResult.NEUTRAL) return
        val correctWord = _uiState.value.currentDialogue?.vocabularyWord

        if (userAnswer.equals(correctWord, ignoreCase = true)) {
            _uiState.update { it.copy(checkResult = CheckResult.CORRECT) }
        } else {
            _uiState.update { it.copy(checkResult = CheckResult.INCORRECT) }
        }
    }

    fun clearCheckResult() {
        if (_uiState.value.checkResult == CheckResult.INCORRECT) {
            _uiState.update { it.copy(checkResult = CheckResult.NEUTRAL) }
        }
    }
    fun retry() {
        loadConversation()
    }
    fun onQuizContinue() {
        val currentState = _uiState.value
        if (currentState.checkResult == CheckResult.INCORRECT) {
            // Nếu Sai -> Thử lại
            _uiState.update { it.copy(checkResult = CheckResult.NEUTRAL, selectedOptionId = null) }
            return
        }

        // Nếu Đúng:
        when (currentState.currentStep) {
            // 4. Đúng Quiz 1 -> Chuyển sang Quiz 2 (Viết)
            ConversationStep.QUIZ_CHOICE -> {
                _uiState.update {
                    it.copy(
                        currentStep = ConversationStep.QUIZ_WRITE,
                        checkResult = CheckResult.NEUTRAL,
                        selectedOptionId = null,
                    )
                }
            }
            // 5. Đúng Quiz 2 -> Chuyển sang câu thoại tiếp theo
            ConversationStep.QUIZ_WRITE -> {
                goToDialogueStep(currentState.currentDialogueIndex + 1)
            }
            else -> {}
        }
    }
    private fun goToDialogueStep(index: Int) {
        val conversation = _uiState.value.conversation ?: return
        val nextDialogue = conversation.dialogue.getOrNull(index)

        if (nextDialogue != null) {
            // Nếu còn hội thoại
            _uiState.update {
                it.copy(
                    currentStep = ConversationStep.SHOWING_DIALOGUE,
                    currentDialogueIndex = index,
                    visibleDialogueLines = it.visibleDialogueLines + nextDialogue,
                    currentSpokenText = nextDialogue.text, // Đọc câu thoại mới
                    checkResult = CheckResult.NEUTRAL
                )
            }
        } else {
            // Hết hội thoại
            _uiState.update { it.copy(currentStep = ConversationStep.FINISHED) }
        }
    }
}






