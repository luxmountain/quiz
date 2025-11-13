package com.uilover.project247.utils

import com.uilover.project247.data.models.CardState
import com.uilover.project247.data.models.FlashcardResult
import com.uilover.project247.data.models.ReviewQuality
import kotlin.math.roundToInt

/**
 * Anki Spaced Repetition System (SRS) Scheduler
 * 
 * Dựa trên thuật toán Anki (Modified SM-2) để tính toán thời điểm ôn tập tiếp theo
 * 
 * Các giai đoạn:
 * - NEW: Thẻ mới chưa học
 * - LEARNING: Đang học (learning steps: 1 phút, 10 phút)
 * - REVIEW: Ôn tập định kỳ (1 ngày, 4 ngày, 10 ngày, ...)
 * - RELEARNING: Học lại sau khi quên
 * 
 * Các mức độ trả lời:
 * - AGAIN: Quên hoàn toàn
 * - HARD: Nhớ khó khăn (interval * 1.2)
 * - GOOD: Nhớ bình thường (interval * ease factor)
 * - EASY: Nhớ rất dễ (interval * ease factor * 1.3)
 */
class AnkiScheduler {
    
    companion object {
        // Learning steps (phút) - Giai đoạn học mới
        private val LEARNING_STEPS = listOf(1, 10)
        
        // Graduating interval (ngày) - Khi hoàn thành learning
        private const val GRADUATING_INTERVAL = 1
        
        // Easy interval (ngày) - Khi bấm Easy ngay từ đầu
        private const val EASY_INTERVAL = 4
        
        // Ease factor constraints
        private const val MIN_EASE_FACTOR = 1.3f
        private const val STARTING_EASE_FACTOR = 2.5f
        
        // Interval modifiers
        private const val HARD_MULTIPLIER = 1.2f
        private const val EASY_BONUS_MULTIPLIER = 1.3f
        
        // Lapse (quên) penalty
        private const val LAPSE_EASE_PENALTY = 0.85f // Giảm 15% ease factor khi quên
        private const val NEW_LAPSE_INTERVAL = 1 // Reset về 1 ngày khi quên
    }
    
    /**
     * Tính toán schedule tiếp theo dựa trên chất lượng trả lời
     * 
     * @param card FlashcardResult hiện tại
     * @param quality Chất lượng trả lời (AGAIN, HARD, GOOD, EASY)
     * @return FlashcardResult mới với schedule đã được cập nhật
     */
    fun scheduleCard(
        card: FlashcardResult,
        quality: ReviewQuality
    ): FlashcardResult {
        return when (card.getCardState()) {
            CardState.NEW -> scheduleNewCard(card, quality)
            CardState.LEARNING -> scheduleLearningCard(card, quality)
            CardState.REVIEW -> scheduleReviewCard(card, quality)
            CardState.RELEARNING -> scheduleRelearningCard(card, quality)
        }
    }
    
    /**
     * Schedule cho thẻ mới (NEW)
     */
    private fun scheduleNewCard(
        card: FlashcardResult,
        quality: ReviewQuality
    ): FlashcardResult {
        val now = System.currentTimeMillis()
        
        return when (quality) {
            ReviewQuality.AGAIN -> {
                // Học lại sau 1 phút
                card.copy(
                    state = CardState.LEARNING.name,
                    currentStep = 0,
                    lastReviewDate = now,
                    nextReviewDate = now + (LEARNING_STEPS[0] * 60 * 1000L),
                    reviewCount = card.reviewCount + 1
                )
            }
            
            ReviewQuality.HARD -> {
                // Giống GOOD nhưng vẫn qua bước 1
                card.copy(
                    state = CardState.LEARNING.name,
                    currentStep = 0,
                    lastReviewDate = now,
                    nextReviewDate = now + (LEARNING_STEPS[0] * 60 * 1000L),
                    reviewCount = card.reviewCount + 1
                )
            }
            
            ReviewQuality.GOOD -> {
                // Qua bước tiếp theo (10 phút)
                card.copy(
                    state = CardState.LEARNING.name,
                    currentStep = 1,
                    lastReviewDate = now,
                    nextReviewDate = now + (LEARNING_STEPS[1] * 60 * 1000L),
                    reviewCount = card.reviewCount + 1
                )
            }
            
            ReviewQuality.EASY -> {
                // Tốt nghiệp luôn sang REVIEW
                card.copy(
                    state = CardState.REVIEW.name,
                    intervalDays = EASY_INTERVAL.toFloat(),
                    lastReviewDate = now,
                    nextReviewDate = now + (EASY_INTERVAL * 24 * 60 * 60 * 1000L),
                    reviewCount = card.reviewCount + 1,
                    learned = true
                )
            }
        }
    }
    
