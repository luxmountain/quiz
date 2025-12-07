package com.uilover.project247.PlacementTestActivity.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.PlacementTestActivity.Model.PlacementTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementTestScreen(
    viewModel: PlacementTestViewModel,
    onTestCompleted: () -> Unit,
    onSkip: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            if (!uiState.showInstructions && !uiState.isTestCompleted) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Bài kiểm tra đầu vào",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            LinearProgressIndicator(
                                progress = { viewModel.getProgress() },
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .padding(top = 4.dp),
                                color = Color(0xFF6200EA)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    ErrorScreen(
                        message = uiState.errorMessage ?: "",
                        onRetry = { onSkip() }
                    )
                }

                uiState.showInstructions -> {
                    InstructionsScreen(
                        test = uiState.placementTest!!,
                        onStart = { viewModel.startTest() },
                        onSkip = onSkip
                    )
                }

                uiState.isTestCompleted -> {
                    TestResultScreen(
                        result = uiState.testResult!!,
                        onContinue = onTestCompleted
                    )
                }

                else -> {
                    TestContentScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructionsScreen(
    test: com.uilover.project247.data.models.PlacementTest,
    onStart: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "🎯",
            fontSize = 72.sp
        )

        Text(
            test.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            test.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Hướng dẫn:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                test.instructions.forEach { instruction ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", color = Color(0xFF6200EA), fontWeight = FontWeight.Bold)
                        Text(
                            instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                InfoRow("Số câu hỏi", "${test.totalQuestions} câu")
                InfoRow("Thời gian", "${test.duration / 60} phút")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EA)
            )
        ) {
            Text(
                "Bắt đầu làm bài",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Bỏ qua (Học từ cơ bản)")
        }
    }
}

@Composable
private fun TestContentScreen(
    viewModel: PlacementTestViewModel,
    uiState: com.uilover.project247.PlacementTestActivity.Model.PlacementTestUiState
) {
    val test = uiState.placementTest ?: return
    val currentQuestion = test.questions.getOrNull(uiState.currentQuestionIndex) ?: return
    val selectedAnswer = uiState.userAnswers[uiState.currentQuestionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Question Navigator
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(test.questions) { index, _ ->
                val isAnswered = uiState.userAnswers.containsKey(index)
                val isCurrent = index == uiState.currentQuestionIndex

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> Color(0xFF6200EA)
                                else -> Color(0xFFE0E0E0)
                            }
                        )
                        .clickable { viewModel.goToQuestion(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${index + 1}",
                        color = if (isCurrent) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Question
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Câu ${uiState.currentQuestionIndex + 1}/${test.questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF6200EA),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        currentQuestion.questionVi,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (currentQuestion.question != currentQuestion.questionVi) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            currentQuestion.question,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Options
            currentQuestion.options.forEachIndexed { index, option ->
                OptionCard(
                    text = option,
                    isSelected = selectedAnswer == index,
                    isCorrect = null,
                    onClick = {
                        if (!uiState.isAnswered) {
                            viewModel.selectAnswer(index)
                        }
                    }
                )
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.currentQuestionIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousQuestion() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trước")
                    }
                }

                Button(
                    onClick = {
                        viewModel.nextQuestion()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isAnswered,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EA)
                    )
                ) {
                    Text(
                        if (uiState.currentQuestionIndex == test.questions.size - 1) "Hoàn thành" else "Tiếp"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect == true -> Color(0xFFE8F5E9)
        isCorrect == false && isSelected -> Color(0xFFFFEBEE)
        isSelected -> Color(0xFFEDE7F6)
        else -> Color.White
    }

    val borderColor = when {
        isCorrect == true -> Color(0xFF4CAF50)
        isCorrect == false && isSelected -> Color(0xFFD32F2F)
        isSelected -> Color(0xFF6200EA)
        else -> Color(0xFFE0E0E0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            if (isCorrect != null) {
                Icon(
                    if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
private fun TestResultScreen(
    result: com.uilover.project247.data.models.PlacementTestResult,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("🎉", fontSize = 72.sp)

        Text(
            "Hoàn thành!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6200EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${result.score}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    "Điểm của bạn",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )

                HorizontalDivider()

                ResultRow("Tổng số câu", "${result.totalQuestions}")
                ResultRow("Trả lời đúng", "${result.correctAnswers}", Color(0xFF4CAF50))
                ResultRow("Trả lời sai", "${result.wrongAnswers}", Color(0xFFD32F2F))

                HorizontalDivider()

                Text(
                    "Level được đề xuất",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEDE7F6))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        result.recommendedLevelVi,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EA)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EA)
            )
        ) {
            Text(
                "Bắt đầu học",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: Color = Color.Black) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) {
                Text("Thử lại")
            }
        }
    }
}
