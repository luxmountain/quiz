package com.uilover.project247.QuestionActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity // Sửa: Dùng ComponentActivity cho Compose
import androidx.activity.compose.setContent
import androidx.activity.viewModels
// import androidx.appcompat.app.AppCompatActivity // (Không cần thiết cho full Compose)
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
// (Các import của bạn)
import com.uilover.project247.QuestionActivity.Model.QuestionViewModel
import com.uilover.project247.QuestionActivity.Model.QuestionViewModelFactory
import com.uilover.project247.ScoreActivity.ScoreActivity
import com.uilover.project247.ui.theme.Project247Theme

// Sửa: Dùng ComponentActivity sẽ nhẹ hơn AppCompatActivity
class QuestionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val topicId = intent.getStringExtra("TOPIC_ID")

        if (topicId==null){
            finish()
            return
        }

        // (Tôi giả định package "Model" là đúng theo ý bạn)
        val viewModel: QuestionViewModel by viewModels { QuestionViewModelFactory(topicId) }

        setContent {
            Project247Theme {
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                QuestionScreen(
                    viewModel=viewModel,
                    onNavigateBack={
                        finish()
                    },
                    onNavigateToScore={ finalScore->
                        val intent= Intent(this, ScoreActivity::class.java)


                        intent.putExtra("finalScore", finalScore)
                        startActivity(intent)
                        finish()
                    }
                )

            }
        }
    }
}