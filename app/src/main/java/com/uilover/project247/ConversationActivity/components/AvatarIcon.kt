package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uilover.project247.R
// --- COMPOSABLE MỚI: AVATAR ---
@Composable
fun AvatarIcon(isUser: Boolean) {
    val avatarIconResId = if (isUser) {
        R.drawable.avatar_weasel
    } else {
        R.drawable.avatar_shark
    }
    Image(
        painter = painterResource(id = avatarIconResId),
        contentDescription = "Speaker Avatar",
        modifier = Modifier
            .size(32.dp) // Kích thước avatar
            .clip(CircleShape) // Bo tròn
    )
}