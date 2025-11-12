package com.uilover.project247.ConversationActivity.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage // Thư viện Coil để tải ảnh
import com.uilover.project247.ConversationActivity.viewmodels.ConversationListViewModel
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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ảnh (tải từ URL)
            AsyncImage(
                model = conversation.imageUrl,
                contentDescription = conversation.title,
                modifier = Modifier
                    .size(120.dp)
                // (Bạn có thể thêm .clip(RoundedCornerShape(16.dp)) nếu muốn)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = conversation.contextDescription, // Tiếng Anh
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

// Màn hình danh sách
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel,
    onConversationClick: (String) -> Unit // Gửi ID của conversation
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hội thoại") })
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            // (Bạn có thể dùng LoadingScreen() ở đây)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(uiState.conversations) { convo ->
                    ConversationTopicItem(
                        conversation = convo,
                        onClick = { onConversationClick(convo.id) }
                    )
                }
            }
        }
    }
}