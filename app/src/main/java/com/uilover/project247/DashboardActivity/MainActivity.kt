package com.uilover.project247.DashboardActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uilover.project247.ConversationActivity.ConversationDetailActivity
import com.uilover.project247.DashboardActivity.screens.MainScreen
import com.uilover.project247.LearningActivity.LearningActivity
import com.uilover.project247.PlacementTestActivity.PlacementTestActivity
import com.uilover.project247.data.repository.PlacementTestManager
import com.uilover.project247.ui.theme.Project247Theme
import com.uilover.project247.DashboardActivity.Model.MainViewModel
import com.uilover.project247.QuestionActivity.QuestionActivity
import com.uilover.project247.utils.ProductTourManager
import com.uilover.project247.ProductTourActivity.ProductTourActivity

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
        }
    }
    
    private lateinit var placementTestManager: PlacementTestManager
    private lateinit var productTourManager: ProductTourManager
    
    override fun onResume() {
        super.onResume()
        // Refresh lại completed topics và reload topics để cập nhật trạng thái lock/unlock
        viewModel.refreshData()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placementTestManager = PlacementTestManager(this)
        productTourManager = ProductTourManager(this)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        setContent {
            Project247Theme {
                MainScreen(
                    viewModel = viewModel,
                    showInAppTour = !productTourManager.hasCompletedTour(),

                    onBoardClick = {
                        // Stay on Board tab - do nothing
                    },

                    onTopicClick = { levelId, topicId ->
                        // Navigate to LearningActivity with both levelId and topicId
                        val intent = Intent(this, LearningActivity::class.java).apply {
                            putExtra("LEVEL_ID", levelId)
                            putExtra("TOPIC_ID", topicId)
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
                    },
                    onTourComplete = {
                        productTourManager.setTourCompleted()
                    }
                )
            }
        }
        
        // Check placement test after UI is set
        if (!placementTestManager.hasCompletedTest()) {
            val intent = Intent(this, PlacementTestActivity::class.java)
            startActivity(intent)
        }
    }
}
