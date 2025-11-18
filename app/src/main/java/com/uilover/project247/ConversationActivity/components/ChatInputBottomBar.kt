package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputBottomBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Thêm padding cho thanh điều hướng
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập câu trả lời của bạn") },
                shape = RoundedCornerShape(24.dp),

                // --- SỬA LỖI 1: SỬA MÀU VIỀN ---
                colors = OutlinedTextFieldDefaults.colors(
                    // Đổi thành màu Primary (thường là xanh dương)
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                ),
                // -----------------------------

                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send, capitalization = KeyboardCapitalization.None),
                keyboardActions = KeyboardActions(onSend = {
                    if (isEnabled) {
                        onSend()
                        keyboardController?.hide()
                    }
                }),
                // `isEnabled` được truyền từ ngoài vào đã đúng
                enabled = isEnabled
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = {
                    if (isEnabled) {
                        onSend()
                        keyboardController?.hide()
                    }
                },
                enabled = isEnabled, // `isEnabled` được truyền từ ngoài vào đã đúng
                modifier = Modifier.size(48.dp),

                // --- SỬA LỖI 2: SỬA MÀU NÚT ---
                colors = IconButtonDefaults.filledIconButtonColors(
                    // Màu khi BẬT (isEnabled = true)
                    containerColor = MaterialTheme.colorScheme.primary, // Màu xanh
                    contentColor = MaterialTheme.colorScheme.onPrimary,  // Màu trắng

                    // Màu khi TẮT (isEnabled = false)
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFFB0B0B0)
                )
                // -----------------------------
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi")
            }
        }
    }
}