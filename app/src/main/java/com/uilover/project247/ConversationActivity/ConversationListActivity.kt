package com.uilover.project247.ConversationActivity

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.uilover.project247.ConversationActivity.screens.ConversationListScreen
import com.uilover.project247.ConversationActivity.viewmodels.ConversationListViewModel
import com.uilover.project247.ui.theme.Project247Theme
import androidx.lifecycle.ViewModelProvider

class ConversationListActivity : ComponentActivity() {

    private val viewModel: ConversationListViewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ConversationListViewModel(application) as T
                }
            }
        )[ConversationListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Project247Theme {
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()

                ConversationListScreen(
                    application = application,
                    onConversationClick = { conversationId ->
                        val intent = Intent(this, ConversationDetailActivity::class.java)
                        intent.putExtra("CONVERSATION_ID", conversationId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.retry()
    }
}