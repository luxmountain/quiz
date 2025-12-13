package com.uilover.project247.PlacementTestActivity.Model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.models.PlacementTest
import com.uilover.project247.data.models.PlacementTestResult
import com.uilover.project247.data.repository.FirebaseRepository
import com.uilover.project247.data.repository.PlacementTestManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlacementTestUiState(
    val isLoading: Boolean = true,
    val placementTest: PlacementTest? = null,
    val currentQuestionIndex: Int = 0,
    val userAnswers: MutableMap<Int, Int> = mutableMapOf(), // questionIndex -> selectedOption
    val isAnswered: Boolean = false,
    val timeRemaining: Int = 0, // seconds
    val isTestCompleted: Boolean = false,
    val testResult: PlacementTestResult? = null,
    val errorMessage: String? = null,
    val showInstructions: Boolean = true
)

class PlacementTestViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlacementTestUiState())
    val uiState: StateFlow<PlacementTestUiState> = _uiState.asStateFlow()
    
    private val firebaseRepository = FirebaseRepository()
    private val placementTestManager = PlacementTestManager(application)

    init {
        loadPlacementTest()
    }

    private fun loadPlacementTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val test = firebaseRepository.getPlacementTest()
                
                if (test != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            placementTest = test,
                            timeRemaining = test.duration
                        )
                    }
                    Log.d("PlacementTestVM", "Loaded ${test.questions.size} questions")
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Không thể tải bài test"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PlacementTestVM", "Error loading placement test", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi: ${e.message}"
                    )
                }
            }
        }
    }

    fun startTest() {
        _uiState.update { it.copy(showInstructions = false) }
    }

    fun selectAnswer(optionIndex: Int) {
        val currentState = _uiState.value
        val currentIndex = currentState.currentQuestionIndex
        
        val newAnswers = currentState.userAnswers.toMutableMap()
        newAnswers[currentIndex] = optionIndex
        
        _uiState.update {
            it.copy(
                userAnswers = newAnswers,
                isAnswered = true
            )
        }
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val test = currentState.placementTest ?: return
        
        if (currentState.currentQuestionIndex < test.questions.size - 1) {
            // Chuyển sang câu tiếp theo
            val nextIndex = currentState.currentQuestionIndex + 1
            val hasAnsweredNext = currentState.userAnswers.containsKey(nextIndex)
            
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    isAnswered = hasAnsweredNext
                )
            }
        } else {
            // Hoàn thành test
            completeTest()
        }
    }

    fun previousQuestion() {
        val currentState = _uiState.value
        
        if (currentState.currentQuestionIndex > 0) {
            val prevIndex = currentState.currentQuestionIndex - 1
            val hasAnsweredPrev = currentState.userAnswers.containsKey(prevIndex)
            
            _uiState.update {
                it.copy(
                    currentQuestionIndex = prevIndex,
                    isAnswered = hasAnsweredPrev
                )
            }
        }
    }

    fun goToQuestion(index: Int) {
        val hasAnswered = _uiState.value.userAnswers.containsKey(index)
        
        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                isAnswered = hasAnswered
            )
        }
    }

    private fun completeTest() {
        val currentState = _uiState.value
        val test = currentState.placementTest ?: return
        
        // Tính điểm
        var correctCount = 0
        test.questions.forEachIndexed { index, question ->
            val userAnswer = currentState.userAnswers[index]
            if (userAnswer == question.correctAnswer) {
                correctCount++
            }
        }
        
        val totalQuestions = test.questions.size
        val wrongCount = totalQuestions - correctCount
        val scorePercentage = (correctCount * 100) / totalQuestions
        
        // Xác định level
        val passingScoresMap = mapOf(
            "beginner" to test.passingScores.beginner,
            "elementary" to test.passingScores.elementary,
            "intermediate" to test.passingScores.intermediate,
            "advanced" to test.passingScores.advanced
        )
        
        val (level, levelVi) = placementTestManager.calculateLevel(
            correctCount,
            totalQuestions,
            passingScoresMap
        )
        
        // Tạo kết quả
        val result = PlacementTestResult(
            totalQuestions = totalQuestions,
            correctAnswers = correctCount,
            wrongAnswers = wrongCount,
            score = scorePercentage,
            recommendedLevel = level,
            recommendedLevelVi = levelVi,
            completedDate = System.currentTimeMillis()
        )
        
        // Lưu kết quả
        placementTestManager.saveTestResult(result)
        
        _uiState.update {
            it.copy(
                isTestCompleted = true,
                testResult = result
            )
        }
        
        Log.d("PlacementTestVM", "Test completed: $correctCount/$totalQuestions = $level")
    }

    fun updateTimeRemaining(seconds: Int) {
        _uiState.update { it.copy(timeRemaining = seconds) }
        
        if (seconds <= 0) {
            completeTest()
        }
    }

    fun getProgress(): Float {
        val state = _uiState.value
        val test = state.placementTest ?: return 0f
        return (state.currentQuestionIndex + 1).toFloat() / test.questions.size
    }
}
