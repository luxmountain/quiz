package com.uilover.project247.DictionaryActivity.Model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.api.DictionaryApiService
import com.uilover.project247.data.models.DictionaryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DictionaryViewModel : ViewModel() {
    
    private val apiService = DictionaryApiService.create()
    
    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()
    
    private val recentSearchesSet = mutableSetOf<String>()
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun searchWord(word: String) {
        if (word.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập từ cần tra") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val response = apiService.searchWord(word.trim().lowercase())
                
                if (response.isSuccessful && response.body() != null) {
                    val entries = response.body()!!
                    
                    // Thêm vào lịch sử tìm kiếm
                    recentSearchesSet.add(word.trim())
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            entries = entries,
                            errorMessage = null,
                            recentSearches = recentSearchesSet.toList().takeLast(10).reversed()
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            entries = emptyList(),
                            errorMessage = "Không tìm thấy từ '${word}'. Vui lòng kiểm tra lại."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        entries = emptyList(),
                        errorMessage = "Lỗi kết nối: ${e.message ?: "Không thể kết nối đến server"}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun selectRecentSearch(word: String) {
        updateSearchQuery(word)
        searchWord(word)
    }
}
