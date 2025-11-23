package com.uilover.project247.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uilover.project247.data.models.Level
import com.uilover.project247.data.models.Topic
import com.uilover.project247.data.models.TopicCompletion
import com.uilover.project247.data.models.Flashcard
import com.uilover.project247.data.models.Conversation
import com.uilover.project247.data.models.FirebasePaths
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository để tương tác với Firebase Realtime Database
 */
class FirebaseRepository {
    
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance("https://english-learning-app-17885-default-rtdb.asia-southeast1.firebasedatabase.app/")
    }
    
    companion object {
        private const val TAG = "FirebaseRepository"
    }
    
    // ==================== LEVELS ====================
    
    /**
     * Lấy tất cả levels
     */
    suspend fun getLevels(): List<Level> {
        return try {
            val snapshot = database.getReference(FirebasePaths.LEVELS)
                .get()
                .await()
            
            val levels = mutableListOf<Level>()
            snapshot.children.forEach { levelSnapshot ->
                try {
                    val level = levelSnapshot.getValue(Level::class.java)
                    if (level != null) {
                        levels.add(level)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing level: ${levelSnapshot.key}", e)
                }
            }
            
            levels.sortBy { it.order }
            Log.d(TAG, "Loaded ${levels.size} levels from Firebase")
            levels
        } catch (e: Exception) {
            Log.e(TAG, "Error loading levels", e)
            emptyList()
        }
    }
    
    /**
     * Lấy một level theo ID
     */
    suspend fun getLevel(levelId: String): Level? {
        return try {
            val snapshot = database.getReference(FirebasePaths.level(levelId))
                .get()
                .await()
            
            snapshot.getValue(Level::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading level $levelId", e)
            null
        }
    }
    
    // ==================== TOPICS ====================
    
    /**
     * Lấy tất cả topics dưới dạng Flow (real-time updates)
     */
    fun getTopicsFlow(): Flow<List<Topic>> = callbackFlow {
        val topicsRef = database.getReference(FirebasePaths.TOPICS)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val topics = mutableListOf<Topic>()
                
                snapshot.children.forEach { topicSnapshot ->
                    try {
                        val topic = topicSnapshot.getValue(Topic::class.java)
                        if (topic != null) {
                            topics.add(topic)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing topic: ${topicSnapshot.key}", e)
                    }
                }
                
                // Sắp xếp theo order
                topics.sortBy { it.order }
                
                trySend(topics)
                Log.d(TAG, "Loaded ${topics.size} topics from Firebase")
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading topics: ${error.message}")
                close(error.toException())
            }
        }
        
        topicsRef.addValueEventListener(listener)
        
        awaitClose {
            topicsRef.removeEventListener(listener)
        }
    }
    
    /**
     * Lấy tất cả topics một lần (không real-time)
     */
    suspend fun getTopics(): List<Topic> {
        return try {
            val snapshot = database.getReference(FirebasePaths.TOPICS)
                .get()
                .await()
            
            val topics = mutableListOf<Topic>()
            snapshot.children.forEach { topicSnapshot ->
                try {
                    val topic = topicSnapshot.getValue(Topic::class.java)
                    if (topic != null) {
                        topics.add(topic)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing topic: ${topicSnapshot.key}", e)
                }
            }
            
            topics.sortBy { it.order }
            Log.d(TAG, "Loaded ${topics.size} topics from Firebase")
            topics
        } catch (e: Exception) {
            Log.e(TAG, "Error loading topics", e)
            emptyList()
        }
    }
    
    /**
     * Lấy topics theo level ID
     */
    suspend fun getTopicsByLevel(levelId: String): List<Topic> {
        return try {
            val snapshot = database.getReference(FirebasePaths.TOPICS)
                .orderByChild("levelId")
                .equalTo(levelId)
                .get()
                .await()
            
            val topics = mutableListOf<Topic>()
            snapshot.children.forEach { topicSnapshot ->
                try {
                    val topic = topicSnapshot.getValue(Topic::class.java)
                    if (topic != null) {
                        topics.add(topic)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing topic: ${topicSnapshot.key}", e)
                }
            }
            
            topics.sortBy { it.order }
            Log.d(TAG, "Loaded ${topics.size} topics for level $levelId")
            topics
        } catch (e: Exception) {
            Log.e(TAG, "Error loading topics for level $levelId", e)
            emptyList()
        }
    }
    
    /**
     * Lấy một topic theo ID (bao gồm flashcards và conversations)
     */
    suspend fun getTopic(topicId: String): Topic? {
        return try {
            val snapshot = database.getReference(FirebasePaths.topic(topicId))
                .get()
                .await()
            
            snapshot.getValue(Topic::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading topic $topicId", e)
            null
        }
    }
    
    // ==================== USER PROGRESS ====================
    
    /**
     * Đánh dấu topic đã hoàn thành
     */
    suspend fun markTopicCompleted(
        userId: String,
        topicId: String,
        totalFlashcardsLearned: Int,
        totalConversationsCompleted: Int,
        totalTimeSpent: Long,
        accuracy: Float
    ): Boolean {
        return try {
            val completion = TopicCompletion(
                topicId = topicId,
                completed = true,
                completionDate = System.currentTimeMillis(),
                totalFlashcardsLearned = totalFlashcardsLearned,
                totalConversationsCompleted = totalConversationsCompleted,
                totalTimeSpent = totalTimeSpent,
                accuracy = accuracy
            )
            
            database.getReference(FirebasePaths.userCompletedTopics(userId))
                .child(topicId)
                .setValue(completion)
                .await()
            
            Log.d(TAG, "Topic $topicId marked as completed for user $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking topic completed", e)
            false
        }
    }
    
    /**
     * Lấy danh sách topics đã hoàn thành của user
     */
    suspend fun getCompletedTopics(userId: String): Map<String, TopicCompletion> {
        return try {
            val snapshot = database.getReference(FirebasePaths.userCompletedTopics(userId))
                .get()
                .await()
            
            val completedTopics = mutableMapOf<String, TopicCompletion>()
            snapshot.children.forEach { completionSnapshot ->
                try {
                    val completion = completionSnapshot.getValue(TopicCompletion::class.java)
                    if (completion != null) {
                        completedTopics[completion.topicId] = completion
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing completion: ${completionSnapshot.key}", e)
                }
            }
            
            Log.d(TAG, "Loaded ${completedTopics.size} completed topics for user $userId")
            completedTopics
        } catch (e: Exception) {
            Log.e(TAG, "Error loading completed topics", e)
            emptyMap()
        }
    }
    
    // ==================== FLASHCARDS ====================
    
    // Flashcards giờ nằm trong Topic.flashcards
    
    // ==================== CONVERSATIONS ====================
    
    /**
     * Lấy tất cả conversations từ tất cả topics
     */
    suspend fun getAllConversations(): List<Conversation> {
        return try {
            val topics = getTopics()
            val conversations = mutableListOf<Conversation>()
            topics.forEach { topic ->
                conversations.addAll(topic.conversations)
            }
            conversations.sortBy { it.order }
            Log.d(TAG, "Loaded ${conversations.size} conversations from all topics")
            conversations
        } catch (e: Exception) {
            Log.e(TAG, "Error loading all conversations", e)
            emptyList()
        }
    }
    
    /**
     * Lấy một conversation theo ID (tìm trong tất cả topics)
     */
    suspend fun getConversation(conversationId: String): Conversation? {
        return try {
            val topics = getTopics()
            topics.forEach { topic ->
                topic.conversations.forEach { conversation ->
                    if (conversation.id == conversationId) {
                        return conversation
                    }
                }
            }
            Log.w(TAG, "Conversation $conversationId not found")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading conversation $conversationId", e)
            null
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Kiểm tra kết nối Firebase
     */
    fun testConnection(onResult: (Boolean) -> Unit) {
        database.getReference(".info/connected")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    Log.d(TAG, "Firebase connected: $connected")
                    onResult(connected)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Connection test failed: ${error.message}")
                    onResult(false)
                }
            })
    }
}
