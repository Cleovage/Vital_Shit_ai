package com.example.vitaai.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vitaai.ui.theme.*

@Composable
fun MainLayout(
    navController: NavController,
    currentRoute: String?,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AuraBackground {
            // Main content
            Box(modifier = Modifier.fillMaxSize()) {
                content(PaddingValues(bottom = 100.dp, top = 24.dp, start = 20.dp, end = 20.dp))
            }

            // Navigation Bar Overlay
            val isNavBarVisible = currentRoute != "chat" && currentRoute != "splash" && 
                                 currentRoute != "onboarding" && currentRoute != "auth"
            
            AnimatedVisibility(
                visible = isNavBarVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                ApexNavigationBar(navController, currentRoute)
            }
        }
    }
}

data class NavItem(
    val route: String,
    val icon: ImageVector,
    val iconSelected: ImageVector = icon
)

@Composable
fun ApexNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    val items = listOf(
        NavItem("chat", Icons.Outlined.SmartToy, Icons.Filled.SmartToy),
        NavItem("activity", Icons.Outlined.MonitorHeart, Icons.Filled.MonitorHeart),
        NavItem("dashboard", Icons.Outlined.Home, Icons.Filled.Home),
        NavItem("analytics", Icons.Outlined.BarChart, Icons.Filled.BarChart),
        NavItem("profile", Icons.Outlined.Person, Icons.Filled.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(modifier = Modifier.fillMaxWidth()) {
        // Main pill - Gleb Glass Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    clip = false,
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(InkDeep.copy(alpha = 0.8f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
                    ),
                    shape = CircleShape
                )
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                
                NavTab(
                    item = item,
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NavTab(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tabScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tabScale"
    )

    Box(
        modifier = Modifier
            .scale(tabScale)
            .clip(CircleShape)
            .background(if (selected) GlebPurple.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (selected) item.iconSelected else item.icon,
            contentDescription = null,
            tint = if (selected) GlebPurple else Slate500,
            modifier = Modifier.size(24.dp)
        )
    }
}
