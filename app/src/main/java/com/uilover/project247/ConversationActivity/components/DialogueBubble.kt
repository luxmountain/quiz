package com.uilover.project247.ConversationActivity.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onSpeakClick: () -> Unit // Callback khi bấm nút loa
) {
    val bubbleColor = if (isUser) Color(0xFFFFF8E1) else Color(0xFFF3F3F3)
    val shape = RoundedCornerShape(16.dp)

    // --- SỬA 2: Thêm State để ẩn/hiện bản dịch ---
    var isTranslationVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {

        if (!isUser) {
            AvatarIcon(isUser = false)
            Spacer(modifier = Modifier.width(8.dp))
        }

        // --- SỬA 3: Sửa lại cấu trúc bên trong Box ---
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(bubbleColor, shape)
        ) {
            // Dùng Row để chứa (Cột Nội dung) và (Nút Dịch)
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top // Căn nút dịch lên trên
            ) {
                // CỘT 1: Chứa Tiếng Anh và Tiếng Việt
                Column(
                    modifier = Modifier.weight(1f) // Chiếm hết không gian
                ) {
                    // Hàng Tiếng Anh (Loa + Text)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_loudspeaker),
                                contentDescription = "Phát âm thanh",
                                tint = Color.Unspecified
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = createStyledDialogueText(
                                text = dialogue.text, // Tiếng Anh
                                target = targetWord
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } // Hết Hàng Tiếng Anh

                    // --- SỬA 4: Thêm Text Tiếng Việt (có điều kiện) ---
                    if (isTranslationVisible) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dialogue.textVi, // Tiếng Việt
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray, // "in nhạt hơn"
                            // Căn lề cho khớp với text Tiếng Anh
                            modifier = Modifier.padding(start = 32.dp)
                        )
                    }
                } // Hết Cột 1

                Spacer(modifier = Modifier.width(8.dp))

                // CỘT 2: Nút Dịch (G-Translate)
                IconButton(
                    onClick = { isTranslationVisible = !isTranslationVisible },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        // TODO: Đảm bảo bạn có icon tên `ic_g_translate` trong `drawable`
                        painter = painterResource(id = R.drawable.ic_translation),
                        contentDescription = "Dịch",
                        tint = Color.Gray
                    )
                }
            } // Hết Row (Nội dung + Dịch)
        } // Hết Box (Bong bóng)

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            AvatarIcon(isUser = true)
        }
    } // Hết Row (Avatar + Bong bóng)
}