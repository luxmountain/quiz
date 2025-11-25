package com.uilover.project247.DashboardActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.uilover.project247.data.models.Topic


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicItem(
    topic: Topic, 
    onClick: () -> Unit,
    isCompleted: Boolean = false
) {
    val containerColor = remember(isCompleted) {
        if (isCompleted) Color(0xFF4CAF50) else Color.White
    }
    
    val textColor = remember(isCompleted) {
        if (isCompleted) Color.White else Color.Black
    }
    
    val subtitleColor = remember(isCompleted) {
        if (isCompleted) Color.White.copy(alpha = 0.9f) else Color.Gray
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick
    ) {
        Row (
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){

            Box(modifier = Modifier.size(56.dp)) {
                AsyncImage(
                    model = topic.imageUrl,
                    contentDescription = topic.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    placeholder = null,
                    error = null,
                    fallback = null
                )
                
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = topic.nameVi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}