package com.example.vitaai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
            ApexNavigationBar(
                navController = navController
            )
        },
        containerColor = Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(navController = navController) }
            composable("activity") { ActivityScreen(navController = navController) }
            composable("nutrition") { NutritionScreen() }
            composable("chat") { ChatScreen() }
            composable("analytics") { AnalyticsScreen() }
            composable("profile") { ProfileScreen() }
            composable("session") { SessionScreen(navController = navController) }
            composable("workout/start") { ActivityScreen(navController = navController) }
            composable(
                route = "workout/session/{templateId}",
                arguments = listOf(navArgument("templateId") { type = NavType.StringType })
            ) {
                SessionScreen(navController = navController)
            }
            composable(
                route = "workout/detail/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { HistoryScreen(navController = navController) }
            composable(
                route = "exercise/{exerciseId}",
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { ActivityScreen(navController = navController) }
            composable("history") { HistoryScreen(navController = navController) }
            composable(
                route = "metric/{metricRoute}",
                arguments = listOf(navArgument("metricRoute") { type = NavType.StringType })
            ) { backStackEntry ->
                MetricDetailScreen(
                    navController = navController,
                    metricRoute = backStackEntry.arguments?.getString("metricRoute")
                )
            }
        }
    }
}

@Composable
private fun ApexNavigationBar(navController: androidx.navigation.NavHostController) {
    val items = listOf(
        NavigationItem("dashboard", "Dashboard", Icons.Default.Dashboard),
        NavigationItem("activity", "Workouts", Icons.Default.FitnessCenter),
        NavigationItem("nutrition", "Nutrition", Icons.Default.Restaurant),
        NavigationItem("analytics", "Analytics", Icons.Default.Leaderboard),
        NavigationItem("profile", "Profile", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val vitaColors = LocalVitaColors.current
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(vitaColors.glassFill)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            vitaColors.glassBorderLight,
                            vitaColors.glassBorderLight.copy(alpha = 0.10f),
                            vitaColors.glassBorderDark
                        )
                    ),
                    shape = shape
                )
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selected) Primary else OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    // Glowing indicator node
                    Box(
                        modifier = Modifier
                            .size(width = 12.dp, height = 3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(if (selected) Primary else Color.Transparent)
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
