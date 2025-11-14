package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uilover.project247.LearningActivity.Model.CheckResult

@Composable
fun QuizResultOverlay(
    checkResult: CheckResult,
    onContinue: () -> Unit
) {
    val isCorrect = checkResult == CheckResult.CORRECT
    val backgroundColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFD32F2F)
    val text = if (isCorrect) "Chính xác!" else "Sai rồi!"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
        ) {
            Text("Tiếp tục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}