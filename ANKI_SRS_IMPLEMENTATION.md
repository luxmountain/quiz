# Anki Space Repetition System (SRS) Implementation

## 📚 Tổng quan

Hệ thống ôn tập lặp lại ngắt quãng (Spaced Repetition) dựa trên thuật toán **Anki (Modified SM-2)** - tương tự app Mochi.

## 🎯 Các thành phần đã implement

### 1. Data Models

#### `CardState` enum
```kotlin
enum class CardState {
    NEW,        // Chưa học lần nào
    LEARNING,   // Đang trong giai đoạn học (< 24h)
    REVIEW,     // Đang ôn tập định kỳ
    RELEARNING  // Học lại sau khi quên
}
```

#### `ReviewQuality` enum
```kotlin
enum class ReviewQuality {
    AGAIN,  // 0 - Quên hoàn toàn
    HARD,   // 1 - Nhớ khó khăn  
    GOOD,   // 2 - Nhớ được bình thường
    EASY    // 3 - Nhớ rất dễ dàng
}
```

#### `FlashcardResult` (Updated)
```kotlin
data class FlashcardResult(
    val flashcardId: String = "",
    val learned: Boolean = false,
    
    // Anki SRS fields
    val state: String = CardState.NEW.name,
    val easeFactor: Float = 2.5f,        // Độ dễ nhớ (1.3 - 2.5+)
    val intervalDays: Float = 0f,        // Khoảng cách ngày cho lần review tiếp
    val currentStep: Int = 0,            // Bước hiện tại trong learning
    val lapses: Int = 0,                 // Số lần quên
    
    val reviewCount: Int = 0,
    val lastReviewDate: Long? = null,
    val nextReviewDate: Long? = null,
    val confidence: Int = 0 // deprecated
)
```

### 2. Core Logic

#### `AnkiScheduler.kt`
Utility class xử lý toàn bộ logic Anki SRS:

**Các tham số quan trọng:**
- **Learning Steps**: `[1 phút, 10 phút]` - Giai đoạn học thẻ mới
- **Graduating Interval**: `1 ngày` - Khi hoàn thành learning
- **Easy Interval**: `4 ngày` - Khi bấm Easy ngay từ đầu
- **Min Ease Factor**: `1.3` - Giới hạn thấp nhất
- **Starting Ease Factor**: `2.5` - Mặc định cho thẻ mới

**Các hàm chính:**
```kotlin
// Schedule card tiếp theo
fun scheduleCard(card: FlashcardResult, quality: ReviewQuality): FlashcardResult

// Lấy cards cần ôn hôm nay
fun getDueCards(allResults: Map<String, FlashcardResult>): List<FlashcardResult>

// Thống kê
fun getNewCardsCount(allResults: Map<String, FlashcardResult>): Int
fun getLearningCardsCount(allResults: Map<String, FlashcardResult>): Int
fun getReviewCardsCount(allResults: Map<String, FlashcardResult>): Int

// Format interval đẹp
fun formatInterval(intervalDays: Float): String
```

### 3. Repository Methods

#### `FirebaseRepository.kt` (Added)
```kotlin
// Lấy user progress
suspend fun getUserProgress(userId: String): UserProgress?

// Lưu user progress
suspend fun saveUserProgress(userProgress: UserProgress): Boolean

// Update flashcard result
suspend fun updateFlashcardResult(
    userId: String, 
    flashcardId: String, 
    result: FlashcardResult
): Boolean

// Lấy tất cả flashcard results
suspend fun getFlashcardResults(userId: String): Map<String, FlashcardResult>

// Lấy results theo topic
suspend fun getFlashcardResultsByTopic(
    userId: String, 
    topicId: String
): Map<String, FlashcardResult>
```

### 4. ViewModels

#### `LearningViewModel` (Updated)
```kotlin
// Constructor có thêm userId
LearningViewModel(
    topicId: String,
    userId: String = "demo_user"
)

// Lưu kết quả học
fun saveStudyResult(quality: ReviewQuality)
```

**Cách sử dụng trong UI:**
```kotlin
// Khi user bấm nút review
viewModel.saveStudyResult(ReviewQuality.GOOD)
```

#### `ReviewViewModel` (Updated)
```kotlin
// Constructor có thêm userId
ReviewViewModel(userId: String = "demo_user")

// UI State có thêm thống kê
data class ReviewUiState(
    val reviewTopics: List<ReviewTopic> = emptyList(),
    val newCardsCount: Int = 0,        // Số thẻ mới
    val learningCardsCount: Int = 0,   // Số thẻ đang học
    val reviewCardsCount: Int = 0      // Số thẻ cần ôn
)
```

