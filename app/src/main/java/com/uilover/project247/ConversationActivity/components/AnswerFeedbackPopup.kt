package com.uilover.project247.ConversationActivity.components

import android.text.style.StyleSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.data.models.VocabularyWordInfo

@Composable
public fun AnswerFeedbackPopup(
    wordInfo: VocabularyWordInfo?, // Nhận thông tin từ vựng đầy đủ
    checkResult: CheckResult,
    onContinue: () -> Unit
) {
    val isCorrect = checkResult == CheckResult.CORRECT
    val backgroundColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFD32F2F)
    val title = if (isCorrect) "Chính xác!" else "Đáp án đúng là:"

    Column(
        modifier = Modifier.fillMaxWidth().background(backgroundColor).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        if (wordInfo != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { /* TODO: TTS Speak */ }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Phát âm", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("${wordInfo.word} (${wordInfo.wordTypeVi})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(wordInfo.pronunciation, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(start = 48.dp)) {
                Text(wordInfo.meaning, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
                // (Bạn có thể thêm câu ví dụ ở đây nếu muốn)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
