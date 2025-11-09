package com.uilover.project247.LearningActivity.Model

import com.uilover.project247.data.models.Flashcard

data class LearningUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentCardIndex: Int = 0,
    val currentStudyMode: StudyMode = StudyMode.FLASHCARD,
    val isLoading: Boolean = true,
    val isTopicComplete: Boolean = false,
    val checkResult: CheckResult = CheckResult.NEUTRAL
) {
    val currentCard: Flashcard?
        get() = flashcards.getOrNull(currentCardIndex)

    // Tính tiến trình
    val progress: Float
        get() = if (flashcards.isEmpty()) 0f else (currentCardIndex + 1) / flashcards.size.toFloat()
}