package com.uilover.project247.ReviewActivity.Model

import com.uilover.project247.data.models.Topic

data class ReviewTopic(
    val topic: Topic,
    val progress: Float,
    val dueCount: Int = 0,      // Số cards cần ôn hôm nay
    val totalCards: Int = 0     // Tổng số cards trong topic
)