package com.uilover.project247.LearningActivity.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uilover.project247.DashboardActivity.components.VocabularyWord
// TODO: Đảm bảo đường dẫn import data class VocabularyWord là đúng
import com.uilover.project247.LearningActivity.Model.LearningViewModel
import com.uilover.project247.LearningActivity.Model.StudyMode
import com.uilover.project247.LearningActivity.components.FlashcardView
import com.uilover.project247.LearningActivity.components.MultipleChoiceView
import com.uilover.project247.LearningActivity.components.WriteWordView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    viewModel: LearningViewModel,
    onNavigateBack: () -> Unit
) {
    // Lắng nghe `uiState` từ ViewModel.
    // Mỗi khi state thay đổi, Composable này sẽ tự động vẽ lại.
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor=Color(0xFFF7F7F7)


    Scaffold(
        topBar = {
            // Thanh TopAppBar với nút X và thanh tiến trình
            CenterAlignedTopAppBar(
                title = {
                    // Thanh tiến trình (placeholder)
                    LinearProgressIndicator(
                        progress = { 0.3f }, // TODO: Lấy tiến trình từ ViewModel
                        modifier = Modifier.fillMaxWidth(0.6f).clip(CircleShape)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor // Đồng màu nền
                )
            )
        },
        containerColor = backgroundColor // Set màu nền cho toàn màn hình
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.isTopicComplete -> CompletionView(onNavigateBack)
                uiState.currentWord != null -> {
                    val word = uiState.currentWord!!

                    when (uiState.currentStudyMode) {
                        // GỌI COMPONENT FLASHCARDVIEW MỚI
                        StudyMode.FLASHCARD -> FlashcardView(
                            word = word,
                            onComplete = { viewModel.onActionCompleted() },
                            onKnowWord = {
                                // TODO: Gọi 1 hàm khác trong ViewModel, ví dụ: viewModel.markAsKnown()
                                viewModel.onActionCompleted() // Tạm thời dùng onComplete
                            }
                        )
                        StudyMode.WRITE_WORD -> WriteWordView(
                            word = word,
                            checkResult = uiState.checkResult, // (1) Trạng thái
                            onCheck = { userAnswer -> // (2) Hàm kiểm tra
                                viewModel.checkWrittenAnswer(userAnswer)
                            },
                            onClearResult = { // (3) Hàm xóa trạng thái
                                viewModel.clearCheckResult()
                            }
                        )
                        StudyMode.MULTIPLE_CHOICE -> MultipleChoiceView(word, { viewModel.onActionCompleted() })
                    }
                }
                else -> Text("Không có từ vựng cho chủ đề này.")
            }
        }
    }
}






@Composable
fun CompletionView(onNavigateBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chúc mừng!", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF00C853))
        Text("Bạn đã hoàn thành chủ đề này.", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNavigateBack) { Text("Quay về") }
    }
}