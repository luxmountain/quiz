# 🤖 AI Study Assistant - Quick Start

## ✨ Tính năng mới

App học từ vựng của bạn giờ đây có **AI Study Assistant** được tích hợp vào tab **MochiHub**!

### 🎯 AI có thể làm gì?

1. **Phân tích tiến trình học tập**
   - Cho điểm tổng quan 0-100
   - Chỉ ra điểm mạnh/yếu
   - Đưa ra lời khuyên cá nhân hóa
   
2. **Gợi ý từ cần ôn tập**
   - Phát hiện topic học lâu rồi (>7 ngày)
   - Topic có độ chính xác thấp (<70%)
   - Sắp xếp theo độ ưu tiên

3. **Tạo quiz thông minh** (Coming soon)
   - Quiz được tạo dựa trên trình độ
   - Điều chỉnh độ khó tự động

---

## 🚀 Setup nhanh (2 phút)

### Bước 1: Lấy Gemini API Key (FREE)
```
1. Vào: https://makersuite.google.com/app/apikey
2. Đăng nhập Google
3. Click "Create API Key"
4. Copy key
```

### Bước 2: Thêm API Key vào app
Mở file: `app/src/main/java/com/uilover/project247/data/ai/AIStudyAssistant.kt`

**Dòng 31**, sửa:
```kotlin
private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
```

Thành:
```kotlin
private val apiKey = "AIzaSy..." // API key vừa copy
```

### Bước 3: Build & Run
```bash
./gradlew assembleDebug
```
hoặc bấm **Run** trong Android Studio

---

## 📱 Cách dùng

1. Mở app → Chọn tab **"MochiHub"** (icon cuối)
2. Tab **"Phân tích"**: Xem đánh giá từ AI
3. Tab **"Gợi ý ôn tập"**: Xem topic nên ôn
4. Click vào topic → Ôn tập ngay

---

## 📚 Chi tiết đầy đủ

Xem file **[AI_ASSISTANT_GUIDE.md](./AI_ASSISTANT_GUIDE.md)** để biết:
- Kiến trúc code
- Cách mở rộng tính năng
- Troubleshooting
- Best practices

---

## 🎨 Screenshots

### Tab Phân tích
- Điểm tổng quan với circle progress
- Điểm mạnh (màu xanh)
- Cần cải thiện (màu cam)
- Lời khuyên từ AI
- Thông điệp động viên

### Tab Gợi ý ôn tập
- Danh sách topic với độ ưu tiên
- Lý do nên ôn tập
- Click để mở Review ngay

---

## 🔧 Dependencies đã thêm

```kotlin
// build.gradle.kts (app)
implementation("com.google.ai.client.generativeai:generativeai:0.1.2")
```

---

## 📁 Files mới tạo

```
app/src/main/java/com/uilover/project247/
├── data/ai/
│   └── AIStudyAssistant.kt                    # ✨ AI Core Logic
├── AIAssistantActivity/Model/
│   └── AIAssistantViewModel.kt                # ✨ ViewModel
└── DashboardActivity/components/
    └── AIAssistantScreenContent.kt            # ✨ UI Components

Docs:
├── AI_ASSISTANT_GUIDE.md                      # ✨ Full Guide
├── AI_ASSISTANT_README.md                     # ✨ This file
└── local.properties.template                  # ✨ Config template
```

---

## ⚡ Performance

- **Loading time**: ~2-3 giây (tùy mạng)
- **Cache**: Kết quả được cache, chỉ reload khi user bấm Refresh
- **API Limits**: 60 requests/phút (Free tier)

---

## 🐛 Troubleshooting

### Không load được analysis?
- ✅ Check API key đã đúng chưa
- ✅ Kiểm tra internet
- ✅ Xem Logcat: `AIStudyAssistant`

### Không có gợi ý ôn tập?
- ✅ Cần học ít nhất 1 topic trước
- ✅ Đợi 7 ngày hoặc học với accuracy thấp

### Build error?
```bash
./gradlew clean
./gradlew build
```

---

## 🎯 Roadmap

- [x] Phân tích tiến trình
- [x] Gợi ý ôn tập
- [ ] AI Quiz Generator UI
- [ ] AI Chatbot
- [ ] Voice pronunciation check
- [ ] Smart notifications

---

**Happy Learning with AI! 🚀**
