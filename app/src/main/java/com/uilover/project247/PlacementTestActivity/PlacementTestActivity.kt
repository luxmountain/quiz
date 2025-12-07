package com.uilover.project247.PlacementTestActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uilover.project247.PlacementTestActivity.Model.PlacementTestViewModel
import com.uilover.project247.PlacementTestActivity.screens.PlacementTestScreen
import com.uilover.project247.ui.theme.Project247Theme

class PlacementTestActivity : ComponentActivity() {

    private val viewModel: PlacementTestViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PlacementTestViewModel(application) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Project247Theme {
                PlacementTestScreen(
                    viewModel = viewModel,
                    onTestCompleted = {
                        // Test hoàn thành - quay về MainActivity
                        finish()
                    },
                    onSkip = {
                        // Skip test - vẫn quay về MainActivity
                        finish()
                    }
                )
            }
        }
    }
}
