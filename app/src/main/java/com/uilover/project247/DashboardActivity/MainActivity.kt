package com.uilover.project247.DashboardActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.uilover.project247.DashboardActivity.screens.MainScreen
import com.uilover.project247.LeaderActivity.LeaderActivity
import com.uilover.project247.LearningActivity.LearningActivity
import com.uilover.project247.ReviewActivity.ReviewActivity
import com.uilover.project247.ui.theme.Project247Theme
import com.uilover.project247.DashboardActivity.Model.MainViewModel
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

                    onTopicClick = { topicId ->
                        val intent = Intent(this, LearningActivity::class.java)
                        intent.putExtra("TOPIC_ID", topicId)
                        startActivity(intent)
                    },

                    onReviewClick = {
                        val intent = Intent(this, ReviewActivity::class.java)
                        startActivity(intent)
                    },
                    
                    onSearchClick = {
                        // Stay on Search tab - do nothing
                    }
                )
            }
        }
    }
}
