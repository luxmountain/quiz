# Firebase Realtime Database Schema Documentation

## 📋 Tổng quan

File này mô tả chi tiết schema của Firebase Realtime Database cho ứng dụng học từ vựng tiếng Anh.

## 🗂️ Cấu trúc Database

```
firebase-database/
├── topics/
│   └── {topicId}/
├── flashcards/
│   └── {flashcardId}/
├── conversations/
│   └── {conversationId}/
├── userProgress/
│   └── {userId}/
└── settings/
    └── app/
```

---

## 📚 Topics Schema

**Path**: `/topics/{topicId}`

Chứa thông tin về các chủ đề học tập.

### Fields:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | String | ✅ | Unique identifier (phải trùng với topicId) |
| `name` | String | ✅ | Tên chủ đề (tiếng Anh) |
| `nameVi` | String | ✅ | Tên chủ đề (tiếng Việt) |
| `description` | String | ✅ | Mô tả chủ đề (tiếng Anh) |
| `descriptionVi` | String | ✅ | Mô tả chủ đề (tiếng Việt) |
| `imageUrl` | String | ✅ | URL hình ảnh đại diện (phải bắt đầu với http/https) |
| `order` | Number | ✅ | Thứ tự hiển thị (>= 0) |
| `totalWords` | Number | ✅ | Tổng số từ vựng trong chủ đề (>= 0) |
| `createdAt` | Number | ✅ | Timestamp tạo (milliseconds) |
| `updatedAt` | Number | ✅ | Timestamp cập nhật cuối (milliseconds) |

### Example:

```json
{
  "topic_001": {
    "id": "topic_001",
    "name": "Daily Routine",
    "nameVi": "Hoạt động hàng ngày",
    "description": "Common words used in daily activities",
    "descriptionVi": "Từ vựng thường dùng trong các hoạt động hàng ngày",
    "imageUrl": "https://images.unsplash.com/photo-1495364141860-b0d03eccd065?w=800",
    "order": 1,
    "totalWords": 10,
    "createdAt": 1699488000000,
    "updatedAt": 1699488000000
  }
}
```

---

## 🎴 Flashcards Schema

**Path**: `/flashcards/{flashcardId}`

Chứa thông tin về từ vựng (flashcard).

### Fields:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `id` | String | ✅ | Phải trùng với flashcardId | Unique identifier |
| `topicId` | String | ✅ | Phải tồn tại trong /topics | ID của chủ đề |
| `word` | String | ✅ | Length > 0 | Từ vựng tiếng Anh |
| `pronunciation` | String | ✅ | - | Phiên âm IPA |
| `meaning` | String | ✅ | Length > 0 | Nghĩa tiếng Việt |
| `wordType` | String | ✅ | Enum: noun, verb, adjective, adverb, preposition, conjunction | Loại từ (tiếng Anh) |
| `wordTypeVi` | String | ✅ | - | Loại từ (tiếng Việt) |
| `imageUrl` | String | ✅ | URL format | Hình ảnh minh họa |
| `contextSentence` | String | ✅ | - | Câu ví dụ có chứa từ (HTML: `<b><u>word</u></b>`) |
| `contextSentenceVi` | String | ✅ | - | Câu ví dụ tiếng Việt |
| `example` | String | ✅ | - | Câu ví dụ bổ sung |
| `exampleVi` | String | ✅ | - | Câu ví dụ bổ sung tiếng Việt |
| `order` | Number | ✅ | >= 0 | Thứ tự trong chủ đề |
| `difficulty` | String | ✅ | Enum: easy, medium, hard | Độ khó |
| `createdAt` | Number | ✅ | - | Timestamp tạo |

### Word Types:

- `noun` / `danh từ`
- `verb` / `động từ`
- `adjective` / `tính từ`
- `adverb` / `trạng từ`
- `preposition` / `giới từ`
- `conjunction` / `liên từ`

### Difficulty Levels:

- `easy`: Dễ
- `medium`: Trung bình
- `hard`: Khó

### Example:

```json
{
  "flashcard_001": {
    "id": "flashcard_001",
    "topicId": "topic_001",
    "word": "breakfast",
    "pronunciation": "/ˈbrek.fəst/",
    "meaning": "bữa sáng",
    "wordType": "noun",
    "wordTypeVi": "danh từ",
    "imageUrl": "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=800",
    "contextSentence": "I usually have <b><u>breakfast</u></b> at 7 AM every morning.",
    "contextSentenceVi": "Tôi thường ăn sáng lúc 7 giờ sáng mỗi ngày.",
    "example": "A healthy breakfast is important for starting your day.",
    "exampleVi": "Một bữa sáng lành mạnh rất quan trọng để bắt đầu ngày mới.",
    "order": 1,
    "difficulty": "easy",
    "createdAt": 1699488000000
  }
}
```

