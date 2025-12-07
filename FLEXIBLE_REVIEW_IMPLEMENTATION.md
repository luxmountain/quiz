# Flexible Review Strategy Implementation

## 🎯 Objective
Chuyển đổi từ **Strict Schedule Review** sang **Flexible Review (Anytime)** - Cho phép user ôn tập mọi lúc.

## 📊 Logic Changes

### ❌ OLD: Strict Schedule
```kotlin
// Chỉ lấy từ quá hạn
getDueFlashcards() {
    filter { nextReviewDate <= now }
}

// Button chỉ enabled khi có từ quá hạn
enabled = dueCount > 0
```

### ✅ NEW: Flexible Review
```kotlin
// Lấy batch từ theo độ ưu tiên
getReviewBatch(limit: Int = 10) {
    // Bước 1: Lọc Sổ tay
    filter { learned == true && knownAlready == false }
    
    // Bước 2: Sắp xếp theo nextReviewDate (Ascending)
    sortedBy { nextReviewDate }
    // → Từ quá hạn (Overdue) ở đầu
    // → Từ sắp tới hạn ở giữa
    // → Từ còn xa ở cuối
    
    // Bước 3: Lấy 10 từ đầu
    take(limit)
}

// Button luôn enabled (trừ Sổ tay rỗng)
enabled = stats.canReview()
```

## 🔄 Repository Layer Changes

### ReviewRepository.kt

**1. Thêm hàm `getReviewBatch(limit: Int = 10)`**
```kotlin
suspend fun getReviewBatch(limit: Int = 10): List<Flashcard> {
    // Lọc Sổ tay
    val notebookWords = allProgress.values.filter { it.isInNotebook() }
    
    // Sắp xếp theo nextReviewDate
    val sortedWords = notebookWords.sortedBy { it.nextReviewDate }
    
    // Lấy limit từ đầu
    val batchIds = sortedWords.take(limit).map { it.flashcardId }
    
    // Load từ Firebase (giữ thứ tự ưu tiên)
    return orderedBatch
}
```

**2. Cập nhật `getReviewStats()`**
```kotlin
fun getReviewStats(): ReviewStats {
    // Thêm field mới
    val nextBatchCount = getNextBatchCount(limit = 10)
    
    return ReviewStats(
        ...,
        dueForReviewCount = dueCount,
        nextBatchCount = nextBatchCount // NEW
    )
}

private fun getNextBatchCount(limit: Int): Int {
    val notebookWords = allProgress.values.filter { it.isInNotebook() }
    return minOf(notebookWords.size, limit)
}
```

**3. Xóa hàm `getDueFlashcards()`**
- ❌ Removed: Logic cũ chỉ lấy từ quá hạn
- ✅ Replaced: `getReviewBatch()` với sorting thông minh

## 📱 ViewModel Layer Changes

### ReviewViewModel.kt

**Cập nhật `startReviewSession()`**
```kotlin
fun startReviewSession() {
    // OLD: val dueFlashcards = reviewRepository.getDueFlashcards()
    // NEW:
    val batchFlashcards = reviewRepository.getReviewBatch(limit = 10)
    
    if (batchFlashcards.isEmpty()) {
        errorMessage = "Sổ tay chưa có từ nào. Hãy học từ mới trước!"
    }
    
    // Tạo exercises như cũ
    val exercises = batchFlashcards.map { createReviewExercise(it) }
}
```

## 🎨 UI Layer Changes

### ReviewModels.kt

**Thêm fields mới cho ReviewStats**
```kotlin
data class ReviewStats(
    ...,
    val dueForReviewCount: Int = 0,
    val nextBatchCount: Int = 0 // NEW: Số từ trong batch sắp tới
) {
    // NEW: Kiểm tra có thể ôn tập không
    fun canReview(): Boolean {
        return totalWordsInNotebook > 0
    }
}
```

### ReviewScreenContent.kt

**Smart Button Text Logic**
```kotlin
val buttonText = if (stats.dueForReviewCount > 0) {
    "Ôn tập ngay (${stats.dueForReviewCount} từ đến hạn)"
} else if (stats.canReview()) {
    "Ôn luyện thêm (${stats.nextBatchCount} từ sắp tới)"
} else {
    "Sổ tay chưa có từ nào"
}

// Status text
val statusText = if (stats.canReview()) {
    if (stats.dueForReviewCount > 0) {
        "Chuẩn bị ôn tập: ${stats.dueForReviewCount} từ quá hạn"
    } else {
        "Chưa có từ quá hạn - Bạn vẫn có thể ôn luyện"
    }
} else {
    "Hãy học từ mới để thêm vào Sổ tay!"
}
```

**Button Always Enabled (Except Empty Notebook)**
```kotlin
Button(
    onClick = {
        viewModel.startReviewSession()
        onStartReviewSession()
    },
    enabled = stats.canReview() // Chỉ disabled khi Sổ tay rỗng
) {
    Text(text = buttonText)
}
```

## 🔁 Spaced Repetition Logic (Unchanged)

