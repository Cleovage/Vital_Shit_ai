package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// coil import removed
import com.example.vitaai.ui.components.ApexCard
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.theme.*

@Composable
fun ProfileScreen() {
    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                ApexCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.small)
                                .border(1.dp, Primary, MaterialTheme.shapes.small)
                                .padding(8.dp)
                                .border(1.dp, Primary.copy(alpha = 0.5f), MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Primary, modifier = Modifier.size(64.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ALEX VANCE",
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp),
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ELITE TIER · Lvl 42",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* TODO */ },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = OnPrimary
                            ),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text("EDIT METRICS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Body Composition
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BODY COMPOSITION", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                    ApexCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ProfileMetricItem("WEIGHT", "178.4", "LBS", "- 1.2%", Modifier.weight(1f))
                                ProfileMetricItem("BODY FAT", "12.8", "%", "- 0.4%", Modifier.weight(1f))
                            }
                            Column {
                                Text("LEAN MUSCLE MASS", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("151.2", style = MaterialTheme.typography.headlineLarge, color = Primary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("LBS", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("+ 0.8%", style = MaterialTheme.typography.labelSmall, color = Primary, modifier = Modifier.padding(bottom = 4.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(SurfaceContainerHigh)) {
                                    Box(modifier = Modifier.fillMaxWidth(0.75f).height(4.dp).background(Primary))
                                }
                            }
                        }
                    }
                }
            }

            // Data Sources
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DATA SOURCES", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                    ApexCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            DataSourceItem(Icons.Default.Favorite, "APPLE HEALTH", "SYNCED")
                            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.2f))
                            DataSourceItem(Icons.Default.Place, "GARMIN", "SYNCED")
                            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.2f))
                            DataSourceItem(Icons.Default.DirectionsRun, "STRAVA", "DISCONNECTED", false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricItem(label: String, value: String, unit: String, trend: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineLarge, color = Primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(unit, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        }
        Text(trend, style = MaterialTheme.typography.labelSmall, color = Primary)
    }
}

@Composable
private fun DataSourceItem(icon: ImageVector, title: String, status: String, synced: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = OnSurface, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = OnSurface, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (synced) Primary else OnSurfaceVariant))
                Spacer(modifier = Modifier.width(6.dp))
                Text(status, style = MaterialTheme.typography.labelSmall, color = if (synced) Primary else OnSurfaceVariant)
            }
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = OnSurfaceVariant)
    }
}
