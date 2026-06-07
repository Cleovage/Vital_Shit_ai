package com.example.vitaai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.vitaai.ui.components.ActionRow
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlowButton
import com.example.vitaai.ui.components.PageHeader
import com.example.vitaai.ui.theme.*
import java.util.Locale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentWeight = state.weightKg
    val currentHeight = state.heightMeters
    val weightLbs = currentWeight?.let { it * 2.20462 }
    val heightInches = currentHeight?.let { it * 39.3701 }

    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 1. HEADER ---
            item {
                PageHeader(title = "Profile", kicker = "Personalization")
            }

            // --- 2. PROFILE HERO CARD (1:1 copy of Web UI) ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left side: Dark slate-900 circle with User icon
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        // Right side: Name and Sync info
                        Column {
                            Text(
                                text = "Alex",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-1.12).sp
                                ),
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.permissionsGranted) 
                                    "Health Connect synced • Premium trial"
                                else 
                                    "Health Connect pending • Premium trial",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // --- 3. ACTION ROWS (1:1 copy of Web UI) ---
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionRow(
                        icon = Icons.Default.Star,
                        title = "Achievements",
                        subtitle = "8 streaks, 3 nutrition badges",
                        onClick = { 
                            Toast.makeText(context, "You're in the top 5% of users this week!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    ActionRow(
                        icon = Icons.Default.VerifiedUser,
                        title = "Health Connect",
                        subtitle = if (state.permissionsGranted) 
                            "Vitals, workouts, nutrition permissions synced" 
                        else 
                            "Permissions pending setup",
                        onClick = {
                            Toast.makeText(context, "Health Connect sync is active.", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    ActionRow(
                        icon = Icons.Default.Settings,
                        title = "Biometric Settings",
                        subtitle = "Calibrate weight, height, BMR, and targets",
                        onClick = { 
                            showEditDialog = true 
                        }
                    )
                    
                    val hydrationGoal = if (state.hydrationGoalLiters > 0.0) state.hydrationGoalLiters else 2.4
                    ActionRow(
                        icon = Icons.Default.LocalDrink,
                        title = "Hydration goal",
                        subtitle = String.format(Locale.US, "%.1f L daily target", hydrationGoal),
                        onClick = {
                            Toast.makeText(context, "Water logged! Hydration target is up to date.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        var wInput by remember { mutableStateOf(weightLbs?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
        var hInput by remember { mutableStateOf(heightInches?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
        var ageInput by remember { mutableStateOf(state.age.toString()) }
        var genderInput by remember { mutableStateOf(state.gender) }
        var activityInput by remember { mutableStateOf(state.activityLevel) }
        var stepsInput by remember { mutableStateOf(state.stepGoal.toString()) }
        var hydrationInput by remember { mutableStateOf(state.hydrationGoalLiters.toString()) }
        var exerciseInput by remember { mutableStateOf(state.exerciseMinutesGoal.toString()) }
        var caloriesInput by remember { mutableStateOf(state.caloriesBurnGoal.toString()) }
        var isInputError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { 
                Text(
                    text = "Biometric Calibration", 
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleLarge,
                    letterSpacing = (-0.5).sp
                ) 
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Configure your details below. These are synced in real-time to compute custom BMR, active burn, and macro balance targets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.55f),
                        lineHeight = 18.sp
                    )
                    
                    OutlinedTextField(
                        value = wInput,
                        onValueChange = { wInput = it; isInputError = false },
                        label = { Text("Weight (lbs)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hInput,
                        onValueChange = { hInput = it; isInputError = false },
                        label = { Text("Height (inches)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it; isInputError = false },
                        label = { Text("Age (years)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Gender type", 
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Male", "Female", "Other").forEach { g ->
                            val isSel = genderInput.lowercase() == g.lowercase()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color(0xFF0F172A) else Color.Black.copy(alpha = 0.03f))
                                    .clickable { genderInput = g }
                                    .border(1.dp, if (isSel) Color(0xFF0F172A) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g,
                                    color = if (isSel) Color.White else Color.Black.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Physical Activity Factor", 
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Sedentary", "Light", "Active", "Very Active").forEach { lvl ->
                            val isSel = activityInput.lowercase() == lvl.lowercase()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) Color(0xFF0F172A) else Color.Black.copy(alpha = 0.03f))
                                    .clickable { activityInput = lvl }
                                    .border(1.dp, if (isSel) Color(0xFF0F172A) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lvl,
                                    color = if (isSel) Color.White else Color.Black.copy(alpha = 0.6f),
                                    style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))
                    Text(
                        text = "Daily Protocol Targets", 
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black.copy(alpha = 0.45f)
                    )

                    OutlinedTextField(
                        value = stepsInput,
                        onValueChange = { stepsInput = it; isInputError = false },
                        label = { Text("Steps Target") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hydrationInput,
                        onValueChange = { hydrationInput = it; isInputError = false },
                        label = { Text("Water Target (liters)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = exerciseInput,
                        onValueChange = { exerciseInput = it; isInputError = false },
                        label = { Text("Exercise Target (mins)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = caloriesInput,
                        onValueChange = { caloriesInput = it; isInputError = false },
                        label = { Text("Active Burn Target (kcal)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isInputError) {
                        Text(
                            text = "Please enter valid positive values.",
                            color = Color(0xFFEF4444),
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
                        val age = ageInput.toIntOrNull()
                        val steps = stepsInput.toLongOrNull()
                        val hydration = hydrationInput.toDoubleOrNull()
                        val exercise = exerciseInput.toDoubleOrNull()
                        val calories = caloriesInput.toDoubleOrNull()
                        
                        if (weight != null && weight > 0.0 && height != null && height > 0.0 &&
                            age != null && age > 0 && steps != null && steps > 0 &&
                            hydration != null && hydration > 0.0 && exercise != null && exercise > 0.0 &&
                            calories != null && calories > 0.0
                        ) {
                            viewModel.calibrate(
                                weightLbs = weight,
                                heightInches = height,
                                age = age,
                                gender = genderInput,
                                activityLevel = activityInput,
                                stepGoal = steps,
                                hydrationGoalLiters = hydration,
                                exerciseMinutesGoal = exercise,
                                caloriesBurnGoal = calories
                            )
                            showEditDialog = false
                        } else {
                            isInputError = true
                        }
                    }
                ) {
                    Text("SYNC CALIBRATION", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("CANCEL", color = Color.Black.copy(alpha = 0.5f))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

