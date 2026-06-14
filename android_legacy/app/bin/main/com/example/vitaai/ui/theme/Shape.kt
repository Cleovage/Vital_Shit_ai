package com.example.vitaai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val VitaShapes = Shapes(
    // sm: 2dp
    extraSmall = RoundedCornerShape(2.dp),
    // DEFAULT: 4dp
    small = RoundedCornerShape(4.dp),
    // md: 6dp
    medium = RoundedCornerShape(6.dp),
    // lg: 8dp — Primary containers
    large = RoundedCornerShape(8.dp),
    // xl: 12dp
    extraLarge = RoundedCornerShape(12.dp)
)

/** Subtle rounding for technical feel */
val TechnicalShape = RoundedCornerShape(4.dp)

/** Pill shape for legacy compatibility, to be phased out */
val PillShape = RoundedCornerShape(50)
