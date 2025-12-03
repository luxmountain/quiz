# 📊 Tính năng Thống kê Học tập

## Tổng quan
Tab "MochiHub" đã được thay thế bằng tab **"Thống kê"** để theo dõi tiến trình học tập của người dùng.

## Tính năng

### 1. 📈 Thẻ Tổng quan
- **Tổng từ đã học**: Tổng số từ vựng đã ôn tập
- **Thời gian học**: Tổng thời gian học tập (phút)
- **Chuỗi hiện tại**: Số ngày học liên tục
- **Kỷ lục chuỗi**: Chuỗi học dài nhất từng đạt được

### 2. 📊 Bar Chart - Hoạt động 7 ngày
- Biểu đồ cột hiển thị số từ đã ôn tập trong 7 ngày gần nhất
- Hiển thị ngày tháng dưới mỗi cột
- Tổng hợp:
  - **Tổng từ**: Tổng số từ ôn tập trong tuần
  - **Độ chính xác**: Độ chính xác trung bình

### 3. 🔥 Calendar Heatmap - Biểu đồ nhiệt
- Hiển thị lịch tháng hiện tại
- **Màu sắc**:
  - Xám nhạt: Không học
  - Tím nhạt → Tím đậm: Học ít → nhiều
- Ô càng đậm = học càng nhiều từ trong ngày đó
- Chú giải màu ở dưới lịch

### 4. 💡 Thông điệp động viên
Thay đổi theo chuỗi học tập hiện tại:
- 0 ngày: "Hãy bắt đầu học hôm nay..."
- 1-2 ngày: "Tuyệt vời! Hãy tiếp tục..."
- 3-6 ngày: "Bạn đang làm rất tốt..."
- 7-29 ngày: "Tuyệt vời! Trên con đường thành công..."
- 30+ ngày: "Phi thường! Học viên xuất sắc..."

## Kiến trúc Code

### Data Models
```
data/models/StatisticsModels.kt
├── DailyStats          # Thống kê theo ngày
├── WeeklyStats         # Thống kê theo tuần
├── MonthlyHeatmapData  # Dữ liệu heatmap
└── LearningStreak      # Chuỗi học tập
```

### ViewModel
```
StatisticsActivity/Model/StatisticsViewModel.kt
- loadStatistics()              # Load dữ liệu
- calculateWeeklyStats()        # Tính toán 7 ngày
- calculateMonthlyHeatmap()     # Tính toán heatmap
- calculateLearningStreak()     # Tính chuỗi học
```

### UI Components
```
StatisticsActivity/components/
├── WeeklyBarChart.kt       # Bar chart 7 ngày
├── CalendarHeatmap.kt      # Lịch nhiệt
└── StatisticsScreen.kt     # Màn hình chính
```

### Integration
```
DashboardActivity/screens/MainScreen.kt
- Thêm StatisticsViewModel
- Tab "Statistics" thay thế "MochiHub"

DashboardActivity/components/BottomNavigationBarStub.kt
- "MochiHub" → "Thống kê"
- "Hub" → "Statistics"
```

## Nguồn dữ liệu

Dữ liệu được lấy từ `UserProgressManager`:
- `getStudyHistory()`: Lịch sử học tập
- Mỗi `StudyResult` chứa:
  - topicId, topicName
  - studyType (flashcard/conversation)
  - totalItems, correctCount
  - timeSpent, accuracy
  - completedDate

## Thuật toán Chuỗi học tập

### Current Streak (Chuỗi hiện tại)
1. Lấy danh sách ngày đã học (distinct)
2. Bắt đầu từ hôm nay
3. Kiểm tra ngược lại từng ngày
4. Nếu có học ngày đó hoặc ngày hôm qua → +1
5. Nếu bỏ lỡ → dừng

