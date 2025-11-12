package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.LearningActivity.Model.CheckResult // (Dùng package của bạn)
import com.uilover.project247.data.models.QuizOption // (Dùng package của bạn)

// Nút bấm cho Quiz
@Composable
fun QuizButton(
    option: QuizOption,
    checkResult: CheckResult,
    selectedOptionId: String?,
    onClick: () -> Unit
) {
    val isSelected = option.id == selectedOptionId

    // --- SỬA LỖI: XÓA DÒNG NÀY ĐI ---
    // val buttonIsEnabled = checkResult == CheckResult.NEUTRAL

    // Logic quyết định màu sắc (ĐÃ ĐÚNG)
    val buttonColors = when (checkResult) {
        CheckResult.NEUTRAL -> ButtonDefaults.buttonColors(
            containerColor = Color.White, contentColor = Color.Black
        )
        // Khi đúng, chỉ tô màu xanh cho đáp án đúng
        CheckResult.CORRECT -> if (option.isCorrect) ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00C853) // Xanh
        ) else ButtonDefaults.buttonColors(
            containerColor = Color.White, contentColor = Color.Black
        )
        // Khi sai, tô đỏ đáp án đã chọn VÀ tô xanh đáp án đúng
        CheckResult.INCORRECT -> if (isSelected) ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F) // Đỏ
        ) else if (option.isCorrect) ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00C853) // Xanh
        ) else ButtonDefaults.buttonColors(
            containerColor = Color.White, contentColor = Color.Black
        )
    }

    Button(
        onClick = onClick,

        // --- SỬA LỖI: SET `enabled = true` ---
        // Nút phải luôn enabled để hiển thị màu sắc.
        // ViewModel sẽ lo việc ngăn bấm lại.
        enabled = true,

        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = buttonColors, // <-- Bây giờ màu này sẽ được áp dụng
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(option.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}