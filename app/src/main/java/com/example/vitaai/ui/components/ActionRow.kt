package com.example.vitaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.Error
import com.example.vitaai.ui.theme.OnBackground
import com.example.vitaai.ui.theme.VitaTextStyles

enum class ActionRowStyle {
    Default,
    Destructive
}

/**
 * Public shared ActionRow component matching the Shader Dash light design spec.
 */
@Composable
fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    style: ActionRowStyle = ActionRowStyle.Default,
    onClick: () -> Unit
) {
    val titleColor = when (style) {
        ActionRowStyle.Default -> OnBackground
        ActionRowStyle.Destructive -> Error
    }
    val iconBackground = when (style) {
        ActionRowStyle.Default -> Color.Black.copy(alpha = 0.04f)
        ActionRowStyle.Destructive -> Error.copy(alpha = 0.08f)
    }
    val iconTint = when (style) {
        ActionRowStyle.Default -> OnBackground
        ActionRowStyle.Destructive -> Error
    }
    val chevronTint = when (style) {
        ActionRowStyle.Default -> OnBackground
        ActionRowStyle.Destructive -> Error.copy(alpha = 0.5f)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = VitaTextStyles.actionTitle,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = VitaTextStyles.actionSubtitle,
                    color = Color.Black.copy(alpha = 0.5f)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = chevronTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
