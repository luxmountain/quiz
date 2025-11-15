package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // Import tất cả layout
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// SỬA 1: Dùng icon AutoMirrored (hỗ trợ RTL tốt hơn)
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // SỬA 2: Thêm import
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
            // SỬA 3: Giữ bo góc (đã có)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(backgroundColor)
            // SỬA 4: Thêm padding cho thanh điều hướng (Home/Back)
            .navigationBarsPadding()
            // SỬA 5: Tăng padding dọc (vertical) cho thoáng hơn
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Tiêu đề (Chính xác! / Đáp án đúng là:)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        // SỬA 6: Tăng khoảng cách
        Spacer(modifier = Modifier.height(24.dp))

        // 2. Nội dung đáp án
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Nút loa
            IconButton(onClick = { /* TODO: TTS Speak */ }) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp, // Dùng icon AutoMirrored
                    contentDescription = "Phát âm",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp) // Cho icon to hơn 1 chút
                )
            }
            // SỬA 7: Tăng khoảng cách
            Spacer(modifier = Modifier.width(12.dp))

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
            modifier = Modifier
                .fillMaxWidth()
                // SỬA 8: Căn lề (padding) chính xác
                // Kích thước IconButton (48dp) + Spacer (12dp) = 60dp
                .padding(start = 60.dp)
        ) {
            Text(
                text = card.meaning,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.contextSentence,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        // SỬA 9: Tăng khoảng cách
        Spacer(modifier = Modifier.height(32.dp))

        // 4. Nút "Tiếp tục" (Giữ nguyên)
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