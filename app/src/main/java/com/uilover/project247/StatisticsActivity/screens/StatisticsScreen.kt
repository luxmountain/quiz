package com.uilover.project247.StatisticsActivity.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.StatisticsActivity.Model.StatisticsViewModel
import com.uilover.project247.StatisticsActivity.components.CalendarHeatmap
import com.uilover.project247.StatisticsActivity.components.WeeklyBarChart
import com.uilover.project247.R

@Composable
fun StatisticsScreenContent(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
                item {
                    HeaderStatsCard(
                        totalWords = uiState.totalWordsLearned,
                        totalTime = uiState.totalStudyTime,
                        currentStreak = uiState.learningStreak.currentStreak,
                        longestStreak = uiState.learningStreak.longestStreak
                    )
                }

                item {
                    if (uiState.weeklyStats != null) {
                        WeeklyBarChart(weeklyStats = uiState.weeklyStats!!)
                    }
                }

                item {
                    if (uiState.monthlyHeatmap != null) {
                        CalendarHeatmap(monthlyHeatmap = uiState.monthlyHeatmap!!)
                    }
                }

                item {
                    MotivationCard(currentStreak = uiState.learningStreak.currentStreak)
                }
            }
        }
    }
}

@Composable
fun HeaderStatsCard(
    totalWords: Int,
    totalTime: Int,
    currentStreak: Int,
    longestStreak: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "📈 Thống kê học tập",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Hàng 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard3D(
                label = "Tổng từ",
                subLabel = "$totalWords từ",
                iconResId = R.drawable.brain,
                backgroundColor = Color(0xFF64B5F6),
                modifier = Modifier.weight(1f)
            )

            StatCard3D(
                label = "Thời gian",
                subLabel = "${totalTime}p",
                iconResId = R.drawable.watch,
                backgroundColor = Color(0xFFFF8A65),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hàng 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard3D(
                label = "Chuỗi",
                subLabel = "$currentStreak ngày",
                iconResId = R.drawable.onfire,
                backgroundColor = Color(0xFFFFB74D),
                modifier = Modifier.weight(1f)
            )

            StatCard3D(
                label = "Kỷ lục",
                subLabel = "$longestStreak ngày",
                iconResId = R.drawable.cup,
                backgroundColor = Color(0xFF9575CD),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard3D(
    label: String,
    subLabel: String,
    iconResId: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = backgroundColor.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
            )

            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 20.sp
                )
            }
        }
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