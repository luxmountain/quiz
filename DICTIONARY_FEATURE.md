# Hướng dẫn sử dụng chức năng Tra từ điển

## Tổng quan

Chức năng tra từ điển sử dụng **Free Dictionary API** để tra cứu từ vựng tiếng Anh, hiển thị:
- Phiên âm (phonetics)
- Phát âm (audio - nếu có)
- Các nghĩa của từ (meanings) theo từ loại
- Định nghĩa và ví dụ (definitions & examples)
- Từ đồng nghĩa (synonyms)
- Từ trái nghĩa (antonyms)
- Nguồn gốc từ (origin)

## Các file đã tạo

### 1. Data Models
📁 `app/src/main/java/com/uilover/project247/data/models/DictionaryModels.kt`
- `DictionaryEntry`: Response từ API
- `Phonetic`: Phiên âm và audio
- `Meaning`: Nghĩa theo từ loại
- `Definition`: Định nghĩa và ví dụ
- `DictionaryUiState`: UI state cho màn hình

### 2. API Service
📁 `app/src/main/java/com/uilover/project247/data/api/DictionaryApiService.kt`
- Retrofit service để gọi Free Dictionary API
- Endpoint: `https://api.dictionaryapi.dev/api/v2/entries/en/{word}`

### 3. ViewModel
📁 `app/src/main/java/com/uilover/project247/DictionaryActivity/Model/DictionaryViewModel.kt`
- Quản lý state tra từ
- Gọi API và xử lý response
- Lưu lịch sử tìm kiếm (recent searches)

### 4. Activity & Screen
📁 `app/src/main/java/com/uilover/project247/DictionaryActivity/DictionaryActivity.kt`
📁 `app/src/main/java/com/uilover/project247/DictionaryActivity/screens/DictionaryScreen.kt`
- UI Compose cho màn hình tra từ
- Search bar với keyboard action
- Hiển thị kết quả tra từ đầy đủ
- Lịch sử tìm kiếm

## Cách sử dụng

### Từ ứng dụng:
1. Mở app → Nhấn vào tab **"Tra từ"** ở bottom navigation
2. Nhập từ cần tra vào ô tìm kiếm
3. Nhấn nút Search trên bàn phím hoặc icon tìm kiếm
4. Xem kết quả chi tiết bao gồm:
   - Từ và phiên âm
   - Các nghĩa theo từ loại (noun, verb, adjective...)
   - Định nghĩa chi tiết
   - Ví dụ sử dụng
   - Từ đồng nghĩa / trái nghĩa
   - Nguồn gốc từ (nếu có)

### Tính năng:
- ✅ Tìm kiếm từ vựng tiếng Anh
- ✅ Hiển thị phiên âm
- ✅ Phát âm thanh (audio có sẵn từ API)
- ✅ Hiển thị nhiều nghĩa theo từ loại
- ✅ Từ đồng nghĩa / trái nghĩa
- ✅ Lịch sử tìm kiếm (10 từ gần nhất)
- ✅ Error handling (không tìm thấy từ, lỗi kết nối)

## Dependencies đã thêm

Đã cập nhật `app/build.gradle.kts`:
```kotlin
// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

## Permissions

Đã thêm vào `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Ví dụ API Response

Khi tìm từ "hello", API trả về:
```json
[
  {
    "word": "hello",
    "phonetic": "/həˈloʊ/",
    "phonetics": [
      {
        "text": "/həˈloʊ/",
        "audio": "https://api.dictionaryapi.dev/media/pronunciations/en/hello-au.mp3"
      }
    ],
    "meanings": [
      {
        "partOfSpeech": "noun",
        "definitions": [
          {
            "definition": "A greeting (salutation) said when meeting someone...",
            "example": "She said hello as she passed by.",
            "synonyms": ["greeting", "hi"]
          }
        ]
      }
    ]
  }
]
```

## Build & Run

1. Sync Gradle dependencies:
```bash
./gradlew clean build
```

2. Chạy ứng dụng trên emulator hoặc thiết bị thực

3. Nhấn tab "Tra từ" để mở Dictionary Activity

## TODO - Tính năng nâng cao (optional)

- [ ] Lưu danh sách từ yêu thích (Favorites)
- [ ] Tích hợp Text-to-Speech để phát âm từ local
- [ ] Lưu cache kết quả tra từ
- [ ] Tra từ offline với database local
- [ ] Thêm gợi ý từ khi gõ (autocomplete)
- [ ] Chia sẻ định nghĩa từ
- [ ] Thêm từ vào flashcard để học

## API Documentation

Free Dictionary API: https://dictionaryapi.dev/
- Miễn phí, không cần API key
- Hỗ trợ tiếng Anh
- Rate limit: Không giới hạn (best effort)
