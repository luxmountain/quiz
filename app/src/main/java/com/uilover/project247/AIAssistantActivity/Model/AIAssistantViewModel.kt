package com.uilover.project247.AIAssistantActivity.Model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.ai.AIStudyAssistant
import com.uilover.project247.data.ai.StudyAnalysis
import com.uilover.project247.data.ai.WordRecommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AIAssistantUiState(
    val isLoading: Boolean = false,
    val analysis: StudyAnalysis? = null,
    val recommendations: List<WordRecommendation> = emptyList(),
    val errorMessage: String? = null,
    val selectedTab: AITab = AITab.ANALYSIS
)

enum class AITab {
    ANALYSIS,    // Phân tích tiến trình
    RECOMMENDATIONS // Gợi ý ôn tập
}

class AIAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()
    
    private val aiAssistant = AIStudyAssistant(application)

    init {
        loadAnalysis()
    }

    fun selectTab(tab: AITab) {
        _uiState.update { it.copy(selectedTab = tab) }
        when (tab) {
            AITab.ANALYSIS -> if (_uiState.value.analysis == null) loadAnalysis()
            AITab.RECOMMENDATIONS -> if (_uiState.value.recommendations.isEmpty()) loadRecommendations()
        }
    }

    fun loadAnalysis() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = aiAssistant.analyzeStudyProgress()
                
                result.onSuccess { analysis ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            analysis = analysis
                        )
                    }
                    Log.d("AIAssistantVM", "Analysis loaded: ${analysis.overallScore}")
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Không thể phân tích: ${error.message}"
                        )
                    }
                    Log.e("AIAssistantVM", "Error loading analysis", error)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Đã xảy ra lỗi: ${e.message}"
                    )
                }
                Log.e("AIAssistantVM", "Exception in loadAnalysis", e)
            }
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = aiAssistant.getReviewRecommendations()
                
                result.onSuccess { recommendations ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            recommendations = recommendations
                        )
                    }
                    Log.d("AIAssistantVM", "Recommendations loaded: ${recommendations.size}")
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Không thể tải gợi ý: ${error.message}"
                        )
                    }
                    Log.e("AIAssistantVM", "Error loading recommendations", error)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Đã xảy ra lỗi: ${e.message}"
                    )
                }
                Log.e("AIAssistantVM", "Exception in loadRecommendations", e)
            }
        }
    }

    fun refresh() {
        when (_uiState.value.selectedTab) {
            AITab.ANALYSIS -> loadAnalysis()
            AITab.RECOMMENDATIONS -> loadRecommendations()
        }
    }
}
