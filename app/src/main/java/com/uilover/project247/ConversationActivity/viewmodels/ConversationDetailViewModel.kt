package com.uilover.project247.ConversationActivity.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.data.models.Conversation
import com.uilover.project247.data.models.DialogueLine
import com.uilover.project247.data.models.QuizOption
import com.uilover.project247.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ConversationPhase {
    LOADING,          // Đang tải
    CONTEXT,          // Đang ở phần giới thiệu ngữ cảnh
    DIALOGUE,         // Đang ở phần hội thoại
    QUESTION,         // Đang ở phần câu hỏi
    FINISHED          // Đã trả lời xong
}
data class ConversationDetailUiState(
    val isLoading: Boolean = true,
    val conversation: Conversation? = null,
    val errorMessage: String? = null, // <-- Thêm trường lỗi
    // --- THAY ĐỔI: Quản lý trạng thái chi tiết ---
    val currentPhase: ConversationPhase = ConversationPhase.LOADING,
    val visibleDialogueLines: List<DialogueLine> = emptyList(), // Các dòng chat đã hiện
    val currentSpokenText: String? = null, // Văn bản cần đọc
    // -------------------------------------------

    // Trạng thái Quiz (giữ nguyên)
    val checkResult: CheckResult = CheckResult.NEUTRAL,
    val selectedOptionId: String? = null
)

class ConversationDetailViewModel(private val conversationId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationDetailUiState())
    val uiState: StateFlow<ConversationDetailUiState> = _uiState.asStateFlow()
    private val firebaseRepository = FirebaseRepository()

    init {
        loadConversation()
    }

    private fun loadConversation() {
        viewModelScope.launch {
            // 1. Bắt đầu tải, reset lỗi
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 2. Lấy dữ liệu từ Firebase
                // (Tôi đang giả lập `firebaseRepository`, bạn hãy thay thế bằng code thật)
                val conversation= firebaseRepository.getConversation(conversationId)

                if (conversation != null) {
                    // 3. THÀNH CÔNG: Cập nhật state VÀ bắt đầu luồng hội thoại
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversation = conversation,
                            // Bắt đầu bằng bước CONTEXT
                            currentPhase = ConversationPhase.CONTEXT,
                            // Yêu cầu đọc `contextDescriptionVi`
                            currentSpokenText = conversation.contextDescription
                        )
                    }
                    Log.d("ConvDetailViewModel", "Loaded conversation: ${conversation.id}")
                } else {
                    // 4. KHÔNG TÌM THẤY: Báo lỗi
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Không tìm thấy dữ liệu hội thoại.")
                    }
                    Log.w("ConvDetailViewModel", "Conversation with id $conversationId not found")
                }
            } catch (e: Exception) {
                // 5. LỖI KẾT NỐI: Báo lỗi
                Log.e("ConvDetailViewModel", "Error loading conversation $conversationId", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Lỗi kết nối: ${e.message}")
                }
            }
        }
    }
    fun onSpeechFinished() {
        val currentState = _uiState.value
        val conversation = currentState.conversation ?: return

        when (currentState.currentPhase) {

            // Nếu vừa đọc xong CONTEXT
            ConversationPhase.CONTEXT -> {
                // Chuyển sang DIALOGUE (câu đầu tiên)
                val firstDialogue = conversation.dialogue.sortedBy { it.order }.firstOrNull()
                if (firstDialogue != null) {
                    _uiState.update {
                        it.copy(
                            currentPhase = ConversationPhase.DIALOGUE,
                            visibleDialogueLines = listOf(firstDialogue), // Hiển thị câu đầu tiên
                            currentSpokenText = firstDialogue.text // Yêu cầu đọc câu đầu tiên
                        )
                    }
                } else {
                    goToQuestionPhase()
                }
            }

            // Nếu vừa đọc xong một câu DIALOGUE
            ConversationPhase.DIALOGUE -> {
                val currentOrder = currentState.visibleDialogueLines.last().order
                val nextDialogue = conversation.dialogue.sortedBy { it.order }
                    .find { it.order == currentOrder + 1 } // Tìm câu tiếp theo

                if (nextDialogue != null) {
                    // Nếu còn câu tiếp theo
                    _uiState.update {
                        it.copy(
                            visibleDialogueLines = it.visibleDialogueLines + nextDialogue,
                            currentSpokenText = nextDialogue.text // Yêu cầu đọc câu tiếp theo
                        )
                    }
                } else {
                    // Hết hội thoại -> Chuyển sang QUESTION
                    goToQuestionPhase()
                }
            }

            // Nếu vừa đọc xong QUESTION
            ConversationPhase.QUESTION -> {
                _uiState.update { it.copy(currentSpokenText = null) } // Dừng lại, chờ trả lời
            }

            else -> {
                _uiState.update { it.copy(currentSpokenText = null) }
            }
        }
    }

    private fun goToQuestionPhase() {
        _uiState.update {
            it.copy(
                currentPhase = ConversationPhase.QUESTION,
                currentSpokenText = it.conversation?.question // Đọc câu hỏi
            )
        }
    }
    fun checkAnswer(selectedOption: QuizOption) {
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
    fun retry() {
        loadConversation()
    }
    fun onQuizContinue() {
        val currentState = _uiState.value

        if(currentState.checkResult == CheckResult.CORRECT) {
            // Nếu đúng -> Kết thúc
            _uiState.update { it.copy(currentPhase = ConversationPhase.FINISHED) }
        } else {
            // Nếu sai -> Reset lại, cho trả lời lại
            _uiState.update {
                it.copy(
                    checkResult = CheckResult.NEUTRAL,
                    selectedOptionId = null
                )
            }
        }
    }
}




    // (Bạn có thể thêm hàm next() hoặc onComplete() ở đây)


