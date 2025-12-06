package com.uilover.project247.StatisticsActivity.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Cấu hình Calendar
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, monthlyHeatmap.year)
    calendar.set(Calendar.MONTH, monthlyHeatmap.month)
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Điều chỉnh để T2 là đầu tuần hoặc CN là đầu tuần tùy theo Locale (ở đây code cũ dùng CN = 0)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

    val maxActivity = monthlyHeatmap.getMaxActivity()
    val monthName = SimpleDateFormat("MMMM yyyy", Locale("vi")).format(calendar.time)

    // Màu chủ đạo: Orange Gradient
    val baseColor = Color(0xFFFFB74D)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { selectedDay = null }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            // --- Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🔥 Chuỗi học tập",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = monthName.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                // Hiển thị tổng số ngày đã học trong tháng (Optional badge)
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${monthlyHeatmap.dailyActivityMap.count { it.value > 0 }} ngày",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEF6C00),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Week days header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.width(36.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Calendar Grid ---
            var dayCounter = 1
            var currentWeekDay = firstDayOfWeek // 0 = CN, 1 = T2...

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                while (dayCounter <= daysInMonth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0..6) {
                            if ((dayCounter == 1 && i < currentWeekDay) || dayCounter > daysInMonth) {
                                // Ô trống
                                Box(modifier = Modifier.size(36.dp))
                            } else {
                                val activity = monthlyHeatmap.dailyActivityMap[dayCounter] ?: 0
                                val currentDay = dayCounter

                                DayCell(
                                    day = currentDay,
                                    activity = activity,
                                    maxActivity = maxActivity,
                                    isSelected = currentDay == selectedDay,
                                    baseColor = baseColor,
                                    onClick = {
                                        selectedDay = if (selectedDay == currentDay) null else currentDay
                                    }
                                )
                                dayCounter++
                            }
                        }
                    }
                    currentWeekDay = 0 // Reset về CN cho các tuần tiếp theo
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Legend (Chú thích) ---
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

                val sampleIntensities = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
                sampleIntensities.forEach { intensity ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(getOrangeHeatmapColor(intensity))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nhiều",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        // --- Floating Tooltip ---
        // Sử dụng Box BoxScope để căn chỉnh tooltip đè lên trên
        AnimatedVisibility(
            visible = selectedDay != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter) // Xuất hiện ở phía trên cùng của Card
                .padding(top = 8.dp)
                .zIndex(10f)
        ) {
            selectedDay?.let { day ->
                val activity = monthlyHeatmap.dailyActivityMap[day] ?: 0
                val tooltipCalendar = Calendar.getInstance()
                tooltipCalendar.set(Calendar.YEAR, monthlyHeatmap.year)
                tooltipCalendar.set(Calendar.MONTH, monthlyHeatmap.month)
                tooltipCalendar.set(Calendar.DAY_OF_MONTH, day)
                val dateFormat = SimpleDateFormat("EEEE, dd/MM", Locale("vi"))
                val dateString = dateFormat.format(tooltipCalendar.time)

                DayTooltip(
                    date = dateString,
                    wordsReviewed = activity
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    activity: Int,
    maxActivity: Int,
    isSelected: Boolean,
    baseColor: Color,
    onClick: () -> Unit
) {
    val intensity = if (maxActivity > 0) {
        (activity.toFloat() / maxActivity.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val backgroundColor = getOrangeHeatmapColor(intensity)

    // Màu chữ: Trắng nếu nền đậm, Đen nếu nền nhạt
    val textColor = if (intensity > 0.4f) Color.White else Color.Black.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp)) // Bo tròn mềm hơn
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
                        color = Color.Black, // Viền đen để nổi bật màu cam
                        shape = RoundedCornerShape(10.dp)
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
    wordsReviewed: Int
) {
    Surface(
        modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2D2D2D) // Nền tối màu Charcoal
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📚", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$wordsReviewed từ",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Logic màu Gradient Cam dựa trên yêu cầu 0xFFFFB74D
private fun getOrangeHeatmapColor(intensity: Float): Color {
    return when {
        intensity == 0f -> Color(0xFFF5F5F5) // Xám rất nhạt cho ngày không học
        intensity < 0.25f -> Color(0xFFFFE0B2) // Cam rất nhạt (Orange 100)
        intensity < 0.50f -> Color(0xFFFFCC80) // Cam nhạt (Orange 200)
        intensity < 0.75f -> Color(0xFFFFB74D) // Màu yêu cầu (Orange 300)
        else -> Color(0xFFFB8C00) // Cam đậm (Orange 600) cho hoạt động cao nhất
    }
}