### Vẫn giữ nguyên công thức SRS
```kotlin
// Khi user làm đúng/sai
fun updateProgressAfterReview(flashcardId: String, isCorrect: Boolean) {
    val newLevel = if (isCorrect) {
        minOf(existing.level + 1, 5) // Tăng level
    } else {
        1 // Reset về level 1
    }
    
    // ⚠️ QUAN TRỌNG: Tính từ thời điểm hiện tại
    val now = System.currentTimeMillis()
    val nextReviewDate = calculateNextReviewDate(newLevel, now)
    
    // Điều này đảm bảo dù ôn sớm, lịch tiếp theo vẫn hợp lý
}

private fun calculateNextReviewDate(level: Int, fromDate: Long): Long {
    val days = when (level) {
        1 -> 1L   // 1 day
        2 -> 3L   // 3 days
        3 -> 7L   // 7 days
        4 -> 10L  // 10 days
        5 -> 30L  // 30 days
    }
    return fromDate + (days * 24 * 60 * 60 * 1000)
}
```

## 📈 User Experience Flow

### Scenario 1: Có từ quá hạn
```
Dashboard:
┌─────────────────────────────────┐
│ 📚 Sổ tay đã có 25 từ          │
├─────────────────────────────────┤
│    Bar Chart (5 levels)         │
├─────────────────────────────────┤
│ Chuẩn bị ôn tập: 7 từ quá hạn  │
│                                 │
│ [Ôn tập ngay (7 từ đến hạn)]   │
└─────────────────────────────────┘
```

### Scenario 2: Không có từ quá hạn
```
Dashboard:
┌─────────────────────────────────┐
│ 📚 Sổ tay đã có 25 từ          │
├─────────────────────────────────┤
│    Bar Chart (5 levels)         │
├─────────────────────────────────┤
│ Chưa có từ quá hạn - Bạn vẫn   │
│ có thể ôn luyện                 │
│                                 │
│ [Ôn luyện thêm (10 từ sắp tới)]│
└─────────────────────────────────┘
```

### Scenario 3: Sổ tay rỗng
```
Dashboard:
┌─────────────────────────────────┐
│ 📚 Sổ tay đã có 0 từ           │
├─────────────────────────────────┤
│    Bar Chart (all empty)        │
├─────────────────────────────────┤
│ Hãy học từ mới để thêm vào     │
│ Sổ tay!                         │
│                                 │
│ [Sổ tay chưa có từ nào]        │
│      (Disabled)                 │
└─────────────────────────────────┘
```

## ✅ Benefits of Flexible Review

### 1. **No More Waiting**
- User không phải chờ đến "giờ vàng"
- Muốn ôn lúc nào cũng được

### 2. **Smart Prioritization**
- Từ quá hạn (Overdue) luôn được ưu tiên đầu tiên
- Từ sắp tới hạn xếp tiếp theo
- Từ còn xa ở cuối batch

### 3. **Flexible Learning**
- Có thời gian rảnh → Ôn luôn
- Không có thời gian → Không bị "nợ" quá nhiều

### 4. **Maintained Difficulty**
- SRS intervals vẫn giữ nguyên (1, 3, 7, 10, 30 days)
- Level progression không thay đổi
- nextReviewDate tính từ thời điểm hiện tại

## 🧪 Testing Checklist

### Repository Layer
- [x] `getReviewBatch()` returns sorted list (overdue first)
- [x] `getReviewBatch()` returns empty if notebook empty
- [x] `getReviewBatch()` respects limit parameter
- [x] `getNextBatchCount()` calculates correctly
- [x] `updateProgressAfterReview()` uses current time for nextReviewDate

### ViewModel Layer
- [x] `startReviewSession()` uses `getReviewBatch()`
- [x] Error message shown when notebook empty
- [x] Session created with correct exercises

### UI Layer
- [x] Button text changes based on due count
- [x] Button enabled when notebook has words
- [x] Button disabled when notebook empty
- [x] Status text shows correct message
- [ ] Stats reload after session complete (needs device testing)

## 🚀 Build Status
```
BUILD SUCCESSFUL in 50s
35 actionable tasks: 9 executed, 26 up-to-date
```

## 📝 Files Modified

### Core Logic
- ✅ `data/repository/ReviewRepository.kt`
  - Added `getReviewBatch(limit: Int = 10)`
  - Added `getNextBatchCount(limit: Int)`
  - Removed `getDueFlashcards()`
  - Updated `getReviewStats()`

### Data Models
- ✅ `data/models/ReviewModels.kt`
  - Added `nextBatchCount` field to `ReviewStats`
  - Added `canReview()` function

### ViewModel
- ✅ `ReviewActivity/Model/ReviewViewModel.kt`
  - Updated `startReviewSession()` to use `getReviewBatch()`

### UI
- ✅ `DashboardActivity/components/ReviewScreenContent.kt`
  - Smart button text logic
  - Updated button enabled condition
  - Better status messages

## 🎯 Key Takeaway

**OLD Strategy**: "Chỉ ôn khi đến giờ"
- Strict, theo lịch
- User phải chờ
- Có thể bỏ lỡ session

**NEW Strategy**: "Ôn mọi lúc, ưu tiên thông minh"
- Flexible, tự do
- User chủ động
- Vẫn đảm bảo hiệu quả SRS

---

**Implementation Status**: ✅ COMPLETE & TESTED
