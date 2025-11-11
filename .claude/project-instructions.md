# English Learning App - Claude Project Instructions

## Project Identity
**Name**: English Learning App (Quiz)
**Package**: com.uilover.project247
**Target Users**: Vietnamese learners of English
**Platform**: Android (Kotlin + Jetpack Compose)

## Technology Stack
- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose + Material3 (no XML layouts)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Firebase Realtime Database
- **API**: Free Dictionary API, Unsplash Images
- **Networking**: Retrofit 2.9.0 + Gson
- **Image Loading**: Coil 2.5.0
- **Build**: Gradle Kotlin DSL
- **Min SDK**: 24, **Target SDK**: 36

## Project Architecture

### MVVM Pattern (Strict)
```
┌─────────────┐
│   Firebase  │ ← Data Source
│     API     │
└──────┬──────┘
       │
┌──────▼──────┐
│ Repository  │ ← Data Layer
└──────┬──────┘
       │
┌──────▼──────┐
│  ViewModel  │ ← Business Logic
│  (StateFlow)│
└──────┬──────┘
       │
┌──────▼──────┐
│ Composable  │ ← UI Layer
│   Screen    │
└─────────────┘
```

### Directory Structure
```
app/src/main/java/com/uilover/project247/
│
├── data/
│   ├── models/              # Data classes
│   │   ├── FirebaseModels.kt    # Topic, Flashcard, Conversation
│   │   └── DictionaryModels.kt  # API response models
│   ├── repository/          # Data access layer
│   │   └── FirebaseRepository.kt
│   └── api/                 # Retrofit services
│       └── DictionaryApiService.kt
│
├── utils/                   # Shared utilities
│   ├── Helper.kt            # HTML parsing, text formatting
│   └── TextToSpeechManager.kt
│
├── DashboardActivity/       # Main screen (Topic list)
│   ├── MainActivity.kt
│   ├── screens/MainScreen.kt
│   ├── components/
│   │   ├── TopicItem.kt
│   │   ├── BottomNavigationBarStub.kt
│   │   └── DictionaryScreenContent.kt
│   └── Model/MainViewModel.kt
│
├── LearningActivity/        # Flashcard learning
│   ├── LearningActivity.kt
│   ├── LearningScreen.kt
│   ├── components/
│   │   ├── FlashcardView.kt      # 3D flip animation
│   │   ├── WriteWordView.kt
│   │   └── MultipleChoiceView.kt
│   └── Model/
│       ├── LearningViewModel.kt
│       └── LearningUiState.kt
│
├── ReviewActivity/          # Review learned words
│   ├── ReviewActivity.kt
│   └── Model/ReviewViewModel.kt
│
├── DictionaryActivity/      # Dictionary lookup
│   ├── DictionaryActivity.kt
│   ├── screens/DictionaryScreen.kt
│   └── Model/DictionaryViewModel.kt
│
└── ui/theme/               # Theme configuration
    └── Theme.kt
```

## Core Principles

### 1. State Management
**Always use StateFlow pattern**:
```kotlin
class FeatureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
    
    fun updateData() {
        _uiState.update { currentState ->
            currentState.copy(data = newData)
        }
    }
}
```

**UI State Pattern**:
```kotlin
data class FeatureUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val errorMessage: String? = null
)
```

### 2. Composable Guidelines
- Keep functions under 100 lines
- Extract complex UI into separate components
- Use `modifier: Modifier = Modifier` as last parameter
- Collect state with `collectAsState()`

```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = { /* AppBar */ },
        bottomBar = { /* Navigation */ }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content
        }
    }
}
```

### 3. Data Layer
**Repository Pattern**:
```kotlin
class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance(
        "https://english-learning-app-17885-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    
    suspend fun getData(): List<Item> = withContext(Dispatchers.IO) {
        // Firebase query
    }
}
```

**API Service**:
```kotlin
interface ApiService {
    @GET("endpoint")
    suspend fun getData(): Response<List<Item>>
    
    companion object {
        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
```

## Design System

