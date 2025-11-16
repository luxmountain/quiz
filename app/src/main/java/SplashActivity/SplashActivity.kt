package com.uilover.project247.SplashActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.uilover.project247.DashboardActivity.MainActivity
import com.uilover.project247.ui.theme.Project247Theme // TODO: Thay bằng tên Theme của bạn
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ẩn thanh status bar và navigation bar (cho full màn hình)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Project247Theme {
                // 1. Hiển thị UI logo
                SplashScreenUI()

                // 2. Chạy logic đếm ngược
                LaunchedEffect(Unit) {
                    delay(2000L) // Đợi 2 giây

                    // 3. Sau 2 giây, chuyển sang MainActivity
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))

                    // 4. Đóng SplashActivity (để người dùng không "Back" về được)
                    finish()
                }
            }
        }
    }
}