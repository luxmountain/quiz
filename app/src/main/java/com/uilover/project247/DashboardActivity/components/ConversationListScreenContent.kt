package com.uilover.project247.DashboardActivity.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.uilover.project247.ConversationActivity.components.ConversationTopicItem
import com.uilover.project247.ConversationActivity.viewmodels.ConversationListViewModel
import com.uilover.project247.data.models.Conversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreenContent(
    viewModel: ConversationListViewModel,
    modifier: Modifier = Modifier,
    onConversationClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.conversations.isEmpty() -> {
                    Text(
                        "Không có hội thoại nào.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
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
}

