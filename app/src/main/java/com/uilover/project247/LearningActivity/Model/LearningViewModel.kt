package com.uilover.project247.LearningActivity.Model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.repository.FirebaseRepository
import com.uilover.project247.data.repository.UserProgressManager
import com.uilover.project247.data.repository.ReviewRepository
import com.uilover.project247.data.repository.StudyResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LearningViewModel(
    application: Application,
    private val levelId: String,
    private val topicId: String
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()
    private val progressManager = UserProgressManager(application)
    private val reviewRepository = ReviewRepository(application)

    init {
        loadFlashcardsForTopic()
        // Reset flashcard progress khi bắt đầu session học mới
        progressManager.resetFlashcardProgress(topicId)
    }

    private fun loadFlashcardsForTopic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load topic từ Firebase theo levelId và topicId
                val topic = firebaseRepository.getTopic(levelId, topicId)
                val flashcards = topic?.flashcards ?: emptyList()
                
                // Sử dụng flashcards.size làm totalWords để đảm bảo chính xác
                // vì có thể topic.totalWords chưa được set đúng trong Firebase
                val totalWords = if (flashcards.isNotEmpty()) flashcards.size else (topic?.totalWords ?: 0)
                
                _uiState.update {
                    it.copy(
                        flashcards = flashcards,
                        isLoading = false,
                        topicName = topic?.name ?: "",
                        topicTotalWords = totalWords,
                        startTime = System.currentTimeMillis()
                    )
                }
                
                Log.d("LearningViewModel", "Loaded topic $topicId: flashcards=${flashcards.size}, totalWords=$totalWords, topic.totalWords=${topic?.totalWords}")
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

        if (userAnswer.trim().equals(correctWord.trim(), ignoreCase = true)) {
            // NẾU ĐÚNG: Chỉ set trạng thái, KHÔNG tự động chuyển
            _uiState.update { 
                it.copy(
                    checkResult = CheckResult.CORRECT,
                    correctAnswers = it.correctAnswers + 1
                ) 
            }
        } else {
            // NẾU SAI: Chỉ set trạng thái
            _uiState.update { 
                it.copy(
                    checkResult = CheckResult.INCORRECT,
                    wrongAnswers = it.wrongAnswers + 1
                ) 
            }
        }
    }
    fun checkListenAnswer(userAnswer: String) {
        val correctWord = _uiState.value.currentCard?.word ?: return
        if (userAnswer.trim().equals(correctWord.trim(), ignoreCase = true)) {
            _uiState.update { 
                it.copy(
                    checkResult = CheckResult.CORRECT,
                    correctAnswers = it.correctAnswers + 1
                ) 
            }
        } else {
            _uiState.update { 
                it.copy(
                    checkResult = CheckResult.INCORRECT,
                    wrongAnswers = it.wrongAnswers + 1
                ) 
            }
        }
    }

    // --- 3. SỬA LẠI LOGIC "TIẾP TỤC" ---
    fun onQuizContinue() {
        val currentState = _uiState.value
        
        // Xử lý theo study mode hiện tại
        when (currentState.currentStudyMode) {
            // 2. Nếu xong WRITE_WORD -> Chuyển sang LISTEN_AND_WRITE (dù đúng hay sai)
            StudyMode.WRITE_WORD -> {
                _uiState.update {
                    it.copy(
                        currentStudyMode = StudyMode.LISTEN_AND_WRITE,
                        checkResult = CheckResult.NEUTRAL
                    )
                }
            }
            // 3. Nếu xong LISTEN_AND_WRITE -> Chuyển sang từ tiếp theo hoặc hoàn thành (dù đúng hay sai)
            StudyMode.LISTEN_AND_WRITE -> {
                // Đánh dấu flashcard đã học (cho Spaced Repetition)
                // NOTE: Đánh dấu DÙ ĐÚNG HAY SAI vì user đã trải qua 3 bước
                val flashcard = currentState.currentCard
                if (flashcard != null) {
                    // Đánh dấu cho Review system (Spaced Repetition)
                    reviewRepository.markFlashcardLearned(flashcard.id, flashcard.word)
                    // Đánh dấu cho Progress tracking (unlock topic, progress bar)
                    progressManager.markFlashcardAsLearned(topicId, flashcard.id)
                }
                
                // Kiểm tra xem đây có phải câu cuối cùng không
                if (currentState.currentCardIndex >= currentState.flashcards.size - 1) {
                    // Câu cuối cùng - Lưu kết quả và đánh dấu hoàn thành
                    saveStudyResult()
                    _uiState.update { 
                        it.copy(
                            isTopicComplete = true,
                            checkResult = CheckResult.NEUTRAL
                        ) 
                    }
                } else {
                    // Chưa phải câu cuối - Chuyển sang câu tiếp theo
                    goToNextCard()
                }
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
        }
        // Không còn xử lý hoàn thành ở đây nữa - đã chuyển sang onQuizContinue
    }
    fun onMarkAsKnown() {
        val currentState = _uiState.value
        val currentCard = currentState.currentCard ?: return

        viewModelScope.launch {
            // 1. Đánh dấu "Tôi đã biết" (knownAlready = true)
            // NOTE: Đánh dấu cho cả Review và Progress tracking
            try {
                reviewRepository.markFlashcardKnownAlready(currentCard.id, currentCard.word)
                // Cũng đánh dấu vào progressManager để tính tiến độ
                progressManager.markFlashcardAsLearned(topicId, currentCard.id)
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Error marking card as known already", e)
            }

            // 2. Logic điều hướng: Kiểm tra xem đã hết từ chưa?
            if (currentState.currentCardIndex >= currentState.flashcards.size - 1) {
                // TRƯỜNG HỢP: ĐÂY LÀ TỪ CUỐI CÙNG -> KẾT THÚC TOPIC
                saveStudyResult() // Quan trọng: Lưu kết quả học tập
                _uiState.update {
                    it.copy(
                        isTopicComplete = true, // Bật cờ này để UI chuyển sang màn Result
                        checkResult = CheckResult.NEUTRAL
                    )
                }
            } else {
                // TRƯỜNG HỢP: CÒN TỪ TIẾP THEO -> NEXT
                goToNextCard()
            }
        }
    }
    private fun saveStudyResult() {
        val currentState = _uiState.value
        val timeSpent = System.currentTimeMillis() - currentState.startTime
        
        // Lấy số flashcards ĐÃ THỰC SỰ HỌC từ learnedFlashcardIds
        val learnedIds = progressManager.getTopicCompletion(topicId)?.learnedFlashcardIds ?: emptySet()
        
        // Validation: đảm bảo topicTotalWords hợp lệ
        val topicTotal = if (currentState.topicTotalWords > 0) {
            currentState.topicTotalWords
        } else {
            currentState.flashcards.size // Fallback nếu topicTotalWords = 0
        }
        
        val result = StudyResult(
            topicId = topicId,
            topicName = currentState.topicName,
            studyType = "flashcard",
            totalItems = learnedIds.size, // Số flashcards đã học thực tế
            correctCount = currentState.correctAnswers,
            timeSpent = timeSpent,
            accuracy = currentState.accuracy,
            completedDate = System.currentTimeMillis(),
            topicTotalWords = topicTotal
        )
        
        progressManager.saveStudyResult(result)
        
        Log.d(
            "LearningViewModel",
            "Saved study result: topicId=$topicId, learned=${learnedIds.size}, total=$topicTotal, accuracy=${currentState.accuracy}%, correct=${currentState.correctAnswers}/${currentState.correctAnswers + currentState.wrongAnswers}"
        )
    }
}