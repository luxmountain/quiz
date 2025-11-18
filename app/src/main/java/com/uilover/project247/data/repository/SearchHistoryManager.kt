package com.uilover.project247.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uilover.project247.data.models.SearchHistoryItem

/**
 * Quản lý lịch sử tra cứu từ điển
 * Lưu trữ vào SharedPreferences
 */
class SearchHistoryManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "dictionary_history"
        private const val KEY_HISTORY = "search_history"
        private const val MAX_HISTORY_SIZE = 50 // Lưu tối đa 50 từ
    }
    
    /**
     * Lưu một từ vào lịch sử
     * Nếu từ đã tồn tại, cập nhật timestamp
     */
    fun saveToHistory(item: SearchHistoryItem) {
        val history = getHistory().toMutableList()
        
        // Xóa từ cũ nếu đã tồn tại
        history.removeAll { it.word.equals(item.word, ignoreCase = true) }
        
        // Thêm từ mới vào đầu danh sách
        history.add(0, item)
        
        // Giới hạn số lượng
        if (history.size > MAX_HISTORY_SIZE) {
            history.subList(MAX_HISTORY_SIZE, history.size).clear()
        }
        
        // Lưu vào SharedPreferences
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }
    
    /**
     * Lấy toàn bộ lịch sử
     */
    fun getHistory(): List<SearchHistoryItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<SearchHistoryItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Lấy N lịch sử gần nhất
     */
    fun getRecentHistory(count: Int = 3): List<SearchHistoryItem> {
        return getHistory().take(count)
    }
    
    /**
     * Xóa toàn bộ lịch sử
     */
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
    
    /**
     * Xóa một từ khỏi lịch sử
     */
    fun removeFromHistory(word: String) {
        val history = getHistory().toMutableList()
        history.removeAll { it.word.equals(word, ignoreCase = true) }
        
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }
}
