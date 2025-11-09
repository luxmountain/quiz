package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uilover.project247.data.VocabularyWord
import  com.uilover.project247.LearningActivity.Model.CheckResult


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteWordView(
    word: VocabularyWord,
    checkResult: CheckResult,
    onCheck: (String) -> Unit,
    onClearResult: () -> Unit
) {
    // 1. State nội bộ cho ô nhập liệu
    var userAnswer by rememberSaveable { mutableStateOf("") }

    // 2. State cho Focus (để tự động mở bàn phím)
    val focusRequester = remember { FocusRequester() }

    // 3. State để điều khiển bàn phím (đóng bàn phím)
    val keyboardController = LocalSoftwareKeyboardController.current

    // Tự động focus vào ô nhập liệu khi màn hình xuất hiện
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // 4. Quyết định màu sắc dựa trên trạng thái
    val buttonColor = when (checkResult) {
        CheckResult.NEUTRAL -> Color.LightGray.copy(alpha = 0.5f)
        CheckResult.CORRECT -> Color(0xFF00C853) // Xanh lá
        CheckResult.INCORRECT -> Color(0xFFD32F2F) // Đỏ
    }
    val buttonContentColor = when (checkResult) {
        CheckResult.NEUTRAL -> Color.DarkGray
        CheckResult.CORRECT -> Color.White
        CheckResult.INCORRECT -> Color.White
    }
    val buttonText = when (checkResult) {
        CheckResult.NEUTRAL -> "Kiểm tra"
        CheckResult.CORRECT -> "Chính xác!"
        CheckResult.INCORRECT -> "Sai rồi!"
    }

    // Bố cục chính
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Đẩy 3 cụm (trên, giữa, dưới)
    ) {
        // Cụm 1: Tiêu đề
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Điền từ",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = word.meaning,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Cụm 2: Ô nhập liệu
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = userAnswer,
                onValueChange = {
                    userAnswer = it
                    // Khi người dùng bắt đầu gõ lại, reset trạng thái "Sai"
                    onClearResult()
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .focusRequester(focusRequester), // Gắn focus
                label = { Text("Nhập câu trả lời...") },
                singleLine = true,
                isError = checkResult == CheckResult.INCORRECT, // Báo lỗi nếu sai
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done, // Nút "Done" trên bàn phím
                    capitalization = KeyboardCapitalization.None // Tắt tự động viết hoa
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Khi bấm "Done" trên bàn phím -> Kiểm tra
                        if (userAnswer.isNotBlank()) {
                            onCheck(userAnswer)
                            keyboardController?.hide() // Đóng bàn phím
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nút "Hand-writing" (Giả)
            OutlinedButton(
                onClick = { /* TODO: Mở màn hình viết tay */ },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Hand-writing",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hand-writing")
            }
        }

        // Cụm 3: Nút "Kiểm tra"
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (userAnswer.isNotBlank()) {
                        onCheck(userAnswer)
                        keyboardController?.hide() // Đóng bàn phím
                    }
                },
                enabled = checkResult != CheckResult.CORRECT, // Vô hiệu hóa khi đã đúng
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonContentColor
                )
            ) {
                Text(
                    buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}