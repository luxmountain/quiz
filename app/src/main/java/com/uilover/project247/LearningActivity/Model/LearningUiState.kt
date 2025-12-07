package com.uilover.project247.LearningActivity.Model

import com.uilover.project247.data.models.Flashcard

data class LearningUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentCardIndex: Int = 0,
    val currentStudyMode: StudyMode = StudyMode.FLASHCARD,
    val isLoading: Boolean = true,
    val isTopicComplete: Boolean = false,
    val checkResult: CheckResult = CheckResult.NEUTRAL,
    val topicName: String = "",
    val topicTotalWords: Int = 0, // Tổng số flashcards trong topic
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val startTime: Long = 0L
) {
    val currentCard: Flashcard?
        get() = flashcards.getOrNull(currentCardIndex)

    // Tính tiến trình
    val progress: Float
        get() = if (flashcards.isEmpty()) 0f else (currentCardIndex + 1) / flashcards.size.toFloat()
    
    // Tính accuracy
    val accuracy: Float
        get() {
            val total = correctAnswers + wrongAnswers
            return if (total == 0) 0f else (correctAnswers.toFloat() / total) * 100
        }
}