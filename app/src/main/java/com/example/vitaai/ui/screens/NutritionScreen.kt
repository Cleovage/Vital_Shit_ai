package com.example.vitaai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vitaai.data.DrinkCatalogItem
import com.example.vitaai.data.FoodCatalogItem
import com.example.vitaai.data.NutritionSummary
import com.example.vitaai.ui.components.GlassCard
import com.example.vitaai.ui.components.GlassCardGlow
import com.example.vitaai.ui.components.AuraBackground
import com.example.vitaai.ui.components.LuminousDonutChart
import com.example.vitaai.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(viewModel: NutritionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "FUELING CENTER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "OPTIMIZATION",
                        style = MaterialTheme.typography.displaySmall,
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Primary.copy(alpha = 0.2f), thickness = 2.dp)
                }
            }

            item {
                NutritionSummaryCard(state.summary)
            }

            item {
                HydrationCommand(state.summary)
            }

            item {
                MealSelector(selectedMeal = state.selectedMeal, onMealSelected = viewModel::setMeal)
            }

            item {
                Text(
                    "QUICK LOGS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.foods) { food ->
                        FoodCard(food = food, onClick = { viewModel.addFood(food) })
                    }
                }
            }

            item {
                QuickAddMacros(onQuickAdd = viewModel::quickAdd)
            }

            item {
                Text(
                    "HYDRATION CATALOG",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.drinks) { drink ->
                        DrinkCard(drink = drink, onClick = { viewModel.addDrink(drink) })
                    }
                }
            }

            item {
                LogHistory(summary = state.summary)
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun NutritionSummaryCard(summary: NutritionSummary) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "MACRO ANALYSIS",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                LuminousDonutChart(
                    values = listOf(
                        summary.proteinGrams.toFloat().coerceAtLeast(0.1f),
                        summary.carbsGrams.toFloat().coerceAtLeast(0.1f),
                        summary.fatGrams.toFloat().coerceAtLeast(0.1f)
                    ),
                    colors = listOf(Primary, Color(0xFFC7C5CE), Color(0xFFE3E1EA).copy(alpha = 0.5f)),
                    modifier = Modifier.size(110.dp),
                    thickness = 12.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.calories.roundToInt().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "KCAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TechnicalMacroRow("PROTEIN", summary.proteinGrams, 120.0, Primary)
                TechnicalMacroRow("CARBS", summary.carbsGrams, 260.0, Color(0xFFC7C5CE))
                TechnicalMacroRow("FAT", summary.fatGrams, 70.0, Color(0xFFE3E1EA).copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun HydrationCommand(summary: NutritionSummary) {
    val progress = (summary.hydrationMl / 3000f).toFloat().coerceIn(0f, 1f)
    
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Volumetric fluid capsule bar
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(progress)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp, topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF00838F)
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(3.dp)
                            .align(Alignment.CenterStart)
                            .padding(start = 2.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HYDRATION LEVEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(summary.hydrationMl / 1000.0).roundToOne()}L / 3.0L",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnBackground,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "STATUS: ${hydrationStatus(summary.hydrationMl).uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (summary.hydrationMl >= 2000) Color(0xFF00E5FF) else Color(0xFFFFB300)
                    )
                }
            }
        }
    }
}

