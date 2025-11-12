package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// --- COMPOSABLE MỚI: AVATAR ---
@Composable
fun AvatarIcon(isUser: Boolean) {
    // TODO: Thay bằng ảnh Mochi thật
    // Giả sử: 'isUser' là quả cam, '!isUser' là quả chanh
    val avatarColor = if (isUser) Color(0xFFFFA500) else Color(0xFFC0CA33)

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(avatarColor)
    )
}