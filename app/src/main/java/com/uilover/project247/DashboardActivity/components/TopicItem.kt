package com.uilover.project247.DashboardActivity.components

// import androidx.compose.foundation.Image // Tạm thời chưa dùng Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer // Thêm Import
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width // Thêm Import
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
// import androidx.compose.ui.res.painterResource // Tạm thời chưa dùng
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.layout.ContentScale // Tạm thời chưa dùng
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uilover.project247.DashboardActivity.screens.Topic

// SỬA LỖI IMPORT: Import `Topic` từ package `screens` nơi bạn định nghĩa nó


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicItem(topic: Topic, onClick: () -> Unit){
    Card (
        // Modifier này đúng nếu bạn KHÔNG set contentPadding trong LazyColumn
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ){
        Row (
            modifier = Modifier
                .padding(16.dp) // Padding 16dp bên trong Card
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){

            // --- SỬA LỖI CRASH ---
            // 1. Dùng Box làm placeholder (ảnh giả) vì chưa có ảnh thật
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Gray) // Tạo một hình tròn màu xám
            )

            /*
            // 2. KHI CÓ ẢNH THẬT, BẠN SẼ DÙNG LẠI CODE NÀY:
            Image(
                painter = painterResource(id = topic.imageResId), // Phải chắc chắn id này tồn tại
                contentDescription = topic.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            */

            // Thêm một khoảng cách giữa ảnh và chữ
            Spacer(modifier = Modifier.width(16.dp))

            Column (
                modifier = Modifier.weight(1f) // Bỏ padding(start = 16.dp) vì đã có Spacer
            ){
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = topic.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}