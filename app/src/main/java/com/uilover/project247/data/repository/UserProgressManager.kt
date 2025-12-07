package com.uilover.project247.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class StudyResult(
    val topicId: String,
    val topicName: String,
    val studyType: String, // "flashcard" hoặc "conversation"
    val totalItems: Int, // Số flashcards đã học trong session này
    val correctCount: Int,
    val timeSpent: Long,
    val accuracy: Float,
    val completedDate: Long,
    val topicTotalWords: Int = 0 // Tổng số flashcards trong topic
)

data class TopicCompletionStatus(
    val topicId: String,
    val isCompleted: Boolean,
    val lastStudyDate: Long,
    val studyCount: Int = 0, // Số lần đã học topic
    val totalConversationsCompleted: Int = 0,
    val bestAccuracy: Float = 0f,
    val totalTimeSpent: Long = 0,
    val learnedFlashcardIds: Set<String>? = emptySet() // Nullable để tương thích với data cũ
)

class UserProgressManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_progress",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val KEY_COMPLETED_TOPICS = "completed_topics"
        private const val KEY_STUDY_HISTORY = "study_history"
        private const val MIN_ACCURACY_TO_COMPLETE = 60f // Giảm xuống 60% để dễ hoàn thành hơn
    }

    fun saveStudyResult(result: StudyResult) {
        // 1. Lưu vào lịch sử học tập
        val history = getStudyHistory().toMutableList()
        history.add(0, result) // Thêm vào đầu danh sách
        
        // Giữ tối đa 100 bản ghi
        if (history.size > 100) {
            history.subList(100, history.size).clear()
        }
        
        val historyJson = gson.toJson(history)
        prefs.edit().putString(KEY_STUDY_HISTORY, historyJson).apply()

        // 2. Luôn cập nhật topic completion để track learnedFlashcardIds
        updateTopicCompletion(result)
    }

    private fun updateTopicCompletion(result: StudyResult) {
        val completedTopics = getCompletedTopics().toMutableMap()
        
        val existing = completedTopics[result.topicId]
        
        // learnedFlashcardIds đã được cập nhật qua markFlashcardAsLearned() trong session
        // Chỉ cần lấy giá trị hiện tại (null-safe cho data cũ)
        val currentLearnedIds = existing?.learnedFlashcardIds ?: emptySet()
        
        // CHỈ đánh dấu hoàn thành khi:
        // 1. Độ chính xác >= 60% HOẶC
        // 2. Số flashcards unique đã học >= tổng số flashcards trong topic
        // => Chỉ cần 1 trong 2 điều kiện là đủ để hoàn thành
        val shouldMarkComplete = (result.accuracy >= MIN_ACCURACY_TO_COMPLETE || 
                                  currentLearnedIds.size >= result.topicTotalWords) &&
                                 result.topicTotalWords > 0
        
        android.util.Log.d("UserProgressManager", 
            "updateTopicCompletion: topicId=${result.topicId}, " +
            "learned=${currentLearnedIds.size}, total=${result.topicTotalWords}, " +
            "accuracy=${result.accuracy}%, shouldComplete=$shouldMarkComplete"
        )
        
        val updated = if (existing != null) {
            existing.copy(
                isCompleted = shouldMarkComplete || existing.isCompleted, // Giữ trạng thái completed nếu đã hoàn thành
                lastStudyDate = result.completedDate,
                studyCount = existing.studyCount + 1,
                totalConversationsCompleted = if (result.studyType == "conversation") 
                    existing.totalConversationsCompleted + result.totalItems 
                else existing.totalConversationsCompleted,
                bestAccuracy = maxOf(existing.bestAccuracy, result.accuracy),
                totalTimeSpent = existing.totalTimeSpent + result.timeSpent,
                learnedFlashcardIds = currentLearnedIds // Giữ nguyên (không reset)
            )
        } else {
            TopicCompletionStatus(
                topicId = result.topicId,
                isCompleted = shouldMarkComplete,
                lastStudyDate = result.completedDate,
                studyCount = 1,
                totalConversationsCompleted = if (result.studyType == "conversation") result.totalItems else 0,
                bestAccuracy = result.accuracy,
                totalTimeSpent = result.timeSpent,
                learnedFlashcardIds = currentLearnedIds
            )
        }
        
        completedTopics[result.topicId] = updated
        
        val json = gson.toJson(completedTopics)
        prefs.edit().putString(KEY_COMPLETED_TOPICS, json).apply()
    }

    fun getStudyHistory(): List<StudyResult> {
        android.util.Log.d("UserProgressManager", "getStudyHistory: START")
        val json = prefs.getString(KEY_STUDY_HISTORY, null)
        android.util.Log.d("UserProgressManager", "getStudyHistory: json is null? ${json == null}, length=${json?.length ?: 0}")
        
        if (json == null) {
            android.util.Log.d("UserProgressManager", "getStudyHistory: Returning empty list (no data)")
            return emptyList()
        }
        
        return try {
            val type = object : TypeToken<List<StudyResult>>() {}.type
            val result = gson.fromJson<List<StudyResult>>(json, type) ?: emptyList()
            android.util.Log.d("UserProgressManager", "getStudyHistory: SUCCESS, size=${result.size}")
            result
        } catch (e: Exception) {
            android.util.Log.e("UserProgressManager", "getStudyHistory: ERROR parsing JSON", e)
            emptyList()
        }
    }

    fun getCompletedTopics(): Map<String, TopicCompletionStatus> {
        android.util.Log.d("UserProgressManager", "getCompletedTopics: START")
        val json = prefs.getString(KEY_COMPLETED_TOPICS, null)
        android.util.Log.d("UserProgressManager", "getCompletedTopics: json is null? ${json == null}, length=${json?.length ?: 0}")
        
        if (json == null) {
            android.util.Log.d("UserProgressManager", "getCompletedTopics: Returning empty map (no data)")
            return emptyMap()
        }
        
        return try {
            val type = object : TypeToken<Map<String, TopicCompletionStatus>>() {}.type
            val result = gson.fromJson<Map<String, TopicCompletionStatus>>(json, type) ?: emptyMap()
            android.util.Log.d("UserProgressManager", "getCompletedTopics: SUCCESS, size=${result.size}")
            result
        } catch (e: Exception) {
            android.util.Log.e("UserProgressManager", "getCompletedTopics: ERROR parsing JSON", e)
            emptyMap()
        }
    }

    fun isTopicCompleted(topicId: String): Boolean {
        return getCompletedTopics()[topicId]?.isCompleted == true
    }

    fun getTopicCompletion(topicId: String): TopicCompletionStatus? {
        return getCompletedTopics()[topicId]
    }

    fun clearAllProgress() {
        prefs.edit().clear().apply()
    }

    fun getTotalCompletedTopics(): Int {
        return getCompletedTopics().count { it.value.isCompleted }
    }

    fun getTotalStudyTime(): Long {
        return getCompletedTopics().values.sumOf { it.totalTimeSpent }
    }

    fun getAverageAccuracy(): Float {
        val completed = getCompletedTopics().values.filter { it.isCompleted }
        if (completed.isEmpty()) return 0f
        return completed.map { it.bestAccuracy }.average().toFloat()
    }

    /**
     * Lấy tổng số từ unique đã học (không trùng lặp)
     * Gộp tất cả learnedFlashcardIds từ mọi topic thành một Set duy nhất
     */
    fun getTotalUniqueWordsLearned(): Int {
        return try {
            val allLearnedFlashcards = mutableSetOf<String>()
            getCompletedTopics().values.forEach { status ->
                // Null-safe: nếu learnedFlashcardIds null (data cũ), skip
                status.learnedFlashcardIds?.let { 
                    allLearnedFlashcards.addAll(it)
                }
            }
            android.util.Log.d("UserProgressManager", "getTotalUniqueWordsLearned: ${allLearnedFlashcards.size} words")
            allLearnedFlashcards.size
        } catch (e: Exception) {
            android.util.Log.e("UserProgressManager", "getTotalUniqueWordsLearned: ERROR", e)
            0
        }
    }

    /**
     * Lưu flashcard đã học - CHỈ dùng để track progress trong 1 lần học
     * KHÔNG dùng để quyết định unlock topic (dùng isTopicCompleted thay vì)
     */
    fun markFlashcardAsLearned(topicId: String, flashcardId: String) {
        val completedTopics = getCompletedTopics().toMutableMap()
        val existing = completedTopics[topicId]
        
        val updated = if (existing != null) {
            val newLearnedIds = (existing.learnedFlashcardIds ?: emptySet()) + flashcardId
            android.util.Log.d("UserProgressManager", 
                "markFlashcardAsLearned: topicId=$topicId, flashcardId=$flashcardId, " +
                "learnedCount=${newLearnedIds.size}, isCompleted=${existing.isCompleted}"
            )
            existing.copy(
                learnedFlashcardIds = newLearnedIds,
                lastStudyDate = System.currentTimeMillis()
            )
        } else {
            android.util.Log.d("UserProgressManager", 
                "markFlashcardAsLearned: NEW topic $topicId, flashcardId=$flashcardId"
            )
            TopicCompletionStatus(
                topicId = topicId,
                isCompleted = false,
                lastStudyDate = System.currentTimeMillis(),
                learnedFlashcardIds = setOf(flashcardId),
                studyCount = 0
            )
        }
        
        completedTopics[topicId] = updated
        val json = gson.toJson(completedTopics)
        prefs.edit().putString(KEY_COMPLETED_TOPICS, json).apply()
    }

    /**
     * Tính phần trăm hoàn thành flashcard của topic trong lần học hiện tại
     * KHÔNG dùng để unlock topic tiếp theo
     * @return 0-100
     */
    fun getTopicFlashcardProgress(topicId: String, totalFlashcards: Int): Float {
        if (totalFlashcards == 0) return 0f
        val learned = getCompletedTopics()[topicId]?.learnedFlashcardIds?.size ?: 0
        return (learned.toFloat() / totalFlashcards.toFloat()) * 100f
    }

    /**
     * Kiểm tra unlock topic tiếp theo - CHỈ dựa vào isTopicCompleted
     * Topic chỉ completed khi làm XỐI 1 lần toàn bộ flashcards với accuracy >= 60%
     */
    fun hasReachedUnlockThreshold(topicId: String): Boolean {
        return isTopicCompleted(topicId)
    }
    
    /**
     * KHÔNG CÒN DÙNG - learnedFlashcardIds giờ track tất cả flashcards đã học, không reset khi học lại
     * Giữ hàm này để tránh lỗi nếu có code cũ gọi
     */
    @Deprecated("Không còn reset learnedFlashcardIds - nó giờ track unique flashcards đã học qua các lần")
    fun resetFlashcardProgress(topicId: String) {
        // Không làm gì cả - learnedFlashcardIds giờ là persistent tracking
    }
}
