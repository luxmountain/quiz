package com.uilover.project247.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Models cho Review Feature - Spaced Repetition System
 */

@Parcelize
data class FlashcardProgress(
    val flashcardId: String = "",
    val word: String = "",
    val learned: Boolean = false, // Đã học xong quy trình (flashcard + write + listen)
    val knownAlready: Boolean = false, // User bấm "Tôi đã biết từ này"
    val level: Int = 1, // Level 1-5 (Spaced Repetition)
    val lastReviewDate: Long = 0,
    val nextReviewDate: Long = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    
    /**
     * Kiểm tra từ có trong "Sổ tay" không
     * Điều kiện: learned == true && knownAlready == false
     */
    fun isInNotebook(): Boolean {
        return learned && !knownAlready
    }
    
    /**
     * Kiểm tra từ có cần ôn tập không
     */
    fun isDueForReview(): Boolean {
        return isInNotebook() && System.currentTimeMillis() >= nextReviewDate
    }
    
    /**
     * Tính accuracy
     */
    fun getAccuracy(): Float {
        val total = correctCount + wrongCount
        return if (total == 0) 0f else (correctCount.toFloat() / total) * 100
    }
}

/**
 * Stats cho Review Dashboard (Standard Spaced Repetition)
 */
data class ReviewStats(
    val totalWordsInNotebook: Int = 0,
    val level1Count: Int = 0,
    val level2Count: Int = 0,
    val level3Count: Int = 0,
    val level4Count: Int = 0,
    val level5Count: Int = 0,
    val dueForReviewCount: Int = 0,        // ALL words where nextReviewDate <= Now
    val nextReviewTime: Long? = null,
    val upcomingCount: Int = 0// Min nextReviewDate where date > Now
) {
    fun getLevelCount(level: Int): Int {
        return when (level) {
            1 -> level1Count
            2 -> level2Count
            3 -> level3Count
            4 -> level4Count
            5 -> level5Count
            else -> 0
        }
    }
    
    fun getMaxCount(): Int {
        return maxOf(level1Count, level2Count, level3Count, level4Count, level5Count)
    }
    
    /**
     * Kiểm tra xem có thể ôn tập không (Sổ tay không rỗng)
     */
    fun canReview(): Boolean {
        return totalWordsInNotebook > 0
    }
}

/**
 * Các bước ôn tập cho mỗi từ (3-Step Review Flow)
 */
enum class ReviewStep {
    FILL_IN_BLANK,      // Bước 1: Điền vào chỗ trống
    LISTEN_AND_WRITE,   // Bước 2: Nghe và viết
    MULTIPLE_CHOICE     // Bước 3: Trắc nghiệm
}

/**
 * Các dạng bài tập ôn tập (Legacy - giữ để tương thích)
 */
enum class ReviewExerciseType {
    LISTEN_AND_WRITE,  // Nghe và gõ lại
    FILL_IN_BLANK,     // Điền vào chỗ trống
    MULTIPLE_CHOICE    // Trắc nghiệm
}

/**
 * Kết quả kiểm tra (giống CheckResult trong Learning)
 */
enum class ReviewCheckResult {
    NEUTRAL,    // Chưa check
    CORRECT,    // Đúng
    INCORRECT   // Sai
}

/**
 * Một bài tập ôn tập (Legacy - kept for compatibility)
 */
data class ReviewExercise(
    val flashcard: Flashcard,
    val type: ReviewExerciseType,
    val question: String,
    val correctAnswer: String,
    val options: List<String> = emptyList() // Chỉ dùng cho Multiple Choice
)

/**
 * Item ôn tập trong session (3-Step Flow)
 */
data class ReviewItem(
    val flashcard: Flashcard,
    val currentStep: ReviewStep = ReviewStep.FILL_IN_BLANK,
    val completedSteps: Set<ReviewStep> = emptySet(),
    val failedAttempts: Int = 0,
    val preloadedWrongOptions: List<String> = emptyList()
) {
    fun isComplete(): Boolean = completedSteps.size == 3
    
    fun getNextStep(): ReviewStep? {
        return when {
            ReviewStep.FILL_IN_BLANK !in completedSteps -> ReviewStep.FILL_IN_BLANK
            ReviewStep.LISTEN_AND_WRITE !in completedSteps -> ReviewStep.LISTEN_AND_WRITE
            ReviewStep.MULTIPLE_CHOICE !in completedSteps -> ReviewStep.MULTIPLE_CHOICE
            else -> null
        }
    }
}

/**
 * Kết quả một lần ôn tập
 */
data class ReviewResult(
    val flashcardId: String,
    val exerciseType: ReviewExerciseType,
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Session ôn tập (Updated for 3-Step Flow)
 */
data class ReviewSession(
    val items: List<ReviewItem> = emptyList(),  // NEW: ReviewItem with 3-step flow
    val exercises: List<ReviewExercise> = emptyList(), // Legacy: kept for compatibility
    val results: List<ReviewResult> = emptyList(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0
) {
    fun getAccuracy(): Float {
        if (results.isEmpty()) return 0f
        val correct = results.count { it.isCorrect }
        return (correct.toFloat() / results.size) * 100
    }
    
    fun getDuration(): Long = if (endTime > 0) endTime - startTime else 0
}
