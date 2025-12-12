# Tài liệu chức năng: Tra từ điển (Dictionary Lookup)

## 0) Tổng quan

Chức năng tra từ điển sử dụng **Free Dictionary API** để tra cứu từ vựng tiếng Anh trong thời gian thực. Hệ thống tự động lưu lịch sử tra cứu và hỗ trợ dropdown gợi ý từ lịch sử khi người dùng gõ.

- **Nguồn dữ liệu**: Free Dictionary API (https://dictionaryapi.dev/)
- **Lưu lịch sử**: SharedPreferences (cục bộ, tối đa 50 từ)
- **Tính năng nổi bật**:
  - Autocomplete từ lịch sử tra cứu
  - Hiển thị đầy đủ phiên âm, nghĩa, ví dụ, synonyms/antonyms
  - Audio phát âm (nếu API cung cấp)
  - Lọc lịch sử thông minh theo từ khóa

---

## a) Luồng hoạt động + ràng buộc nghiệp vụ

### Luồng (activity diagram – Mermaid)

```mermaid
flowchart TD
    A[User mở DictionaryActivity] --> B[DictionaryViewModel.loadSearchHistory()]
    B --> C[Hiển thị SearchBar + Empty State]

    C --> D{User nhập từ?}
    D -- Focus vào input --> E[Hiển thị dropdown lịch sử]
    E --> F{Chọn từ lịch sử?}
    F -- Có --> G[Auto-fill searchQuery]
    F -- Không --> D

    D -- Nhấn Search --> H[searchWord()]
    G --> H

    H --> I[Ẩn dropdown]
    I --> J[Gọi API dictionaryapi.dev]
    J --> K{Response?}

    K -- 200 OK --> L[Parse DictionaryEntry]
    L --> M[Lưu vào SearchHistory]
    M --> N[Hiển thị kết quả: word, phonetic, meanings]

    K -- 404 --> O[Hiển thị ErrorSection: Không tìm thấy từ]
    K -- Network Error --> P[Hiển thị ErrorSection: Lỗi kết nối]

    N --> Q[User xem chi tiết nghĩa, ví dụ, synonyms]
    Q --> R{Phát âm audio?}
    R -- Có --> S[Play audio từ URL]
    R -- Không --> T[Tra từ tiếp?]
    O --> T
    P --> T
    T -- Có --> D
```

### Ràng buộc / Business rules

- **Input validation**: Từ tra cứu phải có ít nhất 1 ký tự (không chấp nhận chuỗi rỗng).
- **Lịch sử tự động**:
  - Chỉ lưu từ tra cứu thành công (HTTP 200).
  - Nếu từ đã tồn tại trong lịch sử → cập nhật timestamp và đưa lên đầu danh sách.
  - Giới hạn 50 từ, xóa từ cũ nhất khi vượt giới hạn.
- **Dropdown autocomplete**:
  - Chỉ hiển thị khi input có focus.
  - Lọc real-time: hiển thị từ có chứa searchQuery (case-insensitive).
  - Khi chọn từ lịch sử → tự động search.
- **Audio**: Nút phát âm chỉ hiển thị khi API trả về URL audio hợp lệ.
- **Error handling**:
  - 404: "Không tìm thấy từ 'xxx'. Vui lòng kiểm tra lại."
  - Network error: "Lỗi kết nối: [message]"

---

## b) Thiết kế UI/UX

### Màn hình chính (DictionaryScreen)

#### 1) TopAppBar

- Tiêu đề: "Tra từ điển"
- NavigationIcon: Nút "Quay lại" (ArrowBack)

#### 2) SearchBar (Floating overlay)

- OutlinedTextField với rounded corners (12dp)
- LeadingIcon: Search icon
- TrailingIcon: Close icon (khi có text)
- Placeholder: "Nhập từ cần tra..."
- Keyboard Action: ImeAction.Search
- onFocusChange: Hiển thị dropdown khi focus=true

#### 3) Dropdown lịch sử (SearchHistoryDropdown)

- Card nổi (shadow 8dp), hiển thị dưới SearchBar
- Mỗi item hiển thị:
  - Icon Search (purple)
  - **word** (bold, large)
  - **partOfSpeech** (italic, right-aligned)
  - **phonetic** (gray, italic)
  - **meaning** (truncated 1 line)
- HorizontalDivider giữa các item
- Giới hạn chiều cao: 300dp

#### 4) Content area

- **Empty State**: Icon Search lớn + text "Nhập từ để bắt đầu tra cứu"
- **Loading**: CircularProgressIndicator (center)
- **Error**: Card đỏ nhạt (0xFFFFEBEE) với message + nút Close
- **Results**: LazyColumn hiển thị DictionaryEntry cards

#### 5) DictionaryEntryCard

- Card trắng, elevation 2dp, padding 16dp
- **Header**:
  - word (headlineMedium, bold, primary color)
  - phonetic (bodyLarge, italic, gray)
  - Audio button (FilledTonalIconButton với PlayArrow icon)
- **Meanings**: Mỗi meaning hiển thị:
  - partOfSpeech (titleMedium, bold, secondary color)
  - Definitions list (numbered):
    - definition (bodyMedium, black)
    - example (bodyMedium, italic, gray, trong quote block)
  - Synonyms/Antonyms (chip-style với icon)
- **Origin** (nếu có): Card xám nhạt với icon Book

### Color scheme

- Primary: Purple 0xFF6200EA
- Background: 0xFFF5F5F5 (light gray)
- Card: White
- Error: Red 0xFFC62828

---

## c) Giải pháp kỹ thuật (MVVM + Retrofit) + điểm mới + thách thức

### Kiến trúc

#### 1) Data Layer

- **DictionaryApiService** (Retrofit):
  - Base URL: `https://api.dictionaryapi.dev/`
  - Endpoint: `GET /api/v2/entries/en/{word}`
  - Return: `Response<List<DictionaryEntry>>`
- **SearchHistoryManager** (SharedPreferences):
  - Prefs name: `dictionary_history`
  - Key: `search_history`
  - Lưu trữ JSON với Gson
  - Methods: `saveToHistory()`, `getRecentHistory()`, `clearHistory()`
- **Models**:
  - `DictionaryEntry`: word, phonetic, phonetics[], meanings[], origin
  - `Meaning`: partOfSpeech, definitions[], synonyms[], antonyms[]
  - `Definition`: definition, example, synonyms[], antonyms[]
  - `SearchHistoryItem`: word, phonetic, meaning, partOfSpeech, timestamp

#### 2) ViewModel Layer

- **DictionaryViewModel** (AndroidViewModel):
  - StateFlow: `_uiState` (private) → `uiState` (public)
  - `DictionaryUiState`:
    - isLoading, searchQuery, entries, errorMessage
    - recentSearches, isInputFocused
    - **computed**: `filteredRecentSearches` (lọc theo searchQuery)
  - Methods:
    - `updateSearchQuery()`: Cập nhật searchQuery trong state
    - `searchWord()`: Gọi API, xử lý response, lưu lịch sử
    - `selectRecentSearch()`: Fill query + tự động search
    - `updateInputFocus()`: Toggle dropdown

#### 3) UI Layer

- **DictionaryActivity**: ComponentActivity với ViewModelFactory
- **DictionaryScreen**: Scaffold + Column + Box overlay
- **SearchBarSection**: OutlinedTextField với keyboard actions
- **SearchHistoryDropdown**: Card + Column + clickable items
- **DictionaryResultsSection**: LazyColumn với DictionaryEntryCard
- **MeaningSection**: Hiển thị định nghĩa theo từ loại

### Luồng dữ liệu

```
User input → ViewModel.searchWord()
  → DictionaryApiService.searchWord()
  → API response
  → Parse DictionaryEntry
  → SearchHistoryManager.saveToHistory()
  → Update _uiState.entries
  → UI recompose → Display results
```

### Điểm mới / novelty

- **Smart autocomplete từ lịch sử**:
  - Computed property `filteredRecentSearches` tự động lọc trong StateFlow.
  - UX tốt hơn so với dropdown tĩnh: user thấy ngay gợi ý liên quan khi gõ.
- **Rich history items**:
  - Lưu không chỉ word mà cả phonetic, meaning, partOfSpeech.
  - Dropdown preview chi tiết, giúp user nhớ ngữ cảnh.
- **Zero-library spotlight UI**:
  - Dropdown được implement thuần Compose với Box overlay + zIndex.
  - Không phụ thuộc thư viện autocomplete ngoài.

### Thách thức khi triển khai

#### 1) Quản lý focus state cho dropdown

- **Vấn đề**: OutlinedTextField mất focus khi click vào dropdown item → dropdown biến mất trước khi onItemClick trigger.
- **Giải pháp**:
  - Dùng `onFocusChanged` để track focus state vào ViewModel.
  - `selectRecentSearch()` tự động đặt `isInputFocused=false` để ẩn dropdown sau khi chọn.

#### 2) Z-index overlay của SearchBar + Dropdown

- **Vấn đề**: Content (LazyColumn) đè lên dropdown khi scroll.
- **Giải pháp**:
  - Tách SearchBar + Dropdown ra khỏi Column chính.
  - Đặt trong Box riêng với padding top cho LazyColumn (80dp).

#### 3) Parse API response không đồng nhất

- **Vấn đề**: API đôi khi trả về `phonetic` ở top-level, đôi khi chỉ trong `phonetics[]`.
- **Giải pháp**:
  - Ưu tiên `entry.phonetic`, fallback sang `entry.phonetics.firstOrNull()?.text`.

#### 4) Network error handling

- **Vấn đề**: Retrofit throw exception khi network fail, cần catch và map sang UI message.
- **Giải pháp**:
  - Try-catch trong viewModelScope.launch.
  - Set `errorMessage` trong state, UI render ErrorSection.

#### 5) SharedPreferences JSON parsing

- **Vấn đề**: Data class thay đổi → JSON cũ không parse được.
- **Giải pháp**:
  - Try-catch trong `getHistory()`, return emptyList() khi lỗi.

---

## d) Hướng phát triển tương lai

- **Phát âm audio**: Tích hợp MediaPlayer/ExoPlayer để play audio URL từ API.
- **Text-to-Speech (TTS)**: Fallback sang Android TTS khi API không có audio.
- **Offline mode**:
  - Cache kết quả tra cứu vào Room Database.
  - Tra từ offline khi không có mạng.
- **Favorites**:
  - Lưu danh sách từ yêu thích.
  - Thêm star icon vào DictionaryEntryCard.
- **Flashcard integration**:
  - Nút "Thêm vào flashcard" để tạo flashcard từ kết quả tra từ.
  - Sync với Firebase flashcards.
- **Vietnamese translation**:
  - Tích hợp Google Translate API để dịch sang tiếng Việt.
- **Word of the day**:
  - Random từ phổ biến mỗi ngày.
- **Search suggestions**:
  - Gọi API autocomplete (https://api.datamuse.com/) khi gõ.

---

## Phụ lục: File liên quan

- `app/src/main/java/com/uilover/project247/DictionaryActivity/DictionaryActivity.kt`
- `app/src/main/java/com/uilover/project247/DictionaryActivity/Model/DictionaryViewModel.kt`
- `app/src/main/java/com/uilover/project247/DictionaryActivity/screens/DictionaryScreen.kt`
- `app/src/main/java/com/uilover/project247/data/models/DictionaryModels.kt`
- `app/src/main/java/com/uilover/project247/data/api/DictionaryApiService.kt`
- `app/src/main/java/com/uilover/project247/data/repository/SearchHistoryManager.kt`

---

## Dependencies

```kotlin
// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Compose UI
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.activity.compose)
```

## Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
