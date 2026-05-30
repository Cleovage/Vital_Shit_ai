package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.*

@Composable
fun ProfileScreen() {
    var showEditDialog by remember { mutableStateOf(false) }

    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 1. ATHLETE DOSSIER ---
            item {
                Column {
                    Text(
                        text = "ATHLETE DOSSIER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ALEX VANCE",
                            style = MaterialTheme.typography.displaySmall,
                            color = OnBackground,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            Modifier
                                .background(Primary, MaterialTheme.shapes.extraSmall)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "LVL 42",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Primary.copy(alpha = 0.2f), thickness = 2.dp)
                }
            }

            // --- 2. PERFORMANCE TIER ---
            item {
                ApexCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            Modifier.size(48.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Primary)
                        }
                        Column {
                            Text("ELITE STATUS", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("TOP 2% PERFORMANCE", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- 3. BIOMETRIC GRID ---
            item {
                Text(
                    "BIOMETRIC SPECIFICATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BiometricTile("WEIGHT", "178.4", "LBS", Modifier.weight(1f))
                    BiometricTile("BODY FAT", "12.8", "%", Modifier.weight(1f))
                }
            }

            item {
                ApexCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Lean Tissue"
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("151.2", style = MaterialTheme.typography.headlineLarge, color = OnBackground, fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(6.dp))
                            Text("LBS", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(4.dp).background(OutlineVariant.copy(alpha = 0.1f))) {
                            Box(Modifier.fillMaxWidth(0.78f).fillMaxHeight().background(Primary))
                        }
                    }
                }
            }

            // --- 4. CONNECTIVITY HUB ---
            item {
                Text(
                    "CONNECTIVITY HUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
            }

            item {
                ApexCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        StatusRow(Icons.Default.CloudSync, "GOOGLE HEALTH", "ACTIVE")
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.1f))
                        StatusRow(Icons.Default.Watch, "SAMSUNG GEAR", "CONNECTED")
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.1f))
                        StatusRow(Icons.Default.Link, "EXT. SENSORS", "SCANNING...", color = Color.Yellow)
                    }
                }
            }

            item {
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text("CALIBRATE BIOMETRICS", color = OnBackground, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("BIOMETRIC CALIBRATION") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = "178.4",
                        onValueChange = {},
                        label = { Text("WEIGHT (LBS)") },
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    OutlinedTextField(
                        value = "71",
                        onValueChange = {},
                        label = { Text("HEIGHT (IN)") },
                        shape = MaterialTheme.shapes.extraSmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("SAVE DATA", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("CANCEL", color = OnSurfaceVariant)
                }
            },
            containerColor = SurfaceContainerHighest,
            shape = MaterialTheme.shapes.extraSmall
        )
    }
}

@Composable
private fun BiometricTile(label: String, value: String, unit: String, modifier: Modifier) {
    ApexCard(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium, color = OnBackground, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = OnSurfaceVariant))
            }
        }
    }
}

@Composable
private fun StatusRow(icon: ImageVector, name: String, status: String, color: Color = Primary) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
            Text(name, style = MaterialTheme.typography.labelLarge, color = OnBackground, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Text(status, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
