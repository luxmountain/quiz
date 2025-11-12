package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.data.models.Flashcard

@Composable
fun AnswerFeedbackPopup(
    card: Flashcard,
    checkResult: CheckResult,
    onContinue: () -> Unit
) {
    val isCorrect = checkResult == CheckResult.CORRECT
    val backgroundColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFD32F2F)
    val title = if (isCorrect) "Chính xác!" else "Đáp án đúng là:"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            // Cong 2 góc trên
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Tiêu đề (Chính xác! / Đáp án đúng là:)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 2. Nội dung đáp án
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Nút loa
            IconButton(onClick = { /* TODO: TTS Speak */ }) {
                Icon(Icons.Default.VolumeUp, "Phát âm", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Từ vựng và phiên âm
            Column {
                Text(
                    text = "${card.word} (${card.wordType})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = card.pronunciation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 3. Nghĩa và câu ví dụ
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 48.dp) // Căn lề
        ) {
            Text(
                text = card.meaning,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.contextSentence, // (Nên dùng parseHtmlToAnnotatedString)
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 4. Nút "Tiếp tục"
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                "Tiếp tục",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}