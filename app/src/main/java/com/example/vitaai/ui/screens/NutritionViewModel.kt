package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.DrinkCatalogItem
import com.example.vitaai.data.FoodCatalogItem
import com.example.vitaai.data.NutritionRepository
import com.example.vitaai.data.NutritionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NutritionUiState(
    val summary: NutritionSummary = NutritionSummary(),
    val foods: List<FoodCatalogItem> = emptyList(),
    val customFoods: List<FoodCatalogItem> = emptyList(),
    val drinks: List<DrinkCatalogItem> = emptyList(),
    val selectedMeal: String = "Breakfast",
    val error: String? = null
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        NutritionUiState(
            foods = nutritionRepository.foodCatalog,
            drinks = nutritionRepository.drinkCatalog
        )
    )
    val uiState: StateFlow<NutritionUiState> = _uiState

    init {
        viewModelScope.launch {
            nutritionRepository.observeTodaySummary().collect { summary ->
                _uiState.value = _uiState.value.copy(summary = summary)
            }
        }
    }

    fun setMeal(meal: String) {
        _uiState.value = _uiState.value.copy(selectedMeal = meal)
    }

    fun addFood(item: FoodCatalogItem, servings: Double = 1.0) {
        viewModelScope.launch {
            runCatching { nutritionRepository.addFood(item, _uiState.value.selectedMeal, servings) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "Unable to add food") }
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
}
