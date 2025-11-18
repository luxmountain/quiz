package com.uilover.project247.data.models

import com.google.gson.annotations.SerializedName

/**
 * Response từ Free Dictionary API
 * API: https://api.dictionaryapi.dev/api/v2/entries/en/{word}
 */
data class DictionaryEntry(
    @SerializedName("word")
    val word: String,
    
    @SerializedName("phonetic")
    val phonetic: String? = null,
    
    @SerializedName("phonetics")
    val phonetics: List<Phonetic> = emptyList(),
    
    @SerializedName("meanings")
    val meanings: List<Meaning> = emptyList(),
    
    @SerializedName("origin")
    val origin: String? = null,
    
    @SerializedName("sourceUrls")
    val sourceUrls: List<String> = emptyList()
)

data class Phonetic(
    @SerializedName("text")
    val text: String? = null,
    
    @SerializedName("audio")
    val audio: String? = null,
    
    @SerializedName("sourceUrl")
    val sourceUrl: String? = null
)

data class Meaning(
    @SerializedName("partOfSpeech")
    val partOfSpeech: String, // noun, verb, adjective, etc.
    
    @SerializedName("definitions")
    val definitions: List<Definition> = emptyList(),
    
    @SerializedName("synonyms")
    val synonyms: List<String> = emptyList(),
    
    @SerializedName("antonyms")
    val antonyms: List<String> = emptyList()
)

data class Definition(
    @SerializedName("definition")
    val definition: String,
    
    @SerializedName("example")
    val example: String? = null,
    
    @SerializedName("synonyms")
    val synonyms: List<String> = emptyList(),
    
    @SerializedName("antonyms")
    val antonyms: List<String> = emptyList()
)

/**
 * Lịch sử tra cứu từ điển
 */
data class SearchHistoryItem(
    val word: String,
    val phonetic: String,
    val meaning: String, // Nghĩa đầu tiên
    val partOfSpeech: String, // Từ loại (noun, verb, etc.)
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * UI State cho Dictionary Screen
 */
data class DictionaryUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val entries: List<DictionaryEntry> = emptyList(),
    val errorMessage: String? = null,
    val recentSearches: List<SearchHistoryItem> = emptyList(),
    val isInputFocused: Boolean = false
) {
    // Lọc lịch sử theo từ khóa
    val filteredRecentSearches: List<SearchHistoryItem>
        get() = if (searchQuery.isBlank()) {
            recentSearches
        } else {
            recentSearches.filter { 
                it.word.contains(searchQuery, ignoreCase = true)
            }
        }
}
