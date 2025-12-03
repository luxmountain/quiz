package com.uilover.project247.DashboardActivity.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.uilover.project247.data.models.InAppTourStep
import kotlin.math.roundToInt

data class TourTarget(
    val id: String,
    var bounds: Rect = Rect.Zero
)

@Composable
fun InAppTourOverlay(
    currentStep: Int,
    tourTargets: Map<String, Rect>,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    val density = LocalDensity.current
    
    val steps = remember {
        listOf(
            InAppTourStep(
                title = "Chào mừng!",
                description = "Hãy để tôi hướng dẫn bạn cách sử dụng app học từ vựng hiệu quả!",
                targetId = "welcome",
                emoji = "👋"
            ),
            InAppTourStep(
                title = "Chọn Level",
                description = "Nhấn vào đây để chọn cấp độ phù hợp: Beginner, Intermediate hoặc Advanced",
                targetId = "level_selector",
                emoji = "📚"
            ),
            InAppTourStep(
                title = "Chủ đề học tập",
                description = "Nhấn vào chủ đề để bắt đầu học từ vựng bằng flashcard. Hoàn thành 100% để mở khóa chủ đề tiếp theo!",
                targetId = "topic_item",
                emoji = "🎯"
            ),
            InAppTourStep(
                title = "Tra từ điển",
                description = "Tra cứu nghĩa, phát âm, ví dụ và từ đồng nghĩa của bất kỳ từ nào",
                targetId = "tab_search",
                emoji = "🔍"
            ),
            InAppTourStep(
                title = "Ôn tập từ vựng",
                description = "Luyện tập lại các từ đã học với nhiều dạng bài tập khác nhau",
                targetId = "tab_board",
                emoji = "📖"
            ),
            InAppTourStep(
                title = "Hội thoại thực tế",
                description = "Thực hành với các đoạn hội thoại mẫu trong tình huống hàng ngày",
                targetId = "tab_chat",
                emoji = "💬"
            ),
            InAppTourStep(
                title = "AI Study Assistant",
                description = "Trợ lý AI giúp bạn học từ vựng thông minh hơn với gợi ý cá nhân hóa",
                targetId = "tab_hub",
                emoji = "🤖"
            )
        )
    }
    
    if (currentStep >= steps.size) {
        onComplete()
        return
    }
    
    val step = steps[currentStep]
    val targetBounds = tourTargets[step.targetId] ?: Rect.Zero
    val hasTarget = targetBounds != Rect.Zero && step.targetId != "welcome"
    
    // Animation
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "overlay_alpha"
    )
    
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f)
    ) {
        // Dark overlay with hole
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (currentStep == steps.size - 1) {
                        onComplete()
                    } else {
                        onNext()
                    }
                }
        ) {
            canvasSize = size
            
            if (hasTarget) {
                // Create path with hole for the target element
                val padding = with(density) { 8.dp.toPx() }
                val cornerRadius = with(density) { 12.dp.toPx() }
                
                // Debug: Log target bounds
                android.util.Log.d("ProductTour", "Step: ${step.targetId}, Bounds: $targetBounds")
                
                val holePath = Path().apply {
                    // Outer rectangle (full screen)
                    addRect(
                        androidx.compose.ui.geometry.Rect(
                            0f, 0f, size.width, size.height
                        )
                    )
                    
                    // Inner rectangle (the hole for element)
                    addRoundRect(
                        RoundRect(
                            left = targetBounds.left - padding,
                            top = targetBounds.top - padding,
                            right = targetBounds.right + padding,
                            bottom = targetBounds.bottom + padding,
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                        )
                    )
                }
                
                // Draw dark overlay with hole using FillType.EvenOdd
                drawPath(
                    path = holePath.apply { fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd },
                    color = Color.Black.copy(alpha = 0.75f)
                )
            } else {
                // No target - just dark overlay
                drawRect(Color.Black.copy(alpha = 0.75f))
            }
        }
        
        // Tooltip card
        if (hasTarget) {
            val tooltipY = with(density) {
                val tooltipHeight = 250.dp.toPx()
                when {
                    targetBounds.top > canvasSize.height / 2 -> {
                        // Element ở nửa dưới màn hình -> tooltip ở trên
                        (targetBounds.top - tooltipHeight - 16.dp.toPx()).coerceAtLeast(16.dp.toPx())
                    }
                    else -> {
                        // Element ở nửa trên màn hình -> tooltip ở dưới
                        (targetBounds.bottom + 16.dp.toPx()).coerceAtMost(canvasSize.height - tooltipHeight - 16.dp.toPx())
                    }
                }
            }
            
            TooltipCard(
                step = step,
                currentStep = currentStep,
                totalSteps = steps.size,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { IntOffset(with(density) { 24.dp.roundToPx() }, tooltipY.roundToInt()) }
                    .fillMaxWidth(0.85f),
                onNext = {
                    if (currentStep == steps.size - 1) {
                        onComplete()
                    } else {
                        onNext()
                    }
                },
                onSkip = onSkip
            )
        } else {
            // Welcome card when no target
            WelcomeCard(
                modifier = Modifier.align(Alignment.Center),
                onStart = onNext,
                onSkip = onSkip
            )
        }
    }
}

@Composable
private fun TooltipCard(
    step: InAppTourStep,
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = modifier
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = step.emoji,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "${currentStep + 1}/$totalSteps",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                IconButton(
                    onClick = onSkip,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = step.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EA)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = step.description,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress indicator
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF6200EA),
                trackColor = Color(0xFFEDE7F6)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EA)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentStep == totalSteps - 1) "Hoàn thành! 🎉" else "Tiếp theo →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.85f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👋",
                fontSize = 80.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Chào mừng đến MochiVocab!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Hãy để tôi hướng dẫn bạn cách sử dụng app học từ vựng hiệu quả!",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EA)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Bắt đầu hướng dẫn →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(onClick = onSkip) {
                Text(
                    text = "Bỏ qua",
                    color = Color.Gray
                )
            }
        }
    }
}
