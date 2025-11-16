package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uilover.project247.R
import com.uilover.project247.data.models.Flashcard
import com.uilover.project247.utils.TextToSpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenWriteView(
    card: Flashcard,
    userAnswer: String,
    onUserAnswerChange: (String) -> Unit,
    onCheckFromKeyboard: () -> Unit,
    isChecking: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- Thiết lập TTS ---
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }

    // Tự động phát âm và focus khi màn hình xuất hiện
    LaunchedEffect(card) {
        focusRequester.requestFocus()
        ttsManager.speak(card.word) // Phát âm từ
    }

    // Dọn dẹp TTS khi Composable bị hủy
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }
    // ---------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Căn trên cùng
    ) {
        // Cụm 1: Tiêu đề và nút loa
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text(
                text = "Nghe và viết lại",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Các nút âm thanh (Giống ảnh)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilledTonalIconButton(
                    onClick = { ttsManager.speak(card.word) },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_loudspeaker),
                        contentDescription = "Phát âm thanh",
                        modifier = Modifier.size(36.dp),
                        tint = Color.Unspecified  // Màu cam
                    )
                }
                FilledTonalIconButton(
                    onClick = {
                        ttsManager.setSpeechRate(0.5f)
                        ttsManager.speak(card.word)
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_snail),
                        contentDescription = "Phát chậm",
                        modifier = Modifier.size(36.dp),
                        tint = Color.Unspecified  // Màu cam
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Ô nhập liệu (Giống ảnh)
            OutlinedTextField(
                value = userAnswer,
                onValueChange = onUserAnswerChange,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .focusRequester(focusRequester),
                label = { Text("Gõ lại từ bạn nghe được") },
                singleLine = true,
                enabled = !isChecking,
                isError = isChecking,
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
                )
            )
        }
    }
}