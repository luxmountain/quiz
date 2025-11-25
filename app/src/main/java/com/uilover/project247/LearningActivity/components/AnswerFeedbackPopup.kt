package com.uilover.project247.LearningActivity.components

import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // Import tất cả layout
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.graphics.Typeface
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // SỬA 2: Thêm import
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.uilover.project247.LearningActivity.Model.CheckResult
import com.uilover.project247.data.models.Flashcard
import com.uilover.project247.utils.TextToSpeechManager

@Composable
fun AnswerFeedbackPopup(
    card: Flashcard,
    checkResult: CheckResult,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    val isCorrect = checkResult == CheckResult.CORRECT
    val backgroundColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFD32F2F)
    val title = if (isCorrect) "Chính xác!" else "Đáp án đúng là:"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(backgroundColor)
            .navigationBarsPadding()
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
            IconButton(onClick = { ttsManager.speak(card.word) }) {
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
                text = parseHtmlToAnnotatedString(card.contextSentence),
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
fun parseHtmlToAnnotatedString(htmlText: String): AnnotatedString {
    // 1. Parse HTML bằng Android legacy API
    val spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)

    // 2. Chuyển đổi sang AnnotatedString của Compose
    return buildAnnotatedString {
        append(spanned.toString())

        val spans = spanned.getSpans(0, spanned.length, Any::class.java)

        for (span in spans) {
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)

            when (span) {
                is StyleSpan -> when (span.style) {
                    Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
                }
                is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
            }
        }
    }
}