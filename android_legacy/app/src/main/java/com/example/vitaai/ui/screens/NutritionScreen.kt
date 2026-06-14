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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.example.vitaai.ui.components.SloshingWaterCapsule
import com.example.vitaai.ui.components.MacroTargetFaders
import com.example.vitaai.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(viewModel: NutritionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredFoods = remember(state.foods, searchQuery, selectedCategory) {
        state.foods.filter { food ->
            val matchesSearch = food.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "PROTEIN" -> listOf("Eggs", "Chicken Breast", "Paneer", "Greek Yogurt", "Whey Protein").contains(food.name)
                "CARBS" -> listOf("Dal", "Rice", "Banana", "Oats", "Peanut Butter").contains(food.name)
                else -> true
            }
            matchesSearch && matchesCategory
        }
    }

    val filteredDrinks = remember(state.drinks, searchQuery) {
        state.drinks.filter { drink ->
            drink.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // Dialog / Modal sheets visibility states
    var selectedFoodDetail by remember { mutableStateOf<FoodCatalogItem?>(null) }
    var selectedDrinkDetail by remember { mutableStateOf<DrinkCatalogItem?>(null) }
    var showCustomFoodDialog by remember { mutableStateOf(false) }
    
    // Interactive alert dropdown state
    var showDiagnosticDialog by remember { mutableStateOf(false) }

    // Renders the modular popups
    selectedFoodDetail?.let { food ->
        FoodDetailDialog(
            food = food,
            onDismiss = { selectedFoodDetail = null },
            onConfirm = { servings ->
                viewModel.addFood(food, servings)
                selectedFoodDetail = null
            }
        )
    }

    selectedDrinkDetail?.let { drink ->
        DrinkDetailDialog(
            drink = drink,
            onDismiss = { selectedDrinkDetail = null },
            onConfirm = { volumeMl ->
                viewModel.addDrink(drink, volumeMl)
                selectedDrinkDetail = null
            }
        )
    }

    if (showCustomFoodDialog) {
        CustomFoodDialog(
            onDismiss = { showCustomFoodDialog = false },
            onConfirm = { name, serving, calories, pro, carbs, fat, sugar, sodium ->
                viewModel.createCustomFood(name, serving, calories, pro, carbs, fat, sugar, sodium)
                showCustomFoodDialog = false
            }
        )
    }
    
    if (showDiagnosticDialog) {
        DiagnosticDialog(
            summary = state.summary,
            onDismiss = { showDiagnosticDialog = false }
        )
    }

    AuraBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Ellipsis pop up option menu button
                    Box {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
                                .background(Color.Black.copy(alpha = 0.04f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options Menu",
                                tint = Color(0xFF0F172A)
                            )
                        }
                        
                        // Solid monochrome dropdown pop up menu
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = "GENERATE FUEL DIAGNOSTIC", 
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                                        color = Color(0xFF0F172A)
                                    ) 
                                },
                                onClick = {
                                    menuExpanded = false
                                    showDiagnosticDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = "DESIGN FOOD BLUEPRINT", 
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                                        color = Color(0xFF0F172A)
                                    ) 
                                },
                                onClick = {
                                    menuExpanded = false
                                    showCustomFoodDialog = true
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Primary.copy(alpha = 0.25f), thickness = 2.dp)
            }

            item {
                NutritionSummaryCard(
                    summary = state.summary,
                    targetCalories = state.targetCalories,
                    targetProtein = state.targetProtein,
                    targetCarbs = state.targetCarbs,
                    targetFat = state.targetFat
                )
            }

            item {
                MacroTargetFaders(
                    initialCalories = state.targetCalories,
                    initialProteinGrams = state.targetProtein,
                    initialCarbsGrams = state.targetCarbs,
                    initialFatGrams = state.targetFat,
                    onMacrosChanged = { calories, proteinPercent, carbsPercent, fatPercent ->
                        viewModel.updateTargetMacros(calories, proteinPercent, carbsPercent, fatPercent)
                    }
                )
            }

            item {
                HydrationCommand(
                    summary = state.summary,
                    targetWaterMl = state.targetWaterMl,
                    onWaterGoalChanged = { viewModel.updateTargetWater(it) },
                    onQuickLog = { ml -> viewModel.addDrinkDirect("Water", ml) }
                )
            }

            item {
                MealSelector(
                    selectedMeal = state.selectedMeal,
                    summary = state.summary,
                    onMealSelected = viewModel::setMeal
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                        placeholder = { Text("Search blueprints...", style = MaterialTheme.typography.bodySmall, color = Color.Black.copy(alpha = 0.45f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                            focusedContainerColor = Color.White.copy(alpha = 0.78f)
                        )
                    )
                    
                    listOf("ALL", "PROTEIN", "CARBS").forEach { cat ->
                        val isSel = selectedCategory == cat
                        val color = if (isSel) Primary else Color.Black.copy(alpha = 0.04f)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .border(1.dp, if (isSel) Primary else Color.Black.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = if (isSel) Color.White else Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "ACTIVE CATALOG BLUEPRINTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        GlassCardGlow(
                            modifier = Modifier
                                .size(width = 160.dp, height = 130.dp)
                                .clickable { showCustomFoodDialog = true },
                            glowColor = Primary,
                            cornerRadius = 16.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "NEW FOOD",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "DESIGN BLUEPRINT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    items(filteredFoods) { food ->
                        FoodCard(food = food, onClick = { selectedFoodDetail = food })
                    }
                }
            }

            item {
                QuickAddMacros(onQuickAdd = viewModel::quickAdd)
            }

            item {
                Text(
                    "HYDRATION CATALOG BLUEPRINTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredDrinks) { drink ->
                        DrinkCard(drink = drink, onClick = { selectedDrinkDetail = drink })
                    }
                }
            }

            item {
                LogHistory(
                    summary = state.summary,
                    onDeleteFood = viewModel::deleteFood,
                    onDeleteDrink = viewModel::deleteDrink
                )
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun NutritionSummaryCard(
    summary: NutritionSummary,
    targetCalories: Double,
    targetProtein: Double,
    targetCarbs: Double,
    targetFat: Double
) {
    val remainingKcal = (targetCalories - summary.calories).roundToInt()

    val remainingText = if (remainingKcal >= 0) {
        "$remainingKcal KCAL REMAINING"
    } else {
        "${kotlin.math.abs(remainingKcal)} KCAL OVERTARGET"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "MACRO & ENERGY CALIBRATION",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.45f),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Color-coded macro pie
                LuminousDonutChart(
                    values = listOf(
                        summary.proteinGrams.toFloat().coerceAtLeast(0.1f),
                        summary.carbsGrams.toFloat().coerceAtLeast(0.1f),
                        summary.fatGrams.toFloat().coerceAtLeast(0.1f)
                    ),
                    colors = listOf(Primary, Secondary, Tertiary),
                    modifier = Modifier.size(110.dp),
                    thickness = 12.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.calories.roundToInt().toString(),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "CONSUMED",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
                        color = Color.Black.copy(alpha = 0.40f)
                    )
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TechnicalMacroRow("PROTEIN", summary.proteinGrams, targetProtein, Primary)
                TechnicalMacroRow("CARBS", summary.carbsGrams, targetCarbs, Secondary)
                TechnicalMacroRow("FAT", summary.fatGrams, targetFat, Tertiary)
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.Black.copy(alpha = 0.06f), thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        // Dynamic targets tracker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = remainingText.uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (remainingKcal >= 0) Color(0xFF0F172A) else Error
                )
            )
            Text(
                text = "TARGET: ${targetCalories.roundToInt()} KCAL",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = Color.Black.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun HydrationCommand(
    summary: NutritionSummary,
    targetWaterMl: Double,
    onWaterGoalChanged: (Double) -> Unit,
    onQuickLog: (Double) -> Unit
) {
    var editingWaterGoal by remember { mutableStateOf(false) }
    val progress = (summary.hydrationMl / targetWaterMl).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "hydrationLevelProgress")
    val hydrationColor = Color(0xFF00B8D4) // Keep distinct visual color coded hydration
    
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Highly interactive fluid capsule with smooth sloshing sinus wave animation
            SloshingWaterCapsule(
                progress = animatedProgress,
                modifier = Modifier
                    .width(36.dp)
                    .height(110.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HYDRATION MANAGEMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${(summary.hydrationMl / 1000.0).roundToOne()}L / ${String.format(Locale.US, "%.1f", targetWaterMl / 1000.0)}L",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.clickable { editingWaterGoal = !editingWaterGoal }
                    )
                    IconButton(
                        onClick = { editingWaterGoal = !editingWaterGoal },
                        modifier = Modifier
                            .size(24.dp)
                            .shadow(6.dp, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (editingWaterGoal) Icons.Default.CheckCircle else Icons.Default.Edit,
                            contentDescription = "Edit Goal",
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                if (editingWaterGoal) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ADJUST DAILY GOAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                    Slider(
                        value = targetWaterMl.toFloat(),
                        onValueChange = { onWaterGoalChanged(it.toDouble()) },
                        valueRange = 1000f..5000f,
                        steps = 7, // 500ml steps: 1L, 1.5L, 2L, 2.5L, 3L, 3.5L, 4L, 4.5L, 5L
                        colors = SliderDefaults.colors(
                            thumbColor = hydrationColor,
                            activeTrackColor = hydrationColor,
                            inactiveTrackColor = Color.Black.copy(alpha = 0.05f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fast hydration injector buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(250.0 to "+250", 500.0 to "+500", 1000.0 to "+1.0L").forEach { (ml, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .clickable { onQuickLog(ml) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicalMacroRow(label: String, value: Double, target: Double, color: Color) {
    val progress = (value / target).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "macroProgress$label")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = Color.Black.copy(alpha = 0.45f)
            )
            Text(
                text = "${value.roundToInt()}G / ${target.roundToInt()}G",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.03f))
                .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun MealSelector(
    selectedMeal: String,
    summary: NutritionSummary,
    onMealSelected: (String) -> Unit
) {
    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val capsuleShape = RoundedCornerShape(20.dp)
    
    // Calculate dynamically total calories logged in each meal slots for diagnostic analysis
    val breakfastCalories = summary.foods.filter { it.meal.equals("Breakfast", ignoreCase = true) }.sumOf { it.calories }.roundToInt()
    val lunchCalories = summary.foods.filter { it.meal.equals("Lunch", ignoreCase = true) }.sumOf { it.calories }.roundToInt()
    val dinnerCalories = summary.foods.filter { it.meal.equals("Dinner", ignoreCase = true) }.sumOf { it.calories }.roundToInt()
    val snackCalories = summary.foods.filter { it.meal.equals("Snack", ignoreCase = true) }.sumOf { it.calories }.roundToInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "MEAL CATEGORY TRACKING",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Black.copy(alpha = 0.45f),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(capsuleShape)
                .background(Color.Black.copy(alpha = 0.03f))
                .border(1.dp, Color.Black.copy(alpha = 0.08f), capsuleShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            meals.forEach { meal ->
                val selected = meal == selectedMeal
                val currentKcal = when (meal) {
                    "Breakfast" -> breakfastCalories
                    "Lunch" -> lunchCalories
                    "Dinner" -> dinnerCalories
                    else -> snackCalories
                }

                val backgroundColor by animateColorAsState(
                    targetValue = if (selected) Primary else Color.Transparent,
                    label = "mealBackground$meal"
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) Color.White else Color.Black.copy(alpha = 0.5f),
                    label = "mealText$meal"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(capsuleShape)
                        .background(backgroundColor)
                        .clickable { onMealSelected(meal) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = meal.uppercase(),
                            color = textColor,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                        if (currentKcal > 0) {
                            Text(
                                text = "${currentKcal}K",
                                color = if (selected) Color.White else Color(0xFF0F172A),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodCard(food: FoodCatalogItem, onClick: () -> Unit) {
    val glowColor = Primary
    GlassCardGlow(
        modifier = Modifier.size(width = 160.dp, height = 130.dp),
        onClick = onClick,
        glowColor = glowColor,
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
                    tint = glowColor,
                    modifier = Modifier.size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .background(glowColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FOOD",
                        style = androidx.compose.ui.text.TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = glowColor)
                    )
                }
            }
            Column {
                Text(
                    text = food.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = food.servingLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.Black.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "${food.proteinGrams.roundToInt()}G PRO | ${food.calories.roundToInt()} KCAL",
                style = androidx.compose.ui.text.TextStyle(fontSize = 9.sp, color = glowColor, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun DrinkCard(drink: DrinkCatalogItem, onClick: () -> Unit) {
    val drinkColor = Color(0xFF00ACC1)
    GlassCardGlow(
        modifier = Modifier.size(width = 160.dp, height = 130.dp),
        onClick = onClick,
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
                        .background(drinkColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
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
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${drink.defaultMl.roundToInt()} ML",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.Black.copy(alpha = 0.5f)
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
            text = "DIRECT MACRO INJECTION",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.45f),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("LOG PERFORMANCE DATA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MacroInput(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
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
            unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
            focusedContainerColor = Color.Black.copy(alpha = 0.03f),
            unfocusedContainerColor = Color.Transparent,
            focusedLabelColor = Color(0xFF0F172A),
            unfocusedLabelColor = Color.Black.copy(alpha = 0.45f),
            cursorColor = Primary
        )
    )
}

@Composable
private fun LogHistory(
    summary: NutritionSummary,
    onDeleteFood: (Long) -> Unit,
    onDeleteDrink: (Long) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "RECENT FUEL INJECTIONS",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.45f),
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
                        text = "NO DIAGNOSTIC LOGS REPORTED TODAY",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
            summary.foods.forEach { food ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            shadowElevation = 8f
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            clip = true
                            ambientShadowColor = Color.Black.copy(alpha = 0.08f)
                            spotShadowColor = Color.Black.copy(alpha = 0.10f)
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.03f))
                        .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = food.name.uppercase(),
                                color = Color(0xFF0F172A),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${food.meal.uppercase()} | ${food.servingMultiplier} SERVINGS",
                                color = Color.Black.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "+${food.calories.roundToInt()} KCAL",
                            color = Primary,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                        IconButton(
                            onClick = { onDeleteFood(food.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete entry",
                                tint = Error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            summary.drinks.forEach { drink ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            shadowElevation = 8f
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            clip = true
                            ambientShadowColor = Color.Black.copy(alpha = 0.08f)
                            spotShadowColor = Color.Black.copy(alpha = 0.10f)
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.03f))
                        .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF00ACC1),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = drink.name.uppercase(),
                                color = Color(0xFF0F172A),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "FLUID SYNCED",
                                color = Color.Black.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "+${drink.volumeMl.roundToInt()} ML",
                            color = Color(0xFF00ACC1),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                        IconButton(
                            onClick = { onDeleteDrink(drink.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete entry",
                                tint = Error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom detail dialog popups - structured as premium dark glassmorphic layouts

@Composable
private fun FoodDetailDialog(
    food: FoodCatalogItem,
    onDismiss: () -> Unit,
    onConfirm: (servings: Double) -> Unit
) {
    var servings by remember { mutableStateOf(1.0f) }
    val roundedServings = (servings * 2).roundToInt() / 2.0f
    val scale = roundedServings.toDouble()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FOOD BLUEPRINT DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = food.name.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Base serving: ${food.servingLabel}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.Black.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.06f), thickness = 1.dp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dynamic display of calories
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(food.calories * scale).roundToInt()} KCAL",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "LOGGED ENERGY VALUE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = Color.Black.copy(alpha = 0.40f)
                    )
                }

                // Macro breakdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.03f))
                        .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroValueColumn("PROTEIN", "${(food.proteinGrams * scale).roundToOne()}G", Primary)
                    MacroValueColumn("CARBS", "${(food.carbsGrams * scale).roundToOne()}G", Secondary)
                    MacroValueColumn("FAT", "${(food.fatGrams * scale).roundToOne()}G", Tertiary)
                }

                // Micro detail overlays (Sodium, Sugar, Fiber)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MicroDetailRow("SUGAR", "${(food.sugarGrams * scale).roundToOne()}g")
                    MicroDetailRow("SODIUM", "${(food.sodiumMg * scale).roundToInt()}mg")
                    MicroDetailRow("FIBER", "${(food.fiberGrams * scale).roundToOne()}g")
                }

                // Serving multiplier slider
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                          Text(
                              text = "SERVINGS MULTIPLIER",
                              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                              color = OnSurfaceVariant
                          )
                          Text(
                              text = "${roundedServings}x",
                              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                              color = Primary
                          )
                      }
                      Spacer(Modifier.height(4.dp))
                      Slider(
                          value = servings,
                          onValueChange = { servings = it },
                          valueRange = 0.5f..4.0f,
                          steps = 6,
                          colors = SliderDefaults.colors(
                              thumbColor = Primary,
                              activeTrackColor = Primary,
                              inactiveTrackColor = Color.Black.copy(alpha = 0.06f)
                          )
                      )
                  }
              }
          },
          confirmButton = {
              Button(
                  onClick = { onConfirm(scale) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color.White),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                  Text("INJECT BLUEPRINT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
              }
          },
          dismissButton = {
              TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                  Text("ABORT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Error)
              }
          }
      )
  }

@Composable
private fun DrinkDetailDialog(
    drink: DrinkCatalogItem,
    onDismiss: () -> Unit,
    onConfirm: (volumeMl: Double) -> Unit
) {
    var volume by remember { mutableStateOf(drink.defaultMl.toFloat()) }
    val roundedVolume = (volume / 50).roundToInt() * 50
    val multiplier = roundedVolume / 100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DRINK PROFILE SETTINGS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = drink.name.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = drink.benefit.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = OnSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.06f), thickness = 1.dp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dynamic display of Hydration
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(roundedVolume * drink.hydrationFactor).roundToInt()} ML",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "NET HYDRATION YIELD",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = Primary
                    )
                }

                // Nutritional details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.03f))
                        .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroValueColumn("CAFFEINE", "${(drink.caffeineMgPer100Ml * multiplier).roundToInt()}MG", Color(0xFF9D84E6))
                    MacroValueColumn("SUGAR", "${(drink.sugarGramsPer100Ml * multiplier).roundToOne()}G", OnSurfaceVariant)
                    MacroValueColumn("SODIUM", "${(drink.sodiumMgPer100Ml * multiplier).roundToInt()}MG", Primary)
                }

                // Volume selector slider
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOGGED VOLUME",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "${roundedVolume}ml",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = Primary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Slider(
                        value = volume,
                        onValueChange = { volume = it },
                        valueRange = 100f..1000f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary,
                            inactiveTrackColor = Color.Black.copy(alpha = 0.06f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(roundedVolume.toDouble()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("LOG FLUID INTAKE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("ABORT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Error)
            }
        }
    )
}

@Composable
private fun CustomFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, servingLabel: String, calories: Double, protein: Double, carbs: Double, fat: Double, sugar: Double, sodium: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var servingLabel by remember { mutableStateOf("100 g") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var sodium by remember { mutableStateOf("") }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
        focusedLabelColor = Primary,
        unfocusedLabelColor = OnSurfaceVariant,
        cursorColor = Primary,
        focusedTextColor = Color(0xFF0F172A),
        unfocusedTextColor = Color(0xFF0F172A)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DESIGN FOOD BLUEPRINT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Custom Food Creator",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.06f), thickness = 1.dp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("FOOD NAME (e.g. Rice Bowl)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = tfColors
                )

                OutlinedTextField(
                    value = servingLabel,
                    onValueChange = { servingLabel = it },
                    label = { Text("SERVING DESCRIPTION (e.g. 1 plate, 150g)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = tfColors
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("KCAL", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = tfColors
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("PRO (G)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = tfColors
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("CHO (G)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = tfColors
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("FAT (G)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = tfColors
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sugar,
                        onValueChange = { sugar = it },
                        label = { Text("SUGAR (G)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = tfColors
                    )
                    OutlinedTextField(
                        value = sodium,
                        onValueChange = { sodium = it },
                        label = { Text("SODIUM (MG)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = tfColors
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            servingLabel,
                            calories.toDoubleOrNull() ?: 0.0,
                            protein.toDoubleOrNull() ?: 0.0,
                            carbs.toDoubleOrNull() ?: 0.0,
                            fat.toDoubleOrNull() ?: 0.0,
                            sugar.toDoubleOrNull() ?: 0.0,
                            sodium.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("INITIALIZE BLUEPRINT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("ABORT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Error)
            }
        }
    )
}

// Opaque Solid Pop up diagnostic dialog to fulfill advanced user menus/cards prompts

@Composable
private fun DiagnosticDialog(
    summary: NutritionSummary,
    onDismiss: () -> Unit
) {
    val totalCalories = summary.calories
    val totalProtein = summary.proteinGrams
    val targetCaffeine = 400.0
    val targetSugar = 50.0
    val targetSodium = 2300.0

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DIAGNOSTIC RADAR REPORT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Fuel Diagnostic Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.06f), thickness = 1.dp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiagnosticItemRow("TOTAL CALORIC DENSITY", "${totalCalories.roundToInt()} kcal", if (totalCalories <= 2300.0) "OPTIMAL RANGE" else "SURPLUS DETECTED", totalCalories <= 2300.0)
                DiagnosticItemRow("PROTEIN COMPLIANCE", "${totalProtein.roundToInt()}g logged", if (totalProtein >= 140.0) "TARGET REACHED" else "BELOW OPTIMAL THRESHOLD", totalProtein >= 140.0)
                DiagnosticItemRow("SUGAR PROFILE LIMITS", "${summary.sugarGrams.roundToInt()}g logged", if (summary.sugarGrams <= targetSugar) "SAFE ZONE" else "EXCESS SUGAR ALERT", summary.sugarGrams <= targetSugar)
                DiagnosticItemRow("CAFFEINE DOCK LEVEL", "${summary.caffeineMg.roundToInt()}mg", if (summary.caffeineMg <= targetCaffeine) "STABLE STIMULATION" else "CAFFEINE SATURATION WARNING", summary.caffeineMg <= targetCaffeine)
                DiagnosticItemRow("SODIUM OVERLOAD SENSORS", "${summary.sodiumMg.roundToInt()}mg", if (summary.sodiumMg <= targetSodium) "OPTIMAL PRESSURE" else "EXCESS SODIUM LOAD DETECTED", summary.sodiumMg <= targetSodium)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("CLOSE DIAGNOSTICS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    )
}

@Composable
private fun DiagnosticItemRow(label: String, value: String, status: String, optimal: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.03f))
            .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = Color.Black.copy(alpha = 0.45f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                color = Color(0xFF0F172A)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = if (optimal) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        )
    }
}

@Composable
private fun MacroValueColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
            color = OnSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
            color = color
        )
    }
}

@Composable
private fun MicroDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.03f))
            .border(1.dp, Color.Black.copy(alpha = 0.07f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
            color = Color(0xFF0F172A)
        )
    }
}

private fun Double.roundToOne(): String = String.format(Locale.US, "%.1f", this)