**ReviewTopic có thêm:**
```kotlin
data class ReviewTopic(
    val topic: Topic,
    val progress: Float,
    val dueCount: Int = 0,      // Số cards cần ôn hôm nay
    val totalCards: Int = 0
)
```

### 5. UI Components

#### `ReviewButtons.kt`
4 nút review theo Anki style:

```kotlin
// Full version với interval
ReviewButtons(
    onReviewQuality = { quality ->
        viewModel.saveStudyResult(quality)
    },
    showInterval = true,
    againInterval = "< 10 phút",
    hardInterval = "1 ngày", 
    goodInterval = "4 ngày",
    easyInterval = "7 ngày"
)

// Compact version
CompactReviewButtons(
    onReviewQuality = { quality ->
        viewModel.saveStudyResult(quality)
    }
)
```

## 📊 Cách hoạt động

### Luồng học thẻ mới (NEW → LEARNING → REVIEW)

1. **Thẻ mới (NEW)**
   - User nhìn thẻ lần đầu
   - Bấm **AGAIN** → Learning (1 phút)
   - Bấm **HARD** → Learning (1 phút)
   - Bấm **GOOD** → Learning (10 phút)
   - Bấm **EASY** → Review (4 ngày)

2. **Đang học (LEARNING)**
   - Steps: 1 phút → 10 phút
   - Bấm **AGAIN** → Reset về bước 1
   - Bấm **HARD** → Giữ nguyên bước
   - Bấm **GOOD** → Qua bước tiếp (hoặc tốt nghiệp nếu hết steps)
   - Bấm **EASY** → Tốt nghiệp sớm (4 ngày)

3. **Ôn tập (REVIEW)**
   - Interval tăng theo ease factor
   - Bấm **AGAIN** → Relearning (học lại)
   - Bấm **HARD** → Interval × 1.2
   - Bấm **GOOD** → Interval × ease factor
   - Bấm **EASY** → Interval × ease factor × 1.3

4. **Học lại (RELEARNING)**
   - Giống LEARNING nhưng ease factor bị giảm 15%
   - Khi tốt nghiệp về REVIEW: interval = 1 ngày

### Công thức tính Ease Factor

```kotlin
newEaseFactor = currentEaseFactor + (0.1 - (3 - quality) * (0.08 + (3 - quality) * 0.02))

// Min: 1.3
// Max: Không giới hạn (thường 2.5-3.0)
```

### Ví dụ timeline

**Thẻ mới học lần đầu (bấm GOOD mọi lần):**
```
NEW → LEARNING (10 phút) → REVIEW (1 ngày) 
→ 2.5 ngày → 6 ngày → 15 ngày → 38 ngày...
```

**Thẻ mới học lần đầu (bấm EASY):**
```
NEW → REVIEW (4 ngày) → 10 ngày → 25 ngày...
```

**Thẻ bị quên (AGAIN):**
```
REVIEW → RELEARNING (1 phút) → (10 phút) → REVIEW (1 ngày)
(ease factor giảm 15%)
```

## 🔧 Cách tích hợp vào UI

### 1. Update LearningActivity

```kotlin
// Thêm ReviewButtons vào FlashcardView
FlashcardView(
    flashcard = currentCard,
    onFlip = { /* flip logic */ }
)

Spacer(modifier = Modifier.height(16.dp))

ReviewButtons(
    onReviewQuality = { quality ->
        viewModel.saveStudyResult(quality)
        viewModel.goToNextCard()
    },
    showInterval = true
)
```

### 2. Update ReviewActivity

```kotlin
// Hiển thị thống kê
val uiState by viewModel.uiState.collectAsState()

Card {
    Column(padding = 16.dp) {
        Text("Thẻ mới: ${uiState.newCardsCount}")
        Text("Đang học: ${uiState.learningCardsCount}")
        Text("Cần ôn: ${uiState.reviewCardsCount}")
    }
}

// Hiển thị topics với số cards cần ôn
LazyColumn {
    items(uiState.reviewTopics) { reviewTopic ->
        ReviewTopicItem(
            topic = reviewTopic.topic,
            progress = reviewTopic.progress,
            dueCount = reviewTopic.dueCount,
            onClick = { /* navigate */ }
        )
    }
}
```

### 3. Pass userId khi khởi tạo ViewModel

```kotlin
// TODO: Lấy userId từ Firebase Auth
val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "demo_user"

// LearningViewModel
val viewModel: LearningViewModel by viewModels {
    LearningViewModelFactory(topicId, userId)
}

// ReviewViewModel  
val viewModel: ReviewViewModel by viewModels {
    ReviewViewModelFactory(userId)
}
```

