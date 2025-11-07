package com.uilover.project247.DashboardActivity.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun BottomNavigationBarStub(
    modifier: Modifier = Modifier,
    onItemSelected: (String) -> Unit // Dùng String thay vì R.id
) {
    var selectedItemIndex by remember { mutableStateOf(1) } // "Học từ vựng" được chọn

    // (Label, Icon, ID)
    val navItems = listOf(
        Triple("Tra từ", Icons.Default.Search, "Search"),
        Triple("Học từ vựng", Icons.Default.Menu, "Board"), // Dùng "Board" làm ID
        Triple("Ôn tập", Icons.Default.Home, "Review"), // Cần icon đúng
        Triple("Hội thoại", Icons.Default.Person, "Chat"), // Cần icon đúng
        Triple("MochiHub", Icons.Default.Home, "Hub") // Cần icon đúng
    )

    NavigationBar(
        modifier = modifier
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedItemIndex,
                onClick = {
                    selectedItemIndex = index
                    onItemSelected(item.third) // Trả về ID là String ("Board")
                },
                icon = { Icon(imageVector = item.second, contentDescription = item.first) },
                label = { Text(item.first) }
            )
        }
    }
}