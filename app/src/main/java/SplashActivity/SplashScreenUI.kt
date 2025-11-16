package com.uilover.project247.SplashActivity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uilover.project247.R // Import file R của bạn

@Composable
fun SplashScreenUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Đặt màu nền bạn muốn
        contentAlignment = Alignment.Center
    ) {
        Image(
            // Dùng tệp PNG bạn vừa kéo vào
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp) // Đặt kích thước bạn muốn
        )
    }
}