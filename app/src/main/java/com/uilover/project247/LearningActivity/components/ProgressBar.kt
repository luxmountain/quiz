package com.uilover.project247.LearningActivity.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Thanh tiến trình tùy chỉnh với một icon chạy theo tiến trình.
 * @param progress Giá trị float từ 0.0f đến 1.0f
 * @param iconResId ID của icon trong drawable (ví dụ: R.drawable.ic_kitty)
 */
@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    @DrawableRes iconResId: Int
) {
    // Kích thước của icon
    val iconSize = 28.dp
    val progressBarHeight = 12.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(iconSize) // Chiều cao bằng icon
    ) {
        // 1. Thanh "track" (nền) màu xám
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(progressBarHeight)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant) // Màu xám nhạt
        )

        // 2. Thanh "progress" (tiến trình) màu cam
        val animatedProgress = animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 300),
            label = "progressAnimation"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.value) // Chiều rộng bằng tiến trình
                .height(progressBarHeight)
                .clip(CircleShape)
                .background(Color(0xFFFFA500)) // Màu cam
        )

        // 3. Icon (ví dụ: ic_kitty)
        // Tính toán vị trí offset
        val iconOffset by animateDpAsState(
            // Vị trí = (tổng chiều rộng * tiến trình) - (một nửa kích thước icon)
            targetValue = (maxWidth * animatedProgress.value) - (iconSize / 2),
            animationSpec = tween(durationMillis = 300),
            label = "iconOffsetAnimation"
        )

        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "Progress Icon",
            modifier = Modifier
                .size(iconSize)
                // Áp dụng offset (đảm bảo icon không đi ra ngoài lề)
                .offset(x = iconOffset.coerceIn(0.dp, maxWidth - iconSize))
        )
    }
}