package com.example.vitaai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vitaai.ui.screens.*
import com.example.vitaai.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            VitaAITheme {
                VitaApp()
            }
        }
    }
}

@Composable
fun VitaApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            GlassNavigationBar(
                navController = navController
            )
        },
        containerColor = Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(bottom = 0.dp) // Let screens handle their own bottom padding so content scrolls behind nav bar
        ) {
            composable("dashboard") { DashboardScreen() }
            composable("activity") { ActivityScreen() }
            composable("chat") { ChatScreen() }
            composable("analytics") { AnalyticsScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

@Composable
private fun GlassNavigationBar(navController: androidx.navigation.NavHostController) {
    val items = listOf(
        NavigationItem("dashboard", "Dashboard", Icons.Rounded.Home),
        NavigationItem("activity", "Activity", Icons.Rounded.DirectionsRun),
        NavigationItem("chat", "Vita AI", Icons.Rounded.SmartToy),
        NavigationItem("analytics", "Analytics", Icons.Rounded.BarChart),
        NavigationItem("profile", "Profile", Icons.Rounded.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .navigationBarsPadding()
            .clip(RoundedCornerShape(32.dp))
            .background(GlassFill)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GlassBorderLight, GlassBorderDark)
                ),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        NavigationBar(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = OnSurfaceVariant,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = OnSurfaceVariant,
                        unselectedTextColor = OnSurfaceVariant,
                        indicatorColor = PrimaryContainer.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
