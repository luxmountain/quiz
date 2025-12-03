package com.uilover.project247.StatisticsActivity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.StatisticsActivity.Model.StatisticsViewModel
import com.uilover.project247.StatisticsActivity.components.CalendarHeatmap
import com.uilover.project247.StatisticsActivity.components.WeeklyBarChart

@Composable
fun StatisticsScreenContent(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Refresh data when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        uiState.errorMessage != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠️ Có lỗi xảy ra",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header stats
                item {
                    HeaderStatsCard(
                        totalWords = uiState.totalWordsLearned,
                        totalTime = uiState.totalStudyTime,
                        currentStreak = uiState.learningStreak.currentStreak,
                        longestStreak = uiState.learningStreak.longestStreak
                    )
                }
                
                // Weekly Bar Chart
                item {
                    if (uiState.weeklyStats != null) {
                        WeeklyBarChart(weeklyStats = uiState.weeklyStats!!)
                    }
                }
                
                // Calendar Heatmap
                item {
                    if (uiState.monthlyHeatmap != null) {
                        CalendarHeatmap(monthlyHeatmap = uiState.monthlyHeatmap!!)
                    }
                }
                
                // Motivation message
                item {
                    MotivationCard(currentStreak = uiState.learningStreak.currentStreak)
                }
            }
        }
    }
}

@Composable
private fun HeaderStatsCard(
    totalWords: Int,
    totalTime: Int,
    currentStreak: Int,
    longestStreak: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF6200EA),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = "📈 Thống kê học tập",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 22.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Tổng từ đã học",
                value = totalWords.toString(),
                emoji = "📚",
                color = Color.White
            )
            
            StatItem(
                label = "Thời gian học",
                value = "${totalTime}p",
                emoji = "⏱️",
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Chuỗi hiện tại",
                value = "$currentStreak ngày",
                emoji = "🔥",
                color = Color.White
            )
            
            StatItem(
                label = "Kỷ lục chuỗi",
                value = "$longestStreak ngày",
                emoji = "🏆",
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    emoji: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Text(text = emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 20.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.9f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MotivationCard(currentStreak: Int) {
    val message = when {
        currentStreak == 0 -> "Hãy bắt đầu học hôm nay để xây dựng chuỗi học tập! 💪"
        currentStreak < 3 -> "Tuyệt vời! Hãy tiếp tục duy trì chuỗi học tập! 🌟"
        currentStreak < 7 -> "Bạn đang làm rất tốt! Chuỗi học tập ấn tượng đấy! 🔥"
        currentStreak < 30 -> "Tuyệt vời! Bạn đang trên con đường thành công! 🚀"
        else -> "Phi thường! Bạn là một học viên xuất sắc! 🏆"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "💡", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
