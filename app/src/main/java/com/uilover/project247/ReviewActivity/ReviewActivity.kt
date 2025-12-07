package com.uilover.project247.ReviewActivity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.uilover.project247.ReviewActivity.Model.ReviewViewModel
import com.uilover.project247.ReviewActivity.screens.ReviewSessionScreen
import com.uilover.project247.ui.theme.Project247Theme

/**
 * Activity cho Review Session - màn hình ôn tập với exercises
 * 
 * LIFECYCLE FIX: Restore session on resume to prevent blank screen
 */
class ReviewActivity : ComponentActivity() {
    
    private lateinit var reviewViewModel: ReviewViewModel
    private var isFirstResume = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("ReviewActivity", "onCreate - Starting review session")
        Log.d("ReviewActivity", "savedInstanceState = ${if (savedInstanceState == null) "null (fresh)" else "not null (restored)"}")
        
        reviewViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[ReviewViewModel::class.java]
        
        // Only start new session on fresh create
        if (savedInstanceState == null) {
            Log.d("ReviewActivity", "Fresh start - Starting NEW session")
            reviewViewModel.startReviewSession()
        } else {
            Log.d("ReviewActivity", "Restored from saved state")
            // ViewModel will check if session needs reload on resume
        }
        
        setContent {
            Project247Theme {
                ReviewSessionScreen(
                    viewModel = reviewViewModel,
                    onExit = {
                        // CRITICAL: Save progress before exiting
                        reviewViewModel.exitReviewMode()
                        finish()
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("ReviewActivity", "onResume - isFirstResume=$isFirstResume")
        
        // Refresh session state when returning (handles process death)
        if (!isFirstResume) {
            Log.d("ReviewActivity", "Refreshing session after resume")
            reviewViewModel.refreshSessionIfNeeded()
        }
        isFirstResume = false
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("ReviewActivity", "onPause")
    }
}
