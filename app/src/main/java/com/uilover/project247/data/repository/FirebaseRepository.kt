package com.uilover.project247.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uilover.project247.data.models.Topic
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
     * Lấy một topic theo ID
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
    
    // ==================== FLASHCARDS ====================
    
    /**
     * Lấy flashcards theo topic ID
     */
    suspend fun getFlashcardsByTopic(topicId: String): List<Flashcard> {
        return try {
            val snapshot = database.getReference(FirebasePaths.FLASHCARDS)
                .orderByChild("topicId")
                .equalTo(topicId)
                .get()
                .await()
            
            val flashcards = mutableListOf<Flashcard>()
            snapshot.children.forEach { flashcardSnapshot ->
                try {
                    val flashcard = flashcardSnapshot.getValue(Flashcard::class.java)
                    if (flashcard != null) {
                        flashcards.add(flashcard)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing flashcard: ${flashcardSnapshot.key}", e)
                }
            }
            
            flashcards.sortBy { it.order }
            Log.d(TAG, "Loaded ${flashcards.size} flashcards for topic $topicId")
            flashcards
        } catch (e: Exception) {
            Log.e(TAG, "Error loading flashcards for topic $topicId", e)
            emptyList()
        }
    }
    
    /**
     * Lấy một flashcard theo ID
     */
    suspend fun getFlashcard(flashcardId: String): Flashcard? {
        return try {
            val snapshot = database.getReference(FirebasePaths.flashcard(flashcardId))
                .get()
                .await()
            
            snapshot.getValue(Flashcard::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading flashcard $flashcardId", e)
            null
        }
    }
    
    // ==================== CONVERSATIONS ====================
    
    /**
     * Lấy tất cả conversations (không real-time)
     */
    suspend fun getAllConversations(): List<Conversation> {
        return try {
            val snapshot = database.getReference(FirebasePaths.CONVERSATIONS)
                .get()
                .await()
            val conversations = mutableListOf<Conversation>()
            snapshot.children.forEach { conversationSnapshot ->
                try {
                    val conversation = conversationSnapshot.getValue(Conversation::class.java)
                    if (conversation != null) {
                        conversations.add(conversation)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing conversation: ${conversationSnapshot.key}", e)
                }
            }
            conversations.sortBy { it.order }
            Log.d(TAG, "Loaded ${conversations.size} conversations from Firebase")
            conversations
        } catch (e: Exception) {
            Log.e(TAG, "Error loading all conversations", e)
            emptyList()
        }
    }

    /**
     * Lấy conversations theo topic ID
     */
    suspend fun getConversationsByTopic(topicId: String): List<Conversation> {
        return try {
            val snapshot = database.getReference(FirebasePaths.CONVERSATIONS)
                .orderByChild("topicId")
                .equalTo(topicId)
                .get()
                .await()
            
            val conversations = mutableListOf<Conversation>()
            snapshot.children.forEach { conversationSnapshot ->
                try {
                    val conversation = conversationSnapshot.getValue(Conversation::class.java)
                    if (conversation != null) {
                        conversations.add(conversation)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing conversation: ${conversationSnapshot.key}", e)
                }
            }
            
            conversations.sortBy { it.order }
            Log.d(TAG, "Loaded ${conversations.size} conversations for topic $topicId")
            conversations
        } catch (e: Exception) {
            Log.e(TAG, "Error loading conversations for topic $topicId", e)
            emptyList()
        }
    }
    
    /**
     * Lấy một conversation theo ID
     */
    suspend fun getConversation(conversationId: String): Conversation? {
        return try {
            val snapshot = database.getReference(FirebasePaths.conversation(conversationId))
                .get()
                .await()
            
            snapshot.getValue(Conversation::class.java)
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
