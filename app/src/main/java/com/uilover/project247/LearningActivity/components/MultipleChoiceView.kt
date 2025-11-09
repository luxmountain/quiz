package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uilover.project247.data.VocabularyWord

@Composable
fun MultipleChoiceView(word: VocabularyWord, onComplete: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Màn hình Chọn nghĩa", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Từ là: ${word.word}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        // TODO: Tạo 4 nút bấm, 1 đáp án đúng (word.meaning)
        // và 3 đáp án sai (lấy ngẫu nhiên từ `words` trong ViewModel)
        Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text(word.meaning) }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* TODO: Xử lý chọn sai */ }, modifier = Modifier.fillMaxWidth()) { Text("Nghĩa sai 1") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* TODO: Xử lý chọn sai */ }, modifier = Modifier.fillMaxWidth()) { Text("Nghĩa sai 2") }
    }
}