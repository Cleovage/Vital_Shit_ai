package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.*
import androidx.navigation.NavController
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityScreen(navController: NavController, viewModel: ActivityViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showManualEntryDialog by remember { mutableStateOf(false) }

    AuraBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "monitoring",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-1).sp
                        ),
                        color = Primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APEX VITALITY",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        ),
                        color = Primary
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = OutlineVariant.copy(alpha = 0.5f))
            }

            // Hero Action
            item {
                Button(
                    onClick = { /* TODO: Start real session tracking */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INITIATE OPEN SESSION", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            // Manual Input Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "MANUAL TELEMETRY INPUT",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    ApexCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("DURATION (MIN)", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.fillMaxWidth().background(SurfaceContainerHigh).padding(12.dp)) {
                                        Text("00", color = Primary)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("MODALITY", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.fillMaxWidth().background(SurfaceContainerHigh).padding(12.dp)) {
                                        Text("RUN", color = Primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TARGET INTENSITY (RPE)", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Text("7 /10", style = MaterialTheme.typography.headlineMedium, color = Primary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = 0.7f,
                                    onValueChange = {},
                                    colors = SliderDefaults.colors(
                                        thumbColor = Primary,
                                        activeTrackColor = Primary,
                                        inactiveTrackColor = SurfaceContainerHigh
                                    )
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("RECOVERY", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Text("MAX EFFORT", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                            }

                            Button(
                                onClick = { showManualEntryDialog = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceContainerHigh,
                                    contentColor = Primary
                                ),
                                shape = MaterialTheme.shapes.small,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
                            ) {
                                Text("COMMIT LOG ENTRY", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Recent Vector Section
            item {
                Text(
                    "RECENT ACTIVITY VECTOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            when (val state = uiState) {
                is ActivityUiState.Loading -> {
                    item { CircularProgressIndicator(color = Primary) }
                }
                is ActivityUiState.Success -> {
                    if (state.sessions.isEmpty()) {
                        item { Text("No recent activities found.", color = OnSurfaceVariant) }
                    } else {
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
                is ActivityUiState.Error -> {
                    item { Text("Error: ${state.message}", color = Error) }
                }
            }

            item {
                TextButton(
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("VIEW FULL TELEMETRY LOG >", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }
        }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showManualEntryDialog) {
        AlertDialog(
            onDismissRequest = { showManualEntryDialog = false },
            title = { Text("Log Manual Entry") },
            text = { Text("Record a completed session to your Health Connect telemetry.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logManualSession()
                    showManualEntryDialog = false
                }) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualEntryDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun ActivityVectorItem(
    icon: ImageVector,
    title: String,
    time: String,
    duration: String,
    rpe: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = Primary, fontWeight = FontWeight.Bold)
            Text(time, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(duration, style = MaterialTheme.typography.labelMedium, color = Primary)
            Text(rpe, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}
