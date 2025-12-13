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
    CONTEXT,
    SHOWING_DIALOGUE,
    QUIZ_CHOICE,
    QUIZ_WRITE,
    FINISHED
}

data class ConversationDetailUiState(
    val isLoading: Boolean = true,
    val conversation: Conversation? = null,
    val errorMessage: String? = null,
    val currentStep: ConversationStep = ConversationStep.LOADING,
    val currentDialogueIndex: Int = 0,
    val visibleDialogueLines: List<DialogueLine> = emptyList(),
    val currentSpokenText: String? = null,
    val checkResult: CheckResult = CheckResult.NEUTRAL,
    val selectedOptionId: String? = null
) {
    // Helper lấy câu thoại (đã sắp xếp)
    val currentDialogue: DialogueLine?
        get() = conversation?.dialogue?.sortedBy { it.order }?.getOrNull(currentDialogueIndex)

    // Helper lấy thông tin từ vựng
    val currentWordInfo: VocabularyWordInfo?
        get() = conversation?.vocabularyWords
            ?.find { it.word.equals(currentDialogue?.vocabularyWord, ignoreCase = true) }

    // --- SỬA LOGIC TIẾN TRÌNH (PROGRESS) ---
    val progress: Float
        get() {
            if (conversation == null || conversation.dialogue.isEmpty()) return 0f

            // 1. Nếu đã xong, trả về 100%
            if (currentStep == ConversationStep.FINISHED) return 1.0f

            // 2. Nếu chưa, tiến trình bằng (số từ đã xong / tổng số từ)
            // `currentDialogueIndex` chính là số từ đã xong (bắt đầu từ 0)
            return currentDialogueIndex.toFloat() / conversation.dialogue.size.toFloat()
        }
    // ------------------------------------
}

class ConversationDetailViewModel(
    private val conversationId: String,
    private val repository: FirebaseRepository // Nhận Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationDetailUiState())
    val uiState: StateFlow<ConversationDetailUiState> = _uiState.asStateFlow()
    // Xóa: private val firebaseRepository = FirebaseRepository() (đã có `repository`)

    init {
        loadConversation()
    }

    private fun loadConversation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Sửa: Dùng `repository`
                val conversation = repository.getConversation(conversationId)
                if (conversation != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversation = conversation,
                            currentStep = ConversationStep.CONTEXT,
                            currentSpokenText = conversation.contextDescription
                        )
                    }
                    
                    // ✅ THÊM: Tự động bắt đầu hội thoại sau khi đọc xong context
                    // Nếu TTS không hoạt động, sẽ tự động chuyển sau 3 giây
                    kotlinx.coroutines.delay(3000)
                    if (_uiState.value.currentStep == ConversationStep.CONTEXT) {
                        goToDialogueStep(0)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy hội thoại.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: ${e.message}") }
            }
        }
    }

    // --- SỬA LOGIC `onSpeechFinished` ---
    fun onSpeechFinished() {
        val currentState = _uiState.value
        val conversation = currentState.conversation ?: return

        when (currentState.currentStep) {
            ConversationStep.CONTEXT -> {
                goToDialogueStep(0)
            }

            ConversationStep.SHOWING_DIALOGUE -> {
                val currentDialogue = currentState.currentDialogue

                // Sửa: Kiểm tra xem câu thoại này CÓ QUIZ không
                if (currentDialogue?.vocabularyWord?.isNotBlank() == true &&
                    currentDialogue.options.isNotEmpty()) {

                    // 1. CÓ QUIZ: Chuyển sang QUIZ_CHOICE
                    _uiState.update {
                        it.copy(
                            currentStep = ConversationStep.QUIZ_CHOICE,
                            currentSpokenText = it.currentDialogue?.question, // Đọc câu hỏi (TV)
                            checkResult = CheckResult.NEUTRAL,
                            selectedOptionId = null
                        )
                    }
                } else {
                    // 2. KHÔNG CÓ QUIZ (ví dụ: câu cuối): Bỏ qua, sang câu tiếp theo
                    goToDialogueStep(currentState.currentDialogueIndex + 1)
                }
            }

            ConversationStep.QUIZ_CHOICE, ConversationStep.QUIZ_WRITE -> {
                _uiState.update { it.copy(currentSpokenText = null) }
            }
            else -> {}
        }
    }

    // (checkChoiceAnswer, checkWriteAnswer, clearCheckResult, retry - Giữ nguyên)
    fun checkChoiceAnswer(selectedOption: QuizOption) {
        if (_uiState.value.checkResult != CheckResult.NEUTRAL) return
        if (selectedOption.isCorrect) {
            _uiState.update { it.copy(checkResult = CheckResult.CORRECT, selectedOptionId = selectedOption.id) }
        } else {
            _uiState.update { it.copy(checkResult = CheckResult.INCORRECT, selectedOptionId = selectedOption.id) }
        }
    }
    fun checkWriteAnswer(userAnswer: String) {
        if (_uiState.value.checkResult != CheckResult.NEUTRAL) return
        val correctWord = _uiState.value.currentDialogue?.vocabularyWord
        if (userAnswer.trim().equals(correctWord?.trim(), ignoreCase = true)) {
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
    fun retry() { loadConversation() }

    // (onQuizContinue - Giữ nguyên)
    fun onQuizContinue() {
        val currentState = _uiState.value
        if (currentState.checkResult == CheckResult.INCORRECT) {
            _uiState.update { it.copy(checkResult = CheckResult.NEUTRAL, selectedOptionId = null) }
            return
        }

        when (currentState.currentStep) {
            ConversationStep.QUIZ_CHOICE -> {
                _uiState.update {
                    it.copy(
                        currentStep = ConversationStep.QUIZ_WRITE,
                        checkResult = CheckResult.NEUTRAL,
                        selectedOptionId = null,
                    )
                }
            }
            ConversationStep.QUIZ_WRITE -> {
                // Sửa: Tiến trình TĂNG LÊN ở đây
                goToDialogueStep(currentState.currentDialogueIndex + 1)
            }
            else -> {}
        }
    }

    // --- SỬA LOGIC `goToDialogueStep` ---
    private fun goToDialogueStep(index: Int) {
        val conversation = _uiState.value.conversation ?: return
        // Sửa: Lấy danh sách đã sắp xếp trước
        val sortedDialogues = conversation.dialogue.sortedBy { it.order }
        val nextDialogue = sortedDialogues.getOrNull(index)

        if (nextDialogue != null) {
            // Nếu còn hội thoại
            _uiState.update {
                it.copy(
                    currentStep = ConversationStep.SHOWING_DIALOGUE,
                    currentDialogueIndex = index, // <-- Cập nhật index
                    visibleDialogueLines = it.visibleDialogueLines + nextDialogue,
                    currentSpokenText = nextDialogue.text,
                    checkResult = CheckResult.NEUTRAL
                )
            }
        } else {
            // Hết hội thoại
            _uiState.update { it.copy(currentStep = ConversationStep.FINISHED) }
        }
    }
}