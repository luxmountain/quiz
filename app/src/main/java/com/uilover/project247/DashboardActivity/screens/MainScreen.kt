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
import com.uilover.project247.DashboardActivity.components.BottomNavigationBarStub
import com.uilover.project247.DashboardActivity.components.Topic
import com.uilover.project247.DashboardActivity.components.TopicItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MainScreen(
    onBoardClick: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    onReviewClick: () -> Unit = {}
) {
    val sampleTopics = listOf(
        Topic(1, "Schools", "1.Trường học", android.R.drawable.ic_menu_gallery),
        Topic(2, "Examination", "2.Kì thi", R.drawable.ic_menu_gallery),
        Topic(3, "Extracurricular Activities", "3.Hoạt động ngoại khóa", android.R.drawable.ic_menu_gallery),
        Topic(4, "School Stationery", "4.Dụng cụ học tập", android.R.drawable.ic_menu_gallery),
        Topic(5, "School Subjects", "5.Các môn học", android.R.drawable.ic_menu_gallery),
        Topic(6, "Classroom", "6.Lớp học", android.R.drawable.ic_menu_gallery)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
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
                },
                navigationIcon = {
                    IconButton(onClick = { /*TODO: Mở drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface // Màu trắng
                )
            )
        },
        bottomBar = {
            BottomNavigationBarStub(
                onItemSelected = { itemId ->
                    if (itemId == "Board") {
                        onBoardClick()
                    }
                    if (itemId == "Review") {
                        onReviewClick()
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {

            item {
                StartMochiItem(onClick = { })
            }

            items(sampleTopics) { topic ->
                TopicItem(
                    topic = topic,
                    onClick = {
                        onTopicClick(topic.id.toString())
                    }
                )
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