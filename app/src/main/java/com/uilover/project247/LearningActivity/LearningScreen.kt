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
import androidx.compose.ui.unit.dp
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

                uiState.isTopicComplete -> CompletionView(onNavigateBack)

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
        }

        if ((uiState.currentStudyMode == StudyMode.WRITE_WORD ||
                    uiState.currentStudyMode == StudyMode.LISTEN_AND_WRITE) &&
            uiState.currentCard != null &&
            uiState.checkResult != CheckResult.NEUTRAL
        ) {
            AlertDialog(
                onDismissRequest = {},
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
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
fun CompletionView(onNavigateBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Chúc mừng!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF00C853)
        )
        Text(
            "Bạn đã hoàn thành chủ đề này.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNavigateBack) {
            Text("Quay về")
        }
    }
}