## 🎨 UI Design Suggestions

### Review Buttons Colors
- **AGAIN**: Red `#E53935` (Sai hoàn toàn)
- **HARD**: Orange `#FF9800` (Khó nhớ)
- **GOOD**: Green `#4CAF50` (Nhớ được)
- **EASY**: Blue `#2196F3` (Dễ dàng)

### Review Screen Statistics Card
```
┌─────────────────────────────┐
│ 📊 Thống kê hôm nay        │
│                             │
│ 🆕 Thẻ mới:        15       │
│ 📖 Đang học:       8        │
│ 🔄 Cần ôn tập:     23       │
│                             │
│ Total: 46 cards             │
└─────────────────────────────┘
```

### Topic Card với due count
```
┌─────────────────────────────┐
│ Daily Routine               │
│ ████████░░ 80%              │
│ 12 cards cần ôn hôm nay     │
└─────────────────────────────┘
```

## 📝 TODO / Improvements

### Ngay lập tức
- [ ] Tích hợp Firebase Authentication để lấy userId thật
- [ ] Thêm ReviewButtons vào LearningActivity UI
- [ ] Update ReviewScreen UI để hiển thị statistics
- [ ] Test với dữ liệu thật

### Nâng cao
- [ ] Cho phép user tùy chỉnh learning steps
- [ ] Cho phép user tùy chỉnh graduating interval
- [ ] Thêm animation cho ReviewButtons
- [ ] Thêm sound effects khi bấm nút
- [ ] Thêm haptic feedback
- [ ] Statistics dashboard (số cards học mỗi ngày, streak, ...)
- [ ] Export/Import progress
- [ ] Backup to Cloud
- [ ] Offline mode với sync sau

### Advanced Features
- [ ] Custom ease factor per card
- [ ] Tag system cho cards
- [ ] Filtered decks (ôn theo tag, độ khó, ...)
- [ ] Cramming mode (ôn nhanh trước khi thi)
- [ ] Heatmap calendar (giống GitHub contributions)
- [ ] Learning analytics & charts
- [ ] Daily goal & streak system

## 🐛 Known Issues

1. **userId hardcoded**: Hiện đang dùng "demo_user" cố định
   - **Fix**: Tích hợp Firebase Auth

2. **No error handling for network failures**: Nếu Firebase fail thì không có retry
   - **Fix**: Thêm retry logic và offline cache

3. **Performance**: Load tất cả results mỗi lần vào ReviewActivity
   - **Fix**: Pagination hoặc lazy loading

## 🔍 Testing Tips

### Test Scenarios

1. **Thẻ mới → Learning → Review**
   ```kotlin
   // Thẻ mới
   val newCard = FlashcardResult(flashcardId = "test1")
   val afterGood = ankiScheduler.scheduleCard(newCard, ReviewQuality.GOOD)
   // Should: state = LEARNING, nextReview = 10 minutes
   ```

2. **Quên thẻ (AGAIN)**
   ```kotlin
   val reviewCard = FlashcardResult(
       flashcardId = "test1",
       state = CardState.REVIEW.name,
       intervalDays = 10f,
       easeFactor = 2.5f
   )
   val afterAgain = ankiScheduler.scheduleCard(reviewCard, ReviewQuality.AGAIN)
   // Should: state = RELEARNING, lapses = 1
   ```

3. **Easy từ đầu**
   ```kotlin
   val newCard = FlashcardResult(flashcardId = "test1")
   val afterEasy = ankiScheduler.scheduleCard(newCard, ReviewQuality.EASY)
   // Should: state = REVIEW, intervalDays = 4, learned = true
   ```

### Test Data
Tạo test data trong Firebase:
```json
{
  "userProgress": {
    "demo_user": {
      "flashcardResults": {
        "flash_001": {
          "flashcardId": "flash_001",
          "state": "NEW",
          "easeFactor": 2.5,
          "intervalDays": 0
        }
      }
    }
  }
}
```

## 📚 References

- [Anki Algorithm Documentation](https://faqs.ankiweb.net/what-spaced-repetition-algorithm.html)
- [SuperMemo SM-2](https://www.supermemo.com/en/archives1990-2015/english/ol/sm2)
- [Mochi App](https://mochi.cards/) - Inspiration

## 🎉 Summary

Đã implement đầy đủ Anki SRS system với:
✅ Data models (CardState, ReviewQuality, FlashcardResult)
✅ Core logic (AnkiScheduler)
✅ Repository methods (Firebase integration)
✅ ViewModels (LearningViewModel, ReviewViewModel)
✅ UI components (ReviewButtons)

**Ready to use!** Chỉ cần tích hợp UI và Firebase Auth là xong.
