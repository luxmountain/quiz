package com.uilover.project247.ReviewActivity.Model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.models.*
import com.uilover.project247.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho Review Feature - 3-Step Flow
 */
class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()
    
    private val reviewRepository = ReviewRepository(application)
    
    // CRITICAL: Track words that failed at least once in this session
    // Used for STRICT GRADING: Fail once = Stay Level 1
    private val failedWordIds = mutableSetOf<String>()
    
    companion object {
        private const val TAG = "ReviewViewModel"
    }
    
    init {
        // Fix old Level 1 data with incorrect intervals
        reviewRepository.fixOldLevel1Data()
        
        // Start observing stats in real-time
        observeReviewStats()
    }
    
    /**
     * Observe Review Stats in Real-time (Flow)
     * Automatically updates UI when progress changes
     * STANDARD SPACED REPETITION: Simple due count and countdown
     */
    private fun observeReviewStats() {
        viewModelScope.launch {
            reviewRepository.observeReviewStats().collect { stats ->
                // STANDARD SR: Simple mapping
                val isReviewAvailable = stats.dueForReviewCount > 0
                val nextReviewTimestamp = stats.nextReviewTime
                val dueCount = stats.dueForReviewCount
                
                _uiState.update {
                    it.copy(
                        stats = stats,
                        isReviewAvailable = isReviewAvailable,
                        nextReviewTimestamp = nextReviewTimestamp,
                        dueCount = dueCount,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                
                Log.d(TAG, "[REAL-TIME] Stats updated (STANDARD SR):")
                Log.d(TAG, "  - Total words: ${stats.totalWordsInNotebook}")
                Log.d(TAG, "  - Due count: $dueCount")
                Log.d(TAG, "  - isReviewAvailable: $isReviewAvailable")
                Log.d(TAG, "  - nextReviewTimestamp: $nextReviewTimestamp")
            }
        }
    }
    
    /**
     * Load thống kê Dashboard (Manual refresh)
     * STANDARD SPACED REPETITION: Simple due count and countdown
     */
    fun loadReviewStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val stats = reviewRepository.getReviewStats()
                
                // STANDARD SR: Simple mapping
                val isReviewAvailable = stats.dueForReviewCount > 0
                val nextReviewTimestamp = stats.nextReviewTime
                val dueCount = stats.dueForReviewCount
                
                _uiState.update {
                    it.copy(
                        stats = stats,
                        isReviewAvailable = isReviewAvailable,
                        nextReviewTimestamp = nextReviewTimestamp,
                        dueCount = dueCount,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                
                Log.d(TAG, "[MANUAL REFRESH] Stats loaded (STANDARD SR):")
                Log.d(TAG, "  - Due count: $dueCount")
                Log.d(TAG, "  - isReviewAvailable: $isReviewAvailable")
                Log.d(TAG, "  - nextReviewTimestamp: $nextReviewTimestamp")
                
                Log.d(TAG, "Loaded stats: ${stats.totalWordsInNotebook} words")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading stats", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi tải dữ liệu: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Bắt đầu review session với 3-step flow
     * Fixed: Batch size = 5 words (not 10)
     */
    fun startReviewSession() {
        viewModelScope.launch {
            // CRITICAL: Clear failed words tracking for new session (STRICT GRADING)
            failedWordIds.clear()
            Log.d(TAG, "[STRICT GRADING] Cleared failed words tracker for new session")
            
            val sessionStartTime = System.currentTimeMillis()
            Log.d(TAG, "========================================")
            Log.d(TAG, "[TIMING] SESSION STARTED")
            Log.d(TAG, "[TIMING] Session Start Time: $sessionStartTime")
            Log.d(TAG, "========================================")
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val batchFlashcards = reviewRepository.getReviewBatch(limit = 5)  // FIXED: 5 words
                
                if (batchFlashcards.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Sổ tay chưa có từ nào. Hãy học từ mới trước!"
                        )
                    }
                    return@launch
                }
                
                // Tạo ReviewItem cho mỗi flashcard
                val items = batchFlashcards.map { flashcard ->
                    val distractors = reviewRepository.getRandomWrongOptions(flashcard, 3)

                    ReviewItem(
                        flashcard = flashcard,
                        preloadedWrongOptions = distractors // Lưu vào item
                    )
                }
                
                val session = ReviewSession(items = items)
                
                _uiState.update {
                    it.copy(
                        currentSession = session,
                        currentItemIndex = 0,
                        isLoading = false,
                        checkResult = ReviewCheckResult.NEUTRAL,
                        wrongOptions = items[0].preloadedWrongOptions
                    )
                }
                
                Log.d(TAG, "Started review session with ${items.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting review session", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi khởi tạo ôn tập: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * LIFECYCLE FIX: Refresh session when returning from pause
     * Prevents blank screen when app is killed in background
     */
    fun refreshSessionIfNeeded() {
        viewModelScope.launch {
            val state = _uiState.value
            
            Log.d(TAG, "refreshSessionIfNeeded - currentSession=${state.currentSession != null}, isSessionComplete=${state.isSessionComplete}")
            
            // If no active session and not completed, restart
            if (state.currentSession == null && !state.isSessionComplete) {
                Log.d(TAG, "No active session - Restarting")
                startReviewSession()
            } else {
                Log.d(TAG, "Session still active - No refresh needed")
            }
        }
    }
    
    /**
     * Generate wrong options cho Multiple Choice từ batch hiện tại
     */
    private fun generateWrongOptions(allFlashcards: List<Flashcard>, currentIndex: Int): List<String> {
        val currentCard = allFlashcards.getOrNull(currentIndex) ?: return emptyList()
        
        return allFlashcards
            .filter { it.id != currentCard.id }
            .map { it.meaning }
            .distinct()
            .shuffled()
            .take(3)
    }
    
    /**
     * Kiểm tra câu trả lời
     */
    fun checkAnswer(userAnswer: String) {
        val state = _uiState.value
        val currentItem = state.currentItem ?: return
        val currentCard = currentItem.flashcard
        
        val correctAnswer = when (currentItem.currentStep) {
            ReviewStep.FILL_IN_BLANK -> currentCard.word.lowercase()
            ReviewStep.LISTEN_AND_WRITE -> currentCard.word.lowercase()
            ReviewStep.MULTIPLE_CHOICE -> currentCard.meaning
        }
        
        val isCorrect = userAnswer.trim().lowercase() == correctAnswer.trim().lowercase()
        
        _uiState.update {
            it.copy(
                checkResult = if (isCorrect) ReviewCheckResult.CORRECT else ReviewCheckResult.INCORRECT
            )
        }
        
        // Log result
        val result = ReviewResult(
            flashcardId = currentCard.id,
            exerciseType = when (currentItem.currentStep) {
                ReviewStep.FILL_IN_BLANK -> ReviewExerciseType.FILL_IN_BLANK
                ReviewStep.LISTEN_AND_WRITE -> ReviewExerciseType.LISTEN_AND_WRITE
                ReviewStep.MULTIPLE_CHOICE -> ReviewExerciseType.MULTIPLE_CHOICE
            },
            isCorrect = isCorrect,
            userAnswer = userAnswer,
            correctAnswer = correctAnswer
        )
        
        val updatedResults = state.currentSession?.results.orEmpty() + result
        _uiState.update {
            it.copy(
                currentSession = it.currentSession?.copy(results = updatedResults)
            )
        }
        
        Log.d(TAG, "Answer checked: ${if (isCorrect) "CORRECT" else "WRONG"} - Step: ${currentItem.currentStep}")
    }
    
    /**
     * Clear check result (khi user typing)
     */
    fun clearCheckResult() {
        if (_uiState.value.checkResult == ReviewCheckResult.INCORRECT) {
            _uiState.update { it.copy(checkResult = ReviewCheckResult.NEUTRAL) }
        }
    }
    
    /**
     * Tiếp tục sau khi check (giống onQuizContinue trong Learning)
     * FIXED: Direct navigate on last item to avoid double-click bug
     */
    fun onContinue() {
        val state = _uiState.value
        val currentItem = state.currentItem ?: return
        val session = state.currentSession ?: return
        
        if (state.checkResult == ReviewCheckResult.INCORRECT) {
            // Nếu sai -> Đẩy item xuống cuối queue, reset steps
            handleIncorrectAnswer(currentItem, session)
        } else if (state.checkResult == ReviewCheckResult.CORRECT) {
            // Nếu đúng -> Xử lý logic tiến độ
            handleCorrectAnswer(currentItem, session)
        }
    }
    
    private fun handleCorrectAnswer(item: ReviewItem, session: ReviewSession) {
        val updatedCompletedSteps = item.completedSteps + item.currentStep
        val nextStep = when {
            ReviewStep.FILL_IN_BLANK !in updatedCompletedSteps -> ReviewStep.FILL_IN_BLANK
            ReviewStep.LISTEN_AND_WRITE !in updatedCompletedSteps -> ReviewStep.LISTEN_AND_WRITE
            ReviewStep.MULTIPLE_CHOICE !in updatedCompletedSteps -> ReviewStep.MULTIPLE_CHOICE
            else -> null // Hoàn thành 3 bước
        }
        
        if (nextStep != null) {
            // Chuyển sang bước tiếp theo của cùng từ
            val updatedItem = item.copy(
                currentStep = nextStep,
                completedSteps = updatedCompletedSteps
            )
            
            val updatedItems = session.items.toMutableList()
            updatedItems[_uiState.value.currentItemIndex] = updatedItem
            
            _uiState.update {
                it.copy(
                    currentSession = session.copy(items = updatedItems),
                    checkResult = ReviewCheckResult.NEUTRAL
                )
            }
            
            Log.d(TAG, "Move to next step: $nextStep")
        } else {
            // Hoàn thành 3 bước -> Update progress
            Log.d(TAG, "[handleCorrectAnswer] Item completed all 3 steps: ${item.flashcard.word}")
            
            // CRITICAL: STRICT GRADING - Check if this word failed at ANY point in the session
            val hadFailure = failedWordIds.contains(item.flashcard.id)
            val finalResult = !hadFailure // true if NO failures, false if failed at least once
            
            if (hadFailure) {
                Log.d(TAG, "[STRICT GRADING] Word '${item.flashcard.word}' FAILED earlier in session")
                Log.d(TAG, "[STRICT GRADING] Forcing isCorrect=FALSE to keep at Level 1")
            } else {
                Log.d(TAG, "[STRICT GRADING] Word '${item.flashcard.word}' had NO failures")
                Log.d(TAG, "[STRICT GRADING] Allowing promotion to next level")
            }
            
            Log.d(TAG, "[handleCorrectAnswer] Calling updateProgressAfterReview with isCorrect=$finalResult")
            //reviewRepository.updateProgressAfterReview(item.flashcard.id, isCorrect = finalResult)
            
            Log.d(TAG, "[handleCorrectAnswer] updateProgressAfterReview completed")
            
            // Update item as completed
            val updatedItem = item.copy(completedSteps = updatedCompletedSteps)
            val updatedItems = session.items.toMutableList()
            updatedItems[_uiState.value.currentItemIndex] = updatedItem
            
            // CRITICAL FIX: Clear checkResult FIRST before navigation check
            _uiState.update {
                it.copy(
                    currentSession = session.copy(items = updatedItems),
                    checkResult = ReviewCheckResult.NEUTRAL  // Clear popup immediately
                )
            }
            
            // Check if this is the last item
            val isLastItem = _uiState.value.currentItemIndex >= session.items.size - 1
            
            if (isLastItem) {
                // DIRECT NAVIGATE: Finish session immediately (no delay)
                finishSession()
                Log.d(TAG, "Last item completed - finishing session")
            } else {
                // Not last item, move to next
                moveToNextItem()
            }
        }
    }
    private fun handleIncorrectAnswer(item: ReviewItem, session: ReviewSession) {
        // CRITICAL: Mark this word as FAILED for the session (STRICT GRADING)
        failedWordIds.add(item.flashcard.id)

        // FIX: Xóa dòng gọi Repository update ở đây (để Batch Save sau)
        // reviewRepository.updateProgressAfterReview(...) <--- BỎ DÒNG NÀY

        val updatedItems = session.items.toMutableList()
        val currentIndex = _uiState.value.currentItemIndex

        // Xóa từ hiện tại khỏi vị trí đang đứng
        updatedItems.removeAt(currentIndex)

        // Reset item về bước 1 và đẩy xuống cuối hàng
        // Lưu ý: resetItem.copy() sẽ GIỮ NGUYÊN preloadedWrongOptions cũ (Vẫn đủ 4 đáp án cho lần sau gặp lại)
        val resetItem = item.copy(
            currentStep = ReviewStep.FILL_IN_BLANK,
            completedSteps = emptySet(),
            failedAttempts = item.failedAttempts + 1
        )
        updatedItems.add(resetItem)

        // Lấy từ tiếp theo sẽ hiển thị lên màn hình (chính là từ trôi vào vị trí index hiện tại)
        // Nếu là từ cuối cùng thì lấy chính nó (trường hợp còn 1 từ xoay vòng)
        val nextItemToShow = updatedItems.getOrNull(currentIndex) ?: updatedItems.last()

        _uiState.update {
            it.copy(
                currentSession = session.copy(items = updatedItems),
                checkResult = ReviewCheckResult.NEUTRAL,

                // 👇👇👇 SỬA ĐOẠN NÀY 👇👇👇
                // SAI: wrongOptions = generateWrongOptions(...)
                // ĐÚNG: Lấy từ túi dự trữ của từ tiếp theo
                wrongOptions = nextItemToShow.preloadedWrongOptions
            )
        }

        Log.d(TAG, "Incorrect - Reset to Level 1 and move to end")
    }
    /**
     * Lưu kết quả của TOÀN BỘ phiên học cùng một lúc.
     * Giúp thời gian đếm ngược bắt đầu tính từ lúc KẾT THÚC phiên.
     */
    private fun saveSessionProgress() {
        val session = _uiState.value.currentSession ?: return

        viewModelScope.launch {
            // Lấy thời gian hiện tại (Lúc kết thúc session)
            // Repository sẽ dùng mốc thời gian này để tính nextReviewDate
            // => Tất cả các từ sẽ bắt đầu đếm ngược từ GIÂY PHÚT NÀY.
            
            // CRITICAL: Làm tròn LÊN đầu giờ tiếp theo để countdown "đẹp"
            // Ví dụ: 13:23:50 -> 14:00:00, 13:58:40 -> 14:00:00
            // Đảm bảo countdown luôn hiển thị số giờ tròn (10h, 3d, 7d...)
            val now = System.currentTimeMillis()
            val finishTime = ((now / 3600000) + 1) * 3600000  // Round UP to next hour (3600000ms = 1 hour)
            
            val sessionStartTime = session.startTime
            val studyDuration = now - sessionStartTime
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "[TIMING] SAVING SESSION PROGRESS")
            Log.d(TAG, "[TIMING] Session Start Time: $sessionStartTime")
            Log.d(TAG, "[TIMING] Actual Time (NOW): $now")
            Log.d(TAG, "[TIMING] Rounded Time (CEILING): $finishTime (+${(finishTime - now) / 1000}s)")
            Log.d(TAG, "[TIMING] Study Duration: ${studyDuration / 1000}s (${studyDuration / 60000}m ${(studyDuration / 1000) % 60}s)")
            Log.d(TAG, "[TIMING] nextReviewDate will be calculated from: $finishTime (ROUNDED UP)")
            Log.d(TAG, "========================================")
            
            // Lấy danh sách các từ duy nhất (tránh trùng lặp do cơ chế đẩy từ sai xuống cuối)
            val uniqueItems = session.items.distinctBy { it.flashcard.id }

            uniqueItems.forEachIndexed { index, item ->
                // Logic Strict Grading: Nếu từ này đã từng sai trong phiên -> Coi như Sai (Về Level 1)
                // Nếu chưa từng sai -> Lên Level
                val isFinalCorrect = !failedWordIds.contains(item.flashcard.id)

                Log.d(TAG, "[TIMING] Saving word ${index + 1}/${uniqueItems.size}: '${item.flashcard.word}'")
                Log.d(TAG, "[TIMING]   - reviewTime passed to repository: $finishTime")
                Log.d(TAG, "[TIMING]   - isCorrect: $isFinalCorrect")

                // Gọi Repository update.
                // Vì repository lấy System.currentTimeMillis() bên trong nó,
                // nên việc gọi liên tiếp ở đây sẽ coi như cùng một thời điểm "Finish".
                reviewRepository.updateProgressAfterReview(item.flashcard.id, isFinalCorrect,reviewTime = finishTime)
            }

            val saveCompleteTime = System.currentTimeMillis()
            val saveDuration = saveCompleteTime - finishTime
            
            Log.d(TAG, "[TIMING] Batch saved ${uniqueItems.size} items on Session Finish")
            Log.d(TAG, "[TIMING] Save Complete Time: $saveCompleteTime")
            Log.d(TAG, "[TIMING] Save Duration: ${saveDuration}ms")
            Log.d(TAG, "========================================")

            // Load lại thống kê ngay để Dashboard cập nhật đồng hồ
            loadReviewStats()
        }
    }
//    private fun handleIncorrectAnswer2(item: ReviewItem, session: ReviewSession) {
//        // CRITICAL: Mark this word as FAILED for the session (STRICT GRADING)
//        failedWordIds.add(item.flashcard.id)
//        Log.d(TAG, "[STRICT GRADING] Word '${item.flashcard.word}' marked as FAILED")
//        Log.d(TAG, "[STRICT GRADING] Total failed words in session: ${failedWordIds.size}")
//
//        // CRITICAL: Update progress to reset level to 1
//        reviewRepository.updateProgressAfterReview(item.flashcard.id, isCorrect = false)
//
//        val updatedItems = session.items.toMutableList()
//        updatedItems.removeAt(_uiState.value.currentItemIndex)
//
//        // Reset item về bước 1 và đẩy xuống cuối
//        val resetItem = item.copy(
//            currentStep = ReviewStep.FILL_IN_BLANK,
//            completedSteps = emptySet(),
//            failedAttempts = item.failedAttempts + 1
//        )
//        updatedItems.add(resetItem)
//
//        _uiState.update {
//            it.copy(
//                currentSession = session.copy(items = updatedItems),
//                checkResult = ReviewCheckResult.NEUTRAL,
//                wrongOptions = generateWrongOptions(
//                    updatedItems.map { it.flashcard },
//                    it.currentItemIndex
//                )
//            )
//        }
//
//        Log.d(TAG, "Incorrect - Reset to Level 1 and move to end")
//    }
    private fun moveToNextItem() {
        val state = _uiState.value
        val session = state.currentSession ?: return

        if (state.currentItemIndex >= session.items.size - 1) {
            // Kết thúc session
            finishSession()
        } else {
            // Chuyển sang item tiếp theo
            val nextIndex = state.currentItemIndex + 1

            // Lấy item tiếp theo để lấy đáp án sai đã lưu trong đó
            val nextItem = session.items.getOrNull(nextIndex)

            // CRITICAL FIX: Reset checkResult to clear UI text input
            _uiState.update {
                it.copy(
                    currentItemIndex = nextIndex,
                    checkResult = ReviewCheckResult.NEUTRAL,  // This resets UI text!

                    // 👇 FIX QUAN TRỌNG: Lấy từ túi dự trữ (Preloaded), KHÔNG generate lại
                    wrongOptions = nextItem?.preloadedWrongOptions ?: emptyList()
                )
            }

            Log.d(TAG, "Moved to next item: $nextIndex (checkResult reset to NEUTRAL)")
        }
    }
//    private fun moveToNextItem() {
//        val state = _uiState.value
//        val session = state.currentSession ?: return
//
//        if (state.currentItemIndex >= session.items.size - 1) {
//            // Kết thúc session
//            finishSession()
//        } else {
//            // Chuyển sang item tiếp theo
//            val nextIndex = state.currentItemIndex + 1
//
//            // CRITICAL FIX: Reset checkResult to clear UI text input
//            _uiState.update {
//                it.copy(
//                    currentItemIndex = nextIndex,
//                    checkResult = ReviewCheckResult.NEUTRAL,  // This resets UI text!
//                    wrongOptions = generateWrongOptions(
//                        session.items.map { it.flashcard },
//                        nextIndex
//                    )
//                )
//            }
//
//            Log.d(TAG, "Moved to next item: $nextIndex (checkResult reset to NEUTRAL)")
//        }
//    }
    
    private fun finishSession() {
        val finishStartTime = System.currentTimeMillis()
        Log.d(TAG, "[TIMING] finishSession() called at: $finishStartTime")
        
        saveSessionProgress()
        
        // CRITICAL: Clear failed words tracker after saving
        failedWordIds.clear()
        Log.d(TAG, "[FINISH] Cleared failed words tracker for next session")
        
        val state = _uiState.value
        val session = state.currentSession?.copy(
            endTime = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                currentSession = session,
                isSessionComplete = true,
                checkResult = ReviewCheckResult.NEUTRAL  // Clear popup when finishing
            )
        }
        
        val finishEndTime = System.currentTimeMillis()
        Log.d(TAG, "[TIMING] finishSession() completed at: $finishEndTime")
        Log.d(TAG, "[TIMING] finishSession() duration: ${finishEndTime - finishStartTime}ms")

        Log.d(TAG, "Session complete: ${session?.getAccuracy()}% accuracy")
    }

    /**
     * Exit review mode (Thoát về Dashboard)
     * CRITICAL: Lưu progress với mốc thời gian THOÁT (không phải lúc học)
     */
    fun exitReviewMode() {
        // 1. Lấy giờ hiện tại (Giây phút bấm nút Thoát)
        // CRITICAL: Làm tròn LÊN đầu giờ để countdown đẹp
        val now = System.currentTimeMillis()
        val exitTime = ((now / 3600000) + 1) * 3600000  // Round UP to next hour
        
        Log.d(TAG, "[EXIT] User exited at: $now (rounded to $exitTime)")
        
        // 2. Nếu còn session chưa lưu -> Lưu với mốc giờ EXIT
        val session = _uiState.value.currentSession
        if (session != null && !_uiState.value.isSessionComplete) {
            Log.d(TAG, "[EXIT] Session not finished yet - Saving with EXIT time")
            
            viewModelScope.launch {
                val uniqueItems = session.items.distinctBy { it.flashcard.id }
                uniqueItems.forEach { item ->
                    val isFinalCorrect = !failedWordIds.contains(item.flashcard.id)
                    reviewRepository.updateProgressAfterReview(
                        item.flashcard.id, 
                        isFinalCorrect,
                        reviewTime = exitTime // Dùng thời điểm THOÁT
                    )
                }
                
                // Clear failed words tracker
                failedWordIds.clear()
                
                // Load lại stats
                loadReviewStats()
            }
        }
        
        // 3. Dọn dẹp UI
        _uiState.update { ReviewUiState() }
    }
    
    /**
     * Reset all progress (All words → Level 1)
     */
    fun resetAllProgress() {
        viewModelScope.launch {
            reviewRepository.resetAllProgress()
            loadReviewStats()
        }
    }
    
    /**
     * Clear all progress (Delete all progress)
     */
    fun clearAllProgress() {
        viewModelScope.launch {
            reviewRepository.clearReviewProgress()
            loadReviewStats()
        }
    }
}

/**
 * UI State cho Review Feature (3-Step Flow + Dashboard)
 */
data class ReviewUiState(
    val isLoading: Boolean = false,
    val stats: ReviewStats = ReviewStats(),
    val errorMessage: String? = null,
    
    // Dashboard State (STANDARD SPACED REPETITION)
    val isReviewAvailable: Boolean = false,      // True if dueForReviewCount > 0
    val nextReviewTimestamp: Long? = null,       // Min nextReviewDate where date > Now
    val dueCount: Int = 0,                       // Number of due words (ALL levels)
    
    // Review Session (3-Step Flow)
    val currentSession: ReviewSession? = null,
    val currentItemIndex: Int = 0,
    val checkResult: ReviewCheckResult = ReviewCheckResult.NEUTRAL,
    val wrongOptions: List<String> = emptyList(), // For Multiple Choice
    val isSessionComplete: Boolean = false
) {
    val currentItem: ReviewItem?
        get() = currentSession?.items?.getOrNull(currentItemIndex)
    
    val progress: Float
        get() = currentSession?.let { session ->
            if (session.items.isEmpty()) return@let 0f
            
            val currentItem = session.items.getOrNull(currentItemIndex)
            val totalSteps = session.items.size * 3
            
            // Calculate completed steps across all items
            val completedStepsCount = session.items.take(currentItemIndex).sumOf { it.completedSteps.size }
            
            // Add current item's progress
            val currentStepIndex = currentItem?.completedSteps?.size ?: 0
            
            val currentProgress = completedStepsCount + currentStepIndex
            currentProgress.toFloat() / totalSteps
        } ?: 0f
}