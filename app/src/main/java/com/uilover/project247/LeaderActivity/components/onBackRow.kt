package com.uilover.project247.LeaderActivity.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.R

@Composable
fun OnBackRow(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBack() }) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = null
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = "Single Palyer",
            fontSize = 20.sp,
            color = colorResource(R.color.navy_blue),
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun OnBackRowPreview() {
    OnBackRow({})
}