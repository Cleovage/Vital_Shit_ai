package com.example.vitaai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlassCardGlow
import com.example.vitaai.ui.components.GlowButton
import com.example.vitaai.ui.components.ProgressRing
import com.example.vitaai.ui.theme.*
import java.util.Locale

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val currentWeight = state.weightKg
    val currentHeight = state.heightMeters
    val weightLbs = currentWeight?.let { it * 2.20462 }
    val heightInches = currentHeight?.let { it * 39.3701 }
    val bmi = if (currentWeight != null && currentHeight != null && currentHeight > 0.0) {
        currentWeight / (currentHeight * currentHeight)
    } else null

    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 1. ATHLETE DOSSIER & PROGRESSION RING ---
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ATHLETE STATUS CARD",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                        ProgressRing(
                            progress = state.levelProgress,
                            size = 150.dp,
                            strokeWidth = 10.dp,
                            glowWidth = 18.dp,
                            colors = listOf(Primary, Secondary),
                            glowColor = GlowPrimary
                        )
                        Box(
                            Modifier
                                .size(108.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .border(1.dp, OutlineVariant.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ALEX VANCE",
                            style = MaterialTheme.typography.titleLarge,
                            color = OnBackground,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Box(
                            Modifier
                                .background(Primary, MaterialTheme.shapes.extraSmall)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "LVL ${state.level}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = "RANK STATUS: ELITE PERFORMA | ${state.completedWorkoutsCount} SESSIONS COMPLETE",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Primary.copy(alpha = 0.2f), thickness = 2.dp)
                }
            }

            // --- 2. BIOMETRIC INTERACTIVE DETAILS ---
            item {
                Text(
                    "BIOMETRIC ANALYSIS HUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.weightKg == null || state.heightMeters == null) {
                // Missing data prompt
                item {
                    GlassCardGlow(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = Secondary
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Secondary, modifier = Modifier.size(36.dp))
                            Text(
                                "BIOMETRIC SYNC INCOMPLETE",
                                style = MaterialTheme.typography.titleMedium,
                                color = Secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Health Connect biometric metrics are uncalibrated. Complete manual calibration below to activate dynamic somatic index analysis.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                // Sourced biometrics display
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BiometricCard(
                            label = "WEIGHT",
                            value = String.format(Locale.US, "%.1f", weightLbs ?: 0.0),
                            unit = "LBS",
                            subtext = String.format(Locale.US, "%.1f kg in Health Connect", state.weightKg),
                            modifier = Modifier.weight(1f)
                        )
                        BiometricCard(
                            label = "HEIGHT",
                            value = String.format(Locale.US, "%.1f", heightInches ?: 0.0),
                            unit = "IN",
                            subtext = String.format(Locale.US, "%.2fm in Health Connect", state.heightMeters),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                bmi?.let { bmiValue ->
                    item {
                        val (bmiCategory, bmiColor) = when {
                            bmiValue < 18.5 -> "UNDERWEIGHT" to Color.Yellow
                            bmiValue < 25.0 -> "NORMAL SOMATOTYPE" to Primary
                            bmiValue < 30.0 -> "OVERWEIGHT" to Color(0xFFFFAB40) // Orange
                            else -> "OBESE SOMATOTYPE" to Color.Red
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("BODY MASS INDEX (BMI)", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                        Text(bmiCategory, style = MaterialTheme.typography.titleMedium, color = bmiColor, fontWeight = FontWeight.Black)
                                    }
                                    Text(
                                        text = String.format(Locale.US, "%.1f", bmiValue),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = OnBackground,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(Modifier.height(4.dp))
                                
                                // Clean dynamic progress slider of body index
                                val progress = (bmiValue / 40.0f).coerceIn(0.0, 1.0)
                                Box(Modifier.fillMaxWidth().height(6.dp).background(OutlineVariant.copy(alpha = 0.15f), MaterialTheme.shapes.small)) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth(progress.toFloat())
                                            .fillMaxHeight()
                                            .background(bmiColor, MaterialTheme.shapes.small)
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("18.5", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Text("25.0", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Text("30.0", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. DYNAMIC CONNECTIVITY HUB ---
            item {
                Text(
                    "CYBERNETIC CONNECTIVITY HUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ConnectionRow(
                            icon = Icons.Default.CloudSync,
                            name = "HEALTH CONNECT DATABASE",
                            status = if (state.permissionsGranted) "SYNC ACTIVE" else "PERMISSION PENDING",
                            connected = state.permissionsGranted
                        )
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.12f))
                        ConnectionRow(
                            icon = Icons.Default.VerifiedUser,
                            name = "VITALS READ/WRITE CONDUIT",
                            status = if (state.permissionsGranted) "ENGAGED" else "DISENGAGED",
                            connected = state.permissionsGranted
                        )
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.12f))
                        ConnectionRow(
                            icon = Icons.Default.CloudUpload,
                            name = "FIREBASE CLOUD SYNC",
                            status = if (state.isLoggedIntoFirebase) "AUTHORIZED" else "LOG IN REQUIRED",
                            connected = state.isLoggedIntoFirebase
                        )
                        
                        if (state.isLoggedIntoFirebase) {
                            Spacer(Modifier.height(8.dp))
                            GlowButton(
                                text = "PUSH TO CLOUD",
                                onClick = { viewModel.syncToCloud() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            state.lastSyncStatus?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. CALIBRATION TRIGGER ---
            item {
                GlowButton(
                    text = "CALIBRATE SOMATIC BIOMETRICS",
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showEditDialog) {
        var wInput by remember { mutableStateOf(weightLbs?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
        var hInput by remember { mutableStateOf(heightInches?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
        var isInputError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { 
                Text(
                    "BIOMETRIC CALIBRATION", 
                    color = Primary, 
                    fontWeight = FontWeight.Black, 
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 1.sp
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Input weight and height. These calibration entries will write directly back to your Health Connect profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    OutlinedTextField(
                        value = wInput,
                        onValueChange = { 
                            wInput = it
                            isInputError = false 
                        },
                        label = { Text("WEIGHT (LBS)") },
                        shape = MaterialTheme.shapes.extraSmall,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hInput,
                        onValueChange = { 
                            hInput = it
                            isInputError = false 
                        },
                        label = { Text("HEIGHT (INCHES)") },
                        shape = MaterialTheme.shapes.extraSmall,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isInputError) {
                        Text(
                            "Please enter valid positive values.",
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val weight = wInput.toDoubleOrNull()
                        val height = hInput.toDoubleOrNull()
                        if (weight != null && weight > 0.0 && height != null && height > 0.0) {
                            viewModel.calibrate(weight, height)
                            showEditDialog = false
                        } else {
                            isInputError = true
                        }
                    }
                ) {
                    Text("SYNC CALIBRATION", color = Primary, fontWeight = FontWeight.Bold)
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
private fun BiometricCard(
    label: String,
    value: String,
    unit: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.displaySmall, color = OnBackground, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            }
            Text(subtext, style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, color = OnSurfaceVariant.copy(alpha = 0.8f)))
        }
    }
}

@Composable
private fun ConnectionRow(
    icon: ImageVector,
    name: String,
    status: String,
    connected: Boolean
) {
    val indicatorColor = if (connected) Primary else Color.Yellow
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
            Text(name, style = MaterialTheme.typography.labelSmall, color = OnBackground, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(indicatorColor))
            Text(status, style = MaterialTheme.typography.labelSmall, color = indicatorColor, fontWeight = FontWeight.Bold)
        }
    }
}
