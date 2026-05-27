package com.example.vitaai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.Error
import com.example.vitaai.ui.theme.Primary
import com.example.vitaai.ui.theme.OnPrimary
import kotlinx.coroutines.delay

@Composable
fun SessionScreen(navController: NavController) {
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }

    val minutes = seconds / 60
    val remSeconds = seconds % 60
    val timeString = String.format("%02d:%02d", minutes, remSeconds)

    AuraBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "ACTIVE PROTOCOL",
                style = MaterialTheme.typography.labelLarge,
                color = Primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                timeString,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                color = Primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    // Navigate back
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Error, contentColor = OnPrimary),
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("STOP & SAVE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
