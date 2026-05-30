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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.vitaai.ui.components.ApexCard
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
                    style = MaterialTheme.typography.labelSmall,
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
                    style = MaterialTheme.typography.labelSmall,
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
    ApexCard(
        modifier = Modifier.fillMaxWidth(),
        title = "Macro Analysis"
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                    colors = listOf(Primary, Secondary, Tertiary),
                    modifier = Modifier.size(110.dp),
                    thickness = 14.dp
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
                TechnicalMacroRow("CARBS", summary.carbsGrams, 260.0, Secondary)
                TechnicalMacroRow("FAT", summary.fatGrams, 70.0, Tertiary)
            }
        }
    }
}

@Composable
private fun HydrationCommand(summary: NutritionSummary) {
    ApexCard(
        modifier = Modifier.fillMaxWidth(),
        title = "Hydration Level"
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                Modifier
                    .width(10.dp)
                    .height(80.dp)
                    .background(OutlineVariant.copy(alpha = 0.1f), MaterialTheme.shapes.extraSmall)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight((summary.hydrationMl / 3000f).toFloat().coerceIn(0f, 1f))
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFF00B0FF), MaterialTheme.shapes.extraSmall)
                )
            }
            
            Column {
                Text(
                    text = "${(summary.hydrationMl / 1000.0).roundToOne()}L / 3.0L",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "STATUS: ${hydrationStatus(summary.hydrationMl).uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (summary.hydrationMl >= 2000) Primary else Color(0xFFFFC107)
                )
            }
        }
    }
}

@Composable
private fun TechnicalMacroRow(label: String, value: Double, target: Double, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text("${value.roundToInt()}G", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(OutlineVariant.copy(alpha = 0.1f))) {
            Box(
                Modifier
                    .fillMaxWidth((value / target).toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
private fun MealSelector(selectedMeal: String, onMealSelected: (String) -> Unit) {
    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        meals.forEach { meal ->
            val selected = meal == selectedMeal
            val backgroundColor by animateColorAsState(
                targetValue = if (selected) Primary else SurfaceContainerHigh,
                label = "mealBackground$meal"
            )
            val textColor by animateColorAsState(
                targetValue = if (selected) OnPrimary else Primary,
                label = "mealText$meal"
            )
            val scale by animateFloatAsState(
                targetValue = if (selected) 1f else 0.97f,
                label = "mealScale$meal"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .background(backgroundColor, MaterialTheme.shapes.extraSmall)
                    .border(
                        width = 1.dp,
                        color = if (selected) Primary.copy(alpha = 0.4f) else OutlineVariant.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .clickable { onMealSelected(meal) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(meal.uppercase(), color = textColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FoodCard(food: FoodCatalogItem, onClick: () -> Unit) {
    ApexCard(
        modifier = Modifier
            .size(width = 160.dp, height = 130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
            Column {
                Text(food.name.uppercase(), style = MaterialTheme.typography.labelLarge, color = OnBackground, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(food.servingLabel, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Text("${food.proteinGrams.roundToInt()}G P | ${food.calories.roundToInt()} KCAL", style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = Primary))
        }
    }
}

@Composable
private fun DrinkCard(drink: DrinkCatalogItem, onClick: () -> Unit) {
    ApexCard(
        modifier = Modifier
            .size(width = 160.dp, height = 130.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(if (drink.type == "Water") Icons.Default.WaterDrop else Icons.Default.LocalDrink, contentDescription = null, tint = Color(0xFF00B0FF), modifier = Modifier.size(16.dp))
            Column {
                Text(drink.name.uppercase(), style = MaterialTheme.typography.labelLarge, color = OnBackground, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${drink.defaultMl.roundToInt()} ML", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Text(drink.benefit.uppercase(), style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, color = OnSurfaceVariant), maxLines = 2)
        }
    }
}

@Composable
private fun QuickAddMacros(onQuickAdd: (Double, Double, Double, Double) -> Unit) {
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    ApexCard(
        modifier = Modifier.fillMaxWidth(),
        title = "Direct Injection"
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text("LOG PERFORMANCE DATA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MacroInput(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall,
        shape = MaterialTheme.shapes.extraSmall
    )
}

@Composable
private fun LogHistory(summary: NutritionSummary) {
    ApexCard(
        modifier = Modifier.fillMaxWidth(),
        title = "Recent Logs"
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (summary.foods.isEmpty() && summary.drinks.isEmpty()) {
                Text("NO DATA LOGGED", color = OnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            summary.foods.take(5).forEach {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${it.meal.uppercase()}: ${it.name.uppercase()}", color = OnBackground, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Text("${it.calories.roundToInt()} KCAL", color = Primary, style = MaterialTheme.typography.labelSmall)
                }
            }
            summary.drinks.take(5).forEach {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(it.name.uppercase(), color = OnBackground, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Text("${it.volumeMl.roundToInt()} ML", color = Color(0xFF00B0FF), style = MaterialTheme.typography.labelSmall)
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
