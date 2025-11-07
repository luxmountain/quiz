package com.uilover.project247.DashboardActivity.components

data class Topic(
    val id: Int,
    val title: String,
    val subtitle: String,
    val imageResId: Int
)

data class VocabularyWord(
    val id: String,
    val word: String,        // Ví dụ: "School"
    val meaning: String,     // Ví dụ: "Trường học"
    val pronunciation: String?, // Ví dụ: "/skuːl/"
    val exampleSentence: String? // Ví dụ: "I go to school every day."
    // Bạn có thể thêm link audio, link ảnh... ở đây
)