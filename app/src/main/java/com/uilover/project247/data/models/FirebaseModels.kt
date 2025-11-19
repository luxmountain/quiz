package com.uilover.project247.data.models

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.parcelize.Parcelize
/**
 * Firebase Realtime Database Models
 * Định nghĩa schema cho toàn bộ dữ liệu trong Firebase
 */

// ==================== TOPIC MODELS ====================

@Parcelize
data class Topic(
    val id: String = "",
    val name: String = "",
    val nameVi: String = "",
    val description: String = "",
    val descriptionVi: String = "",
    val imageUrl: String = "",
    val order: Int = 0,
    val totalWords: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) : Parcelable

// ==================== FLASHCARD MODELS ====================

@Parcelize
data class Flashcard(
    val id: String = "",
    val topicId: String = "",
    val word: String = "",
    val pronunciation: String = "",
    val meaning: String = "",
    val wordType: String = "", // "noun", "verb", "adjective"
    val wordTypeVi: String = "", // "danh từ", "động từ", "tính từ"
    val imageUrl: String = "",
    val contextSentence: String = "",
    val contextSentenceVi: String = "",
    val example: String = "",
    val exampleVi: String = "",
    val order: Int = 0,
    val difficulty: String = "easy", // "easy", "medium", "hard"
    val createdAt: Long = 0
) : Parcelable {

    enum class WordType(val english: String, val vietnamese: String) {
        NOUN("noun", "danh từ"),
        VERB("verb", "động từ"),
        ADJECTIVE("adjective", "tính từ"),
        ADVERB("adverb", "trạng từ"),
        PREPOSITION("preposition", "giới từ"),
        CONJUNCTION("conjunction", "liên từ");

        companion object {
            fun fromString(value: String): WordType {
                return values().find { it.english == value } ?: NOUN
            }
        }
    }

    enum class Difficulty {
        EASY, MEDIUM, HARD;

        companion object {
            fun fromString(value: String): Difficulty {
                return try {
                    valueOf(value.uppercase())
                } catch (e: Exception) {
                    EASY
                }
            }
        }
    }
}

@Parcelize
data class VocabularyWordInfo(
    val word: String = "",
    val meaning: String = "",
    val pronunciation: String = "",
    val wordType: String = "",
    val wordTypeVi: String = ""
): Parcelable

// ==================== CONVERSATION MODELS ====================

@Parcelize
data class Conversation(
    val id: String = "",
    val title: String = "",
    val titleVi: String = "",
    val imageUrl: String = "",
    val contextDescription: String = "",
    val contextDescriptionVi: String = "",
    val dialogue: List<DialogueLine> = emptyList(), // Danh sách các câu thoại (đã chứa quiz)
    val vocabularyWords: List<VocabularyWordInfo> = emptyList(), // Danh sách từ vựng
    val order: Int = 0,
    val createdAt: Long = 0
) : Parcelable {
}

@Parcelize
data class DialogueLine(
    val speaker: String = "",
    val text: String = "",
    val textVi: String = "", // Giữ lại textVi để hỗ trợ nút Dịch
    val order: Int = 0,
    val vocabularyWord: String = "", // Từ vựng mục tiêu của câu này
    val question: String = "",
    val questionVi: String = "",
    val options: List<QuizOption> = emptyList()
) : Parcelable

@Parcelize
data class QuizOption(
    val id: String = "",
    val text: String = "",
    @get:PropertyName("isCorrect")
    val isCorrect: Boolean = false
) : Parcelable

// ==================== USER PROGRESS MODELS ====================

@Parcelize
data class UserProgress(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val totalPoints: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastStudyDate: Long? = null,
    val createdAt: Long = 0,
    val topicProgress: Map<String, TopicProgress> = emptyMap(),
    val flashcardResults: Map<String, FlashcardResult> = emptyMap(),
    val conversationResults: Map<String, ConversationResult> = emptyMap()
) : Parcelable

@Parcelize
data class TopicProgress(
    val topicId: String = "",
    val completedFlashcards: Int = 0,
    val completedConversations: Int = 0,
    val totalFlashcards: Int = 0,
    val totalConversations: Int = 0,
    val progress: Float = 0f,
    val lastStudyDate: Long? = null
) : Parcelable {

    /**
     * Tính phần trăm hoàn thành
     */
    fun getProgressPercentage(): Int {
        val total = totalFlashcards + totalConversations
        if (total == 0) return 0
        val completed = completedFlashcards + completedConversations
        return ((completed.toFloat() / total.toFloat()) * 100).toInt()
    }
}

/**
 * Trạng thái của flashcard trong hệ thống Anki SRS
 */
enum class CardState {
    NEW,        // Chưa học lần nào
    LEARNING,   // Đang trong giai đoạn học (< 24h)
    REVIEW,     // Đang ôn tập định kỳ
    RELEARNING  // Học lại sau khi quên
}

