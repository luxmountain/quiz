package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uilover.project247.data.models.Flashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteWordView(
    card: Flashcard,
    userAnswer: String,
    onUserAnswerChange: (String) -> Unit,
    onCheckFromKeyboard: () -> Unit,
    isChecking: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val correctWord = card.word
    val wordLength = correctWord.length

    // Logic tạo Hint ngẫu nhiên (Giữ nguyên)
    val hints: Map<Int, Char> = remember(card) {
        if (correctWord.isBlank()) {
            emptyMap()
        } else {
            val maxHints = (wordLength / 2).coerceAtLeast(1)
            val hintCount = (2..3).random().coerceAtMost(maxHints)
            val hintIndices = (0 until wordLength).shuffled().take(hintCount)
            hintIndices.map { index ->
                index to correctWord[index]
            }.toMap()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Cụm 1: Tiêu đề (Giữ nguyên)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("Điền từ", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(card.meaning, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cụm 2: Ô nhập liệu
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BasicTextField (ẨN) - (Giữ nguyên)
            // 1. Tạo biến state cục bộ để quản lý cả Text và Con trỏ
            var textFieldValueState by remember {
                mutableStateOf(
                    androidx.compose.ui.text.input.TextFieldValue(
                        text = userAnswer,
                        selection = androidx.compose.ui.text.TextRange(userAnswer.length)
                    )
                )
            }

            // 2. Đồng bộ: Khi userAnswer từ bên ngoài thay đổi (ví dụ chuyển câu hỏi mới), cập nhật lại state cục bộ
            LaunchedEffect(userAnswer) {
                if (textFieldValueState.text != userAnswer) {
                    textFieldValueState = textFieldValueState.copy(
                        text = userAnswer,
                        selection = androidx.compose.ui.text.TextRange(userAnswer.length)
                    )
                }
            }

            BasicTextField(
                // Dùng state cục bộ (có chứa vị trí con trỏ) thay vì chỉ dùng String
                value = textFieldValueState,
                onValueChange = { newTextFieldValue ->
                    val newInput = newTextFieldValue.text

                    // --- LOGIC XỬ LÝ DẤU CÁCH (Code cũ của bạn) ---
                    if (newInput.length <= wordLength) {
                        val sb = StringBuilder()
                        var inputIndex = 0

                        for (i in correctWord.indices) {
                            if (inputIndex >= newInput.length) break

                            val charInCorrect = correctWord[i]
                            val charInInput = newInput[inputIndex]

                            if (charInCorrect == ' ') {
                                sb.append(' ')
                                if (charInInput == ' ') {
                                    inputIndex++
                                }
                            } else {
                                if (charInInput == ' ') {
                                    inputIndex++
                                    if (inputIndex < newInput.length) {
                                        sb.append(newInput[inputIndex])
                                        inputIndex++
                                    }
                                } else {
                                    sb.append(charInInput)
                                    inputIndex++
                                }
                            }
                        }

                        val processedText = sb.toString()

                        // --- QUAN TRỌNG: CẬP NHẬT CON TRỎ ---
                        // Sau khi xử lý xong, ta ép con trỏ về cuối chuỗi (hoặc tính toán vị trí mới)
                        // Với game điền từ này, ép về cuối chuỗi là giải pháp an toàn nhất để tránh lỗi nhảy chữ.
                        textFieldValueState = newTextFieldValue.copy(
                            text = processedText,
                            selection = androidx.compose.ui.text.TextRange(processedText.length)
                        )

                        // Cập nhật ngược lại cho ViewModel
                        onUserAnswerChange(processedText)
                    }
                },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .height(0.dp)
                    .graphicsLayer { alpha = 0f },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.None
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (userAnswer.isNotBlank()) {
                            onCheckFromKeyboard()
                            keyboardController?.hide()
                        }
                    }
                ),
                enabled = !isChecking
            )
//            BasicTextField(
//                value = userAnswer,
//                onValueChange = {
//                    if (it.length <= wordLength) {
//                        onUserAnswerChange(it)
//                    }
//                },
//                modifier = Modifier
//                    .focusRequester(focusRequester)
//                    .fillMaxWidth()
//                    .height(0.dp)
//                    .graphicsLayer { alpha = 0f },
//                keyboardOptions = KeyboardOptions(
//                    imeAction = ImeAction.Done,
//                    capitalization = KeyboardCapitalization.None
//                ),
//                keyboardActions = KeyboardActions(
//                    onDone = {
//                        if (userAnswer.isNotBlank()) {
//                            onCheckFromKeyboard()
//                            keyboardController?.hide()
//                        }
//                    }
//                ),
//                enabled = !isChecking
//            )

            // Hàng (Row) hiển thị các ô gạch chân
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable { focusRequester.requestFocus() }
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- BẮT ĐẦU SỬA LOGIC ---
//                repeat(wordLength) { index ->
//
//                    // 1. Ký tự để hiển thị: Ưu tiên (1) User gõ, (2) Hint
//                    val charToShow: Char? = userAnswer.getOrNull(index) ?: hints[index]
//
//                    // 2. Màu của ký tự:
//                    val charColor = when {
//                        userAnswer.getOrNull(index) != null -> Color.Black // 1. User đã gõ -> Màu đen
//                        hints.containsKey(index) -> Color.Gray         // 2. Là Hint -> Màu xám
//                        else -> Color.Black                            // 3. Rỗng (màu gì cũng được)
//                    }
//
//                    // 3. Màu của gạch chân:
//                    val isCurrentCursorPosition = (index == userAnswer.length) && !isChecking
//
//                    val lineColor = when {
//                        // A. Đã bấm "Kiểm tra"
//                        isChecking -> {
//                            // So sánh ký tự (không phân biệt hoa thường)
//                            val isCorrectChar = userAnswer.getOrNull(index)?.lowercaseChar() == correctWord.getOrNull(index)?.lowercaseChar()
//                            if (isCorrectChar) Color(0xFF00C853) // Xanh lá
//                            else Color.Red // Đỏ
//                        }
//                        // B. Đang gõ, tại vị trí con trỏ
//                        isCurrentCursorPosition -> MaterialTheme.colorScheme.primary // Xanh dương
//                        // C. Mặc định
//                        else -> Color.Gray
//                    }
//
//                    // 4. Render 1 ô ký tự
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text(
//                            text = charToShow?.toString() ?: "",
//                            style = MaterialTheme.typography.headlineSmall.copy(
//                                fontWeight = FontWeight.Normal,
//                                color = charColor // <-- Áp dụng màu chữ
//                            ),
//                            modifier = Modifier.width(24.dp),
//                            textAlign = TextAlign.Center
//                        )
//                        Spacer(modifier = Modifier.height(2.dp))
//                        Box(
//                            modifier = Modifier
//                                .width(24.dp)
//                                .height(2.dp)
//                                .background(lineColor) // <-- Áp dụng màu gạch chân
//                        )
//                    }
//
//                    if (index < wordLength - 1) {
//                        Spacer(modifier = Modifier.width(4.dp))
//                    }
//                }
                repeat(wordLength) { index ->
                    val charInCorrectWord = correctWord[index]

                    // 1. KIỂM TRA: Nếu ký tự gốc là DẤU CÁCH
                    if (charInCorrectWord == ' ') {
                        // Chỉ render một khoảng trống rộng để tách từ, KHÔNG render gạch chân
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    // 2. KIỂM TRA: Nếu là KÝ TỰ THƯỜNG (Logic cũ của bạn)
                    else {
                        // -- Logic tính toán hiển thị (giữ nguyên) --
                        val charToShow: Char? = userAnswer.getOrNull(index) ?: hints[index]

                        val charColor = when {
                            userAnswer.getOrNull(index) != null -> Color.Black
                            hints.containsKey(index) -> Color.Gray
                            else -> Color.Black
                        }

                        val isCurrentCursorPosition = (index == userAnswer.length) && !isChecking

                        val lineColor = when {
                            isChecking -> {
                                val isCorrectChar = userAnswer.getOrNull(index)?.lowercaseChar() == correctWord.getOrNull(index)?.lowercaseChar()
                                if (isCorrectChar) Color(0xFF00C853) else Color.Red
                            }
                            isCurrentCursorPosition -> MaterialTheme.colorScheme.primary
                            else -> Color.Gray
                        }

                        // -- Render chữ và gạch chân --
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = charToShow?.toString() ?: "",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = charColor
                                ),
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.dp)
                                    .background(lineColor) // Gạch chân chỉ hiện ở đây
                            )
                        }

                        // Khoảng cách nhỏ giữa các ký tự chữ cái (để các gạch chân không dính nhau)
                        // Chỉ thêm nếu ký tự tiếp theo không phải là dấu cách (vì dấu cách đã có Spacer to ở trên rồi)
                        if (index < wordLength - 1 && correctWord[index + 1] != ' ') {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
                // --- KẾT THÚC SỬA LOGIC ---
            } // Hết Row (gạch chân)

            Spacer(modifier = Modifier.height(24.dp))

            // Nút "Hand-writing" (Giữ nguyên)
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
    }
}