@Composable
private fun TechnicalMacroRow(label: String, value: Double, target: Double, color: Color) {
    val progress = (value / target).toFloat().coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = OnSurfaceVariant
            )
            Text(
                text = "${value.roundToInt()}G / ${target.roundToInt()}G",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.6f),
                                color
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun MealSelector(selectedMeal: String, onMealSelected: (String) -> Unit) {
    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val vitaColors = LocalVitaColors.current
    val capsuleShape = RoundedCornerShape(24.dp)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(capsuleShape)
            .background(vitaColors.glassFill)
            .border(1.dp, vitaColors.glassBorderDark, capsuleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        meals.forEach { meal ->
            val selected = meal == selectedMeal
            val backgroundColor by animateColorAsState(
                targetValue = if (selected) Primary else Color.Transparent,
                label = "mealBackground$meal"
            )
            val textColor by animateColorAsState(
                targetValue = if (selected) OnPrimary else OnSurfaceVariant,
                label = "mealText$meal"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(capsuleShape)
                    .background(backgroundColor)
                    .clickable { onMealSelected(meal) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = meal.uppercase(),
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun FoodCard(food: FoodCatalogItem, onClick: () -> Unit) {
    GlassCardGlow(
        modifier = Modifier
            .size(width = 160.dp, height = 130.dp)
            .clickable(onClick = onClick),
        glowColor = Primary,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FOOD",
                        style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Primary)
                    )
                }
            }
            Column {
                Text(
                    text = food.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                    color = OnBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = food.servingLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = OnSurfaceVariant
                )
            }
            Text(
                text = "${food.proteinGrams.roundToInt()}G PRO | ${food.calories.roundToInt()} KCAL",
                style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = Primary, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun DrinkCard(drink: DrinkCatalogItem, onClick: () -> Unit) {
    val drinkColor = Color(0xFF00E5FF)
    GlassCardGlow(
        modifier = Modifier
            .size(width = 160.dp, height = 130.dp)
            .clickable(onClick = onClick),
        glowColor = drinkColor,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (drink.type == "Water") Icons.Default.WaterDrop else Icons.Default.LocalDrink,
                    contentDescription = null,
                    tint = drinkColor,
                    modifier = Modifier.size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .background(drinkColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = drink.type.uppercase(),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = drinkColor)
                    )
                }
            }
            Column {
                Text(
                    text = drink.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                    color = OnBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${drink.defaultMl.roundToInt()} ML",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = OnSurfaceVariant
                )
            }
            Text(
                text = drink.benefit.uppercase(),
                style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun QuickAddMacros(onQuickAdd: (Double, Double, Double, Double) -> Unit) {
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "DIRECT INJECTION",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MacroInput("KCAL", calories, { calories = it }, Modifier.weight(1f))
                MacroInput("PRO (G)", protein, { protein = it }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MacroInput("CHO (G)", carbs, { carbs = it }, Modifier.weight(1f))
                MacroInput("FAT (G)", fat, { fat = it }, Modifier.weight(1f))
            }
            Button(
                onClick = {
                    onQuickAdd(
                        calories.toDoubleOrNull() ?: 0.0,
                        protein.toDoubleOrNull() ?: 0.0,
                        carbs.toDoubleOrNull() ?: 0.0,
                        fat.toDoubleOrNull() ?: 0.0
                    )
                    calories = ""
                    protein = ""
                    carbs = ""
                    fat = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("LOG PERFORMANCE DATA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MacroInput(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    val vitaColors = LocalVitaColors.current
    val shape = RoundedCornerShape(12.dp)
    
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = vitaColors.glassBorderLight.copy(alpha = 0.2f),
            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
            unfocusedContainerColor = vitaColors.glassFill,
            focusedLabelColor = Primary,
            unfocusedLabelColor = OnSurfaceVariant,
            cursorColor = Primary
        )
    )
}

@Composable
private fun LogHistory(summary: NutritionSummary) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "RECENT LOGS",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (summary.foods.isEmpty() && summary.drinks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO DATA LOGGED TODAY",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
            summary.foods.take(5).forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.02f))
                        .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${it.meal.uppercase()}: ${it.name.uppercase()}",
                            color = OnBackground,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "+${it.calories.roundToInt()} KCAL",
                        color = Primary,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
            summary.drinks.take(5).forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.02f))
                        .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = it.name.uppercase(),
                            color = OnBackground,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "+${it.volumeMl.roundToInt()} ML",
                        color = Color(0xFF00E5FF),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

private fun hydrationStatus(hydrationMl: Double): String {
    return when {
        hydrationMl >= 2500 -> "optimal"
        hydrationMl >= 1700 -> "stable"
        hydrationMl >= 900 -> "climbing"
        else -> "critical"
    }
}

private fun Double.roundToOne(): String = String.format(Locale.US, "%.1f", this)