/**
 * Mức độ nhớ khi review flashcard
 */
enum class ReviewQuality {
    AGAIN,  // 0 - Quên hoàn toàn, cần học lại
    HARD,   // 1 - Nhớ khó khăn
    GOOD,   // 2 - Nhớ được bình thường
    EASY    // 3 - Nhớ rất dễ dàng
}

@Parcelize
data class FlashcardResult(
    val flashcardId: String = "",
    val learned: Boolean = false,
    
    // Anki SRS Algorithm fields
    val state: String = CardState.NEW.name,  // NEW, LEARNING, REVIEW, RELEARNING
    val easeFactor: Float = 2.5f,            // 1.3 - 2.5+ (độ dễ nhớ)
    val intervalDays: Float = 0f,            // Khoảng cách ngày cho lần review tiếp (hỗ trợ < 1 ngày)
    val currentStep: Int = 0,                // Bước hiện tại trong learning steps (0-based)
    val lapses: Int = 0,                     // Số lần quên (để giảm ease factor)
    
    val reviewCount: Int = 0,
    val lastReviewDate: Long? = null,
    val nextReviewDate: Long? = null,
    val confidence: Int = 0 // 0-100 (deprecated, dùng easeFactor thay thế)
) : Parcelable {
    
    /**
     * Get CardState enum from string
     */
    fun getCardState(): CardState {
        return try {
            CardState.valueOf(state)
        } catch (e: Exception) {
            CardState.NEW
        }
    }
    
    /**
     * Check if card is due for review
     */
    fun isDue(currentTime: Long = System.currentTimeMillis()): Boolean {
        return nextReviewDate?.let { it <= currentTime } ?: true
    }
    
    /**
     * Check if card is new (never studied)
     */
    fun isNew(): Boolean = getCardState() == CardState.NEW
    
    /**
     * Check if card is in learning phase
     */
    fun isLearning(): Boolean {
        val cardState = getCardState()
        return cardState == CardState.LEARNING || cardState == CardState.RELEARNING
    }
}

@Parcelize
data class ConversationResult(
    val conversationId: String = "",
    val completed: Boolean = false,
    val attempts: Int = 0,
    val correctAnswers: Int = 0,
    val lastAttemptDate: Long? = null
) : Parcelable {

    /**
     * Tính tỷ lệ trả lời đúng
     */
    fun getAccuracyPercentage(): Int {
        if (attempts == 0) return 0
        return ((correctAnswers.toFloat() / attempts.toFloat()) * 100).toInt()
    }
}

// ==================== APP SETTINGS MODELS ====================

@Parcelize
data class AppSettings(
    val version: String = "1.0.0",
    val minSupportedVersion: String = "1.0.0",
    val maintenanceMode: Boolean = false,
    val dailyGoal: Int = 10,
    val reminderEnabled: Boolean = true,
    val reminderTime: String = "20:00",
    val soundEnabled: Boolean = true,
    val autoPlayAudio: Boolean = true
) : Parcelable

// ==================== ROOT DATABASE STRUCTURE ====================

/**
 * Root structure của Firebase Realtime Database
 */
data class FirebaseDatabase(
    val topics: Map<String, Topic> = emptyMap(),
    val flashcards: Map<String, Flashcard> = emptyMap(),
    val conversations: Map<String, Conversation> = emptyMap(),
    val userProgress: Map<String, UserProgress> = emptyMap(),
    val settings: Settings = Settings()
)

data class Settings(
    val app: AppSettings = AppSettings()
)

// ==================== FIREBASE PATHS CONSTANTS ====================

/**
 * Constants định nghĩa các path trong Firebase Realtime Database
 */
object FirebasePaths {
    const val TOPICS = "topics"
    const val FLASHCARDS = "flashcards"
    const val CONVERSATIONS = "conversations"
    const val USER_PROGRESS = "userProgress"
    const val SETTINGS = "settings"
    const val APP_SETTINGS = "settings/app"

    // User specific paths
    fun userProgress(userId: String) = "$USER_PROGRESS/$userId"
    fun userTopicProgress(userId: String) = "${userProgress(userId)}/topicProgress"
    fun userFlashcardResults(userId: String) = "${userProgress(userId)}/flashcardResults"
    fun userConversationResults(userId: String) = "${userProgress(userId)}/conversationResults"

    // Topic specific paths
    fun topic(topicId: String) = "$TOPICS/$topicId"
    fun flashcardsForTopic(topicId: String) = FLASHCARDS // Query with .orderByChild("topicId").equalTo(topicId)
    fun conversationsForTopic(topicId: String) = CONVERSATIONS // Query with .orderByChild("topicId").equalTo(topicId)

    // Specific item paths
    fun flashcard(flashcardId: String) = "$FLASHCARDS/$flashcardId"
    fun conversation(conversationId: String) = "$CONVERSATIONS/$conversationId"
}
