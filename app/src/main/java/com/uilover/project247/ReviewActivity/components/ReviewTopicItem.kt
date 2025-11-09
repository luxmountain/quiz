package com.uilover.project247.ReviewActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.ReviewActivity.Model.ReviewTopic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewTopicItem(
    item: ReviewTopic,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                // TODO: Dùng AsyncImage (Coil) khi muốn hiển thị ảnh
                // model = item.topic.imageUrl
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tên Topic
            Text(
                text = item.topic.name, // Thay đổi từ title -> name
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thanh tiến trình
                LinearProgressIndicator(
                    progress = { item.progress }, // Giá trị từ 0.0f đến 1.0f
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    // Màu của phần đã học
                    color = Color(0xFF00C853),
                    // Màu của phần nền (chưa học)
                    trackColor = Color.LightGray.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chữ %
                Text(
                    text = "${(item.progress * 100).toInt()}% đã học",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}