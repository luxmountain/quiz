package com.uilover.project247.LearningActivity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// SỬA IMPORT: Lấy ViewModel và các enum từ package `Model`
import com.uilover.project247.LearningActivity.Model.LearningViewModel
import com.uilover.project247.LearningActivity.Model.StudyMode
import com.uilover.project247.LearningActivity.Model.CheckResult // <-- THÊM IMPORT
// SỬA IMPORT: Lấy component từ package `components`
import com.uilover.project247.LearningActivity.components.FlashcardView
// import com.uilover.project247.LearningActivity.components.MultipleChoiceView // (Tạm)
import com.uilover.project247.LearningActivity.components.WriteWordView
// SỬA IMPORT: Thay ProgressBar bằng AnimatedProgressBar (nếu bạn muốn AnimatedProgressBar)
import com.uilover.project247.LearningActivity.components.ProgressBar // <-- ĐÃ SỬA: Thay thế ProgressBar bằng AnimatedProgressBar
// THÊM IMPORT: Cho các component bottom bar/popup
import com.uilover.project247.LearningActivity.components.AnswerFeedbackPopup
import com.uilover.project247.LearningActivity.components.CheckButtonBottomBar
import com.uilover.project247.R // THÊM IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    viewModel: LearningViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor=Color(0xFFF7F7F7)

    // --- 1. State cho TextField (phải dời lên đây) ---
    var userAnswer by rememberSaveable { mutableStateOf("") }

    // 2. Reset userAnswer khi card thay đổi
    LaunchedEffect(uiState.currentCard) {
        userAnswer = ""
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // SỬA: Dùng AnimatedProgressBar (đã tạo)
                    ProgressBar( // <-- ĐÃ SỬA: Thay thế ProgressBar bằng AnimatedProgressBar
                        progress = uiState.progress,
                        iconResId = R.drawable.ic_kitty // Icon của bạn
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor,
        // --- XÓA: bottomBar ở đây vì popup sẽ nằm giữa màn hình ---
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator() // (Hoặc LoadingScreen())
                uiState.isTopicComplete -> CompletionView(onNavigateBack)
                uiState.currentCard != null -> {
                    val card = uiState.currentCard!!

                    Column( // Bao bọc các chế độ học vào một Column
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween // Đẩy các phần lên xuống
                    ) {
                        when (uiState.currentStudyMode) {
                            StudyMode.FLASHCARD -> FlashcardView(
                                card = card,
                                onComplete = { viewModel.onActionCompleted() },
                                onKnowWord = {
                                    viewModel.goToNextCard()
                                }
                            )

                            // --- 4. SỬA CÁCH GỌI WRITEWORDVIEW ---
                            StudyMode.WRITE_WORD -> {
                                WriteWordView(
                                    card = card,
                                    userAnswer = userAnswer,
                                    onUserAnswerChange = {
                                        userAnswer = it
                                        viewModel.clearCheckResult()
                                    },
                                    onCheckFromKeyboard = {
                                        viewModel.checkWrittenAnswer(userAnswer)
                                    },
                                    isChecking = uiState.checkResult != CheckResult.NEUTRAL
                                )
                                Spacer(modifier = Modifier.height(16.dp)) // Tạo khoảng cách cho nút kiểm tra
                            }

                            StudyMode.MULTIPLE_CHOICE -> {
                                // (Tạm thời giữ placeholder)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Màn hình MultipleChoice (chưa làm)")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { viewModel.onActionCompleted() }) {
                                        Text("Bấm để qua (tạm thời)")
                                    }
                                }
                            }
                        }

                        // --- 5. NÚT KIỂM TRA CHO WRITE_WORD (Nếu không có popup) ---
                        // Vẫn giữ nút Kiểm tra ở đây cho chế độ WRITE_WORD khi không có popup
                        if (uiState.currentStudyMode == StudyMode.WRITE_WORD && uiState.checkResult == CheckResult.NEUTRAL) {
                            CheckButtonBottomBar(
                                isEnabled = userAnswer.isNotBlank(),
                                onClick = {
                                    viewModel.checkWrittenAnswer(userAnswer)
                                }
                            )
                        }
                    }
                }
                else -> Text("Không có từ vựng cho chủ đề này.")
            }
        }

        // --- 6. HIỂN THỊ POPUP Ở GIỮA MÀN HÌNH (ĐÃ SỬA) ---
        if (uiState.currentStudyMode == StudyMode.WRITE_WORD && uiState.currentCard != null && uiState.checkResult != CheckResult.NEUTRAL) {
            AlertDialog(
                onDismissRequest = {
                    // Không cho dismiss khi bấm ra ngoài để ép người dùng bấm "Tiếp tục"
                },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false) // Không dùng chiều rộng mặc định của dialog
            ) {
                // Bọc nội dung popup vào một Box để có thể áp dụng modifier
                Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                    AnswerFeedbackPopup(
                        card = uiState.currentCard!!,
                        checkResult = uiState.checkResult,
                        onContinue = {
                            viewModel.onQuizContinue()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun CompletionView(onNavigateBack: () -> Unit) {
    // (Composable này giữ nguyên, không thay đổi)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chúc mừng!", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF00C853))
        Text("Bạn đã hoàn thành chủ đề này.", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNavigateBack) { Text("Quay về") }
    }
}
