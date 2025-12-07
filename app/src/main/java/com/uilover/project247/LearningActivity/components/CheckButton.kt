package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CheckButton(
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    // 1. Định nghĩa Gradient (Xanh lá nhạt -> Xanh lá đậm)
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF66BB6A), // Green 400
            Color(0xFF2E7D32)  // Green 800
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                // Đặt transparent để hiển thị gradient của Box bên trong
                containerColor = Color.Transparent,
                contentColor = Color.White,

                // Giữ nguyên màu khi disable (Xám)
                disabledContainerColor = Color(0xFFE8E8E8),
                disabledContentColor = Color.Gray
            ),
            // Quan trọng: Xóa padding mặc định để Box gradient tràn viền
            contentPadding = PaddingValues()
        ) {
            // 2. Tạo Box chứa Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        // Chỉ hiện gradient khi Button đang bật (Enabled)
                        if (isEnabled) Modifier.background(gradientBrush)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Kiểm tra",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}