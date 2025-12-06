package com.uilover.project247.StatisticsActivity.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.uilover.project247.data.models.DailyStats
import com.uilover.project247.data.models.WeeklyStats
import java.text.SimpleDateFormat
import java.util.*
import com.uilover.project247.R


@Composable
fun WeeklyBarChart(
    weeklyStats: WeeklyStats,
    modifier: Modifier = Modifier
) {
    val dailyStats = weeklyStats.dailyStats
    val maxWords = dailyStats.maxOfOrNull { it.wordsReviewed } ?: 1

    val activeColor = Color(0xFFFFB74D)
    val inactiveColor = Color(0xFFFFFAE5)
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
                text = "📊 Hoạt động tuần này",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lượt ôn tập trong tuần (CN - T7)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bar Chart Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyStats.forEachIndexed { index, dayStats ->
                    val isSelected = selectedDayIndex == index
                    val barHeight = if (maxWords > 0) {
                        (dayStats.wordsReviewed.toFloat() / maxWords.toFloat()) * 150f
                    } else {
                        0f
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(barHeight.dp.coerceAtLeast(12.dp))
                                .clip(CircleShape)
                                .background(if (isSelected) activeColor else inactiveColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedDayIndex =
                                        if (selectedDayIndex == index) null else index
                                }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = dayStats.date
                        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.SUNDAY -> "CN"
                            Calendar.MONDAY -> "T2"
                            Calendar.TUESDAY -> "T3"
                            Calendar.WEDNESDAY -> "T4"
                            Calendar.THURSDAY -> "T5"
                            Calendar.FRIDAY -> "T6"
                            Calendar.SATURDAY -> "T7"
                            else -> ""
                        }

                        Text(
                            text = dayOfWeek,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )

                        if (isSelected) {
                            Text(
                                text = dateFormat.format(Date(dayStats.date)),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryItem(
                    label = "Tổng từ",
                    value = weeklyStats.getTotalWordsReviewed().toString(),
                    iconResId = R.drawable.book,
                    backgroundColor = Color(0xFFF6E5FC),
                    modifier = Modifier.weight(1f)
                )

                SummaryItem(
                    label = "Thời gian",
                    value = "${weeklyStats.getTotalStudyTime()}p",
                    iconResId = R.drawable.watch,
                    backgroundColor = Color(0xFFFFE5B4),
                    modifier = Modifier.weight(1f)
                )
                
                SummaryItem(
                    label = "Độ chính xác",
                    value = "${weeklyStats.getAverageAccuracy().toInt()}%",
                    iconResId = R.drawable.target,
                    backgroundColor = Color(0xFFBDE0FE),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Show tooltip floating based on position
        selectedDayIndex?.let { index ->
            if (index in dailyStats.indices) {
                val dayStats = dailyStats[index]

                // Tính toán vị trí
                val bias = -1f + (2f * (index + 0.5f) / dailyStats.size)

                BarTooltip(
                    dayStats = dayStats,
                    modifier = Modifier
                        .align(BiasAlignment(bias, -0.2f))
                        .offset(y = (20).dp) // Điều chỉnh vị trí Y để tooltip nằm trên đỉnh
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
    val tooltipColor = Color.White // Nền trắng theo yêu cầu

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Phần hộp nội dung (Bubble)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tooltipColor,
            modifier = Modifier.shadow(6.dp, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dateString.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TooltipItem(
                        label = "Lượt ôn",
                        value = dayStats.wordsReviewed.toString()
                    )
                    TooltipItem(
                        label = "Thời gian",
                        value = "${dayStats.studyTimeMinutes}p"
                    )
                    TooltipItem(
                        label = "Độ chính xác",
                        value = "${dayStats.accuracy.toInt()}%"
                    )
                }
            }
        }

        // Phần mũi nhọn (Arrow/Tail) vẽ bằng Canvas
        Canvas(modifier = Modifier.size(width = 16.dp, height = 8.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f) // Góc trên trái
                lineTo(size.width, 0f) // Góc trên phải
                lineTo(size.width / 2f, size.height) // Đỉnh dưới cùng
                close()
            }
            drawPath(path, color = tooltipColor)
        }
    }
}

@Composable
private fun TooltipItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Đã bỏ Emoji
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
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
    iconResId: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 20.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
    }
}