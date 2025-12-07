package com.uilover.project247.data.models

import androidx.compose.ui.graphics.Color

data class TourPage(
    val title: String,
    val description: String,
    val imageResId: Int? = null,
    val emoji: String = "📚",
    val backgroundColor: Color = Color(0xFF6200EA)
)
