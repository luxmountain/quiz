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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.DashboardActivity.Model.MainViewModel
import com.uilover.project247.DashboardActivity.components.BottomNavigationBarStub
import com.uilover.project247.DashboardActivity.components.TopicItem
import com.uilover.project247.DashboardActivity.components.DictionaryScreenContent
import com.uilover.project247.data.models.Topic
import com.uilover.project247.DictionaryActivity.Model.DictionaryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onBoardClick: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    onReviewClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("Board") }
    val dictionaryViewModel = remember { DictionaryViewModel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (selectedTab) {
                        "Search" -> Text(
                            text = "Tra từ điển",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        else -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { /* TODO: Mở DropdownMenu */ }
                        ) {
                            Text(
                                text = "1000 Từ cơ bản",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Chọn bộ từ"
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /*TODO: Mở drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                        "Review" -> onReviewClick()
                        "Search" -> onSearchClick()
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        when (selectedTab) {
            "Search" -> {
                // Dictionary Screen Content
                DictionaryScreenContent(
                    viewModel = dictionaryViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                // Original Board Screen
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
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
                            Button(onClick = { viewModel.retryLoadTopics() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        item {
                            StartMochiItem(onClick = { /* ... */ })
                        }

                        items(uiState.topics) { topic ->
                            TopicItem(
                                topic = topic,
                                onClick = {
                                    onTopicClick(topic.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartMochiItem(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFA500)) // Màu cam
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Let's start with Mochi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Khởi động cùng Mochi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            AssistChip(
                onClick = { },
                label = { Text("Bài học thử", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFFFECB3)
                ),
                border = null
            )
        }
    }
}