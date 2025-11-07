package com.uilover.project247.QuestionActivity


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.uilover.project247.QuestionActivity.Model.QuestionModel
import com.uilover.project247.QuestionActivity.Model.QuestionUiState
import com.uilover.project247.QuestionActivity.components.AnswerItem
import com.uilover.project247.R

@Composable
fun QuestionScreen(
    questions: List<QuestionModel>,
    onFinish: (finalscore: Int) -> Unit,
    onBackClick: () -> Unit
) {
    var state by remember {
        mutableStateOf(
            QuestionUiState(questions = questions)
        )
    }

    // FIX 1: Thêm kiểm tra danh sách rỗng để tránh crash
    if (state.questions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Không có câu hỏi nào cho chủ đề này.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
        return // Dừng thực thi ở đây
    }


    val currentQuestion = state.questions[state.currentIndex]
    var selectedAnswer = currentQuestion.clickedAnswer
    val context = LocalContext.current
    val imageResId = remember(currentQuestion.picPath) {
        context.resources.getIdentifier(
            currentQuestion.picPath ?: "",
            "drawable",
            context.packageName
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.grey))
    ) {
        item {
            Row(        //Header
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FIX 2: Sửa lại cách gọi hàm onClick
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Back"
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Single Player",
                    fontSize = 20.sp,
                    color = colorResource(R.color.navy_blue),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            //Questions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FIX 3: Dùng state.questions.size thay vì hardcode
                Text(
                    text = "Question ${state.currentIndex + 1}/${state.questions.size}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (state.currentIndex > 0) {
                            selectedAnswer = null
                            state = state.copy(currentIndex = state.currentIndex - 1)
                        }
                    }
                ) {
                    Icon(painterResource(R.drawable.left_arrow), contentDescription = null)
                }
                IconButton(
                    onClick = {
                        // FIX 3: Dùng state.questions.size
                        if (state.currentIndex == state.questions.size - 1) {
                            onFinish(state.score)
                        } else {
                            selectedAnswer = null
                            state = state.copy(currentIndex = state.currentIndex + 1)
                        }
                    }
                ) {
                    Icon(painterResource(R.drawable.right_arrow), contentDescription = null)
                }
            }
        }

        item {
            // FIX 3: Dùng state.questions.size
            LinearProgressIndicator(
                progress = (state.currentIndex + 1) / state.questions.size.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(50)),
                color = colorResource(R.color.orange),
                trackColor = Color(0xffd1d1d1)
            )
        }
        item {
            Text(
                text = currentQuestion.question ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center,
                color = colorResource(R.color.navy_blue),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            //Image
            if (imageResId != 0) { // Thêm kiểm tra xem ảnh có tồn tại không
                Image(
                    painterResource(imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        itemsIndexed(
            listOf(
                currentQuestion.answer_1 ?: "",
                currentQuestion.answer_2 ?: "",
                currentQuestion.answer_3 ?: "",
                currentQuestion.answer_4 ?: ""

            )
        ) { index, answerText ->
            val answerLetter = listOf("a", "b", "c", "d")[index]
            val isCorrect = selectedAnswer != null && answerLetter == currentQuestion.correctAnswer
            val isWrong = selectedAnswer == answerLetter && !isCorrect

            AnswerItem(
                text = answerText,
                isCorrect = isCorrect,
                isWrong = isWrong,
                isSelected = selectedAnswer != null
            ) {
                val updatedQuestion = state.questions.toMutableList()
                val updateQuestion =
                    updatedQuestion[state.currentIndex].copy(clickedAnswer = answerLetter)
                updatedQuestion[state.currentIndex] = updateQuestion
                val scoreToAdd = if (answerLetter == updateQuestion.correctAnswer) 5 else 0
                state = state.copy(
                    questions = updatedQuestion,
                    score = state.score + scoreToAdd
                )
            }

        }
        item {
            Spacer(Modifier.height(32.dp))
        }

    }
}

@Preview
@Composable
fun QuestionScreenPreview() {
    val questions = listOf(
        QuestionModel(
            id = 1,
            question = "What is the capital of France?",
            answer_1 = "Paris",
            answer_2 = "London",
            answer_3 = "Berlin",
            answer_4 = "Madrid",
            correctAnswer = "a", // Sửa lại correctAnswer để khớp với letter
            score = 10,
            picPath = "q_1",
            clickedAnswer = null
        )
    )
    QuestionScreen(questions = questions, onFinish = {}, onBackClick = {})
}
