package com.uilover.project247.DashboardActivity.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uilover.project247.ReviewActivity.Model.ReviewViewModel
import com.uilover.project247.ReviewActivity.components.ReviewTopicItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreenContent(
    viewModel: ReviewViewModel,
    modifier: Modifier = Modifier,
    onReviewTopicClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null // thêm tùy chọn quay lại
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = Color(0xFFF7F7F7)

    Scaffold(
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()

                uiState.reviewTopics.isEmpty() -> {
                    Text(
                        "Không có chủ đề ôn tập nào.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.reviewTopics) { reviewTopic ->
                            ReviewTopicItem(
                                item = reviewTopic,
                                onClick = { onReviewTopicClick(reviewTopic.topic.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
