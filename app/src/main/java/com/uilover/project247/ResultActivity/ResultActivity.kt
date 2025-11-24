package com.uilover.project247.ResultActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.uilover.project247.ui.theme.Project247Theme

class ResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val studyType = intent.getStringExtra("STUDY_TYPE") ?: "flashcard"
        val topicId = intent.getStringExtra("TOPIC_ID") ?: ""
        val topicName = intent.getStringExtra("TOPIC_NAME") ?: ""
        val totalItems = intent.getIntExtra("TOTAL_ITEMS", 0)
        val correctCount = intent.getIntExtra("CORRECT_COUNT", 0)
        val timeSpent = intent.getLongExtra("TIME_SPENT", 0)

        setContent {
            Project247Theme {
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()
                
                ResultScreen(
                    studyType = studyType,
                    topicId = topicId,
                    topicName = topicName,
                    totalItems = totalItems,
                    correctCount = correctCount,
                    timeSpent = timeSpent,
                    onNavigateBack = {
                        val returnIntent = Intent()
                        returnIntent.putExtra("TOPIC_COMPLETED", true)
                        setResult(RESULT_OK, returnIntent)
                        finish()
                    }
                )
            }
        }
    }
}
