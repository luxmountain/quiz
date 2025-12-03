package com.uilover.project247.StatisticsActivity.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.uilover.project247.data.models.DailyStats
import com.uilover.project247.data.models.WeeklyStats
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeeklyBarChart(
    weeklyStats: WeeklyStats,
    modifier: Modifier = Modifier
) {
    val dailyStats = weeklyStats.dailyStats
    val maxWords = dailyStats.maxOfOrNull { it.wordsReviewed } ?: 1
    
    val barColor = Color(0xFF6200EA)
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    selectedDayIndex = null
                }
        ) {
        Text(
            text = "📊 Hoạt động 7 ngày qua",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Số từ đã ôn tập",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bar Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            dailyStats.forEachIndexed { index, dayStats ->
                val barHeight = if (maxWords > 0) {
                    (dayStats.wordsReviewed.toFloat() / maxWords.toFloat()) * 160f
                } else {
                    0f
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Số từ
                    if (dayStats.wordsReviewed > 0) {
                        Text(
                            text = dayStats.wordsReviewed.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = barColor,
                            fontSize = 12.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Bar
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(barHeight.dp.coerceAtLeast(4.dp))
                            .background(
                                if (dayStats.wordsReviewed > 0) barColor else Color.LightGray.copy(alpha = 0.3f),
                                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedDayIndex = if (selectedDayIndex == index) null else index
                            }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Ngày
                    Text(
                        text = dateFormat.format(Date(dayStats.date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem(
                label = "Tổng từ",
                value = weeklyStats.getTotalWordsReviewed().toString(),
                emoji = "📚"
            )
            SummaryItem(
                label = "Độ chính xác",
                value = "${weeklyStats.getAverageAccuracy().toInt()}%",
                emoji = "🎯"
            )
        }
        }
        
        // Show tooltip
        selectedDayIndex?.let { index ->
            if (index in dailyStats.indices) {
                val dayStats = dailyStats[index]
                BarTooltip(
                    dayStats = dayStats,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .zIndex(10f)
                )
            }
        }
    }
}

@Composable
private fun BarTooltip(
    dayStats: DailyStats,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi"))
    val dateString = dateFormat.format(Date(dayStats.date))
    
    Surface(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateString.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6200EA),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TooltipItem(
                    emoji = "📚",
                    label = "Số từ",
                    value = dayStats.wordsReviewed.toString()
                )
                TooltipItem(
                    emoji = "⏱️",
                    label = "Thời gian",
                    value = "${dayStats.studyTimeMinutes}p"
                )
                TooltipItem(
                    emoji = "🎯",
                    label = "Độ chính xác",
                    value = "${dayStats.accuracy.toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun TooltipItem(
    emoji: String,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EA),
            fontSize = 14.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    emoji: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EA)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}
