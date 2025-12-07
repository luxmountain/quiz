# Strict Spaced Repetition with Countdown Timer

## Implementation Summary

### Overview
Refactored Review Feature from "Flexible Review (anytime)" to **Strict Spaced Repetition** with real-time countdown timer. Users can ONLY review words that are due, and must wait for the countdown when no words are ready.

---

## Key Changes

### 1. Data Model Updates (`ReviewModels.kt`)

**ReviewStats - Added Countdown Data**:
```kotlin
data class ReviewStats(
    val totalWordsInNotebook: Int = 0,
    val level1Count: Int = 0,
    val level2Count: Int = 0,
    val level3Count: Int = 0,
    val level4Count: Int = 0,
    val level5Count: Int = 0,
    val dueForReviewCount: Int = 0,
    val nextReviewTime: Long? = null,  // NEW: Timestamp of next word due
    val upcomingCount: Int = 0         // NEW: Count of upcoming words
)
```

---

### 2. Repository Logic (`ReviewRepository.kt`)

#### `getReviewStats()` - Strict Categorization

**OLD Logic** (Flexible):
```kotlin
val dueCount = notebookWords.count { it.isDueForReview() }
val nextBatchCount = getNextBatchCount(limit = 10)
// Returned count of all words, allowing review anytime
```

**NEW Logic** (Strict):
```kotlin
val currentTime = System.currentTimeMillis()

// STRICT: Only count words that ARE DUE
val dueWords = notebookWords.filter { it.isDueForReview() }
val dueCount = dueWords.size

// Get future words (not due yet)
val futureWords = notebookWords.filter { currentTime < it.nextReviewDate }

// Find next review time
val nextReviewTime = futureWords
    .minByOrNull { it.nextReviewDate }
    ?.nextReviewDate

// Count upcoming words (max 10)
val upcomingCount = futureWords.take(10).size

return ReviewStats(
    dueForReviewCount = dueCount,
    nextReviewTime = nextReviewTime,
    upcomingCount = upcomingCount,
    ...
)
```

#### `getReviewBatch()` - Strict Filtering

**OLD Logic** (Flexible):
```kotlin
// Allowed reviewing ALL notebook words (due or not)
val notebookWords = allProgress.values.filter { it.isInNotebook() }
val sortedWords = notebookWords.sortedBy { it.nextReviewDate }
// Always returned words, review anytime ❌
```

**NEW Logic** (Strict):
```kotlin
// ONLY return words that ARE DUE
val dueWords = allProgress.values
    .filter { it.isInNotebook() && it.isDueForReview() }

if (dueWords.isEmpty()) {
    return emptyList()  // ✅ No words ready → Empty list
}

val sortedWords = dueWords.sortedBy { it.nextReviewDate }
// Return overdue words first ✅
```

---

### 3. UI Implementation (`ReviewScreenContent.kt`)

#### State A: Words Are Ready (`dueCount > 0`)

**UI Components**:
- ✅ Green text: "Sẵn sàng ôn tập: X từ đã đến hạn"
- ✅ Green Button: "Ôn tập ngay"
- ✅ Action: `onStartReviewSession()`

**Code**:
```kotlin
if (stats.dueForReviewCount > 0) {
    Text(
        text = "Sẵn sàng ôn tập: ${stats.dueForReviewCount} từ đã đến hạn",
        fontSize = 16.sp,
        color = Color(0xFF4CAF50),
        fontWeight = FontWeight.Medium
    )
    
    Button(
        onClick = onStartReviewSession,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50)
        )
    ) {
        Text("Ôn tập ngay")
    }
}
```

---

#### State B: No Words Ready (`dueCount == 0`)

**UI Components**:
- ⏳ Countdown Card (Gray background)
- 🕐 Hourglass Icon
- 📊 Text: "Chuẩn bị ôn tập: X từ sắp tới hạn"
- ⏱️ **Live Countdown Timer**: `HH:MM:SS`
- 🚫 Review button **HIDDEN** (forced to learn new words)

