package com.uilover.project247.DictionaryActivity

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uilover.project247.DictionaryActivity.Model.DictionaryViewModel
import com.uilover.project247.DictionaryActivity.screens.DictionaryScreen
import com.uilover.project247.ui.theme.Project247Theme

class DictionaryActivity : ComponentActivity() {
    
    private val viewModel: DictionaryViewModel by lazy {
        ViewModelProvider(
            this,
            DictionaryViewModelFactory(applicationContext)
        )[DictionaryViewModel::class.java]
    }
    
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

class DictionaryViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            return DictionaryViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
