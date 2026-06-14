package com.example.vitaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitaai.data.MeditationSession
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.PageHeader

@Composable
fun MeditationScreen(
    navController: NavController,
    viewModel: MeditationViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val playing by viewModel.currentlyPlaying.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    AuraBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                PageHeader(
                    title = "Meditation",
                    kicker = "Pause, breathe, reset."
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (sessions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                            )
                        }
                    } else {
                        item {
                            HeroMeditationCard(
                                session = sessions.first(),
                                onClick = { viewModel.play(sessions.first()) }
                            )
                        }
                        item {
                            Text(
                                text = "All Sessions",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(sessions, key = { it.id }) { session ->
                            MeditationListItem(
                                session = session,
                                isCurrent = playing?.id == session.id,
                                isPlayingThis = playing?.id == session.id && isPlaying,
                                onClick = { viewModel.play(session) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(120.dp)) }
                    }
                }
            }

            // Mini player
            playing?.let { current ->
                MiniPlayer(
                    session = current,
                    isPlaying = isPlaying,
                    onToggle = { viewModel.togglePlayback() },
                    onClose = { viewModel.stop() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroMeditationCard(session: MeditationSession, onClick: () -> Unit) {
    val accent = Color(session.accent)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.55f))
                )
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "RECOMMENDED TODAY",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column {
                Text(
                    text = session.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${session.durationMinutes} min • ${session.category}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun MeditationListItem(
    session: MeditationSession,
    isCurrent: Boolean,
    isPlayingThis: Boolean,
    onClick: () -> Unit
) {
    val accent = Color(session.accent)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = if (isCurrent) accent.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.07f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = "${session.durationMinutes} min • ${session.category}",
                color = Color(0xFF475569),
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlayingThis) "Pause" else "Play",
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MiniPlayer(
    session: MeditationSession,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(session.accent)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = if (isPlaying) "Now playing" else "Paused",
                color = Color(0xFF475569),
                fontSize = 11.sp
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text("✕", color = Color(0xFF475569), fontSize = 14.sp)
        }
    }
}