### Color Palette
```kotlin
val PrimaryPurple = Color(0xFF6200EA)  // Main brand color
val LightPurple = Color(0xFFEDE7F6)    // Backgrounds
val DarkPurple = Color(0xFF512DA8)     // Text accent
val Blue = Color(0xFF1976D2)           // Info, badges
val LightBlue = Color(0xFFE3F2FD)      // Synonyms
val Green = Color(0xFF4CAF50)          // Success
val Orange = Color(0xFFFF6F00)         // Origin
val LightOrange = Color(0xFFFFF3E0)    // Origin bg
val Red = Color(0xFFC62828)            // Error, antonyms
val LightRed = Color(0xFFFFEBEE)       // Error bg
val Yellow = Color(0xFFFFF9C4)         // Examples
val Gray = Color(0xFF757575)           // Secondary text
val Background = Color(0xFFF5F5F5)     // Screen bg
```

### Spacing
```kotlin
8.dp   // Small gaps
12.dp  // Card radius
16.dp  // Standard padding
20.dp  // Card internal padding
24.dp  // Large spacing
```

### Component Sizes
```kotlin
48.dp  // Small button
56.dp  // Standard button
72.dp  // Large touch target
20.dp  // Small icon
24.dp  // Standard icon
28.dp  // Medium icon
```

## Key Features

### 1. Flashcard Learning System
**Front Side**:
- Image (200dp height)
- Context sentence with HTML formatting
- Example: `"I usually have <b><u>breakfast</u></b> at 7 AM"`

**Back Side**:
- Word (large, green)
- Pronunciation (IPA, gray)
- Meaning (Vietnamese, black)
- Word type abbreviation: (n), (v), (adj), etc.

**Implementation**:
```kotlin
// Parse HTML content
val annotatedText = parseHtmlToAnnotatedString(contextSentence)

// Get word type abbreviation
val abbrev = getWordTypeAbbreviation("noun") // returns "n"
```

### 2. Dictionary Feature
**API**: Free Dictionary API
- Endpoint: `https://api.dictionaryapi.dev/api/v2/entries/en/{word}`
- Response includes: phonetics, meanings, definitions, examples, synonyms, antonyms, origin

**UI Components**:
- Search bar with search button
- Recent searches (last 10 words)
- Rich result display with colored cards
- Part of speech badges
- Synonym/Antonym cards
- Example cards with icon

### 3. Navigation System
**Bottom Navigation** (5 tabs):
1. "Tra từ" - Dictionary (icon: Search)
2. "Học từ vựng" - Learn (icon: Menu) - **Default**
3. "Ôn tập" - Review (icon: Home)
4. "Hội thoại" - Conversation (icon: Person)
5. "MochiHub" - Hub (icon: Home)

**Navigation Flow**:
```
MainActivity (Topic List)
  ├→ LearningActivity (Flashcards)
  ├→ ReviewActivity (Review)
  └→ DictionaryActivity (Dictionary)
```

### 4. Firebase Integration
**Database URL**: `https://english-learning-app-17885-default-rtdb.asia-southeast1.firebasedatabase.app/`

**Data Structure**:
```json
{
  "topics": {
    "topic1": {
      "id": "topic1",
      "name": "Daily Routine",
      "nameVi": "Hoạt động hàng ngày",
      "description": "Learn daily activities",
      "imageUrl": "https://images.unsplash.com/...",
      "order": 1,
      "totalWords": 10
    }
  },
  "flashcards": {
    "flash1": {
      "id": "flash1",
      "topicId": "topic1",
      "word": "breakfast",
      "pronunciation": "/ˈbrekfəst/",
      "meaning": "bữa sáng",
      "wordType": "noun",
      "wordTypeVi": "danh từ",
      "contextSentence": "I usually have <b><u>breakfast</u></b> at 7 AM",
      "contextSentenceVi": "Tôi thường ăn sáng lúc 7 giờ sáng",
      "imageUrl": "https://..."
    }
  },
  "conversations": { /* ... */ },
  "userProgress": { /* ... */ }
}
```

## Important Conventions

### File Naming
- Activities: `[Feature]Activity.kt`
- Screens: `[Feature]Screen.kt`
- ViewModels: `[Feature]ViewModel.kt`
- UI States: `[Feature]UiState.kt`
- Components: Descriptive names (`FlashcardView.kt`)

### Code Style
- Use `MaterialTheme.colorScheme` when possible
- Always add `contentDescription` to Icons
- Use `Modifier.fillMaxWidth()` for responsive layouts
- Prefer `LazyColumn` over `Column` for lists
- Use `HorizontalDivider()` not deprecated `Divider()`

