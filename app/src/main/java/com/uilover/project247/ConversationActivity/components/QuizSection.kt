package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.R
import com.uilover.project247.data.models.QuizOption

@Composable
fun QuizSection(
    question: String,
    options: List<QuizOption>,
    onAnswerSelected: (QuizOption) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.forEach { option ->
            QuizButton(
                option = option,
                checkResult = CheckResult.NEUTRAL,
                selectedOptionId = null,
                onClick = { onAnswerSelected(option) }
            )
        }
    }
}