**Code**:
```kotlin
else if (stats.totalWordsInNotebook > 0 && stats.nextReviewTime != null) {
    CountdownCard(
        nextReviewTime = stats.nextReviewTime,
        currentTime = currentTime,
        upcomingCount = stats.upcomingCount
    )
}
```

---

### 4. Countdown Timer Logic

#### Real-time Updates (1 Second Tick)

```kotlin
var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

LaunchedEffect(stats.nextReviewTime) {
    while (true) {
        delay(1000L)  // Update every 1 second
        currentTime = System.currentTimeMillis()
        
        // Auto-refresh when countdown reaches zero
        if (stats.nextReviewTime != null && currentTime >= stats.nextReviewTime) {
            viewModel.loadReviewStats()  // Switch to State A
        }
    }
}
```

#### Time Formatting (`HH:MM:SS`)

```kotlin
private fun formatTimeLeft(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
```

**Examples**:
- 9 hours, 57 minutes, 3 seconds → `09:57:03`
- 23 hours, 0 minutes, 45 seconds → `23:00:45`
- 0 hours, 5 minutes, 12 seconds → `00:05:12`

---

### 5. CountdownCard Component

```kotlin
@Composable
private fun CountdownCard(
    nextReviewTime: Long,
    currentTime: Long,
    upcomingCount: Int
) {
    val timeLeft = (nextReviewTime - currentTime).coerceAtLeast(0)
    val formattedTime = formatTimeLeft(timeLeft)
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEEEEEE)  // Gray background
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Hourglass Icon
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF9E9E9E)
            )
            
            // Title
            Text("Chuẩn bị ôn tập", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            // Count
            Text("$upcomingCount từ sắp tới hạn", fontSize = 16.sp, color = Color.Gray)
            
            // Countdown Timer
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(vertical = 20.dp)
            ) {
                Text(
                    text = formattedTime,  // "09:57:03"
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EA),
                    letterSpacing = 2.sp
                )
            }
            
            Text("Thời gian còn lại", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
```

---

## User Experience Flow

### Scenario 1: Words Are Due

```
┌─────────────────────────────────────┐
│  📚  Sổ tay đã có 25 từ            │
└─────────────────────────────────────┘

    [Bar Chart: L1=5, L2=8, ...]

  ✅ Sẵn sàng ôn tập: 7 từ đã đến hạn

┌─────────────────────────────────────┐
│         Ôn tập ngay                 │  ← Green Button
└─────────────────────────────────────┘
```

**Action**: User taps → Start Review Session with 7 due words

---

### Scenario 2: No Words Due (Countdown Active)

```
┌─────────────────────────────────────┐
│  📚  Sổ tay đã có 25 từ            │
└─────────────────────────────────────┘

    [Bar Chart: L1=5, L2=8, ...]

┌─────────────────────────────────────┐
│          🕐                         │
│     Chuẩn bị ôn tập                │
│   10 từ sắp tới hạn                │
│                                     │
│  ┌───────────────────────────────┐ │
│  │        09:57:03               │ │  ← Live countdown
│  └───────────────────────────────┘ │
│     Thời gian còn lại              │
└─────────────────────────────────────┘
```

**Behavior**: 
- Timer ticks down every second: `09:57:03` → `09:57:02` → `09:57:01`...
- When reaches `00:00:00` → Auto-refresh → Switch to State A (Green Button)
- **Review action DISABLED** → User must learn new words

---

### Scenario 3: Empty Notebook

```
┌─────────────────────────────────────┐
│  📚  Sổ tay đã có 0 từ             │
└─────────────────────────────────────┘

    [Bar Chart: All zeros]

      Sổ tay chưa có từ nào

┌─────────────────────────────────────┐
│    Chưa có từ để ôn tập            │  ← Gray Disabled Button
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│        💡 Hướng dẫn                 │
│                                     │
│  Để có từ trong Sổ tay:            │
│  1. Chọn Level (Beginner/...)       │
│  2. Chọn Topic để học               │
│  3. Hoàn thành 3 bước học           │
│  4. Từ tự động vào Sổ tay!         │
└─────────────────────────────────────┘
```

