package com.uilover.project247.DashboardActivity.screens

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu // Ví dụ icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Rect
import com.uilover.project247.ConversationActivity.viewmodels.ConversationListViewModel
import com.uilover.project247.DashboardActivity.Model.MainViewModel
import com.uilover.project247.DashboardActivity.components.BottomNavigationBarStub
import com.uilover.project247.DashboardActivity.components.ConversationListScreenContent
import com.uilover.project247.DashboardActivity.components.TopicItem
import com.uilover.project247.DashboardActivity.components.DictionaryScreenContent
import com.uilover.project247.DashboardActivity.components.ReviewScreenContent
import com.uilover.project247.DashboardActivity.components.AIAssistantScreenContent
import com.uilover.project247.AIAssistantActivity.Model.AIAssistantViewModel
import com.uilover.project247.data.models.Level
import com.uilover.project247.DictionaryActivity.Model.DictionaryViewModel
import com.uilover.project247.ReviewActivity.Model.ReviewViewModel
import com.uilover.project247.StatisticsActivity.Model.StatisticsViewModel
import com.uilover.project247.StatisticsActivity.screens.StatisticsScreenContent
import com.uilover.project247.DashboardActivity.components.InAppTourOverlay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    showInAppTour: Boolean = false,
    onBoardClick: () -> Unit = {},
    onTopicClick: (levelId: String, topicId: String) -> Unit = { _, _ -> },
    onTopicReviewClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onConversationClick: (String) -> Unit = {},
    onTourComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("Board") }
    val context = LocalContext.current
    val dictionaryViewModel = remember { DictionaryViewModel(context) }
    val conversationViewModel = remember { ConversationListViewModel(context.applicationContext as android.app.Application) }
    val reviewViewModel = remember { ReviewViewModel() }
    val aiAssistantViewModel = remember { AIAssistantViewModel(context.applicationContext as android.app.Application) }
    val statisticsViewModel = remember { StatisticsViewModel(context.applicationContext as android.app.Application) }
    
    // In-app tour state
    var showTour by remember { mutableStateOf(showInAppTour) }
    var tourStep by remember { mutableStateOf(0) }
    var tourTargets by remember { mutableStateOf<Map<String, Rect>>(emptyMap()) }
    
    fun updateTourTarget(id: String, rect: Rect) {
        tourTargets = tourTargets + (id to rect)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                title = {
                    when (selectedTab) {
                        "Search" -> Text(
                            text = "Tra từ điển",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        "Review" -> Text(
                            text = "Ôn tập từ vựng",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        "Chat" -> Text(
                            text = "Hội thoại mẫu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        "Statistics" -> Text(
                            text = "Thống kê học tập 📊",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        "MochiHub" -> Text(
                            text = "AI Study Assistant 🤖",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        else -> {
                            // Level selector in title area
                            var expanded by remember { mutableStateOf(false) }

                            val selectedLevel = uiState.levels.find { it.id == uiState.selectedLevelId }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { expanded = true }
                                    .onGloballyPositioned { coordinates ->
                                        val pos = coordinates.positionInRoot()
                                        updateTourTarget(
                                            "level_selector",
                                            Rect(
                                                left = pos.x,
                                                top = pos.y,
                                                right = pos.x + coordinates.size.width,
                                                bottom = pos.y + coordinates.size.height
                                            )
                                        )
                                    }
                            ) {
                                Text(
                                    text = selectedLevel?.nameVi ?: "Chọn level",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Chọn level"
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    uiState.levels.forEach { level ->
                                        DropdownMenuItem(
                                            text = { Text(level.nameVi) },
                                            onClick = {
                                                expanded = false
                                                viewModel.loadTopicsByLevel(level.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBarStub(
                selectedItem = selectedTab,
                onItemSelected = { itemId ->
                    selectedTab = itemId
                    when (itemId) {
                        "Board" -> onBoardClick()
                        "Review" -> {}
                        "Search" -> onSearchClick()
                        "Chat" -> {}
                    }
                },
                onTargetPositioned = { id, rect ->
                    updateTourTarget(id, rect)
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        when (selectedTab) {
            "Search" -> {
                DictionaryScreenContent(
                    viewModel = dictionaryViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }


            "Review" -> {
                // Review Screen
                ReviewScreenContent(
                    viewModel = reviewViewModel, // cần có ReviewViewModel trong MainViewModel
                    modifier = Modifier.padding(paddingValues),
                    onReviewTopicClick = { topicId ->
                        onTopicReviewClick(topicId)
                    },
                    onNavigateBack = null // nếu muốn, có thể truyền { selectedTab = "Board" }
                )
            }

            "Chat" -> {
                // Conversation Screen
                ConversationListScreenContent(
                    viewModel = conversationViewModel,
                    modifier = Modifier.padding(paddingValues),
                    onConversationClick = { conversationId ->
                        onConversationClick(conversationId)
                    }
                )
            }

            "Statistics" -> {
                // Statistics Screen
                StatisticsScreenContent(
                    viewModel = statisticsViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            "MochiHub" -> {
                // AI Study Assistant Screen
                AIAssistantScreenContent(
                    viewModel = aiAssistantViewModel,
                    modifier = Modifier.padding(paddingValues),
                    onReviewTopicClick = { topicId ->
                        onTopicReviewClick(topicId)
                    }
                )
            }

            else -> {
                // 👉 Màn hình chính (Board) - Hiển thị danh sách topics của level đã chọn
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "Lỗi không xác định",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(onClick = { viewModel.retryLoadLevels() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.topicsWithStatus,
                            key = { it.topic.id }
                        ) { topicStatus ->
                            val isFirstTopic = uiState.topicsWithStatus.firstOrNull() == topicStatus
                            TopicItem(
                                topic = topicStatus.topic,
                                isCompleted = topicStatus.isCompleted,
                                isLocked = topicStatus.isLocked,
                                progress = topicStatus.progress,
                                onClick = { 
                                    if (!topicStatus.isLocked) {
                                        uiState.selectedLevelId?.let { levelId ->
                                            onTopicClick(levelId, topicStatus.topic.id)
                                        }
                                    }
                                },
                                modifier = if (isFirstTopic) {
                                    Modifier.onGloballyPositioned { coordinates ->
                                        val pos = coordinates.positionInRoot()
                                        updateTourTarget(
                                            "topic_item",
                                            Rect(
                                                left = pos.x,
                                                top = pos.y,
                                                right = pos.x + coordinates.size.width,
                                                bottom = pos.y + coordinates.size.height
                                            )
                                        )
                                    }
                                } else Modifier
                            )
                        }
                    }
                }
            }
        }
    }
        
        // In-app tour overlay
        if (showTour) {
            InAppTourOverlay(
                currentStep = tourStep,
                tourTargets = tourTargets,
                onNext = { tourStep++ },
                onSkip = { 
                    showTour = false
                    onTourComplete()
                },
                onComplete = {
                    showTour = false
                    onTourComplete()
                }
            )
        }
    }
}
