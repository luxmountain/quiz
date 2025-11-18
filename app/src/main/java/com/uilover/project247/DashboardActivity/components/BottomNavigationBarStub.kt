package com.uilover.project247.DashboardActivity.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uilover.project247.R

@Composable
fun BottomNavigationBarStub(
    modifier: Modifier = Modifier,
    selectedItem: String = "Board",
    onItemSelected: (String) -> Unit
) {
    // (Label, IconSelected, IconUnselected, ID)
    val navItems = listOf(
        Quadruple("Tra từ", R.drawable.ic_search_selected, R.drawable.ic_search, "Search"),
        Quadruple("Học từ vựng", R.drawable.ic_learning_selected, R.drawable.ic_learning, "Board"),
        Quadruple("Ôn tập", R.drawable.ic_rising_selected, R.drawable.ic_rising, "Review"),
        Quadruple("Hội thoại", R.drawable.ic_chat_selected, R.drawable.ic_chat, "Chat"),
        Quadruple("MochiHub", R.drawable.ic_hub_selected, R.drawable.ic_hub, "Hub")
    )

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White
    ) {
        navItems.forEach { item ->
            val isSelected = item.fourth == selectedItem
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item.fourth) },
                icon = {
                    val iconRes = if (isSelected) item.second else item.third
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.first,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(25.dp) // 👈 giảm kích thước icon
                    )
                },
                label = {
                    Text(
                        text = item.first,
                        fontSize = 15.sp, // 👈 giảm kích thước chữ
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF2196F3) else Color.Gray
                    )
                },
                alwaysShowLabel = true // vẫn hiển thị nhãn
            )
        }
    }
}

// Hỗ trợ tuple 4 phần tử
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
