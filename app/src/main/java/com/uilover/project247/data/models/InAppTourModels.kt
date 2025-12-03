package com.uilover.project247.data.models

data class InAppTourStep(
    val title: String,
    val description: String,
    val targetId: String,
    val emoji: String = "👉"
)