### Common Pitfalls - AVOID
```kotlin
// ❌ Wrong - Don't use in Row
Row(crossAxisAlignment = Alignment.Start) { }

// ✅ Correct
Row(verticalAlignment = Alignment.Top) { }

// ❌ Wrong - Don't use in Column
Column(crossAxisAlignment = Alignment.Center) { }

// ✅ Correct
Column(horizontalAlignment = Alignment.CenterHorizontally) { }

// ❌ Wrong - Direct mutation
_uiState.value = newState

// ✅ Correct - Use update
_uiState.update { it.copy(data = newData) }
```

### HTML Content Handling
**Always parse HTML before display**:
```kotlin
// ❌ Wrong
Text(text = htmlString)

// ✅ Correct
Text(text = parseHtmlToAnnotatedString(htmlString))
```

Supported tags: `<b>`, `<u>`, `<i>`, `<strike>`

### Vietnamese Context
- **UI Labels**: Always in Vietnamese
- **Content**: English (learning material)
- **Translations**: Vietnamese meanings
- **Examples**: 
  - ✅ "Tra từ" (UI)
  - ✅ "breakfast" (content)
  - ✅ "bữa sáng" (meaning)

## Testing & Debugging

### Build Commands
```bash
# Clean build
./gradlew clean build

# Run on device
./gradlew installDebug
```

### Common Issues
1. **Manifest**: Always add new activities to `AndroidManifest.xml`
2. **Permissions**: Add INTERNET permission for API calls
3. **Parcelable**: Use `@Parcelize` for data passed between activities
4. **State**: Never mutate StateFlow directly, use `.update { }`
5. **HTML**: Always parse HTML content with Helper functions

## Dependencies Reference
```kotlin
// Core Android
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.activity.compose)
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.material3)
implementation(libs.androidx.lifecycle.viewmodel.ktx)

// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-database-ktx")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")

// Material Icons
implementation("androidx.compose.material:material-icons-extended-android:1.6.7")

// ConstraintLayout for Compose
implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
```

## When Providing Assistance

### Always Do:
1. ✅ Follow MVVM architecture strictly
2. ✅ Use Jetpack Compose (never suggest XML)
3. ✅ Implement StateFlow for state management
4. ✅ Parse HTML content when needed
5. ✅ Use Material3 components
6. ✅ Add Vietnamese UI labels
7. ✅ Include error handling
8. ✅ Follow existing code style
9. ✅ Update AndroidManifest when adding activities
10. ✅ Use helper functions from `utils/` package

### Never Do:
1. ❌ Suggest XML layouts
2. ❌ Mutate StateFlow directly
3. ❌ Use deprecated APIs
4. ❌ Mix English and Vietnamese in UI
5. ❌ Forget INTERNET permission
6. ❌ Create duplicate Repository instances
7. ❌ Display raw HTML without parsing
8. ❌ Use wrong alignment properties
9. ❌ Ignore Vietnamese context
10. ❌ Break MVVM pattern

## Development Status

### Completed ✅
- Firebase Realtime Database integration
- Dictionary API with Retrofit
- Flashcard learning with 3D flip animation
- Bottom navigation (5 tabs)
- HTML parsing for rich text
- Recent search history
- Topic list display
- Material3 UI with color scheme

### In Progress 🔄
- Text-to-Speech integration
- Conversation practice feature
- Progress tracking UI
- User authentication

### Planned 📋
- Offline mode
- Spaced repetition algorithm
- Achievement system
- Social features
- Multiple language support

## Quick Reference

### Create New Feature
1. Create `[Feature]Activity.kt`
2. Create `[Feature]ViewModel.kt` with StateFlow
3. Create `[Feature]Screen.kt` composable
4. Create `[Feature]UiState.kt` data class
5. Add to `AndroidManifest.xml`
6. Add navigation from parent activity

### Add API Integration
1. Create model in `data/models/`
2. Create service in `data/api/`
3. Create repository method
4. Call from ViewModel
5. Update UI state
6. Handle errors

### Update Existing UI
1. Locate composable in `components/` or `screens/`
2. Follow Material3 guidelines
3. Use existing color scheme
4. Keep components small
5. Extract reusable parts

---

**Remember**: This is a learning app for Vietnamese users. Keep UI in Vietnamese, content in English, and always follow MVVM architecture with Jetpack Compose.
