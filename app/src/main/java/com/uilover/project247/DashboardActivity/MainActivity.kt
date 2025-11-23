package com.uilover.project247.DashboardActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.uilover.project247.ConversationActivity.ConversationDetailActivity
import com.uilover.project247.DashboardActivity.screens.MainScreen
import com.uilover.project247.TopicListActivity.TopicListActivity
import com.uilover.project247.ui.theme.Project247Theme
import com.uilover.project247.DashboardActivity.Model.MainViewModel
import com.uilover.project247.QuestionActivity.QuestionActivity

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        setContent {
            Project247Theme {
                MainScreen(
                    viewModel = viewModel,

                    onBoardClick = {
                        // Stay on Board tab - do nothing
                    },

                    onLevelClick = { levelId ->
                        // Navigate to TopicListActivity to show topics in this level
                        val uiState = viewModel.uiState.value
                        val level = uiState.levels.find { it.id == levelId }
                        val intent = Intent(this, TopicListActivity::class.java).apply {
                            putExtra("LEVEL_ID", levelId)
                            putExtra("LEVEL_NAME", level?.nameVi ?: level?.name ?: "Topics")
                        }
                        startActivity(intent)
                    },

                    onTopicReviewClick = { topicId ->
                        val intent = Intent(this, QuestionActivity::class.java)
                        intent.putExtra("TOPIC_ID", topicId)
                        startActivity(intent)

                    },
                    
                    onSearchClick = {
                        // Stay on Search tab - do nothing
                    },
                    onConversationClick = { conversationId ->
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
