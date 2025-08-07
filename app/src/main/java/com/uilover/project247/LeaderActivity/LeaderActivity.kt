package com.uilover.project247.LeaderActivity

import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.uilover.project247.LeaderActivity.Model.UserModel
import com.uilover.project247.R

class LeaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.grey)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val topUsers = loadData().take(3)
        val otherUsers = loadData().drop(3)

        setContent {
            LeaderScreen(
                topUsers = topUsers,
                otherUsers = otherUsers,
                onBackClick = { finish() })
        }
    }

    private fun loadData(): List<UserModel> {
        return listOf(
            UserModel(1, "Sophia", "person1", 4850),
            UserModel(2, "Daniel", "person2", 4560),
            UserModel(3, "James", "person3", 3873),
            UserModel(4, "John Smith", "person4", 3250),
            UserModel(5, "Emily Johnson", "person5", 3015),
            UserModel(6, "David Brown", "person6", 2970),
            UserModel(7, "Sarah Wilson", "person7", 2870),
            UserModel(8, "Michael Davis", "person8", 2670),
            UserModel(9, "Sarah Wilson", "person9", 2380),
            UserModel(10, "Sarah Wilson", "person10", 2376),
        )
    }
}