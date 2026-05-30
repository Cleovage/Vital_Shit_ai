package com.example.vitaai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.Error
import com.example.vitaai.ui.theme.OnSurfaceVariant
import com.example.vitaai.ui.theme.Primary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: ActivityViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    AuraBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Workout History", style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.Bold)
                            Text("Recent sessions", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { padding ->
            when (val state = uiState) {
                is ActivityUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is ActivityUiState.Success -> {
                    if (state.sessions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Text("No workout history found.", color = OnSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.sessions) { session ->
                                ApexCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                        Text(session.title, color = Primary, fontWeight = FontWeight.Bold)
                                        Text(
                                            DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                                                .withZone(ZoneId.systemDefault())
                                                .format(Instant.ofEpochMilli(session.startTimeMillis)),
                                            color = OnSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "${session.durationSeconds / 60} min | ${session.totalSets} sets | ${session.totalReps} reps | ${session.calories.toInt()} kcal",
                                            color = OnSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is ActivityUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Error)
                    }
                }
            }
        }
    }
}
