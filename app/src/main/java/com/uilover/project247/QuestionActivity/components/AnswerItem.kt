package com.uilover.project247.QuestionActivity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.R

@Composable
fun AnswerItem(
    text: String,
    isCorrect: Boolean = false,
    isWrong: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> colorResource(R.color.green)
        isWrong -> colorResource(R.color.red)
        else -> Color.White
    }
    val textColor = if (isCorrect || isWrong) Color.White else Color.Black
    val icon = when {
        isCorrect -> painterResource(R.drawable.tick)
        isWrong -> painterResource(R.drawable.thieves)
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(enabled = !isSelected) { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            icon?.let{
                Icon(
                    painter = it,
                    contentDescription = null,
                    tint=Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun AnswerItemPreview() {
    AnswerItem(
        text = "Sample Answer",
        isCorrect = false,
        isWrong = false,
        isSelected = false,
        onClick = {}
    )
}

@Preview
@Composable
fun CorrectAnswerItemPreview() {
    AnswerItem(text = "Correct Answer", isCorrect = true, onClick = {})
}

@Preview
@Composable
fun WrongAnswerItemPreview() {
    AnswerItem(text = "Wrong Answer", isWrong = true, onClick = {})
}


