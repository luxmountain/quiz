# Product Tour - Hướng dẫn sử dụng (Spotlight Style)

## Tính năng Product Tour đã được tích hợp thành công! 🎉

### Kiểu Spotlight Tour (như intro.js):

#### 1. **Lần đầu mở app:**
- User sẽ thấy **màn hình chào mừng** giới thiệu app
- Nhấn "Bắt đầu hướng dẫn" để xem tour
- Tour sẽ **highlight từng element** cụ thể trên màn hình với hiệu ứng spotlight

#### 2. **Tour với 6 bước highlight:**
1. ✅ **Level Selector** - Cách chọn cấp độ (Beginner/Intermediate/Advanced)
2. ✅ **Topic Item** - Cách chọn chủ đề để học
3. ✅ **Tab Tra từ** - Giới thiệu tính năng từ điển
4. ✅ **Tab Học từ vựng** - Giới thiệu tab chính
5. ✅ **Tab Hội thoại** - Luyện hội thoại mẫu
6. ✅ **Tab AI Assistant** - Trợ lý AI

#### 3. **Hiệu ứng Spotlight:**
- ✨ **Background tối** (80% opacity) che toàn màn hình
- 🔦 **Cutout sáng** highlight element đang hướng dẫn
- 💜 **Border tím** quanh element được highlight
- 💬 **Tooltip card** xuất hiện phía trên/dưới element
- 🖱️ **Tap anywhere** để next bước tiếp theo

#### 4. **Tương tác:**
- **Tap màn hình tối**: Chuyển sang bước tiếp theo
- **Nút X** (góc phải tooltip): Bỏ qua tour
- **Progress bar**: Hiển thị tiến độ tour
- **"Hoàn thành! 🎉"**: Nút ở bước cuối

### UI/UX Features:

✅ **Spotlight effect** - Làm tối xung quanh, sáng element target  
✅ **Smooth animation** - Fade in/out mượt mà
✅ **Smart tooltip positioning** - Tự động đặt phía trên/dưới element
✅ **Rounded cutout** - Bo góc 12dp cho đẹp
✅ **Purple border** - Viền tím highlight element
✅ **Auto-tracking** - Tự động track vị trí element
✅ **Responsive** - Thích ứng với kích thước màn hình

### Files đã tạo/cập nhật:

```
app/src/main/java/com/uilover/project247/
├── utils/
│   └── ProductTourManager.kt           # Quản lý trạng thái tour
├── data/models/
│   └── InAppTourModels.kt              # Data model
├── DashboardActivity/
│   ├── components/
│   │   ├── InAppTourOverlay.kt         # ⭐ Spotlight overlay + tooltip
│   │   ├── BottomNavigationBarStub.kt  # Track vị trí tabs
│   │   └── TopicItem.kt                # Support modifier
│   ├── screens/
│   │   └── MainScreen.kt               # Track vị trí elements
│   └── MainActivity.kt                 # Trigger tour
```

### Cách hoạt động kỹ thuật:

#### 1. **Tracking element positions:**
```kotlin
.onGloballyPositioned { coordinates ->
    val pos = coordinates.positionInRoot()
    updateTourTarget(
        "element_id",
        Rect(left, top, right, bottom)
    )
}
```

#### 2. **Drawing spotlight:**
```kotlin
Canvas {
    // Draw black overlay
    drawRect(Color.Black.copy(alpha = 0.8f))
    
    // Cut out spotlight area
    drawPath(
        path = roundRectPath,
        color = Color.Transparent,
        blendMode = BlendMode.Clear
    )
    
    // Draw border
    drawRoundRect(color = Purple, style = Stroke)
}
```

#### 3. **Smart tooltip positioning:**
```kotlin
val tooltipY = if (elementBottom + tooltipHeight < screenHeight) {
    elementBottom + 16.dp // Below
} else {
    elementTop - tooltipHeight - 16.dp // Above
}
```

### Thêm target mới:

#### 1. Trong composable cần highlight:
```kotlin
MyComponent(
    modifier = Modifier.onGloballyPositioned { coordinates ->
        val pos = coordinates.positionInRoot()
        updateTourTarget(
            "my_element_id",
            Rect(pos.x, pos.y, pos.x + width, pos.y + height)
        )
    }
)
```

#### 2. Thêm step trong `InAppTourOverlay.kt`:
```kotlin
InAppTourStep(
    title = "Tính năng mới",
    description = "Mô tả chi tiết về tính năng",
    targetId = "my_element_id",
    emoji = "✨"
)
```

### Tùy chỉnh màu sắc:

```kotlin
// Overlay
Color.Black.copy(alpha = 0.8f) // Độ tối

// Border highlight
Color(0xFF6200EA) // Tím chủ đạo

// Tooltip background
Color.White

// Padding around spotlight
8.dp // Khoảng cách viền
```

### Testing:

1. **Xóa app data**: Settings → Apps → Clear Data
2. Mở app lần đầu
3. Nhấn "Bắt đầu hướng dẫn"
4. Quan sát:
   - ✅ Level selector được highlight
   - ✅ Topic item đầu tiên được highlight
   - ✅ Các tab bottom được highlight lần lượt
5. Tap màn hình tối để next
6. Hoặc nhấn X để skip

### Lợi ích của Spotlight Tour:

✅ **Tương tác trực quan** - User nhìn thấy đúng element cần dùng  
✅ **Không gây nhiễu** - Chỉ highlight 1 element tại 1 thời điểm  
✅ **Học nhanh hơn** - Hiểu ngay vị trí và cách dùng  
✅ **Chuyên nghiệp** - Giống Uber, Airbnb, Google apps  
✅ **Giữ chân user** - Giảm confusion, tăng engagement

### So sánh với tour cũ:

| Feature | Tour cũ | Tour mới (Spotlight) |
|---------|---------|---------------------|
| Hiển thị | Card giữa màn hình | Highlight element |
| Tương tác | Đọc mô tả | Thấy element thật |
| Animation | Fade in/out card | Spotlight + tooltip |
| UX | Passive reading | Active discovery |
| Retention | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## 🎯 Kết quả

User lần đầu mở app sẽ được **hướng dẫn trực quan** từng tính năng với **spotlight effect**, giúp học cách dùng app nhanh chóng và hiệu quả!

### Demo Flow:

```
1. Mở app → Màn chào mừng
2. "Bắt đầu hướng dẫn" → Màn tối + Level selector sáng + tooltip
3. Tap màn hình → Topic item sáng + tooltip
4. Tap → Tab "Tra từ" sáng + tooltip
5. Tap → Tab "Học từ vựng" sáng + tooltip
6. Tap → Tab "Hội thoại" sáng + tooltip  
7. Tap → Tab "AI Assistant" sáng + tooltip
8. "Hoàn thành! 🎉" → Bắt đầu sử dụng app
```

