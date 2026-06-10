package com.example.vitaai.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.DrinkCatalogItem
import com.example.vitaai.data.FoodCatalogItem
import com.example.vitaai.data.NutritionRepository
import com.example.vitaai.data.NutritionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class NutritionUiState(
    val summary: NutritionSummary = NutritionSummary(),
    val foods: List<FoodCatalogItem> = emptyList(),
    val customFoods: List<FoodCatalogItem> = emptyList(),
    val drinks: List<DrinkCatalogItem> = emptyList(),
    val selectedMeal: String = "Breakfast",
    val error: String? = null,
    val targetCalories: Double = 2300.0,
    val targetProtein: Double = 140.0,
    val targetCarbs: Double = 275.0,
    val targetFat: Double = 75.0,
    val targetWaterMl: Double = 2500.0
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val prefs = context.getSharedPreferences("nutrition_targets", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        NutritionUiState(
            foods = nutritionRepository.foodCatalog,
            drinks = nutritionRepository.drinkCatalog
        )
    )
    val uiState: StateFlow<NutritionUiState> = _uiState

    init {
        // Load targets from SharedPreferences
        val targetCal = prefs.getFloat("target_calories", 2300f).toDouble()
        val targetProt = prefs.getFloat("target_protein", 140f).toDouble()
        val targetCarb = prefs.getFloat("target_carbs", 275f).toDouble()
        val targetFat = prefs.getFloat("target_fat", 75f).toDouble()
        val targetWater = prefs.getFloat("target_water_ml", 2500f).toDouble()

        _uiState.value = _uiState.value.copy(
            targetCalories = targetCal,
            targetProtein = targetProt,
            targetCarbs = targetCarb,
            targetFat = targetFat,
            targetWaterMl = targetWater
        )

        viewModelScope.launch {
            nutritionRepository.observeTodaySummary().collect { summary ->
                _uiState.value = _uiState.value.copy(summary = summary)
            }
        }
    }

    fun updateTargetMacros(calories: Double, proteinPercent: Double, carbsPercent: Double, fatPercent: Double) {
        val proteinGrams = (calories * (proteinPercent / 100.0)) / 4.0
        val carbsGrams = (calories * (carbsPercent / 100.0)) / 4.0
        val fatGrams = (calories * (fatPercent / 100.0)) / 9.0

        _uiState.value = _uiState.value.copy(
            targetCalories = calories,
            targetProtein = proteinGrams,
            targetCarbs = carbsGrams,
            targetFat = fatGrams
        )

        prefs.edit()
            .putFloat("target_calories", calories.toFloat())
            .putFloat("target_protein", proteinGrams.toFloat())
            .putFloat("target_carbs", carbsGrams.toFloat())
            .putFloat("target_fat", fatGrams.toFloat())
            .apply()
    }

    fun updateTargetWater(ml: Double) {
        _uiState.value = _uiState.value.copy(targetWaterMl = ml)
        prefs.edit().putFloat("target_water_ml", ml.toFloat()).apply()
    }

    fun selectMeal(meal: String) {
        _uiState.value = _uiState.value.copy(selectedMeal = meal)
    }

    fun setMeal(meal: String) {
        selectMeal(meal)
    }

    fun addFood(item: FoodCatalogItem, servings: Double = 1.0) {
        viewModelScope.launch {
            runCatching {
                nutritionRepository.addFood(item, _uiState.value.selectedMeal, servings)
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message ?: "Unable to log food")
            }
        }
    }

    fun createCustomFood(
        name: String,
        servingLabel: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        sugar: Double,
        sodium: Double
    ) {
        val newItem = FoodCatalogItem(
            name = name,
            servingLabel = servingLabel,
            calories = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            fiberGrams = 0.0,
            sugarGrams = sugar,
            sodiumMg = sodium,
            caffeineMg = 0.0
        )
        val updatedCustom = _uiState.value.customFoods + newItem
        val updatedAll = listOf(newItem) + _uiState.value.foods
        _uiState.value = _uiState.value.copy(
            customFoods = updatedCustom,
            foods = updatedAll
        )
    }

    fun quickAdd(calories: Double, protein: Double, carbs: Double, fat: Double) {
        viewModelScope.launch {
            runCatching {
                nutritionRepository.quickAddMacros("Quick Add", _uiState.value.selectedMeal, calories, protein, carbs, fat)
            }.onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "Unable to quick add") }
        }
    }

    fun addDrink(item: DrinkCatalogItem, volumeMl: Double = item.defaultMl) {
        viewModelScope.launch {
            runCatching { nutritionRepository.addDrink(item, volumeMl) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "Unable to add drink") }
        }
    }

    fun addDrinkDirect(name: String, volumeMl: Double) {
        viewModelScope.launch {
            val drinkItem = _uiState.value.drinks.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: DrinkCatalogItem("Water", "Water", 250.0, 1.0, 0.0, 0.0, 0.0, "Direct hydration with no calories.")
            runCatching { nutritionRepository.addDrink(drinkItem, volumeMl) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "Unable to log drink") }
        }
    }

    fun deleteFood(id: Long) {
        viewModelScope.launch {
            runCatching { nutritionRepository.deleteFood(id) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "Unable to delete food") }
        }
    }

    fun deleteDrink(id: Long) {
        viewModelScope.launch {
            runCatching { nutritionRepository.deleteDrink(id) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "Unable to delete drink") }
        }
    }
}
