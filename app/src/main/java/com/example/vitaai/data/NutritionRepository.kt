package com.example.vitaai.data

import androidx.health.connect.client.records.MealType
import com.example.vitaai.data.local.DrinkEntryEntity
import com.example.vitaai.data.local.FoodEntryEntity
import com.example.vitaai.data.local.VitaDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class FoodCatalogItem(
    val name: String,
    val servingLabel: String,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val sugarGrams: Double,
    val sodiumMg: Double,
    val caffeineMg: Double = 0.0
)

data class DrinkCatalogItem(
    val name: String,
    val type: String,
    val defaultMl: Double,
    val hydrationFactor: Double,
    val caffeineMgPer100Ml: Double,
    val sugarGramsPer100Ml: Double,
    val sodiumMgPer100Ml: Double,
    val benefit: String
)

data class NutritionSummary(
    val foods: List<FoodEntryEntity> = emptyList(),
    val drinks: List<DrinkEntryEntity> = emptyList(),
    val calories: Double = 0.0,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val fiberGrams: Double = 0.0,
    val sugarGrams: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val caffeineMg: Double = 0.0,
    val fluidMl: Double = 0.0,
    val hydrationMl: Double = 0.0
)

@Singleton
class NutritionRepository @Inject constructor(
    private val dao: VitaDao,
    private val healthConnectManager: HealthConnectManager
) {
    val foodCatalog = listOf(
        FoodCatalogItem("Eggs", "2 large", 156.0, 12.6, 1.2, 10.6, 0.0, 1.1, 124.0),
        FoodCatalogItem("Chicken Breast", "100 g", 165.0, 31.0, 0.0, 3.6, 0.0, 0.0, 74.0),
        FoodCatalogItem("Paneer", "100 g", 265.0, 18.3, 1.2, 20.8, 0.0, 1.2, 22.0),
        FoodCatalogItem("Greek Yogurt", "170 g", 100.0, 17.0, 6.0, 0.7, 0.0, 6.0, 61.0),
        FoodCatalogItem("Dal", "1 bowl", 198.0, 12.0, 32.0, 3.0, 8.0, 2.0, 360.0),
        FoodCatalogItem("Rice", "1 cup cooked", 206.0, 4.3, 45.0, 0.4, 0.6, 0.1, 2.0),
        FoodCatalogItem("Banana", "1 medium", 105.0, 1.3, 27.0, 0.4, 3.1, 14.4, 1.0),
        FoodCatalogItem("Oats", "50 g", 190.0, 6.5, 33.0, 3.5, 5.0, 0.5, 2.0),
        FoodCatalogItem("Peanut Butter", "2 tbsp", 188.0, 7.0, 6.0, 16.0, 2.0, 3.0, 152.0),
        FoodCatalogItem("Whey Protein", "1 scoop", 120.0, 24.0, 3.0, 1.5, 0.0, 1.0, 160.0)
    )

    val drinkCatalog = listOf(
        DrinkCatalogItem("Water", "Water", 250.0, 1.0, 0.0, 0.0, 0.0, "Direct hydration with no calories."),
        DrinkCatalogItem("Coffee", "Coffee", 200.0, 0.75, 40.0, 0.0, 2.0, "Alertness support; hydrate alongside caffeine."),
        DrinkCatalogItem("Tea", "Tea", 200.0, 0.85, 20.0, 0.0, 3.0, "Light caffeine and warm fluid intake."),
        DrinkCatalogItem("ORS", "ORS", 250.0, 1.05, 0.0, 6.0, 260.0, "Electrolytes help replenish sodium after sweat loss."),
        DrinkCatalogItem("Milk", "Milk", 250.0, 0.9, 0.0, 12.0, 105.0, "Hydration plus protein and minerals."),
        DrinkCatalogItem("Juice", "Juice", 250.0, 0.8, 0.0, 24.0, 10.0, "Quick carbohydrate intake; sugar is counted."),
        DrinkCatalogItem("Soda", "Soda", 330.0, 0.65, 8.0, 35.0, 35.0, "Fluid with high sugar; useful to see in totals."),
        DrinkCatalogItem("Energy Drink", "Energy", 250.0, 0.65, 32.0, 27.0, 120.0, "Caffeine boost with sugar and sodium tracked.")
    )

    fun observeTodaySummary(): Flow<NutritionSummary> = flow {
        while (true) {
            val bounds = todayBounds()
            val start = Instant.ofEpochMilli(bounds.first)
            val end = Instant.ofEpochMilli(bounds.second)
            
            val hcTotals = healthConnectManager.readDailyNutrition(start, end)
            val hcHydration = healthConnectManager.readDailyHydration(start, end)
            
            // Still get local entries for detailed list
            val foods = dao.getFoodEntries(bounds.first, bounds.second)
            val drinks = dao.getDrinkEntries(bounds.first, bounds.second)
            
            emit(NutritionSummary(
                foods = foods,
                drinks = drinks,
                calories = maxOf(hcTotals.calories, foods.sumOf { it.calories }),
                proteinGrams = maxOf(hcTotals.proteinGrams, foods.sumOf { it.proteinGrams }),
                carbsGrams = maxOf(hcTotals.carbsGrams, foods.sumOf { it.carbsGrams }),
                fatGrams = maxOf(hcTotals.fatGrams, foods.sumOf { it.fatGrams }),
                fiberGrams = foods.sumOf { it.fiberGrams },
                sugarGrams = foods.sumOf { it.sugarGrams } + drinks.sumOf { it.sugarGrams },
                sodiumMg = foods.sumOf { it.sodiumMg } + drinks.sumOf { it.sodiumMg },
                caffeineMg = foods.sumOf { it.caffeineMg } + drinks.sumOf { it.caffeineMg },
                fluidMl = drinks.sumOf { it.volumeMl },
                hydrationMl = maxOf(hcHydration * 1000.0, drinks.sumOf { it.hydrationMl })
            ))
            
            delay(10000) // Refresh every 10 seconds
        }
    }

    suspend fun getSummaryForRange(startMillis: Long, endMillis: Long): NutritionSummary {
        val start = Instant.ofEpochMilli(startMillis)
        val end = Instant.ofEpochMilli(endMillis)
        val hcTotals = healthConnectManager.readDailyNutrition(start, end)
        val hcHydration = healthConnectManager.readDailyHydration(start, end)
        
        val foods = dao.getFoodEntries(startMillis, endMillis)
        val drinks = dao.getDrinkEntries(startMillis, endMillis)
        
        return NutritionSummary(
            foods = foods,
            drinks = drinks,
            calories = maxOf(hcTotals.calories, foods.sumOf { it.calories }),
            proteinGrams = maxOf(hcTotals.proteinGrams, foods.sumOf { it.proteinGrams }),
            carbsGrams = maxOf(hcTotals.carbsGrams, foods.sumOf { it.carbsGrams }),
            fatGrams = maxOf(hcTotals.fatGrams, foods.sumOf { it.fatGrams }),
            fiberGrams = foods.sumOf { it.fiberGrams },
            sugarGrams = foods.sumOf { it.sugarGrams } + drinks.sumOf { it.sugarGrams },
            sodiumMg = foods.sumOf { it.sodiumMg } + drinks.sumOf { it.sodiumMg },
            caffeineMg = foods.sumOf { it.caffeineMg } + drinks.sumOf { it.caffeineMg },
            fluidMl = drinks.sumOf { it.volumeMl },
            hydrationMl = maxOf(hcHydration * 1000.0, drinks.sumOf { it.hydrationMl })
        )
    }

    suspend fun addFood(item: FoodCatalogItem, meal: String, servings: Double) {
        val now = Instant.now()
        val entry = FoodEntryEntity(
            name = item.name,
            meal = meal,
            servingLabel = item.servingLabel,
            servingMultiplier = servings,
            calories = item.calories * servings,
            proteinGrams = item.proteinGrams * servings,
            carbsGrams = item.carbsGrams * servings,
            fatGrams = item.fatGrams * servings,
            fiberGrams = item.fiberGrams * servings,
            sugarGrams = item.sugarGrams * servings,
            sodiumMg = item.sodiumMg * servings,
            caffeineMg = item.caffeineMg * servings,
            timestampMillis = now.toEpochMilli()
        )
        dao.insertFoodEntry(entry)
        runCatching {
            healthConnectManager.writeNutrition(
                name = entry.name,
                mealType = mealToHealthConnectType(meal),
                calories = entry.calories,
                proteinGrams = entry.proteinGrams,
                carbsGrams = entry.carbsGrams,
                fatGrams = entry.fatGrams,
                fiberGrams = entry.fiberGrams,
                sugarGrams = entry.sugarGrams,
                sodiumMg = entry.sodiumMg,
                caffeineMg = entry.caffeineMg,
                timestamp = now
            )
        }
    }

    suspend fun quickAddMacros(name: String, meal: String, calories: Double, protein: Double, carbs: Double, fat: Double) {
        addFood(
            item = FoodCatalogItem(name, "quick add", calories, protein, carbs, fat, 0.0, 0.0, 0.0),
            meal = meal,
            servings = 1.0
        )
    }

    suspend fun addDrink(item: DrinkCatalogItem, volumeMl: Double) {
        val multiplier = volumeMl / 100.0
        val entry = DrinkEntryEntity(
            name = item.name,
            drinkType = item.type,
            volumeMl = volumeMl,
            hydrationMl = volumeMl * item.hydrationFactor,
            caffeineMg = item.caffeineMgPer100Ml * multiplier,
            sugarGrams = item.sugarGramsPer100Ml * multiplier,
            sodiumMg = item.sodiumMgPer100Ml * multiplier,
            benefit = item.benefit,
            timestampMillis = Instant.now().toEpochMilli()
        )
        dao.insertDrinkEntry(entry)
        runCatching { healthConnectManager.writeHydration(entry.hydrationMl / 1000.0) }
    }

    private fun buildSummary(foods: List<FoodEntryEntity>, drinks: List<DrinkEntryEntity>): NutritionSummary {
        return NutritionSummary(
            foods = foods,
            drinks = drinks,
            calories = foods.sumOf { it.calories },
            proteinGrams = foods.sumOf { it.proteinGrams },
            carbsGrams = foods.sumOf { it.carbsGrams },
            fatGrams = foods.sumOf { it.fatGrams },
            fiberGrams = foods.sumOf { it.fiberGrams },
            sugarGrams = foods.sumOf { it.sugarGrams } + drinks.sumOf { it.sugarGrams },
            sodiumMg = foods.sumOf { it.sodiumMg } + drinks.sumOf { it.sodiumMg },
            caffeineMg = foods.sumOf { it.caffeineMg } + drinks.sumOf { it.caffeineMg },
            fluidMl = drinks.sumOf { it.volumeMl },
            hydrationMl = drinks.sumOf { it.hydrationMl }
        )
    }

    private fun mealToHealthConnectType(meal: String): Int {
        return when (meal.lowercase()) {
            "breakfast" -> MealType.MEAL_TYPE_BREAKFAST
            "lunch" -> MealType.MEAL_TYPE_LUNCH
            "dinner" -> MealType.MEAL_TYPE_DINNER
            "snack" -> MealType.MEAL_TYPE_SNACK
            else -> MealType.MEAL_TYPE_UNKNOWN
        }
    }

    private fun todayBounds(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = Instant.now().atZone(zone).toLocalDate().atStartOfDay(zone).toInstant()
        return start.toEpochMilli() to Instant.now().toEpochMilli()
    }
}