    /**
     * Schedule cho thẻ đang học (LEARNING)
     */
    private fun scheduleLearningCard(
        card: FlashcardResult,
        quality: ReviewQuality
    ): FlashcardResult {
        val now = System.currentTimeMillis()
        
        return when (quality) {
            ReviewQuality.AGAIN -> {
                // Reset về bước đầu
                card.copy(
                    currentStep = 0,
                    lastReviewDate = now,
                    nextReviewDate = now + (LEARNING_STEPS[0] * 60 * 1000L),
                    reviewCount = card.reviewCount + 1
                )
            }
            
            ReviewQuality.HARD -> {
                // Giữ nguyên bước hiện tại
                val currentStepTime = LEARNING_STEPS.getOrNull(card.currentStep) ?: LEARNING_STEPS.last()
                card.copy(
                    lastReviewDate = now,
                    nextReviewDate = now + (currentStepTime * 60 * 1000L),
                    reviewCount = card.reviewCount + 1
                )
            }
            
            ReviewQuality.GOOD -> {
                if (card.currentStep < LEARNING_STEPS.size - 1) {
                    // Qua bước tiếp theo
                    val nextStep = card.currentStep + 1
                    card.copy(
                        currentStep = nextStep,
                        lastReviewDate = now,
                        nextReviewDate = now + (LEARNING_STEPS[nextStep] * 60 * 1000L),
                        reviewCount = card.reviewCount + 1
                    )
                } else {
                    // Tốt nghiệp sang REVIEW
                    card.copy(
                        state = CardState.REVIEW.name,
                        intervalDays = GRADUATING_INTERVAL.toFloat(),
                        lastReviewDate = now,
                        nextReviewDate = now + (GRADUATING_INTERVAL * 24 * 60 * 60 * 1000L),
                        reviewCount = card.reviewCount + 1,
                        learned = true
                    )
                }
            }
            
            ReviewQuality.EASY -> {
                // Tốt nghiệp sớm với interval dài hơn
                card.copy(
                    state = CardState.REVIEW.name,
                    intervalDays = EASY_INTERVAL.toFloat(),
                    lastReviewDate = now,
                    nextReviewDate = now + (EASY_INTERVAL * 24 * 60 * 60 * 1000L),
                    reviewCount = card.reviewCount + 1,
                    learned = true
                )
            }
        }
    }
    
    /**
     * Schedule cho thẻ đang ôn tập (REVIEW)
     */
    private fun scheduleReviewCard(
        card: FlashcardResult,
        quality: ReviewQuality
    ): FlashcardResult {
        val now = System.currentTimeMillis()
        
        // Nếu AGAIN hoặc HARD mà quá khó → chuyển sang RELEARNING
        if (quality == ReviewQuality.AGAIN) {
            return card.copy(
                state = CardState.RELEARNING.name,
                currentStep = 0,
                lapses = card.lapses + 1,
                lastReviewDate = now,
                nextReviewDate = now + (LEARNING_STEPS[0] * 60 * 1000L),
                reviewCount = card.reviewCount + 1,
                learned = false
            )
        }
        
        // Tính ease factor mới (chỉ cho HARD, GOOD, EASY)
        val q = when (quality) {
            ReviewQuality.AGAIN -> 0
            ReviewQuality.HARD -> 1
            ReviewQuality.GOOD -> 2
            ReviewQuality.EASY -> 3
        }
        
        var newEaseFactor = card.easeFactor + (0.1f - (3 - q) * (0.08f + (3 - q) * 0.02f))
        if (newEaseFactor < MIN_EASE_FACTOR) newEaseFactor = MIN_EASE_FACTOR
        
        // Tính interval multiplier
        val multiplier = when (quality) {
            ReviewQuality.HARD -> HARD_MULTIPLIER
            ReviewQuality.GOOD -> newEaseFactor
            ReviewQuality.EASY -> newEaseFactor * EASY_BONUS_MULTIPLIER
            else -> 1.0f
        }
        
        // Tính interval mới (tối thiểu 1 ngày)
        val newInterval = (card.intervalDays * multiplier).roundToInt().coerceAtLeast(1)
        
        return card.copy(
            easeFactor = newEaseFactor,
            intervalDays = newInterval.toFloat(),
            lastReviewDate = now,
            nextReviewDate = now + (newInterval * 24 * 60 * 60 * 1000L),
            reviewCount = card.reviewCount + 1
        )
    }
    
