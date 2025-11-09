package com.uilover.project247.LearningActivity.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uilover.project247.data.models.Flashcard
import com.uilover.project247.utils.parseHtmlToAnnotatedString
import com.uilover.project247.utils.getWordTypeAbbreviation


@Composable
fun FlashcardView(card: Flashcard, onComplete: () -> Unit, onKnowWord: () -> Unit) {

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

        // Cụm 1: Nút Âm thanh & Tốc độ (Giảm kích thước)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            val buttonColor = Color(0xFFFFEB3B)
            FilledTonalIconButton(
                onClick = { /* TODO: Play audio */ },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White,
                    contentColor = buttonColor
                )
            ) {
                Icon(Icons.Default.VolumeUp, "Phát âm thanh", modifier = Modifier.size(28.dp))
            }
            FilledTonalIconButton(
                onClick = { /* TODO: Play slow audio */ },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White,
                    contentColor = buttonColor
                )
            ) {
                Icon(Icons.Default.AccessTime, "Phát chậm", modifier = Modifier.size(28.dp))
            }
        }

        // Cụm 2: Thẻ từ vựng (Card - Tăng kích thước)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isFlipped = !isFlipped },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            // Thêm "chiều sâu" 3D cho đẹp
                            cameraDistance = 12 * density
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 4b: Logic hiển thị 2 mặt
                    // Nếu chưa lật quá 90 độ, hiện MẶT TRƯỚC (Câu ví dụ)
                    if (rotation < 90f) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Show image if available
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
                            
                            // Hiển thị câu ví dụ với HTML formatting (từ in đậm, gạch chân)
                            Text(
                                text = parseHtmlToAnnotatedString(card.contextSentence),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        // Nếu đã lật > 90 độ, hiện MẶT SAU (Word + Pronunciation + Meaning + Type)
                        // (Phải xoay 180 độ để nó không bị ngược)
                        Column(
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                rotationY = 180f // <-- Quay mặt sau lại
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Word (từ vựng)
                            Text(
                                text = card.word,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF00C853) // Màu xanh
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Pronunciation (phát âm)
                            Text(
                                text = card.pronunciation,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Meaning (nghĩa)
                            Text(
                                text = card.meaning,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Word Type (loại từ - viết tắt trong ngoặc tròn)
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