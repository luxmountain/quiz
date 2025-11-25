package com.uilover.project247.DashboardActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.AIAssistantActivity.Model.AIAssistantViewModel
import com.uilover.project247.AIAssistantActivity.Model.AITab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreenContent(
    viewModel: AIAssistantViewModel,
    modifier: Modifier = Modifier,
    onReviewTopicClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Tabs
        TabRow(
            selectedTabIndex = when (uiState.selectedTab) {
                AITab.ANALYSIS -> 0
                AITab.RECOMMENDATIONS -> 1
            },
            containerColor = Color.White,
            contentColor = Color(0xFF6200EA)
        ) {
            Tab(
                selected = uiState.selectedTab == AITab.ANALYSIS,
                onClick = { viewModel.selectTab(AITab.ANALYSIS) },
                text = { Text("Phân tích") }
            )
            Tab(
                selected = uiState.selectedTab == AITab.RECOMMENDATIONS,
                onClick = { viewModel.selectTab(AITab.RECOMMENDATIONS) },
                text = { Text("Gợi ý ôn tập") }
            )
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI đang phân tích...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thử lại")
                        }
                    }
                }
            }

            uiState.selectedTab == AITab.ANALYSIS && uiState.analysis != null -> {
                AnalysisContent(
                    analysis = uiState.analysis!!,
                    onRefresh = { viewModel.loadAnalysis() }
                )
            }

            uiState.selectedTab == AITab.RECOMMENDATIONS -> {
                RecommendationsContent(
                    recommendations = uiState.recommendations,
                    onRefresh = { viewModel.loadRecommendations() },
                    onReviewClick = onReviewTopicClick
                )
            }
        }
    }
}

@Composable
private fun AnalysisContent(
    analysis: com.uilover.project247.data.ai.StudyAnalysis,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Score Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Điểm tổng quan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    analysis.overallScore >= 80 -> Color(0xFF4CAF50)
                                    analysis.overallScore >= 60 -> Color(0xFFFF9800)
                                    else -> Color(0xFFD32F2F)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${analysis.overallScore}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when {
                            analysis.overallScore >= 80 -> "Xuất sắc! 🌟"
                            analysis.overallScore >= 60 -> "Tốt! 👍"
                            else -> "Cần cố gắng thêm! 💪"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Strengths
        if (analysis.strengthAreas.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Điểm mạnh",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        analysis.strengthAreas.forEach { strength ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text("• ", color = Color(0xFF4CAF50))
                                Text(strength, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // Weaknesses
        if (analysis.weaknessAreas.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Cần cải thiện",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        analysis.weaknessAreas.forEach { weakness ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text("• ", color = Color(0xFFFF9800))
                                Text(weakness, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // Recommendation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Lời khuyên từ AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        analysis.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Motivational Message
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "💡",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        analysis.motivationalMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6A1B9A)
                    )
                }
            }
        }

        // Refresh Button
        item {
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EA)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Phân tích lại")
            }
        }
    }
}

@Composable
private fun RecommendationsContent(
    recommendations: List<com.uilover.project247.data.ai.WordRecommendation>,
    onRefresh: () -> Unit,
    onReviewClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (recommendations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🎯",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Chưa có gợi ý ôn tập",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Học thêm một số chủ đề để nhận gợi ý từ AI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(recommendations.sortedByDescending { it.priority }) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    onReviewClick = { onReviewClick(recommendation.topicId) }
                )
            }
        }

        item {
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EA)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Làm mới gợi ý")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationCard(
    recommendation: com.uilover.project247.data.ai.WordRecommendation,
    onReviewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onReviewClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    recommendation.topicName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                // Priority indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (recommendation.priority) {
                                5 -> Color(0xFFD32F2F)
                                4 -> Color(0xFFFF5722)
                                3 -> Color(0xFFFF9800)
                                else -> Color(0xFF4CAF50)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Ưu tiên ${recommendation.priority}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    recommendation.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
