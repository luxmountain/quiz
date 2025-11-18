package com.uilover.project247.ConversationActivity.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio // <-- 1. THÊM IMPORT
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale // <-- 2. THÊM IMPORT
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.uilover.project247.data.models.Conversation

// Composable cho từng item trong danh sách
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationTopicItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // <-- Sửa: Dùng 16dp (chuẩn hơn)
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ảnh (tải từ URL)
            AsyncImage(
                model = conversation.imageUrl,
                contentDescription = conversation.title,
                // --- SỬA LẠI MODIFIER CỦA ẢNH ---
                modifier = Modifier
                    .fillMaxWidth() // 1. Cho ảnh chiếm hết chiều rộng
                    .aspectRatio(16f / 9f) // 2. Đặt tỉ lệ 16:9
                    .clip(RoundedCornerShape(16.dp)), // 3. Bo tròn 4 góc
                // 4. Thêm contentScale để ảnh lấp đầy khung
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp)) // <-- Tăng khoảng cách
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = conversation.contextDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}