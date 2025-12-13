package com.uilover.project247.DashboardActivity.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.ReviewActivity.Model.ReviewViewModel
import com.uilover.project247.data.models.ReviewStats
import kotlinx.coroutines.delay

/**
 * Review Dashboard Screen - Strict Spaced Repetition with Countdown Timer
 */
@Composable
fun ReviewScreenContent(
    viewModel: ReviewViewModel,
    modifier: Modifier = Modifier,
    onReviewTopicClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)?,
    onStartReviewSession: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.stats
    
    // CRITICAL: Use ViewModel's mapped state (GLOBAL TIMER)
    val isReviewAvailable = uiState.isReviewAvailable
    val nextReviewTimestamp = uiState.nextReviewTimestamp
    val dueCount = uiState.dueCount
    
    // Live countdown state
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Update countdown every second (GLOBAL TIMER)
    LaunchedEffect(nextReviewTimestamp) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
            
            // CRITICAL: Auto-refresh when countdown reaches 0
            if (nextReviewTimestamp != null && currentTime >= nextReviewTimestamp) {
                viewModel.loadReviewStats()  // Refresh to show green button
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFB39DDB), // Purple 200
                            Color(0xFF7E57C2)  // Purple 500
                        )
                    )
                )
                .padding(16.dp)
        ) {
            NotebookHeader(totalWords = stats.totalWordsInNotebook)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bar Chart (in card)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            ReviewBarChart(stats = stats)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // STATE A: Words are ready (dueCount > 0) - GRADIENT BUTTON
        if (isReviewAvailable) {
            Text(
                text = "Sẵn sàng ôn tập: $dueCount từ đã đến hạn",
                fontSize = 16.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStartReviewSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF66BB6A), // Green 400
                                    Color(0xFF2E7D32)  // Green 800
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ôn tập ngay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        // STATE B: Countdown Timer (nextReviewTimestamp exists)
        else if (nextReviewTimestamp != null) {
            CountdownCard(
                nextReviewTime = nextReviewTimestamp,
                currentTime = currentTime
            )
        }
        // STATE C: Empty (No words in notebook)
        else {
            Text(
                text = "Bạn đã học hết từ vựng! Hãy học thêm từ mới.",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = false
            ) {
                Text(
                    text = "Chưa có từ để ôn tập",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // DEBUG BUTTONS - Reset & Clear Progress
        DebugButtons(
            onResetAllProgress = { viewModel.resetAllProgress() },
            onClearAllProgress = { viewModel.clearAllProgress() }
        )
        
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Debug Buttons - Reset & Clear Progress for Testing
 */
@Composable
private fun DebugButtons(
    onResetAllProgress: () -> Unit,
    onClearAllProgress: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🛠️ Testing Tools",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9E9E9E)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reset All Progress Button
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Reset All\n(→ Level 1)",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
            
            // Clear All Progress Button
            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFF44336)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Clear All\n(Delete)",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
    
    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Progress?") },
            text = { 
                Text("Tất cả từ sẽ quay về Level 1 và có thể ôn tập ngay lập tức.\n\nDùng để test lại flow từ đầu.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetAllProgress()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = Color(0xFFFF9800))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Progress?") },
            text = { 
                Text("⚠️ XÓA TOÀN BỘ tiến trình!\n\nTất cả từ sẽ biến mất khỏi sổ tay.\n\nDùng để test học từ đầu.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllProgress()
                        showClearDialog = false
                    }
                ) {
                    Text("Delete All", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Countdown Card - Show timer until next review
 */
@Composable
private fun CountdownCard(
    nextReviewTime: Long,
    currentTime: Long
) {
    val timeLeft = (nextReviewTime - currentTime).coerceAtLeast(0)
    val formattedTime = formatTimeLeft(timeLeft)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEEEEEE)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = "Countdown",
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF9E9E9E)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Chuẩn bị ôn tập",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Countdown Timer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EA),
                    letterSpacing = 2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Thời gian còn lại",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Format time left in HH:MM:SS
 */
private fun formatTimeLeft(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Header: "Sổ tay đã có X từ"
 */
@Composable
private fun NotebookHeader(totalWords: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Icon sổ tay
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📖",
                fontSize = 32.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Text "Sổ tay đã có X từ"
        Text(
            text = "Sổ tay đã có $totalWords từ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )
    }
}

/**
 * Bar Chart - 5 cột theo level
 */
@Composable
private fun ReviewBarChart(stats: ReviewStats) {
    val maxCount = stats.getMaxCount().coerceAtLeast(1)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📊 Phân bố theo mức độ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF333333)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Số lượng từ trong mỗi level ôn tập",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chart area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            for (level in 1..5) {
                BarColumn(
                    level = level,
                    count = stats.getLevelCount(level),
                    maxCount = maxCount
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (level in 1..5) {
                Text(
                    text = "Level $level",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Một cột trong biểu đồ
 */
@Composable
private fun BarColumn(
    level: Int,
    count: Int,
    maxCount: Int
) {
    val heightFraction = if (maxCount > 0) count.toFloat() / maxCount else 0f
    val animatedHeight by animateFloatAsState(
        targetValue = heightFraction,
        animationSpec = tween(durationMillis = 800),
        label = "barHeight"
    )
    
    val barColor = when (level) {
        1 -> Color(0xFFFF6B6B) // Đỏ nhạt
        2 -> Color(0xFFFFD93D) // Vàng
        3 -> Color(0xFF6BCF7F) // Xanh lá
        4 -> Color(0xFF4D96FF) // Xanh dương
        5 -> Color(0xFF9D84B7) // Tím
        else -> Color.Gray
    }
    
    Column(
        modifier = Modifier.width(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Số lượng từ
        Text(
            text = "$count từ",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(animatedHeight.coerceAtLeast(0.05f))
                .background(barColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        )
    }
}
