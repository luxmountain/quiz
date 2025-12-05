package com.uilover.project247.DashboardActivity.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uilover.project247.data.models.Topic
import com.uilover.project247.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicItem(
    topic: Topic,
    onClick: () -> Unit,
    isCompleted: Boolean = false,
    isLocked: Boolean = false,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    val isActive = !isCompleted && !isLocked

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            // Active màu xanh nhạt, còn lại (Completed/Locked) màu xám nhạt
                            if (isActive) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = topic.imageUrl,
                        contentDescription = topic.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(24.dp),
                        placeholder = null,
                        error = null,
                        fallback = null
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Middle Text Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        // Nếu bị khóa thì màu chữ xám, ngược lại màu đen
                        color = if (isLocked) Color.Gray else Color.Black
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = topic.nameVi,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                // Right Status Icons
                if (isCompleted) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.checked),
                        contentDescription = "Completed",
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isLocked) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.key), // Đảm bảo bạn có icon này trong drawable
                        contentDescription = "Locked",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Button "Start" chỉ hiện khi Active (không khóa, chưa xong)
            if (isActive) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFE082),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Start",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}