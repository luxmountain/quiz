package com.uilover.project247.LearningActivity.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.LearningActivity.Model.LearningViewModel
import com.uilover.project247.LearningActivity.Model.StudyMode
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.LearningActivity.components.*
import com.uilover.project247.R
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    viewModel: LearningViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = Color(0xFFF7F7F7)
    var userAnswer by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.currentCard, uiState.currentStudyMode) {
        userAnswer = ""
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    ProgressBar(
                        progress = uiState.progress,
                        iconResId = R.drawable.ic_kitty
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
        containerColor = backgroundColor
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {

            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.isTopicComplete -> CompletionView(
                    uiState = uiState,
                    onNavigateBack = onNavigateBack
                )

                uiState.currentCard != null -> {
                    val card = uiState.currentCard!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()                 // <-- quan trọng
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // PHẦN HIỂN THỊ BÀI HỌC
                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            when (uiState.currentStudyMode) {
                                StudyMode.FLASHCARD -> FlashcardView(
                                    card = card,
                                    onComplete = { viewModel.onActionCompleted() },
                                    onKnowWord = { viewModel.goToNextCard() }
                                )

                                StudyMode.WRITE_WORD -> WriteWordView(
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

                                StudyMode.LISTEN_AND_WRITE -> ListenWriteView(
                                    card = card,
                                    userAnswer = userAnswer,
                                    onUserAnswerChange = {
                                        userAnswer = it
                                        viewModel.clearCheckResult()
                                    },
                                    onCheckFromKeyboard = {
                                        viewModel.checkListenAnswer(userAnswer)
                                    },
                                    isChecking = uiState.checkResult != CheckResult.NEUTRAL
                                )

                                StudyMode.MULTIPLE_CHOICE -> {
                                    // TODO
                                }
                            }
                        }

                        // NÚT KIỂM TRA — TRƯỢT LÊN/XUỐNG
                        AnimatedVisibility(
                            visible = (
                                    (uiState.currentStudyMode == StudyMode.WRITE_WORD ||
                                            uiState.currentStudyMode == StudyMode.LISTEN_AND_WRITE)
                                            && uiState.checkResult == CheckResult.NEUTRAL
                                    ),
                            enter = slideInVertically(
                                initialOffsetY = { it },  // từ dưới lên
                                animationSpec = tween(280)
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { it },    // trượt xuống đáy
                                animationSpec = tween(240)
                            ) + fadeOut()
                        ) {
                            CheckButton(
                                isEnabled = userAnswer.isNotBlank(),
                                onClick = {
                                    if (uiState.currentStudyMode == StudyMode.WRITE_WORD)
                                        viewModel.checkWrittenAnswer(userAnswer)
                                    else viewModel.checkListenAnswer(userAnswer)
                                }
                            )
                        }
                    }
                }

                else -> Text("Không có từ vựng cho chủ đề này.")
            }


        if ((uiState.currentStudyMode == StudyMode.WRITE_WORD ||
                    uiState.currentStudyMode == StudyMode.LISTEN_AND_WRITE) &&
            uiState.currentCard != null &&
            uiState.checkResult != CheckResult.NEUTRAL
        ) {
            AnimatedVisibility(
                visible = (uiState.currentStudyMode == StudyMode.WRITE_WORD ||
                        uiState.currentStudyMode == StudyMode.LISTEN_AND_WRITE) &&
                        uiState.currentCard != null &&
                        uiState.checkResult != CheckResult.NEUTRAL,

                // Hiệu ứng xuất hiện: Trượt từ dưới lên + Mờ dần vào
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),

                // Hiệu ứng biến mất: Trượt xuống dưới + Mờ dần đi
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),

                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                // Gọi component Popup của bạn
                AnswerFeedbackPopup(
                    card = uiState.currentCard!!,
                    checkResult = uiState.checkResult,
                    onContinue = { viewModel.onQuizContinue() }
                )
            }
        }
        }
    }
}


@Composable
fun CompletionView(
    uiState: com.uilover.project247.LearningActivity.Model.LearningUiState,
    onNavigateBack: () -> Unit
) {
    val total = uiState.correctAnswers + uiState.wrongAnswers
    val accuracyPercent = if (total > 0) (uiState.correctAnswers.toFloat() / total * 100).toInt() else 0
    val isPassed = accuracyPercent >= 60
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon và tiêu đề
        Text(
            text = if (isPassed) "🎉" else "💪",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 72.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isPassed) "Chúc mừng!" else "Cố lên!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = if (isPassed) Color(0xFF4CAF50) else Color(0xFFFF9800)
        )
        
        Text(
            text = if (isPassed) 
                "Bạn đã hoàn thành chủ đề này" 
            else 
                "Cần đạt 60% để hoàn thành",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Thống kê
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Kết quả học tập",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                HorizontalDivider()
                
                // Accuracy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Độ chính xác:", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "$accuracyPercent%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isPassed) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
                
                // Correct answers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Câu trả lời đúng:", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${uiState.correctAnswers}/$total",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                // Wrong answers
                if (uiState.wrongAnswers > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Câu trả lời sai:", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${uiState.wrongAnswers}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
                
                // Total words
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng số từ:", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${uiState.flashcards.size}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Buttons
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EA)
            )
        ) {
            Text(
                "Hoàn thành",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
