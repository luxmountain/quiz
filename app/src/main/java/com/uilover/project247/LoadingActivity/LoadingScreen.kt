package com.uilover.project247.LoadingActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.* // <-- Import Lottie
import com.uilover.project247.R // <-- Import R (để truy cập drawable)

/**
 * Màn hình chờ (Loading) chung,
 * hiển thị hoạt ảnh Lottie lặp lại vô hạn.
 */
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    // 1. Tải tệp Lottie từ drawable
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading)
    )

    // 2. Thiết lập cho hoạt ảnh chạy lặp lại
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever // Lặp lại vô hạn
    )

    // 3. Giao diện
    Box(
        modifier = modifier
            .fillMaxSize()
            // Đặt màu nền trắng (hoặc trong suốt) để che nội dung bên dưới
            .background(Color(0xFFF7F7F7)), // Màu trắng giống nền app
        contentAlignment = Alignment.Center
    ) {
        // 4. Hiển thị hoạt ảnh Lottie
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp) // Điều chỉnh kích thước
        )
    }
}