---

## 💬 Conversations Schema

**Path**: `/conversations/{conversationId}`

Chứa thông tin về các bài hội thoại học từ vựng.

### Fields:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `id` | String | ✅ | Phải trùng với conversationId | Unique identifier |
| `topicId` | String | ✅ | Phải tồn tại trong /topics | ID chủ đề |
| `flashcardId` | String | ✅ | Phải tồn tại trong /flashcards | ID flashcard liên quan |
| `title` | String | ✅ | Length > 0 | Tiêu đề (tiếng Anh) |
| `titleVi` | String | ✅ | Length > 0 | Tiêu đề (tiếng Việt) |
| `imageUrl` | String | ✅ | URL format | Hình ảnh minh họa |
| `contextDescription` | String | ✅ | - | Mô tả ngữ cảnh (tiếng Anh) |
| `contextDescriptionVi` | String | ✅ | - | Mô tả ngữ cảnh (tiếng Việt) |
| `dialogue` | Array | ✅ | - | Danh sách câu thoại |
| `targetWord` | String | ✅ | Length > 0 | Từ vựng mục tiêu |
| `question` | String | ✅ | Length > 0 | Câu hỏi (tiếng Anh) |
| `questionVi` | String | ✅ | Length > 0 | Câu hỏi (tiếng Việt) |
| `options` | Array | ✅ | - | Các lựa chọn trả lời |
| `order` | Number | ✅ | >= 0 | Thứ tự |
| `createdAt` | Number | ✅ | - | Timestamp tạo |

### Dialogue Schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `speaker` | String | ✅ | Tên người nói |
| `text` | String | ✅ | Nội dung (tiếng Anh) |
| `textVi` | String | ✅ | Nội dung (tiếng Việt) |
| `order` | Number | ✅ | Thứ tự câu |

### QuizOption Schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | String | ✅ | ID lựa chọn |
| `text` | String | ✅ | Nội dung đáp án |
| `isCorrect` | Boolean | ✅ | Đáp án đúng hay sai |

### Example:

```json
{
  "conversation_001": {
    "id": "conversation_001",
    "topicId": "topic_001",
    "flashcardId": "flashcard_001",
    "title": "Morning Routine",
    "titleVi": "Thói quen buổi sáng",
    "imageUrl": "https://images.unsplash.com/photo-1495364141860-b0d03eccd065?w=800",
    "contextDescription": "Tom is talking to his friend Sarah about his morning habits.",
    "contextDescriptionVi": "Tom đang nói chuyện với bạn Sarah về thói quen buổi sáng của anh ấy.",
    "dialogue": [
      {
        "speaker": "Tom",
        "text": "I always have breakfast before going to work.",
        "textVi": "Tôi luôn ăn sáng trước khi đi làm.",
        "order": 1
      },
      {
        "speaker": "Sarah",
        "text": "That's a good habit! What do you usually eat?",
        "textVi": "Đó là một thói quen tốt! Bạn thường ăn gì?",
        "order": 2
      }
    ],
    "targetWord": "breakfast",
    "question": "What does 'breakfast' mean?",
    "questionVi": "Từ 'breakfast' có nghĩa là gì?",
    "options": [
      {
        "id": "option_a",
        "text": "bữa sáng",
        "isCorrect": true
      },
      {
        "id": "option_b",
        "text": "bữa trưa",
        "isCorrect": false
      }
    ],
    "order": 1,
    "createdAt": 1699488000000
  }
}
```

---

## 👤 User Progress Schema

**Path**: `/userProgress/{userId}`

Lưu trữ tiến độ học tập của người dùng.

### Fields:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `userId` | String | ✅ | Phải trùng với userId | ID người dùng |
| `displayName` | String | ✅ | - | Tên hiển thị |
| `email` | String | ✅ | Email format | Email người dùng |
| `totalPoints` | Number | ✅ | >= 0 | Tổng điểm |
| `level` | Number | ✅ | >= 1 | Cấp độ |
| `streak` | Number | ✅ | >= 0 | Số ngày học liên tục |
| `lastStudyDate` | Number/null | ❌ | - | Ngày học gần nhất |
| `createdAt` | Number | ✅ | - | Timestamp tạo tài khoản |
| `topicProgress` | Map | ❌ | - | Tiến độ theo chủ đề |
| `flashcardResults` | Map | ❌ | - | Kết quả học flashcard |
| `conversationResults` | Map | ❌ | - | Kết quả học conversation |

