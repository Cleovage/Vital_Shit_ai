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
}
