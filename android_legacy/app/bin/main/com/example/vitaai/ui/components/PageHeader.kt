package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.Primary
import com.example.vitaai.ui.theme.VitaTextStyles
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
                style = VitaTextStyles.pageKicker,
                color = Color.Black.copy(alpha = 0.40f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = VitaTextStyles.pageTitle,
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
                    .background(Primary)
            )
            Text(
                text = "AI Sync",
                style = VitaTextStyles.syncBadge,
                color = Color(0xFF0891B2)
            )
        }
    }
}
