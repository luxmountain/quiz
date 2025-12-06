package com.uilover.project247.LearningActivity.components

import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.graphics.Typeface
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

    // Tạo Brush Gradient
    val backgroundBrush = if (isCorrect) {
        // Gradient Xanh lá (Đúng)
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF66BB6A), // Xanh tươi sáng hơn ở trên
                Color(0xFF2E7D32)  // Xanh đậm đầm hơn ở dưới
            )
        )
    } else {
        // Gradient Đỏ (Sai)
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFEF5350), // Đỏ tươi sáng hơn ở trên
                Color(0xFFC62828)  // Đỏ đậm đầm hơn ở dưới
            )
        )
    }

    val title = if (isCorrect) "Chính xác!" else "Đáp án đúng là:"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(brush = backgroundBrush) // Sử dụng brush thay vì color
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Tiêu đề
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Nội dung đáp án
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Nút loa
            IconButton(onClick = { ttsManager.speak(card.word) }) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

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

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Nút "Tiếp tục"
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828) // Text button đổi màu theo trạng thái
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
    val spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)

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