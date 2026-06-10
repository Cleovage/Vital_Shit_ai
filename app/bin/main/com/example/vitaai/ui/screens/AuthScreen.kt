package com.example.vitaai.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlowButton
import com.example.vitaai.ui.theme.Primary
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val webClientId = context.getString(com.example.vitaai.R.string.default_web_client_id)

    val googleSignInClient = remember(webClientId) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogleCredential(idToken, onAuthSuccess)
                } else {
                    viewModel.error = "Google ID token is missing."
                }
            } catch (e: ApiException) {
                viewModel.error = e.localizedMessage ?: "Google Sign-In failed."
            }
        }
    }

    AuraBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo & Header
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "VitaAI Logo",
                    tint = Primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "VITA AI",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.28).sp
                    ),
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Circadian Wellness & AI Coaching",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Main Auth Glass Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                ) {
                    AnimatedContent(
                        targetState = viewModel.isRegistering,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "AuthModeTransition"
                    ) { isReg ->
                        if (isReg) {
                            RegistrationWizard(
                                viewModel = viewModel,
                                onAuthSuccess = onAuthSuccess
                            )
                        } else {
                            LoginPanel(
                                viewModel = viewModel,
                                onAuthSuccess = onAuthSuccess,
                                googleSignInLauncher = googleSignInLauncher,
                                googleSignInClient = googleSignInClient
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginPanel(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
    googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it; viewModel.error = null },
            label = { Text("Email address") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it; viewModel.error = null },
            label = { Text("Password") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        if (viewModel.error != null) {
            Text(
                text = viewModel.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            // Login button
            InteractiveGlowButton(
                text = "SIGN IN",
                onClick = {
                    viewModel.login(onAuthSuccess)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Social Logins (Google & Apple)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InteractiveGlowButton(
                    text = "GOOGLE",
                    onClick = {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.weight(1f),
                    ghost = true
                )

                InteractiveGlowButton(
                    text = "APPLE",
                    onClick = {
                        val activity = context as? android.app.Activity
                        if (activity != null) {
                            viewModel.signInWithApple(activity, onAuthSuccess)
                        } else {
                            viewModel.error = "Activity context not found."
                        }
                    },
                    modifier = Modifier.weight(1f),
                    ghost = true
                )
            }

            // Guest pass
            InteractiveGlowButton(
                text = "GUEST PASS",
                onClick = {
                    viewModel.loginAnonymously(onAuthSuccess)
                },
                modifier = Modifier.fillMaxWidth(),
                ghost = true
            )
        }

        HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.5f)
            )
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Primary
                ),
                modifier = Modifier.clickable {
                    viewModel.isRegistering = true
                    viewModel.currentStep = 0
                    viewModel.error = null
                }
            )
        }
    }
}

@Composable
private fun RegistrationWizard(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step indicator header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step ${viewModel.currentStep + 1} of 4",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Primary
            )
            Text(
                text = when (viewModel.currentStep) {
                    0 -> "Create Account"
                    1 -> "Biometrics"
                    2 -> "Measurements"
                    else -> "Your Goals"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black.copy(alpha = 0.4f)
            )
        }

        LinearProgressIndicator(
            progress = { (viewModel.currentStep + 1) / 4f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Primary,
            trackColor = Color.Black.copy(alpha = 0.05f)
        )

        AnimatedContent(
            targetState = viewModel.currentStep,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "WizardStepTransition"
        ) { step ->
            when (step) {
                0 -> CredentialsStep(viewModel)
                1 -> SomaticStep(viewModel)
                2 -> MeasurementsStep(viewModel)
                3 -> GoalsStep(viewModel)
            }
        }

        if (viewModel.error != null) {
            Text(
                text = viewModel.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (viewModel.currentStep > 0) {
                    InteractiveGlowButton(
                        text = "BACK",
                        onClick = { viewModel.currentStep-- },
                        modifier = Modifier.weight(1f),
                        ghost = true
                    )
                }

                InteractiveGlowButton(
                    text = if (viewModel.currentStep == 3) "CREATE ACCOUNT" else "CONTINUE",
                    onClick = {
                        viewModel.error = null
                        if (viewModel.currentStep < 3) {
                            if (viewModel.currentStep == 0) {
                                if (viewModel.email.isBlank() || viewModel.password.isBlank()) {
                                    viewModel.error = "Email and password cannot be empty."
                                } else if (viewModel.password != viewModel.confirmPassword) {
                                    viewModel.error = "Passwords do not match."
                                } else {
                                    viewModel.currentStep++
                                }
                            } else {
                                viewModel.currentStep++
                            }
                        } else {
                            viewModel.register(onAuthSuccess)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.5f)
            )
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Primary
                ),
                modifier = Modifier.clickable {
                    viewModel.isRegistering = false
                    viewModel.error = null
                }
            )
        }
    }
}

