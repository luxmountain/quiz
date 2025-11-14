package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.Image // <-- Thêm
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn // <-- Thêm
import androidx.compose.foundation.shape.CircleShape // <-- Thêm
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp // <-- Sửa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // <-- Thêm
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration // <-- Thêm
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uilover.project247.ConversationActivity.screens.createStyledDialogueText
import com.uilover.project247.data.models.DialogueLine
import com.uilover.project247.R

@Composable
fun DialogueBubble(
    dialogue: DialogueLine,
    isUser: Boolean,
    targetWord: String,
    onSpeakClick: () -> Unit,
    onTranslateClick: () -> Unit,
    isTranslationVisible: Boolean,
    showBlank: Boolean
) {
    val bubbleColor = if (isUser) Color(0xFFFFF8E1) else Color(0xFFF3F3F3)
    val shape = RoundedCornerShape(16.dp)

    // Lấy chiều rộng màn hình để giới hạn kích thước bong bóng
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {

        // 1. Avatar (Bên trái, nếu không phải user)
        if (!isUser) {
            AvatarIcon(isUser = false)
            Spacer(modifier = Modifier.width(8.dp))
        }

        // 2. Bong bóng chat (Tự co giãn)
        Box(
            modifier = Modifier
                // SỬA: Dùng `widthIn` để bong bóng "nhỏ hơn",
                // thay vì fillMaxWidth(0.85f)
                .widthIn(min = 80.dp, max = screenWidth * 0.7f)
                .background(bubbleColor, shape)
        ) {
            // Cột chứa (Tiếng Anh) và (Tiếng Việt)
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Hàng Tiếng Anh (Loa + Text)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSpeakClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_loudspeaker),
                            "Phát âm",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = createStyledDialogueText(
                            text = dialogue.text,
                            target = targetWord,
                            showBlank = showBlank
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } // Hết Hàng Tiếng Anh

                // Hàng Tiếng Việt (nếu hiển thị)
                if (isTranslationVisible) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        dialogue.textVi,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 32.dp) // Thụt lề
                    )
                }
            } // Hết Cột nội dung
        } // Hết Box (Bubble)

        // 3. Nút Dịch (Bên ngoài, nếu không phải user)
        if (!isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onTranslateClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    painterResource(id = R.drawable.ic_translation),
                    "Dịch",
                    tint = Color.Unspecified // Giữ màu gốc
                )
            }
        }

        // 4. Avatar (Bên phải, nếu là user)
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            AvatarIcon(isUser = true)
        }
    }
}