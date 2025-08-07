package com.uilover.project247.DashboardActivity.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uilover.project247.R


@Composable
fun BottomNavigationBar(onItemSelected:(Int)->Unit,modifier: Modifier = Modifier){
    NavigationBar(containerColor = colorResource(R.color.white),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
        ) {
        NavigationBarItem(
            selected = false,
            onClick = {onItemSelected(R.id.home)},
            icon = { Icon(painterResource(R.drawable.bottom_btn1),contentDescription = null) },
            label = {Text("Home")}
        )
        NavigationBarItem(
            selected = false,
            onClick = {onItemSelected(R.id.Board)},
            icon = { Icon(painterResource(R.drawable.bottom_btn2),contentDescription = null) },
            label = {Text("Board")}
        )
        NavigationBarItem(
            selected = false,
            onClick = {onItemSelected(R.id.favorites)},
            icon = { Icon(painterResource(R.drawable.bottom_btn3),contentDescription = null) },
            label = {Text("Favorites")}
        )
        NavigationBarItem(
            selected = false,
            onClick = {onItemSelected(R.id.profile)},
            icon = { Icon(painterResource(R.drawable.bottom_btn4),contentDescription = null) },
            label = {Text("Profile")}
        )
    }
}