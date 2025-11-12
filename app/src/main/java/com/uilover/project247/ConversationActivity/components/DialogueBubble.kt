package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uilover.project247.ConversationActivity.screens.createStyledDialogueText
import com.uilover.project247.data.models.DialogueLine

@Composable
fun DialogueBubble(
    dialogue: DialogueLine,
    isUser: Boolean,
    targetWord: String,
    onSpeakClick: () -> Unit // Callback khi bấm nút loa
) {
    val bubbleColor = if (isUser) Color(0xFFFFF8E1) else Color(0xFFF3F3F3)
    val shape = RoundedCornerShape(16.dp)

    // 1. Dùng Row để căn chỉnh (Avatar + Bong bóng)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Bottom, // Đặt avatar ở dưới cùng
        // Căn lề cho cả Row
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {

        // 2. Avatar (Nếu không phải user, hiện bên trái)
        if (!isUser) {
            AvatarIcon(isUser = false)
            Spacer(modifier = Modifier.width(8.dp))
        }

        // 3. Bong bóng chat
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Bong bóng chiếm 85%
                .background(bubbleColor, shape)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 4. Nút Loa
                IconButton(
                    onClick = onSpeakClick, // Gọi TTS khi bấm
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Phát âm thanh",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                // 5. Text (với style gạch chân/in đậm)
                Text(
                    text = createStyledDialogueText(
                        text = dialogue.text, // Tiếng Anh
                        target = targetWord
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // 6. Avatar (Nếu là user, hiện bên phải)
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            AvatarIcon(isUser = true)
        }
    }
}