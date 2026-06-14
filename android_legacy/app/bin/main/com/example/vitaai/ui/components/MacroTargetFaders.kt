package com.example.vitaai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitaai.ui.theme.Primary
import com.example.vitaai.ui.theme.Secondary
import com.example.vitaai.ui.theme.Tertiary
import kotlin.math.roundToInt

@Composable
fun MacroTargetFaders(
    initialCalories: Double = 2300.0,
    initialProteinGrams: Double = 140.0,
    initialCarbsGrams: Double = 275.0,
    initialFatGrams: Double = 75.0,
    onMacrosChanged: (calories: Double, proteinPercent: Double, carbsPercent: Double, fatPercent: Double) -> Unit
) {
    var caloriesInput by remember(initialCalories) { mutableStateOf(initialCalories.roundToInt().toString()) }
    
    // Convert initial grams to percentages
    val initialProtPct = ((initialProteinGrams * 4.0) / initialCalories * 100.0).toFloat().coerceIn(10f, 80f)
    val initialCarbPct = ((initialCarbsGrams * 4.0) / initialCalories * 100.0).toFloat().coerceIn(10f, 80f)
    val initialFatPct = (100f - initialProtPct - initialCarbPct).coerceIn(10f, 60f)

    var proteinPercent by remember(initialProteinGrams, initialCalories) { mutableStateOf(initialProtPct) }
    var carbPercent by remember(initialCarbsGrams, initialCalories) { mutableStateOf(initialCarbPct) }
    var fatPercent by remember(initialFatGrams, initialCalories) { mutableStateOf(initialFatPct) }

    val currentCal = caloriesInput.toDoubleOrNull() ?: initialCalories

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "MACRO TARGET CALIBRATOR",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.45f),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                "CALORIC TARGET", 
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black.copy(alpha = 0.6f)
            )
            OutlinedTextField(
                value = caloriesInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && newValue.length < 6) {
                        caloriesInput = newValue
                        val cal = newValue.toDoubleOrNull() ?: 2000.0
                        onMacrosChanged(cal, proteinPercent.toDouble(), carbPercent.toDouble(), fatPercent.toDouble())
                    }
                },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.03f)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Protein Slider
        MacroSlider(
            label = "PROTEIN TARGET",
            percent = proteinPercent,
            color = Primary,
            grams = ((currentCal * (proteinPercent / 100)) / 4).toInt(),
            onValueChange = { newVal ->
                val delta = newVal - proteinPercent
                proteinPercent = newVal
                // Balance other faders
                carbPercent = (carbPercent - delta / 2).coerceIn(10f, 80f)
                fatPercent = (100f - proteinPercent - carbPercent).coerceIn(10f, 60f)
                onMacrosChanged(currentCal, proteinPercent.toDouble(), carbPercent.toDouble(), fatPercent.toDouble())
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Carbs Slider
        MacroSlider(
            label = "CARBS TARGET",
            percent = carbPercent,
            color = Secondary,
            grams = ((currentCal * (carbPercent / 100)) / 4).toInt(),
            onValueChange = { newVal ->
                val delta = newVal - carbPercent
                carbPercent = newVal
                proteinPercent = (proteinPercent - delta / 2).coerceIn(10f, 80f)
                fatPercent = (100f - carbPercent - proteinPercent).coerceIn(10f, 60f)
                onMacrosChanged(currentCal, proteinPercent.toDouble(), carbPercent.toDouble(), fatPercent.toDouble())
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fat Slider
        MacroSlider(
            label = "FAT TARGET",
            percent = fatPercent,
            color = Tertiary,
            grams = ((currentCal * (fatPercent / 100)) / 9).toInt(),
            onValueChange = { newVal ->
                val delta = newVal - fatPercent
                fatPercent = newVal
                proteinPercent = (proteinPercent - delta / 2).coerceIn(10f, 80f)
                carbPercent = (100f - fatPercent - proteinPercent).coerceIn(10f, 80f)
                onMacrosChanged(currentCal, proteinPercent.toDouble(), carbPercent.toDouble(), fatPercent.toDouble())
            }
        )
    }
}

@Composable
private fun MacroSlider(
    label: String,
    percent: Float,
    color: Color,
    grams: Int,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.Black.copy(alpha = 0.5f))
            Text("${percent.toInt()}% (${grams}g)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF0F172A))
        }
        Slider(
            value = percent,
            onValueChange = onValueChange,
            valueRange = 10f..80f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color.Black.copy(alpha = 0.05f)
            )
        )
    }
}
