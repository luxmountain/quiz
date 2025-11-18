package com.uilover.project247.DictionaryActivity.Model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uilover.project247.data.api.DictionaryApiService
import com.uilover.project247.data.models.DictionaryUiState
import com.uilover.project247.data.models.SearchHistoryItem
import com.uilover.project247.data.repository.SearchHistoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DictionaryViewModel(private val context: Context) : ViewModel() {
    
    private val apiService = DictionaryApiService.create()
    private val historyManager = SearchHistoryManager(context)
    
    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()
    
    init {
        loadSearchHistory()
    }
    
    private fun loadSearchHistory() {
        val history = historyManager.getRecentHistory(10) // Tăng lên 10 để có nhiều kết quả filter
        _uiState.update { it.copy(recentSearches = history) }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun searchWord(word: String) {
        if (word.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập từ cần tra") }
            return
        }
        
        // Ẩn dropdown khi bắt đầu search
        _uiState.update { it.copy(isInputFocused = false) }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val response = apiService.searchWord(word.trim().lowercase())
                
                if (response.isSuccessful && response.body() != null) {
                    val entries = response.body()!!
                    
                    // Lưu vào lịch sử với thông tin chi tiết
                    if (entries.isNotEmpty()) {
                        val entry = entries.first()
                        val phonetic = entry.phonetic ?: entry.phonetics.firstOrNull()?.text ?: ""
                        val firstMeaning = entry.meanings.firstOrNull()
                        val meaning = firstMeaning?.definitions?.firstOrNull()?.definition ?: ""
                        val partOfSpeech = firstMeaning?.partOfSpeech ?: ""
                        
                        val historyItem = SearchHistoryItem(
                            word = entry.word,
                            phonetic = phonetic,
                            meaning = meaning,
                            partOfSpeech = partOfSpeech
                        )
                        historyManager.saveToHistory(historyItem)
                        loadSearchHistory() // Reload lịch sử
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            entries = entries,
                            errorMessage = null
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
    
    fun updateInputFocus(isFocused: Boolean) {
        _uiState.update { it.copy(isInputFocused = isFocused) }
    }
}
