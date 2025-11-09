package com.uilover.project247.LearningActivity.Model

import com.uilover.project247.data.VocabularyWord

data class LearningUiState(
    val words: List<VocabularyWord> = emptyList(),
    val currentWordIndex: Int = 0,
    val currentStudyMode: StudyMode = StudyMode.FLASHCARD,
    val isLoading: Boolean = true,
    val isTopicComplete: Boolean = false,
    val checkResult: CheckResult = CheckResult.NEUTRAL
) {
    val currentWord: VocabularyWord?
        get() = words.getOrNull(currentWordIndex)

    // (Thêm lại trường progress để dùng cho thanh tiến trình)
    val progress: Float
        get() = if (words.isEmpty()) 0f else (currentWordIndex + 1) / words.size.toFloat()
}