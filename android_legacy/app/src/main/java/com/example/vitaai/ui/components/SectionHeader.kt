package com.example.vitaai.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vitaai.ui.theme.VitaTextStyles
import java.util.Locale

/**
 * Section label for grouped content blocks (e.g. Profile menu sections).
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Text(
            text = title.uppercase(Locale.US),
            style = VitaTextStyles.sectionLabel,
            color = Color.Black.copy(alpha = 0.35f)
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = VitaTextStyles.sectionSubtitle,
                color = Color.Black.copy(alpha = 0.45f)
            )
        }
    }
}
