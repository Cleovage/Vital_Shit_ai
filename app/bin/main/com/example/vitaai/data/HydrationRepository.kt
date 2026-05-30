package com.example.vitaai.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HydrationRepository @Inject constructor(
    private val nutritionRepository: NutritionRepository
) {
    val drinkCatalog: List<DrinkCatalogItem> = nutritionRepository.drinkCatalog

    suspend fun logDrink(item: DrinkCatalogItem, volumeMl: Double) {
        nutritionRepository.addDrink(item, volumeMl)
    }
}
