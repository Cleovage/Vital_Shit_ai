package com.example.vitaai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.example.vitaai.ui.theme.SurfaceContainerHigh
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
                    title = { Text("Telemetry History") },
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
                            Text("No telemetry history found.", color = OnSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.sessions) { session ->
                                ApexCard(modifier = Modifier.fillMaxWidth()) {
                                    ActivityVectorItem(
                                        icon = Icons.Default.Person,
                                        title = session.title ?: "Exercise",
                                        time = DateTimeFormatter.ofPattern("MMM dd // HH:mm")
                                            .withZone(ZoneId.systemDefault())
                                            .format(session.startTime),
                                        duration = "${java.time.Duration.between(session.startTime, session.endTime).toMinutes()}M",
                                        rpe = "RPE -" // RPE isn't a standard field in ExerciseSessionRecord
                                    )
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
