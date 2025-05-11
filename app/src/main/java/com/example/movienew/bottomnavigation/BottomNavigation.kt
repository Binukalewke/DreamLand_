package com.example.movienew.bottomnavigation


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.movienew.ui.theme.Blue
import com.example.movienew.ui.theme.beige

@Composable
fun BottomNav(
    currentTab: String,
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(currentTab) { selected ->
                if (selected == "Profile") {
                    navController.navigate("profile") {
                        popUpTo("main?tab=$currentTab") { inclusive = false }
                    }
                } else {
                    navController.navigate("main?tab=$selected") {
                        popUpTo("main?tab=$currentTab") { inclusive = true }
                    }
                }
            }
        },
        content = content
    )
}

@Composable
fun BottomNavigationBar(currentTab: String, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(beige)
            .padding(12.dp)
            .height(60.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(Icons.Default.Home, "Home", currentTab == "Home") { onTabSelected("Home") }
        BottomNavItem(Icons.Default.Bookmark, "Bookmark", currentTab == "Bookmark") { onTabSelected("Bookmark") }
        BottomNavItem(Icons.Default.Person, "Profile", currentTab == "Profile") { onTabSelected("Profile") }
        BottomNavItem(Icons.Default.Search, "Search", currentTab == "Search") { onTabSelected("Search") }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) Blue else Color.Gray)
        Text(text = label, color = if (isSelected) Blue else Color.Gray)
    }
}
