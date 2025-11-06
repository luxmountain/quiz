package com.uilover.project247.DashboardActivity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home // Ví dụ icon
import androidx.compose.material.icons.filled.Menu // Ví dụ icon
import androidx.compose.material.icons.filled.Person // Ví dụ icon
import androidx.compose.material.icons.filled.Search // Ví dụ icon
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
import com.uilover.project247.DashboardActivity.components.TopicItem

// import com.uilover.project247.R // Tạm thời không dùng R.id
// import com.uilover.project247.DashboardActivity.components.BottomNavigationBar // Sẽ dùng stub bên dưới

// 1. ĐỊNH NGHĨA DATA CLASS CHO TOPIC
// (Nên di chuyển ra file riêng, ví dụ: data/Topic.kt)
data class Topic(
    val id: Int,
    val title: String,
    val subtitle: String,
    val imageResId: Int // Tạm thời sẽ không dùng
)

// 2. MÀN HÌNH CHÍNH (VIẾT LẠI)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MainScreen(
    onSinglePlayerClick: () -> Unit = {}, // Giữ lại tham số, dù không dùng ở đây
    onBoardClick: () -> Unit = {},
    onTopicClick: (Topic) -> Unit = {},
    onReviewClick: () -> Unit = {}
) {
    // Dữ liệu mẫu giống trong ảnh
    val sampleTopics = listOf(
        Topic(1, "Schools", "1.Trường học", android.R.drawable.ic_menu_gallery),
        Topic(2, "Examination", "2.Kì thi", android.R.drawable.ic_menu_gallery),
        Topic(3, "Extracurricular Activities", "3.Hoạt động ngoại khóa", android.R.drawable.ic_menu_gallery),
        Topic(4, "School Stationery", "4.Dụng cụ học tập", android.R.drawable.ic_menu_gallery),
        Topic(5, "School Subjects", "5.Các môn học", android.R.drawable.ic_menu_gallery),
        Topic(6, "Classroom", "6.Lớp học", android.R.drawable.ic_menu_gallery)
    )

    // Dùng Scaffold để có cấu trúc TopBar, BottomBar và Content chuẩn
    Scaffold(
        topBar = {
            // Thanh tiêu đề giống trong ảnh
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
                // Thêm icon menu bên trái (giống trong ảnh)
                navigationIcon = {
                    IconButton(onClick = { /*TODO: Mở drawer */ }) {
                        // Bạn cần thay icon này bằng icon 3 gạch
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface // Màu trắng
                )
            )
        },
        bottomBar = {
            // Thanh điều hướng dưới cùng
            // (Dùng stub bên dưới để chạy, bạn có thể thay bằng component thật)
            BottomNavigationBarStub(
                onItemSelected = { itemId ->
                    // So sánh bằng String thay vì R.id
                    if (itemId == "Board") { // "Board" là ID giả lập
                        onBoardClick()
                    }
                    if (itemId == "Review") {
                        onReviewClick()
                    }
                }
            )
        },
        // Màu nền cho nội dung (giống R.color.grey)
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        // DÙNG LAZYCOLUMN CHO NỘI DUNG CUỘN
        // Đây là cách đúng để hiển thị danh sách, thay vì Column + verticalScroll
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Áp dụng padding từ Scaffold
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // Mục 1: "Let's start with Mochi" (item đặc biệt)
            item {
                StartMochiItem(onClick = { /* TODO: Xử lý click bài học thử */ })
            }

            // Mục 2: Danh sách các topic
            items(sampleTopics) { topic ->
                TopicItem(
                    topic = topic,
                    onClick = {
                        onTopicClick(topic)
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
            // Placeholder cho ảnh Mochi
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

            // Chip "Bài học thử"
            AssistChip(
                onClick = { /* Không cần hành động */ },
                label = { Text("Bài học thử", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFFFECB3) // Màu vàng nhạt
                ),
                border = null
            )
        }
    }
}


