package com.uilover.project247.QuestionActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.uilover.project247.QuestionActivity.Model.QuestionModel
import com.uilover.project247.R
import com.uilover.project247.ScoreActivity.ScoreActivity

class QuestionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.grey)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val receivedList =
            intent.getParcelableArrayListExtra<QuestionModel>("list") ?: arrayListOf()

        setContent {
            QuestionScreen(
                questions = receivedList,
                onBackClick = { finish() },
                onFinish = {
                    finalScore-> val intent= Intent(this, ScoreActivity::class.java)
                    intent.putExtra("Score",finalScore)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}