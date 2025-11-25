package com.uilover.project247.LearningActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Cần import này
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb // Cần import này
import com.uilover.project247.LearningActivity.Model.LearningViewModel
import com.uilover.project247.LearningActivity.Model.LearningViewModelFactory
import com.uilover.project247.LearningActivity.screens.LearningScreen
import com.uilover.project247.ui.theme.Project247Theme // TODO: Thay bằng tên Theme của bạn

class LearningActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Lấy `levelId` và `topicId` từ Intent
        val levelId = intent.getStringExtra("LEVEL_ID")
        val topicId = intent.getStringExtra("TOPIC_ID")

        // 2. Rất quan trọng: Kiểm tra nếu levelId hoặc topicId bị null
        if (levelId == null || topicId == null) {
            // Nếu không có ID, không thể tải dữ liệu -> đóng Activity
            finish()
            return
        }

        // 3. Khởi tạo ViewModel bằng Factory (để truyền `application`, `levelId` và `topicId` vào)
        val viewModel: LearningViewModel by viewModels {
            LearningViewModelFactory(application, levelId, topicId)
        }

        setContent {
            // TODO: Thay Project247Theme bằng tên Theme của bạn
            Project247Theme {
                // Cập nhật màu thanh status bar
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()

                LearningScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        finish() // Đóng Activity khi người dùng bấm back
                    }
                )
            }
        }
    }
}