package com.example.vitaai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    @JvmField
    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Kick off a one-shot push+pull sync as soon as the activity starts.
        // Best-effort; logs and continues on failure (offline is the common case).
        val sync = (application as? dagger.hilt.android.HiltAndroidApp)
        val coordinator: com.example.vitaai.data.SyncCoordinator? = try {
            val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                this.applicationContext,
                SyncEntryPoint::class.java
            )
            entryPoint.syncCoordinator()
        } catch (t: Throwable) {
            android.util.Log.w("MainActivity", "could not get SyncCoordinator", t)
            null
        }
        if (coordinator != null) {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                coordinator.fullSync()
            }
        }
        @Suppress("UNUSED_VARIABLE") val _ignored = sync

        setContent {
            VitaAITheme {
                VitaApp(auth = auth)
            }
        }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
private interface SyncEntryPoint {
    fun syncCoordinator(): com.example.vitaai.data.SyncCoordinator
}

@Composable
fun VitaApp(auth: FirebaseAuth?) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define which routes hide the bottom navigation bar
    val shouldShowBottomBar = currentRoute != null &&
            currentRoute != "session" &&
            currentRoute != "auth" &&
            !currentRoute.startsWith("workout/session")

    // Density and dimensions for translation Y calculation
    val density = LocalDensity.current
    val bottomBarHeight = 100.dp
    val bottomBarHeightPx = with(density) { bottomBarHeight.toPx() }

    // Scroll state offset for auto-hide bottom navigation
    var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx - delta
                bottomBarOffsetHeightPx = newOffset.coerceIn(0f, bottomBarHeightPx)
                return Offset.Zero
            }
        }
    }

    // Animate the vertical translation offset smoothly
    val animatedTranslationY by animateFloatAsState(
        targetValue = if (shouldShowBottomBar) bottomBarOffsetHeightPx else bottomBarHeightPx,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "BottomBarOffset"
    )

    Scaffold(
        containerColor = Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            fun getRouteIndex(route: String?): Int {
                val cleanRoute = route?.split("/")?.firstOrNull() ?: ""
                return when (cleanRoute) {
                    "chat" -> 0
                    "activity" -> 1
                    "dashboard" -> 2
                    "analytics" -> 3
                    "profile" -> 4
                    else -> -1
                }
            }

            val startDest = if (auth?.currentUser == null) "auth" else "dashboard"

            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn(animationSpec = tween(300))
                        } else if (targetIndex < initialIndex) {
                            slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it } + fadeIn(animationSpec = tween(300))
                        } else {
                            fadeIn(animationSpec = tween(300))
                        }
                    } else {
                        fadeIn(animationSpec = tween(300))
                    }
                },
                exitTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it } + fadeOut(animationSpec = tween(300))
                        } else if (targetIndex < initialIndex) {
                            slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it } + fadeOut(animationSpec = tween(300))
                        } else {
                            fadeOut(animationSpec = tween(300))
                        }
                    } else {
                        fadeOut(animationSpec = tween(300))
                    }
                },
                popEnterTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn(animationSpec = tween(300))
                        } else if (targetIndex < initialIndex) {
                            slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it } + fadeIn(animationSpec = tween(300))
                        } else {
                            fadeIn(animationSpec = tween(300))
                        }
                    } else {
                        fadeIn(animationSpec = tween(300))
                    }
                },
                popExitTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex != -1) {
                        if (targetIndex > initialIndex) {
                            slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it } + fadeOut(animationSpec = tween(300))
                        } else if (targetIndex < initialIndex) {
                            slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it } + fadeOut(animationSpec = tween(300))
                        } else {
                            fadeOut(animationSpec = tween(300))
                        }
                    } else {
                        fadeOut(animationSpec = tween(300))
                    }
                }
            ) {
                composable("auth") {
                    AuthScreen(
                        onAuthSuccess = {
                            // If the signed-in user has no displayName, route to the
                            // first-run name picker; otherwise straight to dashboard.
                            val current = auth?.currentUser
                            val needsOnboarding = current != null &&
                                current.displayName.isNullOrBlank()
                            val nextRoute = if (needsOnboarding) "onboarding/name" else "dashboard"
                            navController.navigate(nextRoute) {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    )
                }
                composable("onboarding/name") {
                    OnboardingNameScreen(
                        onContinue = {
                            navController.navigate("dashboard") {
                                popUpTo("onboarding/name") { inclusive = true }
                            }
                        }
                    )
                }
                composable("dashboard") { DashboardScreen(navController = navController) }
                composable("activity") { ActivityScreen(navController = navController) }
                composable("nutrition") { NutritionScreen() }
                composable("chat") {
                    ChatScreen(
                        onOpenHistory = { navController.navigate("chat/history") }
                    )
                }
                composable("chat/history") {
                    val parentEntry = remember(it) { navController.getBackStackEntry("chat") }
                    val chatVm: com.example.vitaai.ui.screens.ChatViewModel = hiltViewModel(parentEntry)
                    ChatHistoryScreen(
                        onBack = { navController.popBackStack() },
                        onOpenConversation = { id ->
                            chatVm.openConversation(id)
                            navController.popBackStack()
                        },
                        onNewConversation = {
                            chatVm.startNewConversation()
                            navController.popBackStack()
                        }
                    )
                }
                composable("analytics") { AnalyticsScreen(navController = navController) }
                composable("profile") { ProfileScreen(navController = navController) }
                composable("circadian") { CircadianScreen(navController = navController) }
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
                composable("meditation") { MeditationScreen(navController = navController) }
                composable("hydration/detail") { HydrationDetailScreen(navController = navController) }
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

            // Floating Navigation Bar overlay
            ApexNavigationBar(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        translationY = animatedTranslationY
                    }
            )
        }
    }
}

@Composable
private fun ApexNavigationBar(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem("chat", "Vita", Icons.Default.SmartToy),
        NavigationItem("activity", "Vitals", Icons.Default.FitnessCenter),
        NavigationItem("dashboard", "Today", Icons.Default.Home),
        NavigationItem("analytics", "Trends", Icons.Default.Leaderboard),
        NavigationItem("profile", "You", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .widthIn(max = 480.dp)
    ) {
        // Soft shadow bloom behind nav
        Box(
            modifier = Modifier
                .fillMaxWidth()
               .height(72.dp)
                .offset(y = 8.dp)
                .padding(horizontal = 20.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset.Zero
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color.White.copy(alpha = 0.85f)) // bg-white/85
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.07f),
                    shape = shape
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                val itemBg = if (selected) Color(0xFF0F172A) else Color.Transparent
                val itemContentColor = if (selected) Color.White else Color.Black.copy(alpha = 0.45f)
                val scale = if (selected) 1.05f else 1.0f

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(itemBg)
                        .clickable {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(vertical = if (selected) 12.dp else 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = itemContentColor,
                        modifier = Modifier.size(if (selected) 26.dp else 24.dp)
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.title,
                            style = VitaTextStyles.navLabel,
                            color = itemContentColor
                        )
                    }
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
