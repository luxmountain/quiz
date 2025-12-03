package com.uilover.project247.StatisticsActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.uilover.project247.data.models.MonthlyHeatmapData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarHeatmap(
    monthlyHeatmap: MonthlyHeatmapData,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var tooltipOffset by remember { mutableStateOf(Pair(0.dp, 0.dp)) }
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, monthlyHeatmap.year)
    calendar.set(Calendar.MONTH, monthlyHeatmap.month)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    
    val maxActivity = monthlyHeatmap.getMaxActivity()
    
    val monthName = SimpleDateFormat("MMMM yyyy", Locale("vi")).format(calendar.time)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                selectedDay = null
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
        Text(
            text = "🔥 Chuỗi học tập",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = monthName.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Week days header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid
        var dayCounter = 1
        var currentWeekDay = firstDayOfWeek
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            while (dayCounter <= daysInMonth) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0..6) {
                        if (i < currentWeekDay && dayCounter == 1) {
                            // Empty cell before first day
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                            )
                        } else if (dayCounter <= daysInMonth) {
                            val activity = monthlyHeatmap.dailyActivityMap[dayCounter] ?: 0
                            val currentDay = dayCounter
                            DayCell(
                                day = currentDay,
                                activity = activity,
                                maxActivity = maxActivity,
                                isSelected = currentDay == selectedDay,
                                onClick = { 
                                    selectedDay = if (selectedDay == currentDay) null else currentDay
                                }
                            )
                            dayCounter++
                        } else {
                            // Empty cell after last day
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                            )
                        }
                    }
                }
                currentWeekDay = 0 // Reset after first week
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ít",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { intensity ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(getHeatmapColor(intensity))
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            Text(
                text = "Nhiều",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
        }
        
        // Show tooltip
        selectedDay?.let { day ->
            val activity = monthlyHeatmap.dailyActivityMap[day] ?: 0
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, monthlyHeatmap.year)
            calendar.set(Calendar.MONTH, monthlyHeatmap.month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateString = dateFormat.format(calendar.time)
            
            DayTooltip(
                date = dateString,
                wordsReviewed = activity,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .zIndex(10f)
            )
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    activity: Int,
    maxActivity: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val intensity = if (maxActivity > 0) {
        (activity.toFloat() / maxActivity.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    
    val backgroundColor = getHeatmapColor(intensity)
    val textColor = if (intensity > 0.5f) Color.White else Color.DarkGray
    
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFF6200EA),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else if (activity == 0) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (activity > 0) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DayTooltip(
    date: String,
    wordsReviewed: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF424242)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📚",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$wordsReviewed từ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getHeatmapColor(intensity: Float): Color {
    return when {
        intensity == 0f -> Color(0xFFEEEEEE)
        intensity < 0.25f -> Color(0xFFE1BEE7) // Light purple
        intensity < 0.5f -> Color(0xFFBA68C8)  // Medium purple
        intensity < 0.75f -> Color(0xFF9C27B0) // Dark purple
        else -> Color(0xFF6A1B9A)              // Darkest purple
    }
}

private fun SimpleDateFormat(pattern: String, locale: Locale): java.text.SimpleDateFormat {
    return java.text.SimpleDateFormat(pattern, locale)
}
