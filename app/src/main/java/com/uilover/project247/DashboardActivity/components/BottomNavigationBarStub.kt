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
    selectedItem: String = "Board",
    onItemSelected: (String) -> Unit
) {
    // (Label, Icon, ID)
    val navItems = listOf(
        Triple("Tra từ", Icons.Default.Search, "Search"),
        Triple("Học từ vựng", Icons.Default.Menu, "Board"),
        Triple("Ôn tập", Icons.Default.Home, "Review"),
        Triple("Hội thoại", Icons.Default.Person, "Chat"),
        Triple("MochiHub", Icons.Default.Home, "Hub")
    )

    NavigationBar(
        modifier = modifier
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = item.third == selectedItem,
                onClick = {
                    onItemSelected(item.third)
                },
                icon = { Icon(imageVector = item.second, contentDescription = item.first) },
                label = { Text(item.first) }
            )
        }
    }
}