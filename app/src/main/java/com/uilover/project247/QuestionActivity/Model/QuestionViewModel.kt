package com.uilover.project247.QuestionActivity.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.MockData
import com.uilover.project247.data.MultipleChoiceQuiz
import com.uilover.project247.LearningActivity.Model.CheckResult // Tái sử dụng enum
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuestionViewModel(private val topicId: String) : ViewModel(){
    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    init {
        loadQuizzesForTopic()
    }

    private fun loadQuizzesForTopic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val words= MockData.wordsByTopicId[topicId] ?:emptyList()
            val  allQuizzes= words.flatMap { it.quizzes }
            _uiState.update {
                it.copy(isLoading = false, quizzes = allQuizzes.shuffled())
            }
        }
    }
    fun submitAnswer(selectedOption: String) {
        if (_uiState.value.checkResult!=CheckResult.NEUTRAL) return

        val correctWord= _uiState.value.currentQuiz?.correctAnswer ?: return

        if (selectedOption==correctWord){
            _uiState.update {
                it.copy(
                    selectedAnswer = selectedOption,
                    checkResult = CheckResult.CORRECT,
                    score = it.score+1
                )
            }

        }
        else{
            _uiState.update{
                it.copy(
                    selectedAnswer = selectedOption,
                    checkResult = CheckResult.INCORRECT
                )
            }
        }
        viewModelScope.launch {
            delay(1000)
            goToNextQuestion()
        }
    }
    private fun goToNextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuizIndex < currentState.quizzes.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuizIndex = it.currentQuizIndex + 1,
                    checkResult = CheckResult.NEUTRAL,
                    selectedAnswer = null
                )
            }
        } else {
            _uiState.update { it.copy(isFinished = true) }
        }
    }
}