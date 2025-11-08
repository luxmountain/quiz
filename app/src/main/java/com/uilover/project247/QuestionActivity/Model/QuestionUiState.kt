package com.uilover.project247.QuestionActivity.Model

import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.data.MultipleChoiceQuiz

data class QuestionUiState(
    val quizzes: List<MultipleChoiceQuiz> = emptyList(),
    val currentQuizIndex: Int = 0,
    val isLoading: Boolean = true,
    val score: Int = 0,
    val selectedAnswer: String? = null,
    val checkResult: CheckResult = CheckResult.NEUTRAL,
    val isFinished: Boolean = false
) {
    val currentQuiz: MultipleChoiceQuiz?
        get() = quizzes.getOrNull(currentQuizIndex)

    val progress: Float
        get() = if (quizzes.isEmpty()) 0f else (currentQuizIndex + 1) / quizzes.size.toFloat()
}