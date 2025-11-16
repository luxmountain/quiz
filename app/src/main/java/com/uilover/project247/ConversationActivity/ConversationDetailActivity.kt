package com.uilover.project247.ConversationActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.uilover.project247.ConversationActivity.screens.ConversationDetailScreen
import com.uilover.project247.ConversationActivity.viewmodels.ConversationDetailViewModel
import com.uilover.project247.ConversationActivity.viewmodels.ConversationDetailViewModelFactory
import com.uilover.project247.ui.theme.Project247Theme
import com.uilover.project247.data.repository.FirebaseRepository
import kotlin.getValue

class ConversationDetailActivity : ComponentActivity() {
    private val firebaseRepository by lazy { FirebaseRepository() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Lấy ID của conversation từ Intent
        val conversationId = intent.getStringExtra("CONVERSATION_ID")

        // 2. Kiểm tra ID (Rất quan trọng)
        if (conversationId == null) {
            // Nếu không có ID, không thể tải dữ liệu -> đóng Activity
            finish()
            return
        }

        // 3. Khởi tạo ViewModel bằng Factory (để truyền `conversationId` vào)
        val viewModel: ConversationDetailViewModel by viewModels {
            ConversationDetailViewModelFactory(conversationId,firebaseRepository)
        }

        setContent {
            // TODO: Thay Project247Theme bằng tên Theme của bạn
            Project247Theme {
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()

                ConversationDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        finish() // Đóng Activity này khi bấm nút Back
                    }
                )
            }
        }
    }
}