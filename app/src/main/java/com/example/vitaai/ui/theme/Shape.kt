package com.example.vitaai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val VitaShapes = Shapes(
    // sm: 0.25rem = 4dp
    extraSmall = RoundedCornerShape(4.dp),
    // DEFAULT: 0.5rem = 8dp
    small = RoundedCornerShape(8.dp),
    // md: 0.75rem = 12dp
    medium = RoundedCornerShape(12.dp),
    // lg: 1rem = 16dp — Primary containers
    large = RoundedCornerShape(16.dp),
    // xl: 1.5rem = 24dp
    extraLarge = RoundedCornerShape(24.dp)
)

/** Full pill shape for buttons and chips */
val PillShape = RoundedCornerShape(50)
