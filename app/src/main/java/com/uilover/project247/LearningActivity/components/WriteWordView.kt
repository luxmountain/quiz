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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("Điền từ", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(card.meaning, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = userAnswer,
                onValueChange = {
                    if (it.length <= wordLength) {
                        onUserAnswerChange(it)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF0F4F8)
                            )
                        )
                    )
                    .clickable { focusRequester.requestFocus() }
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(wordLength) { index ->
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
                            if (isCorrectChar) Color(0xFF00C853)
                            else Color.Red
                        }
                        isCurrentCursorPosition -> MaterialTheme.colorScheme.primary
                        else -> Color.Gray
                    }

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
                                .background(lineColor)
                        )
                    }

                    if (index < wordLength - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* TODO: Mở màn hình viết tay */ },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFE0F7FA),
                                    Color(0xFFB2EBF2)
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Hand-writing",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF006064)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Hand-writing",
                            color = Color(0xFF006064),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}