@Composable
private fun CredentialsStep(viewModel: AuthViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Let's Get Started",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it; viewModel.error = null },
            label = { Text("Email Address") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it; viewModel.error = null },
            label = { Text("Password") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        OutlinedTextField(
            value = viewModel.confirmPassword,
            onValueChange = { viewModel.confirmPassword = it; viewModel.error = null },
            label = { Text("Confirm Password") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun SomaticStep(viewModel: AuthViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Biometric Calibration",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )

        OutlinedTextField(
            value = viewModel.age,
            onValueChange = { viewModel.age = it },
            label = { Text("Age (years)") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
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
                val isSel = viewModel.gender.lowercase() == g.lowercase()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) Color(0xFF0F172A) else Color.Black.copy(alpha = 0.03f))
                        .clickable { viewModel.gender = g }
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
            text = "Physical Activity Level",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Black.copy(alpha = 0.45f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Sedentary", "Light", "Active", "Very Active").forEach { lvl ->
                val isSel = viewModel.activityLevel.lowercase() == lvl.lowercase()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Color(0xFF0F172A) else Color.Black.copy(alpha = 0.03f))
                        .clickable { viewModel.activityLevel = lvl }
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
    }
}

@Composable
private fun MeasurementsStep(viewModel: AuthViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Measurements",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )

        OutlinedTextField(
            value = viewModel.weightLbs,
            onValueChange = { viewModel.weightLbs = it },
            label = { Text("Weight (lbs)") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        OutlinedTextField(
            value = viewModel.heightInches,
            onValueChange = { viewModel.heightInches = it },
            label = { Text("Height (inches)") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun GoalsStep(viewModel: AuthViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configure Daily Goals",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )

        OutlinedTextField(
            value = viewModel.stepGoal,
            onValueChange = { viewModel.stepGoal = it },
            label = { Text("Daily Steps Goal") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        OutlinedTextField(
            value = viewModel.hydrationGoalLiters,
            onValueChange = { viewModel.hydrationGoalLiters = it },
            label = { Text("Daily Water Goal (liters)") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        OutlinedTextField(
            value = viewModel.exerciseMinutesGoal,
            onValueChange = { viewModel.exerciseMinutesGoal = it },
            label = { Text("Daily Exercise Goal (minutes)") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )

        OutlinedTextField(
            value = viewModel.caloriesBurnGoal,
            onValueChange = { viewModel.caloriesBurnGoal = it },
            label = { Text("Daily Active Burn Goal (kcal)") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Black.copy(alpha = 0.1f)
            )
        )
    }
}

/**
 * Interactive version of GlowButton with a subtle press scaling animation.
 */
@Composable
fun InteractiveGlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ghost: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1.0f, label = "ButtonScale")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        val shape = RoundedCornerShape(24.dp)
        if (ghost) {
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = shape,
                interactionSource = interactionSource,
                border = BorderStroke(1.dp, Color(0x1F0F172A)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = shape,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
    }
}
