# Android English Learning App - AI Assistant Instructions

## Project Context
English vocabulary learning mobile app for Vietnamese users.
**Package**: `com.uilover.project247`
**Architecture**: MVVM + Jetpack Compose + Firebase

## Core Technologies
- Kotlin 2.0 + Jetpack Compose (Material3)
- Firebase Realtime Database
- Retrofit 2.9.0 (Dictionary API)
- Min SDK 24, Target SDK 36

## Project Structure
```
app/src/main/java/com/uilover/project247/
├── data/ (models, repository, api)
├── utils/ (Helper.kt, TextToSpeechManager.kt)
├── DashboardActivity/ (Main screen - Topic list)
├── LearningActivity/ (Flashcard learning)
├── ReviewActivity/ (Review screen)
└── DictionaryActivity/ (Dictionary lookup)
```

## Architecture Rules
**MVVM Pattern - Strictly Follow**:
1. **Model**: Data classes in `data/models/`
2. **View**: Composable functions (no XML)
3. **ViewModel**: StateFlow + Repository pattern

**State Management**:
```kotlin
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Update state
_uiState.update { it.copy(property = newValue) }

// Collect in Composable
val uiState by viewModel.uiState.collectAsState()
```

## Code Patterns

### Composable Structure
```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = { /* TopBar */ },
        bottomBar = { /* BottomBar */ }
    ) { padding ->
        // Content
    }
}
```

### ViewModel Pattern
```kotlin
class FeatureViewModel : ViewModel() {
    private val repository = Repository()
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val data = repository.getData()
                _uiState.update { 
                    it.copy(data = data, isLoading = false) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = e.message, isLoading = false) 
                }
            }
        }
    }
}
```

### Data Classes
```kotlin
// Always use @Parcelize for navigation
@Parcelize
data class Topic(
    val id: String,
    val name: String,
    val nameVi: String,
    val imageUrl: String
) : Parcelable

// UI State pattern
data class FeatureUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val errorMessage: String? = null
)
```

## Design System

### Colors
```kotlin
val PrimaryPurple = Color(0xFF6200EA)
val LightPurple = Color(0xFFEDE7F6)
val Blue = Color(0xFF1976D2)
val Green = Color(0xFF4CAF50)
val Background = Color(0xFFF5F5F5)
```

### Spacing
```kotlin
val SmallPadding = 8.dp
val MediumPadding = 16.dp
val LargePadding = 24.dp
val CardRadius = 12.dp
val ButtonRadius = 16.dp
```

### Typography
- Use `MaterialTheme.typography`
- Headlines: `headlineLarge`, `headlineMedium`
- Body: `bodyLarge`, `bodyMedium`
- Labels: `labelLarge`, `labelMedium`

## Firebase Integration

### Data Structure
```json
{
  "topics": { "topic1": {...} },
  "flashcards": { "flash1": {...} },
  "conversations": { "conv1": {...} },
  "userProgress": { "userId": {...} }
}
```

### Repository Pattern
```kotlin
class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance(
        "https://english-learning-app-17885-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    
    suspend fun getTopics(): List<Topic> {
        // Firebase query implementation
    }
}
```

## Key Features

### 1. Flashcard Learning
- **Front**: Image + HTML contextSentence
- **Back**: word + pronunciation + meaning + (type)
- Use `parseHtmlToAnnotatedString()` from `utils/Helper.kt`
- HTML tags: `<b>`, `<u>`, `<i>`, `<strike>`

### 2. Dictionary Lookup
- API: `https://api.dictionaryapi.dev/api/v2/entries/en/{word}`
- Display: phonetics, definitions, examples, synonyms, antonyms
- Recent searches (last 10)

### 3. Bottom Navigation
- "Tra từ" (Dictionary)
- "Học từ vựng" (Learn - default)
- "Ôn tập" (Review)
- "Hội thoại" (Conversation)
- "MochiHub" (Hub)

## Common Tasks

### Adding New Screen
1. Create Activity in `[Feature]Activity/`
2. Create ViewModel with StateFlow
3. Create Screen composable
4. Add to AndroidManifest.xml
5. Add navigation from parent

### Updating UI
1. Use Material3 components
2. Follow color scheme
3. Use `Modifier` for layouts
4. Keep composables small (<100 lines)
5. Extract reusable components

### API Integration
1. Define data models in `data/models/`
2. Create Retrofit service in `data/api/`
3. Use Repository pattern
4. Handle errors in ViewModel
5. Update UI state

## Common Mistakes to Avoid

❌ **Don't**:
- Use XML layouts (Compose only)
- Mutate StateFlow directly
- Forget INTERNET permission
- Use `crossAxisAlignment` in Row (use `verticalAlignment`)
- Use `crossAxisAlignment` in Column (use `horizontalAlignment`)
- Create multiple Repository instances
- Display raw HTML without parsing

✅ **Do**:
- Follow MVVM strictly
- Use StateFlow for state
- Parse HTML with Helper functions
- Add activities to manifest
- Use Material3 components
- Handle loading/error states
- Use Vietnamese for UI labels

## Vietnamese Context
- UI: Vietnamese labels
- Data: English content
- Meanings: Vietnamese translations
- Keep user-facing text in Vietnamese

## Dependencies
```kotlin
// Core
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.compose.material3)

// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
implementation("com.google.firebase:firebase-database-ktx")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Image
implementation("io.coil-kt:coil-compose:2.5.0")
```

## When Assisting
1. ✅ Always use MVVM pattern
2. ✅ Generate Compose code (no XML)
3. ✅ Follow existing code style
4. ✅ Use StateFlow for state management
5. ✅ Include proper error handling
6. ✅ Add Vietnamese UI labels
7. ✅ Parse HTML content when needed
8. ✅ Use Material3 components
9. ✅ Follow project structure
10. ✅ Update manifest when adding activities

## Development Status
✅ Firebase integration
✅ Dictionary API
✅ Flashcard learning
✅ Bottom navigation
✅ HTML parsing
🔄 TTS integration (TODO)
🔄 Conversation practice (TODO)
🔄 Progress tracking UI (TODO)
