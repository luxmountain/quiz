# AGENTS.md - Quick Reference for Coding Agents

## Build/Lint/Test Commands
```bash
./gradlew clean build                    # Clean and build project
./gradlew assembleDebug                  # Build debug APK
./gradlew test                           # Run unit tests
./gradlew connectedAndroidTest          # Run instrumented tests
./gradlew test --tests ExampleUnitTest  # Run single unit test
./gradlew connectedAndroidTest --tests ExampleInstrumentedTest  # Run single instrumented test
```

## Code Style Guidelines

**Architecture**: MVVM (Model-View-ViewModel) with Jetpack Compose - strictly enforce this pattern

**Imports**: Group by Android, Third-party, Internal; use explicit imports, avoid wildcards

**StateFlow Pattern** (mandatory):
```kotlin
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
_uiState.update { it.copy(data = newData) }  // Never mutate directly
```

**Naming Conventions**: PascalCase for classes/files, camelCase for functions/variables, UPPER_SNAKE for constants

**File Structure**: `[Feature]Activity.kt`, `[Feature]ViewModel.kt`, `[Feature]Screen.kt`, `[Feature]UiState.kt`

**Error Handling**: Use try-catch in ViewModels with StateFlow error states; handle loading/success/error in UI

**Types**: Explicit types for public APIs, inference for locals; use `data class` for models with `@Parcelize` for navigation

**Formatting**: 4-space indent, 100-char line limit, Material3 components only, `modifier: Modifier = Modifier` as last param

**HTML Content**: Always parse with `parseHtmlToAnnotatedString()` from `utils/Helper.kt` before display

**Vietnamese UI**: All UI labels in Vietnamese, English content, Vietnamese meanings - never mix in same context

**Common Pitfalls**: Use `verticalAlignment` in Row (not crossAxisAlignment), `horizontalAlignment` in Column, always update AndroidManifest for new Activities

**Project Context**: Package `com.uilover.project247`, Min SDK 24, Target SDK 36, Firebase+Retrofit+Coil stack, see .cursorrules/.github/copilot-instructions.md/.claude/project-instructions.md for full details
