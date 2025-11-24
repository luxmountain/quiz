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
import com.uilover.project247.ConversationActivity.viewmodels.ConversationListViewModel
import com.uilover.project247.DashboardActivity.Model.MainViewModel
import com.uilover.project247.DashboardActivity.components.BottomNavigationBarStub
import com.uilover.project247.DashboardActivity.components.ConversationListScreenContent
import com.uilover.project247.DashboardActivity.components.TopicItem
import com.uilover.project247.DashboardActivity.components.DictionaryScreenContent
import com.uilover.project247.DashboardActivity.components.ReviewScreenContent
import com.uilover.project247.data.models.Level
import com.uilover.project247.DictionaryActivity.Model.DictionaryViewModel
import com.uilover.project247.ReviewActivity.Model.ReviewViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onBoardClick: () -> Unit = {},
    onTopicClick: (levelId: String, topicId: String) -> Unit = { _, _ -> },
    onTopicReviewClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onConversationClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("Board") }
    val context = LocalContext.current
    val dictionaryViewModel = remember { DictionaryViewModel(context) }
    val conversationViewModel = remember { ConversationListViewModel() }
    val reviewViewModel = remember { ReviewViewModel() }



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
                        else -> {
                            // Level selector in title area
                            var expanded by remember { mutableStateOf(false) }

                            val selectedLevel = uiState.levels.find { it.id == uiState.selectedLevelId }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { expanded = true }
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
                    viewModel = conversationViewModel, // cần có ConversationListViewModel trong MainViewModel
                    modifier = Modifier.padding(paddingValues),
                    onConversationClick = { conversationId ->
                        onConversationClick(conversationId)
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
                        items(uiState.topics) { topic ->
                            TopicItem(
                                topic = topic,
                                isCompleted = viewModel.isTopicCompleted(topic.id),
                                onClick = { 
                                    uiState.selectedLevelId?.let { levelId ->
                                        onTopicClick(levelId, topic.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
