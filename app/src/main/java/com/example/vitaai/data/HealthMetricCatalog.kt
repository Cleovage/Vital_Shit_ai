package com.example.vitaai.data

enum class HealthDataSource(val label: String) {
    SAMSUNG_HEALTH("Samsung Health"),
    GOOGLE_HEALTH_FITBIT("Google Health / Fitbit"),
    VITA_AI("VitaAI")
}

enum class HealthMetricType(
    val route: String,
    val title: String,
    val shortLabel: String,
    val unit: String,
    val description: String,
    val sources: Set<HealthDataSource>
) {
    ACTIVE_CALORIES(
        route = "active_calories",
        title = "Active Calories",
        shortLabel = "Calories",
        unit = "kcal",
        description = "Calories burned through movement and exercise.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    BASAL_CALORIES(
        route = "basal_calories",
        title = "Idle Calories",
        shortLabel = "Idle",
        unit = "kcal",
        description = "Calories burned while resting (BMR).",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    STEPS(
        route = "steps",
        title = "Steps",
        shortLabel = "Steps",
        unit = "steps",
        description = "Total steps counted across devices and synced sources.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    HEART_RATE(
        route = "heart_rate",
        title = "Heart Rate",
        shortLabel = "Heart",
        unit = "bpm",
        description = "Average heart rate and daily variability trend.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    SLEEP(
        route = "sleep",
        title = "Sleep",
        shortLabel = "Sleep",
        unit = "hours",
        description = "Sleep duration and consistency over time.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    DISTANCE(
        route = "distance",
        title = "Distance",
        shortLabel = "Distance",
        unit = "km",
        description = "Daily walking and workout distance.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    HYDRATION(
        route = "hydration",
        title = "Hydration",
        shortLabel = "Water",
        unit = "L",
        description = "Water intake and hydration consistency.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    EXERCISE_MINUTES(
        route = "exercise_minutes",
        title = "Exercise Minutes",
        shortLabel = "Exercise",
        unit = "min",
        description = "Minutes spent in recorded exercise sessions.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    CALORIES_INTAKE(
        route = "calories_intake",
        title = "Calories Intake",
        shortLabel = "Intake",
        unit = "kcal",
        description = "Calories consumed from nutrition logs.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    PROTEIN(
        route = "protein",
        title = "Protein",
        shortLabel = "Protein",
        unit = "g",
        description = "Daily protein intake from food records.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    CARBOHYDRATE(
        route = "carbohydrate",
        title = "Carbohydrates",
        shortLabel = "Carbs",
        unit = "g",
        description = "Daily total carbohydrate intake.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    FAT(
        route = "fat",
        title = "Fat",
        shortLabel = "Fat",
        unit = "g",
        description = "Daily total fat intake.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT, HealthDataSource.VITA_AI)
    ),
    RESTING_HEART_RATE(
        route = "resting_heart_rate",
        title = "Resting Heart Rate",
        shortLabel = "RHR",
        unit = "bpm",
        description = "Resting heart rate trend when available from source apps.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT)
    ),
    HEART_RATE_VARIABILITY(
        route = "hrv",
        title = "Heart Rate Variability",
        shortLabel = "HRV",
        unit = "ms",
        description = "Heart rate variability trend for recovery insights.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT)
    ),
    BLOOD_OXYGEN(
        route = "blood_oxygen",
        title = "Blood Oxygen",
        shortLabel = "SpO2",
        unit = "%",
        description = "Oxygen saturation, often captured during sleep.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT)
    ),
    RESPIRATORY_RATE(
        route = "respiratory_rate",
        title = "Respiratory Rate",
        shortLabel = "Breathing",
        unit = "rpm",
        description = "Breathing rate trend from supported wearables.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT)
    ),
    SKIN_TEMPERATURE(
        route = "skin_temperature",
        title = "Skin Temperature",
        shortLabel = "Skin Temp",
        unit = "delta",
        description = "Skin temperature variation trend.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT)
    ),
    BODY_WEIGHT(
        route = "body_weight",
        title = "Body Weight",
        shortLabel = "Weight",
        unit = "kg",
        description = "Logged body weight trend.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT)
    ),
    BODY_FAT(
        route = "body_fat",
        title = "Body Fat",
        shortLabel = "Body Fat",
        unit = "%",
        description = "Body fat percentage trend from smart scales.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT)
    ),
    BLOOD_GLUCOSE(
        route = "blood_glucose",
        title = "Blood Glucose",
        shortLabel = "Glucose",
        unit = "mg/dL",
        description = "Blood glucose values from connected medical devices.",
        sources = setOf(HealthDataSource.SAMSUNG_HEALTH, HealthDataSource.GOOGLE_HEALTH_FITBIT)
    );

    companion object {
        fun fromRoute(route: String?): HealthMetricType? = entries.firstOrNull { it.route == route }
    }
}

val PrimaryDashboardMetrics = listOf(
    HealthMetricType.ACTIVE_CALORIES,
    HealthMetricType.STEPS,
    HealthMetricType.HEART_RATE,
    HealthMetricType.SLEEP,
    HealthMetricType.DISTANCE,
    HealthMetricType.HYDRATION,
    HealthMetricType.EXERCISE_MINUTES,
    HealthMetricType.CALORIES_INTAKE,
    HealthMetricType.PROTEIN,
    HealthMetricType.CARBOHYDRATE,
    HealthMetricType.FAT
)