### Longest Streak (Kỷ lục)
1. Duyệt qua tất cả ngày đã học
2. Tính khoảng cách giữa các ngày
3. Nếu ≤ 1 ngày → cùng chuỗi
4. Nếu > 1 ngày → chuỗi mới
5. Trả về chuỗi dài nhất

## Màu sắc Heatmap

Intensity được tính dựa trên số từ ôn tập so với max trong tháng:

```kotlin
0%       → Color(0xFFEEEEEE) // Xám nhạt
< 25%    → Color(0xFFE1BEE7) // Tím rất nhạt
< 50%    → Color(0xFFBA68C8) // Tím nhạt
< 75%    → Color(0xFF9C27B0) // Tím đậm
≥ 75%    → Color(0xFF6A1B9A) // Tím rất đậm
```

## Cách sử dụng

1. Mở app
2. Chọn tab **"Thống kê"** (icon cuối cùng)
3. Xem:
   - Tổng quan ở trên cùng
   - Bar chart 7 ngày
   - Calendar heatmap
   - Thông điệp động viên

## Tương lai

### Có thể mở rộng:
- [ ] Chọn tháng để xem lịch sử
- [ ] So sánh theo tuần/tháng
- [ ] Export báo cáo PDF
- [ ] Chia sẻ thành tích
- [ ] Badges/achievements dựa trên chuỗi học
- [ ] Biểu đồ accuracy theo thời gian
- [ ] Top topics đã học nhiều nhất
- [ ] Đề xuất thời gian học tốt nhất

## Lưu ý kỹ thuật

### StateFlow Pattern
```kotlin
private val _uiState = MutableStateFlow(StatisticsUiState())
val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

// Update
_uiState.update { it.copy(property = newValue) }

// Collect
val uiState by viewModel.uiState.collectAsState()
```

### LazyColumn Layout
```kotlin
LazyColumn {
    item { HeaderStatsCard() }
    item { WeeklyBarChart() }
    item { CalendarHeatmap() }
    item { MotivationCard() }
}
```

### Xử lý ngày tháng
```kotlin
// Lấy đầu ngày (00:00:00)
calendar.set(Calendar.HOUR_OF_DAY, 0)
calendar.set(Calendar.MINUTE, 0)
calendar.set(Calendar.SECOND, 0)
calendar.set(Calendar.MILLISECOND, 0)

// Tính khoảng cách ngày
val diffDays = (date1 - date2) / 86400000L
```

## Files đã tạo/sửa

### Tạo mới:
```
✅ data/models/StatisticsModels.kt
✅ StatisticsActivity/Model/StatisticsViewModel.kt
✅ StatisticsActivity/screens/StatisticsScreen.kt
✅ StatisticsActivity/components/WeeklyBarChart.kt
✅ StatisticsActivity/components/CalendarHeatmap.kt
```

### Cập nhật:
```
✅ DashboardActivity/screens/MainScreen.kt
✅ DashboardActivity/components/BottomNavigationBarStub.kt
✅ DashboardActivity/components/InAppTourOverlay.kt
```

## Testing

### Test dữ liệu:
1. Học một số flashcard/conversation
2. Quay lại tab Thống kê
3. Kiểm tra:
   - Bar chart có hiển thị đúng số từ?
   - Heatmap có tô màu ngày hôm nay?
   - Chuỗi học có đúng?

### Test chuỗi:
- Học hôm nay → Current streak = 1
- Học hôm qua + hôm nay → Current streak = 2
- Bỏ lỡ 1 ngày → Current streak reset về 1

## Troubleshooting

### Dữ liệu không hiển thị?
- Kiểm tra `UserProgressManager.getStudyHistory()`
- Đảm bảo đã có `StudyResult` được lưu
- Check logcat cho exceptions

### Bar chart rỗng?
- Chưa có dữ liệu 7 ngày gần đây
- Thử học một số flashcard

### Heatmap không có màu?
- Chưa học trong tháng hiện tại
- Kiểm tra `completedDate` trong StudyResult

---

**Tính năng hoàn thiện! 🎉**
