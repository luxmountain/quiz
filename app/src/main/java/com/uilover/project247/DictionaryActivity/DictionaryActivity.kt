package com.uilover.project247.DictionaryActivity

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.uilover.project247.DictionaryActivity.Model.DictionaryViewModel
import com.uilover.project247.DictionaryActivity.screens.DictionaryScreen
import com.uilover.project247.ui.theme.Project247Theme

class DictionaryActivity : ComponentActivity() {
    
    private val viewModel: DictionaryViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        
        setContent {
            Project247Theme {
                DictionaryScreen(
                    viewModel = viewModel,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
