package com.uilover.project247.LearningActivity.components

import androidx.compose.animation.core.animateFloatAsState // <-- THAY ĐỔI: Import animation
import androidx.compose.animation.core.tween // <-- THAY ĐỔI: Import animation spec
import androidx.compose.foundation.clickable // <-- THAY ĐỔI: Import clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.* // <-- THAY ĐỔI: Import `getValue`, `setValue`, `remember`
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // <-- THAY ĐỔI: Import để xoay
import androidx.compose.ui.text.AnnotatedString // <-- THAY ĐỔI: Import
import androidx.compose.ui.text.SpanStyle // <-- THAY ĐỔI: Import
import androidx.compose.ui.text.buildAnnotatedString // <-- THAY ĐỔI: Import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle // <-- THAY ĐỔI: Import
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.DashboardActivity.components.VocabularyWord

// *** HÀM HELPER ĐỂ TẠO CHỮ IN ĐẬM ***
// (Bạn đã comment nó ra, nhưng chúng ta cần nó cho mặt trước của thẻ)
fun createExampleSentence(sentence: String, wordToBold: String): AnnotatedString {
    return buildAnnotatedString {
        try {
            val startIndex = sentence.indexOf(wordToBold, ignoreCase = true)
            if (startIndex == -1) {
                append(sentence)
                return@buildAnnotatedString
            }
            val endIndex = startIndex + wordToBold.length
            append(sentence.substring(0, startIndex))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(sentence.substring(startIndex, endIndex))
            }
            append(sentence.substring(endIndex))
        } catch (e: Exception) {
            append(sentence)
        }
    }
}


@Composable
fun FlashcardView(word: VocabularyWord, onComplete: () -> Unit, onKnowWord: () -> Unit) {

    // --- BƯỚC 1: Thêm State để biết thẻ lật hay chưa ---
    var isFlipped by remember { mutableStateOf(false) }

    // --- BƯỚC 2: Thêm Animation cho góc quay ---
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600), // Tốc độ lật
        label = "flipAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Cụm 1: Nút Âm thanh & Tốc độ (Giữ nguyên)
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            val buttonColor = Color(0xFFFFEB3B)
            FilledTonalIconButton(
                onClick = { /* TODO: Play audio */ },
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White,
                    contentColor = buttonColor
                )
            ) {
                Icon(Icons.Default.VolumeUp, "Phát âm thanh", modifier = Modifier.size(36.dp))
            }
            FilledTonalIconButton(
                onClick = { /* TODO: Play slow audio */ },
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White,
                    contentColor = buttonColor
                )
            ) {
                Icon(Icons.Default.AccessTime, "Phát chậm", modifier = Modifier.size(36.dp))
            }
        }

        // Cụm 2: Thẻ từ vựng (Card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                // --- BƯỚC 3: Thêm Modifier.clickable để kích hoạt lật ---
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isFlipped = !isFlipped }, // <-- KÍCH HOẠT LẬT
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                // --- BƯỚC 4: Áp dụng animation và logic hiện 2 mặt ---
                Column(
                    // 4a: Áp dụng góc quay (rotationY)
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            // Thêm "chiều sâu" 3D cho đẹp
                            cameraDistance = 12 * density
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 4b: Logic hiển thị 2 mặt
                    // Nếu chưa lật quá 90 độ, hiện MẶT TRƯỚC (Từ vựng)
                    if (rotation < 90f) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // (Bạn đã comment phần này, tôi mở lại vì nó cần cho mặt trước)
                            Text(
                                text = createExampleSentence(
                                    sentence = word.exampleSentence ?: "",
                                    wordToBold = word.word
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                lineHeight = 40.sp
                            )
                        }
                    } else {
                        // Nếu đã lật > 90 độ, hiện MẶT SAU (Nghĩa)
                        // (Phải xoay 180 độ để nó không bị ngược)
                        Column(
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                rotationY = 180f // <-- Quay mặt sau lại
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = word.meaning,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF00C853) // Màu xanh cho nghĩa
                            )
                        }
                    }
                }
            }

            // Icon bàn tay (Giữ nguyên)
            // (Nó sẽ biến mất khi thẻ lật vì logic 'rotation < 90f')
            if (rotation < 90f) { // <-- THAY ĐỔI: Chỉ hiện khi chưa lật
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Tap hint",
                    tint = Color(0xFFFFEB3B).copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(32.dp)
                        .size(48.dp)
                )
            }
        }

        // Cụm 3: Nút hành động (Giữ nguyên)
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray.copy(alpha = 0.5f),
                    contentColor = Color.DarkGray
                )
            ) {
                Text(
                    "Tiếp tục",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onKnowWord) {
                Text(
                    "Mình đã biết từ này",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
    }
}