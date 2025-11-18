package com.uilover.project247.ConversationActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.uilover.project247.ConversationActivity.screens.ConversationListScreen
import com.uilover.project247.ConversationActivity.viewmodels.ConversationListViewModel
import com.uilover.project247.ui.theme.Project247Theme

class ConversationListActivity : ComponentActivity() {

    // 1. Khởi tạo ViewModel cho màn hình danh sách
    private val viewModel: ConversationListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // TODO: Thay Project247Theme bằng tên Theme của bạn
            Project247Theme {
                // Set màu thanh status bar
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()

                ConversationListScreen(
                    viewModel = viewModel,
                    onConversationClick = { conversationId ->
                        // 2. Xử lý sự kiện click
                        // Khi người dùng bấm vào 1 chủ đề,
                        // khởi động ConversationDetailActivity

                        val intent = Intent(this, ConversationDetailActivity::class.java)

                        // 3. Gửi ID của chủ đề qua Intent
                        intent.putExtra("CONVERSATION_ID", conversationId)

                        startActivity(intent)
                    }
                )
            }
        }
    }
}