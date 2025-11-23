package com.uilover.project247.TopicListActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.uilover.project247.LearningActivity.LearningActivity
import com.uilover.project247.TopicListActivity.Model.TopicListViewModel
import com.uilover.project247.TopicListActivity.Model.TopicListViewModelFactory
import com.uilover.project247.TopicListActivity.screens.TopicListScreen
import com.uilover.project247.ui.theme.Project247Theme

class TopicListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy levelId từ Intent
        val levelId = intent.getStringExtra("LEVEL_ID")
        val levelName = intent.getStringExtra("LEVEL_NAME") ?: "Topics"

        if (levelId == null) {
            finish()
            return
        }

        // Khởi tạo ViewModel với levelId
        val viewModel: TopicListViewModel by viewModels {
            TopicListViewModelFactory(levelId)
        }

        setContent {
            Project247Theme {
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()

                TopicListScreen(
                    viewModel = viewModel,
                    levelName = levelName,
                    onNavigateBack = { finish() },
                    onTopicClick = { topicId ->
                        val intent = Intent(this, LearningActivity::class.java).apply {
                            putExtra("LEVEL_ID", levelId)
                            putExtra("TOPIC_ID", topicId)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
