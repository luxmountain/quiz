# Hệ thống Kết Quả và Lưu Trạng Thái Học Tập (Local Storage)

## Tổng quan

Hệ thống lưu trữ kết quả học tập và trạng thái hoàn thành của topic **chỉ trên máy user** sử dụng SharedPreferences thông qua `UserProgressManager`.

## Cấu trúc

### 1. UserProgressManager
**Path**: `app/src/main/java/com/uilover/project247/data/repository/UserProgressManager.kt`

Quản lý toàn bộ tiến độ học tập local:
- Lưu kết quả học tập (StudyResult)
- Theo dõi topic đã hoàn thành (TopicCompletionStatus)
- Tính toán thống kê (tổng thời gian, độ chính xác trung bình, v.v.)

**Data Models**:
```kotlin
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
    val totalTimeSpent: Long = 0
)
```

**API Methods**:
```kotlin
// Lưu kết quả học
fun saveStudyResult(result: StudyResult)

// Kiểm tra topic đã hoàn thành chưa
fun isTopicCompleted(topicId: String): Boolean

// Lấy trạng thái hoàn thành của topic
fun getTopicCompletion(topicId: String): TopicCompletionStatus?

// Lấy lịch sử học tập
fun getStudyHistory(): List<StudyResult>

// Lấy tất cả topic đã hoàn thành
fun getCompletedTopics(): Map<String, TopicCompletionStatus>

// Xóa toàn bộ tiến độ
fun clearAllProgress()

// Thống kê
fun getTotalCompletedTopics(): Int
fun getTotalStudyTime(): Long
fun getAverageAccuracy(): Float
```

### 2. ResultActivity
**Path**: `app/src/main/java/com/uilover/project247/ResultActivity/`

Màn hình hiển thị kết quả học tập sau khi hoàn thành flashcard hoặc conversation.

**Features**:
- ✨ Animation hiệu ứng khi hiển thị
- 📊 Hiển thị độ chính xác, số câu đúng/sai
- ⏱️ Hiển thị thời gian học
- 💾 Tự động lưu kết quả vào local storage
- 🎉 Thông báo động viên dựa trên kết quả

**Intent Parameters**:
```kotlin
intent.putExtra("STUDY_TYPE", "flashcard") // hoặc "conversation"
intent.putExtra("TOPIC_ID", topicId)
intent.putExtra("TOPIC_NAME", topicName)
intent.putExtra("TOTAL_ITEMS", 10)
intent.putExtra("CORRECT_COUNT", 8)
intent.putExtra("TIME_SPENT", 120000L) // milliseconds
```

### 3. Dashboard Integration

Topic đã hoàn thành sẽ hiển thị:
- ✅ Icon check màu trắng trong vòng tròn xanh lá
- 🟢 Background màu xanh nhạt (Light Green)

## Cách Sử dụng

### 1. Từ LearningActivity

Khi user hoàn thành tất cả flashcards:

```kotlin
// Trong LearningViewModel hoặc LearningScreen
fun onFinishLearning() {
    val intent = Intent(context, ResultActivity::class.java).apply {
        putExtra("STUDY_TYPE", "flashcard")
        putExtra("TOPIC_ID", topicId)
        putExtra("TOPIC_NAME", topicName)
        putExtra("TOTAL_ITEMS", totalFlashcards)
        putExtra("CORRECT_COUNT", correctAnswers)
        putExtra("TIME_SPENT", elapsedTime) // milliseconds
    }
    context.startActivity(intent)
    finish() // Đóng LearningActivity
}
```

**Ví dụ tính toán kết quả**:
```kotlin
// Trong ViewModel
private var startTime = System.currentTimeMillis()
private var correctCount = 0
private var totalItems = flashcards.size

fun checkAnswer(isCorrect: Boolean) {
    if (isCorrect) correctCount++
    
    // Khi hết flashcards
    if (currentIndex >= totalItems - 1) {
        val timeSpent = System.currentTimeMillis() - startTime
        navigateToResult(totalItems, correctCount, timeSpent)
    }
}
```

### 2. Từ ConversationActivity

Khi user hoàn thành conversation:

```kotlin
fun onFinishConversation() {
    val intent = Intent(context, ResultActivity::class.java).apply {
        putExtra("STUDY_TYPE", "conversation")
        putExtra("TOPIC_ID", conversationId) // hoặc topicId
        putExtra("TOPIC_NAME", conversationTitle)
        putExtra("TOTAL_ITEMS", totalQuestions)
        putExtra("CORRECT_COUNT", correctAnswers)
        putExtra("TIME_SPENT", elapsedTime)
    }
    context.startActivity(intent)
    finish()
}
```

### 3. Dashboard tự động cập nhật

Dashboard đã được tích hợp để hiển thị topic đã học:

```kotlin
// MainViewModel tự động kiểm tra
fun isTopicCompleted(topicId: String): Boolean {
    return progressManager.isTopicCompleted(topicId)
}

// MainScreen sử dụng
TopicItem(
    topic = topic,
    isCompleted = viewModel.isTopicCompleted(topic.id),
    onClick = { ... }
)
```

## Điều kiện hoàn thành

Topic được đánh dấu hoàn thành khi:
- **Độ chính xác >= 60%** (MIN_ACCURACY_TO_COMPLETE)
- Kết quả được lưu tự động trong ResultScreen

```kotlin
// UserProgressManager.kt
companion object {
    private const val MIN_ACCURACY_TO_COMPLETE = 60f
}
```

## Dữ liệu lưu trữ

