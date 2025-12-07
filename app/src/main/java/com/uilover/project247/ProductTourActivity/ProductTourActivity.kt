package com.uilover.project247.ProductTourActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.uilover.project247.ProductTourActivity.screens.ProductTourScreen
import com.uilover.project247.ui.theme.Project247Theme
import com.uilover.project247.utils.ProductTourManager

class ProductTourActivity : ComponentActivity() {
    private lateinit var tourManager: ProductTourManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        tourManager = ProductTourManager(this)
        
        setContent {
            Project247Theme {
                ProductTourScreen(
                    onFinish = {
                        tourManager.setTourCompleted()
                        finish()
                    },
                    onSkip = {
                        tourManager.setTourCompleted()
                        finish()
                    }
                )
            }
        }
    }
}
