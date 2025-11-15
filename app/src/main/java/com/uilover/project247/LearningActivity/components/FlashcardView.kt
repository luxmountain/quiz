package com.uilover.project247.LearningActivity.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// THÊM:
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.uilover.project247.data.models.Flashcard
import com.uilover.project247.utils.TextToSpeechManager
import com.uilover.project247.utils.parseHtmlToAnnotatedString
import com.uilover.project247.utils.getWordTypeAbbreviation
import com.uilover.project247.R

@Composable
fun FlashcardView(card: Flashcard, onComplete: () -> Unit, onKnowWord: () -> Unit) {

    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "flipAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // --- BỐ CỤC MỚI (Cụm 1 và 2 gộp làm 1) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Chiếm hết không gian giữa
                .padding(vertical = 16.dp, horizontal = 8.dp),
            contentAlignment = Alignment.TopCenter // Căn chỉnh nút Loa lên trên cùng
        ) {

            // LỚP 1: CÁC NÚT (LOA, ỐC SÊN)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .zIndex(1f)
                    .offset(y = 28.dp)
            ) {
                val buttonColor = Color(0xFFFFEB3B)
                FilledTonalIconButton(
                    onClick = { ttsManager.speak(card.word) },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White,
                        contentColor = buttonColor
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_loudspeaker),
                        contentDescription = "Phát âm thanh",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Unspecified // <-- SỬA LỖI: Thêm dòng này
                    )
                }
                FilledTonalIconButton(
                    onClick = {
                        ttsManager.setSpeechRate(0.5f)
                        ttsManager.speak(card.word)
                    },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White,
                        contentColor = buttonColor
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_snail),
                        contentDescription = "Phát chậm",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Unspecified // <-- SỬA LỖI: Thêm dòng này
                    )
                }
            }

            // LỚP 2: THẺ FLASHCARD
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp)
                    .clickable { isFlipped = !isFlipped },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                // (Toàn bộ logic lật thẻ bên trong Card giữ nguyên)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12 * density
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (rotation < 90f) {
                        // Mặt trước (Câu ví dụ)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            card.imageUrl.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = card.word,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    placeholder = null,
                                    error = null
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            Text(
                                text = parseHtmlToAnnotatedString(card.contextSentence),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        // Mặt sau (Từ vựng)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = 180f
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = card.word,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF00C853)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = card.pronunciation,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = card.meaning,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "(${getWordTypeAbbreviation(card.wordType)})",
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Icon bàn tay
            if (rotation < 90f) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_left_click),
                    contentDescription = "Tap hint",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(32.dp)
                        .size(48.dp)
                )
            }
        } // --- Hết Box (Cụm gộp 1 & 2) ---

        // Cụm 3: Nút hành động (Giữ nguyên)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White // Chữ màu trắng
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