Dữ liệu được lưu trong SharedPreferences:
- **File**: `user_progress` (MODE_PRIVATE)
- **Keys**:
  - `completed_topics`: Map<String, TopicCompletionStatus>
  - `study_history`: List<StudyResult> (tối đa 100 records)

**Format**: JSON sử dụng Gson

## Ví dụ hoàn chỉnh

### LearningActivity cập nhật

```kotlin
// 1. Thêm biến tracking trong ViewModel
private var sessionStartTime = 0L
private var correctAnswers = 0
private val totalFlashcards get() = flashcards.size

// 2. Bắt đầu session
init {
    sessionStartTime = System.currentTimeMillis()
}

// 3. Track correct answers
fun submitAnswer(userAnswer: String) {
    val isCorrect = checkAnswer(userAnswer)
    if (isCorrect) correctAnswers++
    
    // Move to next or finish
    if (currentCardIndex < totalFlashcards - 1) {
        nextCard()
    } else {
        finishSession()
    }
}

// 4. Navigate to Result
private fun finishSession() {
    val timeSpent = System.currentTimeMillis() - sessionStartTime
    _uiState.update {
        it.copy(
            shouldNavigateToResult = true,
            sessionStats = SessionStats(
                totalItems = totalFlashcards,
                correctCount = correctAnswers,
                timeSpent = timeSpent
            )
        )
    }
}

// 5. Trong Activity
LaunchedEffect(uiState.shouldNavigateToResult) {
    if (uiState.shouldNavigateToResult) {
        val stats = uiState.sessionStats
        val intent = Intent(this@LearningActivity, ResultActivity::class.java).apply {
            putExtra("STUDY_TYPE", "flashcard")
            putExtra("TOPIC_ID", topicId)
            putExtra("TOPIC_NAME", topicName)
            putExtra("TOTAL_ITEMS", stats.totalItems)
            putExtra("CORRECT_COUNT", stats.correctCount)
            putExtra("TIME_SPENT", stats.timeSpent)
        }
        startActivity(intent)
        finish()
    }
}
```

### ConversationActivity cập nhật

```kotlin
// Trong ConversationDetailViewModel
private var sessionStart = 0L
private var correctAnswers = 0
private val totalQuestions get() = conversation.dialogue.filter { it.options.isNotEmpty() }.size

fun answerQuestion(dialogueIndex: Int, selectedOptionId: String) {
    val dialogue = conversation.dialogue[dialogueIndex]
    val isCorrect = dialogue.options.find { it.id == selectedOptionId }?.isCorrect == true
    
    if (isCorrect) correctAnswers++
    
    // Check if finished
    if (isLastQuestion()) {
        finishConversation()
    }
}

private fun finishConversation() {
    val timeSpent = System.currentTimeMillis() - sessionStart
    _uiState.update {
        it.copy(
            shouldShowResult = true,
            resultData = ResultData(
                conversationId = conversation.id,
                conversationTitle = conversation.titleVi,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                timeSpent = timeSpent
            )
        )
    }
}
```

## API Thống kê

Sử dụng UserProgressManager để hiển thị thống kê:

```kotlin
val progressManager = UserProgressManager(context)

// Tổng topic đã hoàn thành
val totalCompleted = progressManager.getTotalCompletedTopics()

// Tổng thời gian học
val totalTime = progressManager.getTotalStudyTime()
val hours = TimeUnit.MILLISECONDS.toHours(totalTime)

// Độ chính xác trung bình
val avgAccuracy = progressManager.getAverageAccuracy()

// Lịch sử học gần đây
val recentStudies = progressManager.getStudyHistory().take(10)
```

## Lưu ý

1. **Không cần Firebase Auth**: Dữ liệu lưu local không cần userId
2. **Dữ liệu bị mất khi**: 
   - Xóa app data
   - Gọi `clearAllProgress()`
   - Uninstall app
3. **Performance**: SharedPreferences nhanh cho dữ liệu nhỏ (<100 topics)
4. **Thread-safe**: Tất cả operations đều main-thread safe
5. **Backup**: Người dùng có thể export/import nếu cần (TODO feature)

## Màu sắc sử dụng

```kotlin
// Topic đã hoàn thành
val completedTopicBackground = Color(0xFFE8F5E9) // Light Green
val completedIconBackground = Color(0xFF4CAF50) // Green
val completedIconColor = Color.White

// Kết quả
val perfectColor = Color(0xFF4CAF50) // Green - 100%
val excellentColor = Color(0xFF2196F3) // Blue - >= 80%
val goodColor = Color(0xFFFF9800) // Orange - >= 60%
val tryAgainColor = Color(0xFFF44336) // Red - < 60%
```

## Manifest

Đã thêm vào AndroidManifest.xml:
```xml
<activity
    android:name=".ResultActivity.ResultActivity"
    android:exported="false" />
```

## Dependencies

Chỉ cần:
```kotlin
implementation("com.google.code.gson:gson:2.10.1") // Đã có trong project
```

## Testing

Test UserProgressManager:
```kotlin
@Test
fun testSaveAndRetrieveProgress() {
    val manager = UserProgressManager(context)
    
    val result = StudyResult(
        topicId = "test_001",
        topicName = "Test Topic",
        studyType = "flashcard",
        totalItems = 10,
        correctCount = 8,
        timeSpent = 60000,
        accuracy = 80f,
        completedDate = System.currentTimeMillis()
    )
    
    manager.saveStudyResult(result)
    
    assertTrue(manager.isTopicCompleted("test_001"))
    assertEquals(80f, manager.getTopicCompletion("test_001")?.bestAccuracy)
}
```