---

## Technical Implementation Details

### Countdown Precision
- Updates every **1000ms** (1 second)
- Uses `LaunchedEffect` with `delay(1000L)`
- Minimal performance impact

### Auto-Refresh
- When `currentTime >= nextReviewTime` → Auto-refresh stats
- Seamlessly switches from Countdown to Green Button
- No manual reload needed

### Edge Cases Handled
1. **nextReviewTime is null** → Show empty notebook UI
2. **Multiple words due at same time** → All included in batch
3. **User stares at countdown** → Auto-switches when time up
4. **Countdown already passed** → Immediately shows Green Button

---

## Spaced Repetition Intervals

| Level | Interval | Days |
|-------|----------|------|
| 1     | 1 day    | 24h  |
| 2     | 3 days   | 72h  |
| 3     | 7 days   | 168h |
| 4     | 10 days  | 240h |
| 5     | 30 days  | 720h |

**Example Timeline**:
- Day 0: Learn word → Level 1 → Next review: Day 1
- Day 1: Review correct → Level 2 → Next review: Day 4
- Day 4: Review correct → Level 3 → Next review: Day 11
- Day 11: Review correct → Level 4 → Next review: Day 21
- Day 21: Review correct → Level 5 → Next review: Day 51

**Failure**:
- Any level: Review incorrect → **Reset to Level 1**

---

## Testing Checklist

### State A (Ready to Review)
- [ ] Green button shows when `dueCount > 0`
- [ ] Text displays correct count: "7 từ đã đến hạn"
- [ ] Button click starts review session
- [ ] Only due words included in batch

### State B (Countdown)
- [ ] Countdown shows when `dueCount == 0` and `nextReviewTime != null`
- [ ] Timer updates every second
- [ ] Format is strictly `HH:MM:SS`
- [ ] Upcoming count displays correctly
- [ ] Auto-switches to State A when time up

### Countdown Accuracy
- [ ] 10 hours left → Shows `10:00:00`
- [ ] 1 minute left → Shows `00:01:00`
- [ ] 30 seconds left → Shows `00:00:30`
- [ ] Countdown reaches zero → Green button appears

### Edge Cases
- [ ] Empty notebook → Shows guide card
- [ ] All words at max level → Long countdown
- [ ] User learns new word while on countdown → Stats update

---

## Build Status

✅ **BUILD SUCCESSFUL**

All files compiled without errors. Ready for device testing.

---

## Files Modified

1. **ReviewModels.kt**
   - Added `nextReviewTime: Long?` to `ReviewStats`
   - Added `upcomingCount: Int` to `ReviewStats`

2. **ReviewRepository.kt**
   - Updated `getReviewStats()` → Strict categorization (due vs future)
   - Updated `getReviewBatch()` → Only return due words
   - Removed `getNextBatchCount()` helper

3. **ReviewScreenContent.kt**
   - Added `CountdownCard` component
   - Added `formatTimeLeft()` helper
   - Implemented State A/B logic
   - Added `LaunchedEffect` for live countdown
   - Added auto-refresh on countdown completion

---

## Next Steps

1. **Test on Device**:
   - Learn 5-10 words
   - Wait 24 hours (or manipulate time for testing)
   - Verify countdown timer accuracy
   - Test auto-switch from countdown to green button

2. **Optional Enhancements**:
   - Add notification when words become due
   - Add "Skip to next batch" feature (premium)
   - Add stats animation when countdown completes

3. **Performance Monitoring**:
   - Monitor battery usage with 1-second updates
   - Optimize if needed (e.g., pause when app backgrounded)

---

## Summary

**Before**: Flexible review - users could review any words anytime ❌

**After**: Strict spaced repetition - users MUST wait for countdown ✅

**Key Feature**: Live countdown timer (`HH:MM:SS`) enforces discipline and proper spaced repetition intervals.

**User Impact**: Better long-term retention through scientifically proven spaced intervals! 🧠
