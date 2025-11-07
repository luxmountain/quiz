package com.uilover.project247.DashboardActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.uilover.project247.DashboardActivity.screens.MainScreen
import com.uilover.project247.LeaderActivity.LeaderActivity
import com.uilover.project247.LearningActivity.LearningActivity
import com.uilover.project247.ReviewActivity.ReviewActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        setContent {
            MainScreen(
                onBoardClick = {
                    startActivity(Intent(this, LeaderActivity::class.java))
                },

                // Sửa lỗi: Luôn gửi ID dưới dạng String
                onTopicClick = { topicId ->
                    val intent = Intent(this, LearningActivity::class.java)
                    // Chuyển đổi ID thành String để khớp với LearningActivity
                    intent.putExtra("TOPIC_ID", topicId.toString())
                    startActivity(intent)
                },

                onReviewClick = {
                    val intent = Intent(this, ReviewActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}
