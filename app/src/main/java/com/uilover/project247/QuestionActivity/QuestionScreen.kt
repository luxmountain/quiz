package com.uilover.project247.QuestionActivity


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.QuestionActivity.Model.QuestionUiState
import com.uilover.project247.QuestionActivity.Model.QuestionViewModel
import com.uilover.project247.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    viewModel: QuestionViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScore: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentQuiz = uiState.currentQuiz
    if (uiState.isFinished){
        LaunchedEffect(Unit) {
            onNavigateToScore(uiState.score)
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    LinearProgressIndicator(
                        progress = uiState.progress,
                        modifier = Modifier.fillMaxWidth(0.6f).clip(CircleShape)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF7F7F7)
                )
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ){
            paddingValues ->

        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.isFinished -> {
                    Text("Hoàn thành! Đang chuyển đến màn hình điểm...")
                }

                currentQuiz != null -> {
                    // Cột chứa toàn bộ nội dung quiz
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cụm 1: Câu hỏi
                        Text(
                            text = currentQuiz.question,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        // Cụm 2: Các lựa chọn
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            currentQuiz.options.forEach { option ->
                                QuizOptionButton(
                                    text = option,
                                    isSelected = uiState.selectedAnswer == option,
                                    checkResult = uiState.checkResult,
                                    correctAnswer = currentQuiz.correctAnswer,
                                    onClick = {
                                        viewModel.submitAnswer(option)
                                    }
                                )
                            }
                        }

                        // Cụm 3: Placeholder (để đẩy 2 cụm kia lên)
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }

                else -> {
                    Text("Không có câu hỏi nào cho chủ đề này.")
                }
            }
        }
    }
}
@Composable
private fun QuizOptionButton(
    text: String,
    isSelected: Boolean,
    checkResult: CheckResult,
    correctAnswer: String,
    onClick: () -> Unit
) {
    val isCorrect = text == correctAnswer
    val buttonIsEnabled = checkResult == CheckResult.NEUTRAL

    // Logic quyết định màu sắc
    val buttonColors = when (checkResult) {
        CheckResult.NEUTRAL -> ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
        CheckResult.CORRECT -> if (isSelected) ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)) else ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        CheckResult.INCORRECT -> if (isSelected) ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) else if (isCorrect) ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)) else ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
    }

    Button(
        onClick = onClick,
        enabled = buttonIsEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = buttonColors,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
