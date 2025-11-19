package com.uilover.project247.LearningActivity.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uilover.project247.data.models.ReviewQuality

/**
 * 4 nút review theo Anki Algorithm
 * Again (đỏ) - Hard (cam) - Good (xanh lá) - Easy (xanh dương)
 */
@Composable
fun ReviewButtons(
    onReviewQuality: (ReviewQuality) -> Unit,
    modifier: Modifier = Modifier,
    showInterval: Boolean = false,
    againInterval: String = "< 10 phút",
    hardInterval: String = "1 ngày",
    goodInterval: String = "4 ngày",
    easyInterval: String = "7 ngày"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Again Button (Đỏ)
        ReviewButton(
            text = "Again",
            interval = againInterval,
            showInterval = showInterval,
            color = Color(0xFFE53935),
            onClick = { onReviewQuality(ReviewQuality.AGAIN) },
            modifier = Modifier.weight(1f)
        )
        
        // Hard Button (Cam)
        ReviewButton(
            text = "Hard",
            interval = hardInterval,
            showInterval = showInterval,
            color = Color(0xFFFF9800),
            onClick = { onReviewQuality(ReviewQuality.HARD) },
            modifier = Modifier.weight(1f)
        )
        
        // Good Button (Xanh lá)
        ReviewButton(
            text = "Good",
            interval = goodInterval,
            showInterval = showInterval,
            color = Color(0xFF4CAF50),
            onClick = { onReviewQuality(ReviewQuality.GOOD) },
            modifier = Modifier.weight(1f)
        )
        
        // Easy Button (Xanh dương)
        ReviewButton(
            text = "Easy",
            interval = easyInterval,
            showInterval = showInterval,
            color = Color(0xFF2196F3),
            onClick = { onReviewQuality(ReviewQuality.EASY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReviewButton(
    text: String,
    interval: String,
    showInterval: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        modifier = modifier.height(if (showInterval) 64.dp else 48.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            
            if (showInterval) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = interval,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Compact version cho màn hình nhỏ
 */
@Composable
fun CompactReviewButtons(
    onReviewQuality: (ReviewQuality) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Again (Icon X)
        OutlinedButton(
            onClick = { onReviewQuality(ReviewQuality.AGAIN) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFE53935)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Again", style = MaterialTheme.typography.labelMedium)
        }
        
        // Hard
        OutlinedButton(
            onClick = { onReviewQuality(ReviewQuality.HARD) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFFF9800)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Hard", style = MaterialTheme.typography.labelMedium)
        }
        
        // Good
        Button(
            onClick = { onReviewQuality(ReviewQuality.GOOD) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Good", style = MaterialTheme.typography.labelMedium)
        }
        
        // Easy
        Button(
            onClick = { onReviewQuality(ReviewQuality.EASY) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Easy", style = MaterialTheme.typography.labelMedium)
        }
    }
}