### TopicProgress Schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `topicId` | String | ✅ | ID chủ đề |
| `completedFlashcards` | Number | ✅ | Số flashcard đã hoàn thành |
| `completedConversations` | Number | ✅ | Số conversation đã hoàn thành |
| `totalFlashcards` | Number | ✅ | Tổng số flashcard |
| `totalConversations` | Number | ✅ | Tổng số conversation |
| `progress` | Number | ✅ | Phần trăm hoàn thành (0-100) |
| `lastStudyDate` | Number/null | ❌ | Ngày học gần nhất |

### FlashcardResult Schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `flashcardId` | String | ✅ | ID flashcard |
| `learned` | Boolean | ✅ | Đã học xong chưa |
| `reviewCount` | Number | ✅ | Số lần ôn tập |
| `lastReviewDate` | Number/null | ❌ | Lần ôn gần nhất |
| `nextReviewDate` | Number/null | ❌ | Lần ôn tiếp theo |
| `confidence` | Number | ✅ | Độ tự tin (0-100) |

### ConversationResult Schema:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `conversationId` | String | ✅ | ID conversation |
| `completed` | Boolean | ✅ | Đã hoàn thành chưa |
| `attempts` | Number | ✅ | Số lần thử |
| `correctAnswers` | Number | ✅ | Số câu trả lời đúng |
| `lastAttemptDate` | Number/null | ❌ | Lần thử gần nhất |

---

## ⚙️ Settings Schema

**Path**: `/settings/app`

Cài đặt ứng dụng.

### Fields:

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `version` | String | ✅ | Format: x.x.x | Phiên bản hiện tại |
| `minSupportedVersion` | String | ✅ | Format: x.x.x | Phiên bản tối thiểu |
| `maintenanceMode` | Boolean | ✅ | - | Chế độ bảo trì |
| `dailyGoal` | Number | ✅ | >= 1 | Mục tiêu hàng ngày |
| `reminderEnabled` | Boolean | ✅ | - | Bật nhắc nhở |
| `reminderTime` | String | ✅ | Format: HH:mm | Giờ nhắc nhở |
| `soundEnabled` | Boolean | ✅ | - | Bật âm thanh |
| `autoPlayAudio` | Boolean | ✅ | - | Tự động phát âm |

---

## 🔒 Firebase Security Rules

File `firebase-rules.json` định nghĩa các quy tắc bảo mật:

- **Topics/Flashcards/Conversations**: Đọc công khai, chỉ admin mới được ghi
- **UserProgress**: Chỉ user sở hữu mới được đọc/ghi
- **Settings**: Đọc công khai, chỉ admin mới được ghi

### Validation Rules:

- URL phải bắt đầu với `http://` hoặc `https://`
- Email phải đúng format
- Version phải theo format `x.y.z`
- Reminder time theo format `HH:mm`
- Foreign keys phải tồn tại (topicId, flashcardId, etc.)

---

## 🔑 Firebase Paths Constants

Sử dụng class `FirebasePaths` trong Kotlin để truy cập:

```kotlin
// Lấy tất cả topics
val topicsRef = database.getReference(FirebasePaths.TOPICS)

// Lấy một topic cụ thể
val topicRef = database.getReference(FirebasePaths.topic("topic_001"))

// Lấy user progress
val userProgressRef = database.getReference(FirebasePaths.userProgress(userId))

// Query flashcards theo topic
val flashcardsRef = database.getReference(FirebasePaths.FLASHCARDS)
    .orderByChild("topicId")
    .equalTo("topic_001")
```

---

## 📝 Data Models

Tất cả data models được định nghĩa trong file:
`app/src/main/java/com/uilover/project247/data/models/FirebaseModels.kt`

Các models chính:
- `Topic`
- `Flashcard`
- `Conversation` (với `DialogueLine` và `QuizOption`)
- `UserProgress` (với `TopicProgress`, `FlashcardResult`, `ConversationResult`)
- `AppSettings`

---

## 🚀 Cách import dữ liệu lên Firebase

1. Vào Firebase Console
2. Chọn Realtime Database
3. Click vào menu (⋮) → Import JSON
4. Chọn file `firebase-data.json`
5. Click Import

---

## 📊 Naming Conventions

- **IDs**: `{entity}_{số thứ tự}` (vd: `topic_001`, `flashcard_001`)
- **Timestamps**: Milliseconds (long)
- **URLs**: Phải có scheme (http/https)
- **Fields**: camelCase
- **Boolean**: `is` prefix không bắt buộc
