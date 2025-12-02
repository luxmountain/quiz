package com.uilover.project247.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class StudyResult(
    val topicId: String,
    val topicName: String,
    val studyType: String, // "flashcard" hoặc "conversation"
    val totalItems: Int,
    val correctCount: Int,
    val timeSpent: Long,
    val accuracy: Float,
    val completedDate: Long
)

data class TopicCompletionStatus(
    val topicId: String,
    val isCompleted: Boolean,
    val lastStudyDate: Long,
    val totalFlashcardsLearned: Int = 0,
    val totalConversationsCompleted: Int = 0,
    val bestAccuracy: Float = 0f,
    val totalTimeSpent: Long = 0,
    val learnedFlashcardIds: Set<String> = emptySet() // Danh sách flashcard đã học
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
        private const val MIN_ACCURACY_TO_COMPLETE = 60f
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

        // 2. Cập nhật trạng thái hoàn thành topic (chỉ khi đạt độ chính xác tối thiểu)
        // Topic chỉ được đánh dấu hoàn thành nếu làm đủ số lượng items trong 1 lần
        if (result.accuracy >= MIN_ACCURACY_TO_COMPLETE) {
            updateTopicCompletion(result)
        }
    }

    private fun updateTopicCompletion(result: StudyResult) {
        val completedTopics = getCompletedTopics().toMutableMap()
        
        val existing = completedTopics[result.topicId]
        val updated = if (existing != null) {
            existing.copy(
                isCompleted = true,
                lastStudyDate = result.completedDate,
                totalFlashcardsLearned = if (result.studyType == "flashcard") 
                    existing.totalFlashcardsLearned + result.totalItems 
                else existing.totalFlashcardsLearned,
                totalConversationsCompleted = if (result.studyType == "conversation") 
                    existing.totalConversationsCompleted + result.totalItems 
                else existing.totalConversationsCompleted,
                bestAccuracy = maxOf(existing.bestAccuracy, result.accuracy),
                totalTimeSpent = existing.totalTimeSpent + result.timeSpent,
                // Không update learnedFlashcardIds ở đây vì chỉ dùng để track từng flashcard riêng lẻ
                learnedFlashcardIds = existing.learnedFlashcardIds
            )
        } else {
            TopicCompletionStatus(
                topicId = result.topicId,
                isCompleted = true,
                lastStudyDate = result.completedDate,
                totalFlashcardsLearned = if (result.studyType == "flashcard") result.totalItems else 0,
                totalConversationsCompleted = if (result.studyType == "conversation") result.totalItems else 0,
                bestAccuracy = result.accuracy,
                totalTimeSpent = result.timeSpent,
                learnedFlashcardIds = emptySet() // Không set flashcard IDs ở đây
            )
        }
        
        completedTopics[result.topicId] = updated
        
        val json = gson.toJson(completedTopics)
        prefs.edit().putString(KEY_COMPLETED_TOPICS, json).apply()
    }

    fun getStudyHistory(): List<StudyResult> {
        val json = prefs.getString(KEY_STUDY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<StudyResult>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getCompletedTopics(): Map<String, TopicCompletionStatus> {
        val json = prefs.getString(KEY_COMPLETED_TOPICS, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, TopicCompletionStatus>>() {}.type
        return gson.fromJson(json, type)
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
     * Lưu flashcard đã học - CHỈ dùng để track progress trong 1 lần học
     * KHÔNG dùng để quyết định unlock topic (dùng isTopicCompleted thay vì)
     */
    fun markFlashcardAsLearned(topicId: String, flashcardId: String) {
        val completedTopics = getCompletedTopics().toMutableMap()
        val existing = completedTopics[topicId]
        
        val updated = if (existing != null) {
            val newLearnedIds = existing.learnedFlashcardIds + flashcardId
            existing.copy(
                learnedFlashcardIds = newLearnedIds,
                lastStudyDate = System.currentTimeMillis()
            )
        } else {
            TopicCompletionStatus(
                topicId = topicId,
                isCompleted = false,
                lastStudyDate = System.currentTimeMillis(),
                learnedFlashcardIds = setOf(flashcardId),
                totalFlashcardsLearned = 0
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
     * Reset flashcard progress khi bắt đầu 1 session học mới
     */
    fun resetFlashcardProgress(topicId: String) {
        val completedTopics = getCompletedTopics().toMutableMap()
        val existing = completedTopics[topicId]
        
        if (existing != null) {
            val updated = existing.copy(learnedFlashcardIds = emptySet())
            completedTopics[topicId] = updated
            val json = gson.toJson(completedTopics)
            prefs.edit().putString(KEY_COMPLETED_TOPICS, json).apply()
        }
    }
}
