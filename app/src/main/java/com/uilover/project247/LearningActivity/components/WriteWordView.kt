package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uilover.project247.data.models.Flashcard
import com.uilover.project247.LearningActivity.Model.CheckResult // (Dùng package của bạn)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteWordView(
    card: Flashcard,
    // SỬA: Thêm 2 tham số mới
    userAnswer: String,
    onUserAnswerChange: (String) -> Unit,
    // SỬA: Sửa lại tham số onCheck
    onCheckFromKeyboard: () -> Unit,
    // SỬA: Xóa checkResult, onClearResult
    // checkResult: CheckResult,
    // onClearResult: () -> Unit

    // SỬA: Thêm isChecking
    isChecking: Boolean // Để vô hiệu hóa TextField khi đang kiểm tra
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Bố cục chính
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // SỬA: Không dùng SpaceBetween nữa
        verticalArrangement = Arrangement.Top
    ) {
        // Cụm 1: Tiêu đề
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp) // Thêm padding
        ) {
            Text(
                text = "Nghe và viết lại", // <-- Sửa tiêu đề
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = card.meaning, // <-- Hiển thị nghĩa
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cụm 2: Ô nhập liệu
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = userAnswer,
                onValueChange = onUserAnswerChange, // <-- Dùng callback
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .focusRequester(focusRequester),
                label = { Text("Nhập câu trả lời...") },
                singleLine = true,
                // SỬA: Vô hiệu hóa khi đang check
                enabled = !isChecking,
                // SỬA: Lỗi (isError) sẽ do parent quyết định
                isError = isChecking,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.None
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (userAnswer.isNotBlank()) {
                            onCheckFromKeyboard() // <-- Dùng callback
                            keyboardController?.hide()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // (Nút Hand-writing giữ nguyên)
            OutlinedButton(
                onClick = { /* TODO */ },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
            ) {
                Icon(Icons.Default.Edit, "Hand-writing", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hand-writing")
            }
        }

        // SỬA: XÓA BỎ "Cụm 3" (Nút Kiểm tra)
    }
}