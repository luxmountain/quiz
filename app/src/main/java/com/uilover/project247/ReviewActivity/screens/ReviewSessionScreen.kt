package com.uilover.project247.ReviewActivity.screens

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.LearningActivity.components.*
import com.uilover.project247.ReviewActivity.Model.ReviewViewModel
import com.uilover.project247.data.models.ReviewCheckResult
import com.uilover.project247.data.models.ReviewStep
import com.uilover.project247.utils.TextToSpeechManager
import com.uilover.project247.R

/**
 * Review Session Screen - 3-Step Flow (Mirror LearningScreen UI)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSessionScreen(
    viewModel: ReviewViewModel,
    onExit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = Color(0xFFF7F7F7)
    var userAnswer by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val tts = remember { TextToSpeechManager(context) }
    
    // CRITICAL FIX: Reset user answer when moving to next item OR next step
    LaunchedEffect(uiState.currentItem?.currentStep, uiState.currentItem?.flashcard?.id) {
        userAnswer = ""
        Log.d("ReviewSessionScreen", "User input cleared (step or item changed)")
    }
    
    // Cleanup TTS
    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    ProgressBar(
                        progress = uiState.progress,
                        iconResId = R.drawable.cat1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
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
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                
                uiState.errorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                        Button(onClick = onExit) {
                            Text("Quay lại")
                        }
                    }
                }
                
                uiState.isSessionComplete -> {
                    ReviewCompletionView(
                        session = uiState.currentSession,
                        onExit = onExit
                    )
                }
                
                uiState.currentItem != null -> {
                    val item = uiState.currentItem!!
                    val card = item.flashcard
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // CONTENT AREA
                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            when (item.currentStep) {
                                ReviewStep.FILL_IN_BLANK -> {
                                    // Reuse WriteWordView component
                                    WriteWordView(
                                        card = card,
                                        userAnswer = userAnswer,
                                        onUserAnswerChange = {
                                            userAnswer = it
                                            viewModel.clearCheckResult()
                                        },
                                        onCheckFromKeyboard = {
                                            viewModel.checkAnswer(userAnswer)
                                        },
                                        isChecking = uiState.checkResult != ReviewCheckResult.NEUTRAL
                                    )
                                }
                                
                                ReviewStep.LISTEN_AND_WRITE -> {
                                    // Reuse ListenWriteView component
                                    ListenWriteView(
                                        card = card,
                                        userAnswer = userAnswer,
                                        onUserAnswerChange = {
                                            userAnswer = it
                                            viewModel.clearCheckResult()
                                        },
                                        onCheckFromKeyboard = {
                                            viewModel.checkAnswer(userAnswer)
                                        },
                                        isChecking = uiState.checkResult != ReviewCheckResult.NEUTRAL
                                    )
                                }
                                
                                ReviewStep.MULTIPLE_CHOICE -> {
                                    MultipleChoiceView(
                                        card = card,
                                        wrongOptions = uiState.wrongOptions,
                                        checkResult = uiState.checkResult,
                                        onOptionSelected = { selectedMeaning ->
                                            viewModel.checkAnswer(selectedMeaning)
                                        }
                                    )
                                }
                            }
                        }
                        
                        // CHECK BUTTON (for Fill in Blank & Listen & Write)
                        AnimatedVisibility(
                            visible = (
                                (item.currentStep == ReviewStep.FILL_IN_BLANK ||
                                 item.currentStep == ReviewStep.LISTEN_AND_WRITE)
                                && uiState.checkResult == ReviewCheckResult.NEUTRAL
                            ),
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(280)
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(240)
                            ) + fadeOut()
                        ) {
                            CheckButton(
                                isEnabled = userAnswer.isNotBlank(),
                                onClick = {
                                    viewModel.checkAnswer(userAnswer)
                                }
                            )
                        }
                    }
                }
                
                else -> {
                    Text("Đang tải...")
                }
            }
            
            // FEEDBACK POPUP (for all steps)
            if (uiState.currentItem != null && uiState.checkResult != ReviewCheckResult.NEUTRAL) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    AnswerFeedbackPopup(
                        card = uiState.currentItem!!.flashcard,
                        checkResult = when (uiState.checkResult) {
                            ReviewCheckResult.CORRECT -> com.uilover.project247.LearningActivity.Model.CheckResult.CORRECT
                            ReviewCheckResult.INCORRECT -> com.uilover.project247.LearningActivity.Model.CheckResult.INCORRECT
                            else -> com.uilover.project247.LearningActivity.Model.CheckResult.NEUTRAL
                        },
                        onContinue = { viewModel.onContinue() }
                    )
                }
            }
        }
    }
}

/**
 * Multiple Choice View
 */
@Composable
private fun MultipleChoiceView(
    card: com.uilover.project247.data.models.Flashcard,
    wrongOptions: List<String>,
    checkResult: ReviewCheckResult,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question header
        Text(
            text = "Nghĩa của từ sau là gì?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Word card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEDE7F6)
            )
        ) {
            Text(
                text = card.word,
                modifier = Modifier.padding(32.dp),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EA)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Options - Remember shuffled state to prevent UI jumping on recomposition
        val allOptions = remember(wrongOptions, card.meaning) {
            (wrongOptions + card.meaning).shuffled()
        }
        val isAnswered = checkResult != ReviewCheckResult.NEUTRAL
        
        allOptions.forEach { option ->
            val isCorrect = option == card.meaning
            val backgroundColor = when {
                !isAnswered -> Color.White
                isCorrect -> Color(0xFF4CAF50)
                else -> Color.White
            }
            
            val borderColor = when {
                !isAnswered -> Color.LightGray
                isCorrect -> Color(0xFF4CAF50)
                else -> Color.LightGray
            }
            
            Button(
                onClick = { 
                    if (!isAnswered) {
                        onOptionSelected(option)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor,
                    disabledContainerColor = backgroundColor
                ),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp, 
                    borderColor
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                enabled = !isAnswered
            ) {
                Text(
                    text = option,
                    fontSize = 16.sp,
                    color = if (isAnswered && isCorrect) Color.White else Color.Black
                )
            }
        }
    }
}

/**
 * Review Completion View
 */
@Composable
private fun ReviewCompletionView(
    session: com.uilover.project247.data.models.ReviewSession?,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            fontSize = 72.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Hoàn thành!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        session?.let {
            Text(
                text = "Độ chính xác: ${it.getAccuracy().toInt()}%",
                fontSize = 24.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${it.results.size} câu hỏi",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Hoàn tất",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