    /**
     * Schedule cho thẻ học lại (RELEARNING)
     */
    private fun scheduleRelearningCard(
        card: FlashcardResult,
        quality: ReviewQuality
    ): FlashcardResult {
        val now = System.currentTimeMillis()
        
        // Xử lý giống LEARNING
        val result = when (quality) {
            ReviewQuality.AGAIN -> {
                card.copy(
                    currentStep = 0,
                    lastReviewDate = now,
                    nextReviewDate = now + (LEARNING_STEPS[0] * 60 * 1000L),
                    reviewCount = card.reviewCount + 1
                )
            }
            
            ReviewQuality.HARD -> {
                val currentStepTime = LEARNING_STEPS.getOrNull(card.currentStep) ?: LEARNING_STEPS.last()
                card.copy(
                    lastReviewDate = now,
                    nextReviewDate = now + (currentStepTime * 60 * 1000L),
                    reviewCount = card.reviewCount + 1
                )
            }
            
            ReviewQuality.GOOD -> {
                if (card.currentStep < LEARNING_STEPS.size - 1) {
                    val nextStep = card.currentStep + 1
                    card.copy(
                        currentStep = nextStep,
                        lastReviewDate = now,
                        nextReviewDate = now + (LEARNING_STEPS[nextStep] * 60 * 1000L),
                        reviewCount = card.reviewCount + 1
                    )
                } else {
                    // Tốt nghiệp về REVIEW với ease factor bị giảm
                    val penaltyEaseFactor = (card.easeFactor * LAPSE_EASE_PENALTY).coerceAtLeast(MIN_EASE_FACTOR)
                    card.copy(
                        state = CardState.REVIEW.name,
                        intervalDays = NEW_LAPSE_INTERVAL.toFloat(),
                        easeFactor = penaltyEaseFactor,
                        lastReviewDate = now,
                        nextReviewDate = now + (NEW_LAPSE_INTERVAL * 24 * 60 * 60 * 1000L),
                        reviewCount = card.reviewCount + 1,
                        learned = true
                    )
                }
            }
            
            ReviewQuality.EASY -> {
                val penaltyEaseFactor = (card.easeFactor * LAPSE_EASE_PENALTY).coerceAtLeast(MIN_EASE_FACTOR)
                card.copy(
                    state = CardState.REVIEW.name,
                    intervalDays = EASY_INTERVAL.toFloat(),
                    easeFactor = penaltyEaseFactor,
                    lastReviewDate = now,
                    nextReviewDate = now + (EASY_INTERVAL * 24 * 60 * 60 * 1000L),
                    reviewCount = card.reviewCount + 1,
                    learned = true
                )
            }
        }
        
        return result
    }
    
    /**
     * Lấy danh sách cards cần ôn tập hôm nay
     */
    fun getDueCards(
        allResults: Map<String, FlashcardResult>,
        currentTime: Long = System.currentTimeMillis()
    ): List<FlashcardResult> {
        return allResults.values.filter { it.isDue(currentTime) }
    }
    
    /**
     * Lấy số lượng cards mới (chưa học)
     */
    fun getNewCardsCount(allResults: Map<String, FlashcardResult>): Int {
        return allResults.values.count { it.isNew() }
    }
    
    /**
     * Lấy số lượng cards đang học (Learning + Relearning)
     */
    fun getLearningCardsCount(allResults: Map<String, FlashcardResult>): Int {
        return allResults.values.count { it.isLearning() }
    }
    
    /**
     * Lấy số lượng cards cần review hôm nay
     */
    fun getReviewCardsCount(
        allResults: Map<String, FlashcardResult>,
        currentTime: Long = System.currentTimeMillis()
    ): Int {
        return allResults.values.count { 
            it.getCardState() == CardState.REVIEW && it.isDue(currentTime)
        }
    }
    
    /**
     * Format interval thành text dễ đọc
     */
    fun formatInterval(intervalDays: Float): String {
        return when {
            intervalDays < 1 -> "${(intervalDays * 24 * 60).roundToInt()} phút"
            intervalDays < 30 -> "${intervalDays.roundToInt()} ngày"
            intervalDays < 365 -> "${(intervalDays / 30).roundToInt()} tháng"
            else -> "${(intervalDays / 365).roundToInt()} năm"
        }
    }
}
