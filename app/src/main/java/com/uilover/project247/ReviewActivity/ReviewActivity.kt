package com.uilover.project247.ReviewActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.uilover.project247.QuestionActivity.QuestionActivity
import com.uilover.project247.ReviewActivity.Model.ReviewViewModel
import com.uilover.project247.ui.theme.Project247Theme

class ReviewActivity : ComponentActivity() {
    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // TODO: Thay Project247Theme bằng tên Theme của bạn
            Project247Theme {
                // Set màu thanh status bar
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()

                ReviewScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        finish() // Đóng Activity khi bấm back
                    },
                    onTopicClick = { topicId ->
                        val intent = Intent(this, QuestionActivity::class.java)
                        intent.putExtra("TOPIC_ID", topicId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
