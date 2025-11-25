# AI Study Assistant - Setup Guide

## 🤖 Tính năng AI đã tích hợp

### 1. **Phân tích tiến trình học tập**
- Đánh giá tổng quan (điểm 0-100)
- Phân tích điểm mạnh/yếu
- Lời khuyên cá nhân hóa
- Thông điệp động viên

### 2. **Gợi ý ôn tập thông minh**
- Phát hiện topic cần ôn (>7 ngày không học)
- Topic có accuracy thấp (<70%)
- Sắp xếp theo độ ưu tiên
- Giải thích lý do cần ôn

### 3. **Tạo quiz cá nhân hóa** (Sẵn sàng, chưa UI)
- Dựa vào accuracy để điều chỉnh độ khó
- Nhiều dạng câu hỏi: nghĩa, ngữ cảnh, từ đồng nghĩa
- Giải thích chi tiết

---

## 🔑 Cách lấy Gemini API Key

### Bước 1: Truy cập Google AI Studio
1. Vào https://makersuite.google.com/app/apikey
2. Đăng nhập bằng Google account

### Bước 2: Tạo API Key
1. Click **"Create API Key"**
2. Chọn project hoặc tạo mới
3. Copy API key

### Bước 3: Thêm vào app
Mở file: `app/src/main/java/com/uilover/project247/data/ai/AIStudyAssistant.kt`

Dòng 31, thay thế:
```kotlin
private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
```

Thành:
```kotlin
private val apiKey = "AIzaSy..." // API key của bạn
```

### Bước 4: Build lại app
```bash
./gradlew clean build
```

---

## 📱 Cách sử dụng

### 1. Truy cập AI Assistant
- Mở app → Chọn tab **"MochiHub"** (biểu tượng cuối cùng)

### 2. Tab "Phân tích"
- Xem điểm tổng quan
- Đọc điểm mạnh/yếu
- Nhận lời khuyên từ AI
- Bấm **"Phân tích lại"** để cập nhật

### 3. Tab "Gợi ý ôn tập"
- Xem danh sách topic cần ôn
- Sắp xếp theo độ ưu tiên (1-5)
- Click vào topic → Mở màn hình ôn tập
- Bấm **"Làm mới gợi ý"** để cập nhật

---

## 🛠️ Kiến trúc Code

### Files đã tạo:

```
app/src/main/java/com/uilover/project247/
├── data/ai/
│   └── AIStudyAssistant.kt         # Core AI logic
├── AIAssistantActivity/Model/
│   └── AIAssistantViewModel.kt     # ViewModel
└── DashboardActivity/components/
    └── AIAssistantScreenContent.kt # UI
```

### AIStudyAssistant.kt
**3 Functions chính:**
1. `analyzeStudyProgress()` → `StudyAnalysis`
2. `getReviewRecommendations()` → `List<WordRecommendation>`
3. `generatePersonalizedQuiz()` → `List<AIQuizQuestion>` (chưa dùng UI)

**Data models:**
- `StudyAnalysis`: Kết quả phân tích
- `WordRecommendation`: Gợi ý ôn tập
- `AIQuizQuestion`: Câu hỏi quiz AI

### AIAssistantViewModel.kt
**State management:**
- `AIAssistantUiState`: isLoading, analysis, recommendations, error
- `AITab`: ANALYSIS | RECOMMENDATIONS
- Auto load khi switch tab

### AIAssistantScreenContent.kt
**UI Components:**
- `AnalysisContent`: Hiển thị phân tích
- `RecommendationsContent`: Hiển thị gợi ý
- `RecommendationCard`: Card cho mỗi gợi ý

---

## 🎨 Thiết kế UI

### Màu sắc:
- **Xuất sắc** (≥80%): Xanh lá `#4CAF50`
- **Tốt** (≥60%): Cam `#FF9800`
- **Cần cố gắng** (<60%): Đỏ `#D32F2F`

### Card types:
- **Điểm tổng quan**: Circle score với màu động
- **Điểm mạnh**: Background xanh nhạt
- **Cần cải thiện**: Background cam nhạt
- **Lời khuyên**: Background xanh dương nhạt
- **Động viên**: Background tím nhạt

---

## 🔮 Tính năng mở rộng (Tương lai)

### 1. AI Quiz trong app
Đã có backend (`generatePersonalizedQuiz`), cần:
- Tạo `AIQuizActivity`
- UI hiển thị câu hỏi
- Lưu kết quả quiz

### 2. AI Chatbot
- Chat thực tế với AI
- Sửa lỗi ngữ pháp
- Tạo hội thoại theo topic

### 3. Voice Analysis
- Speech-to-Text
- Đánh giá phát âm
- So sánh với native speaker

### 4. Smart Notifications
- Nhắc nhở ôn tập
- Gợi ý thời gian tối ưu
- Streak tracking

---

## 📊 Prompt Engineering

### Nguyên tắc prompts:
1. **Rõ ràng**: Mô tả chính xác data + yêu cầu
2. **Structured output**: Yêu cầu JSON để parse dễ
3. **Vietnamese**: Output bằng tiếng Việt
4. **Context**: Cung cấp đủ thông tin lịch sử học

### Ví dụ prompt tốt:
```
You are an AI English learning coach.
Analyze this student's data...
[Data chi tiết]
Return JSON format:
{
  "field": "value"
}
Only return valid JSON, no other text.
```

---

## ⚙️ Tối ưu Performance

### Caching:
- ViewModel cache analysis/recommendations
- Chỉ reload khi user bấm "Refresh"
- `remember()` trong Composable

### Error Handling:
- Try-catch với fallback values
- Show error message thân thiện
- Retry button

### API Limits:
- Gemini Free: 60 requests/minute
- Cache kết quả để tránh spam
- Debounce user actions

---

## 🐛 Troubleshooting

### Lỗi: "API key not valid"
→ Kiểm tra lại API key trong `AIStudyAssistant.kt`

### Lỗi: "Failed to parse response"
→ Gemini trả về format không đúng
→ Check logs: `Logcat → AIStudyAssistant`

### Không có gợi ý
→ Cần học ít nhất 1 topic
→ Đợi 7 ngày để có topic "cần ôn"

### Loading mãi
→ Kiểm tra internet
→ Check API quota limits

---

## 📝 Testing Checklist

- [ ] API key đã thêm
- [ ] Build thành công
- [ ] Tab MochiHub hiện đúng
- [ ] Phân tích load được (có data)
- [ ] Gợi ý ôn tập hoạt động
- [ ] Click vào topic → mở Review
- [ ] Refresh button hoạt động
- [ ] Error handling đúng

---

## 🎯 Clean Code Practices

### Separation of Concerns:
- **AIStudyAssistant**: Business logic, API calls
- **ViewModel**: State management, UI logic
- **Composable**: Pure UI, no logic

### MVVM Pattern:
```
View (Composable)
  ↓ events
ViewModel (StateFlow)
  ↓ calls
Repository/Service (AIStudyAssistant)
  ↓ calls
Gemini API
```

### Naming Conventions:
- Functions: `loadAnalysis()`, `getReviewRecommendations()`
- Data classes: `StudyAnalysis`, `WordRecommendation`
- UI States: `AIAssistantUiState`
- Composables: `AnalysisContent`, `RecommendationCard`

---

**Tác giả**: GitHub Copilot + Claude  
**Ngày tạo**: November 25, 2025  
**Version**: 1.0
