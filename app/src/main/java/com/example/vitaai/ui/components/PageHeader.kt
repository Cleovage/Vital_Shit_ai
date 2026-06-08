package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Public shared PageHeader component matching the Shader Dash light design spec.
 */
@Composable
fun PageHeader(
    title: String,
    kicker: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = kicker.uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 4.4.sp // 0.34em
                ),
                color = Color.Black.copy(alpha = 0.40f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-2).sp // -0.05em
                ),
                color = Color(0xFF0F172A) // slate-900
            )
        }

        // AI Sync active ping badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(20.dp)
                )
                .background(Color.White.copy(alpha = 0.8f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF06B6D4))
            )
            Text(
                text = "AI Sync",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF0891B2)
            )
        }
    }
}
