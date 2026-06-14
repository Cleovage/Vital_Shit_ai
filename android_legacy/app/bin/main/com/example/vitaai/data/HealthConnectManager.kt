package com.example.vitaai.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Volume
import androidx.health.connect.client.units.Length
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class NutritionTotals(
    val calories: Double = 0.0,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0
)

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            val status = HealthConnectClient.getSdkStatus(context)
            if (status == HealthConnectClient.SDK_AVAILABLE) {
                HealthConnectClient.getOrCreate(context)
            } else {
                null
            }
        } catch (t: Throwable) {
            android.util.Log.e("HealthConnectManager", "Failed to initialize HealthConnectClient", t)
            null
        }
    }

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    val permissions = requiredPermissions + setOf(
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
        HealthPermission.getWritePermission(HeightRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getWritePermission(NutritionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(requiredPermissions)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getGrantedPermissions(): Set<String> {
        val client = healthConnectClient ?: return emptySet()
        return try {
            client.permissionController.getGrantedPermissions()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun readDailySteps(startTime: Instant, endTime: Instant): Long {
        val client = healthConnectClient ?: return 0L
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun readHeartRate(startTime: Instant, endTime: Instant): List<Long> {
        val client = healthConnectClient ?: return emptyList()
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.flatMap { record ->
                record.samples.map { it.beatsPerMinute }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun readAvgHeartRate(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(HeartRateRecord.BPM_AVG),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun readRestingHeartRate(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val samples = response.records.map { it.beatsPerMinute.toDouble() }
            if (samples.isNotEmpty()) {
                samples.average()
            } else {
                estimateRestingHeartRate(startTime, endTime)
            }
        } catch (e: Exception) {
            estimateRestingHeartRate(startTime, endTime)
        }
    }

    private suspend fun estimateRestingHeartRate(startTime: Instant, endTime: Instant): Double {
        val samples = readHeartRate(startTime, endTime)
        if (samples.isEmpty()) return 0.0
        val sorted = samples.sorted()
        val bottomCount = maxOf(1, sorted.size / 4)
        return sorted.take(bottomCount).average()
    }

    suspend fun readSleepDuration(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val duration = response[SleepSessionRecord.SLEEP_DURATION_TOTAL]
            duration?.toMinutes()?.toDouble()?.div(60.0) ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun readSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionRecord> {
        val client = healthConnectClient ?: return emptyList()
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun readDailyCalories(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun readDailyBasalCalories(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL]?.inKilocalories ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun readLatestWeight(): Double? {
        val client = healthConnectClient ?: return null
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.before(Instant.now()),
                    ascendingOrder = false,
                    pageSize = 1
                )
            )
            response.records.firstOrNull()?.weight?.inKilograms
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readLatestHeight(): Double? {
        val client = healthConnectClient ?: return null
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    HeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.before(Instant.now()),
                    ascendingOrder = false,
                    pageSize = 1
                )
            )
            response.records.firstOrNull()?.height?.inMeters
        } catch (e: Exception) {
            null
        }
    }

    suspend fun writeWeight(weightKg: Double) {
        val client = healthConnectClient ?: return
        try {
            val now = Instant.now()
            val zoneOffset = ZoneId.systemDefault().rules.getOffset(now)
            val record = WeightRecord(
                time = now,
                zoneOffset = zoneOffset,
                weight = Mass.kilograms(weightKg),
                metadata = Metadata.manualEntry()
            )
            client.insertRecords(listOf(record))
        } catch (e: Exception) {
            // handle gracefully
        }
    }

    suspend fun writeHeight(heightMeters: Double) {
        val client = healthConnectClient ?: return
        try {
            val now = Instant.now()
            val zoneOffset = ZoneId.systemDefault().rules.getOffset(now)
            val record = HeightRecord(
                time = now,
                zoneOffset = zoneOffset,
                height = Length.meters(heightMeters),
                metadata = Metadata.manualEntry()
            )
            client.insertRecords(listOf(record))
        } catch (e: Exception) {
            // handle gracefully
        }
    }

    suspend fun readDailyHydration(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(HydrationRecord.VOLUME_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[HydrationRecord.VOLUME_TOTAL]?.inLiters ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun writeHydration(liters: Double) {
        val client = healthConnectClient ?: return
        try {
            val now = Instant.now()
            val zoneOffset = java.time.ZoneId.systemDefault().rules.getOffset(now)
            val record = HydrationRecord(
                startTime = now,
                startZoneOffset = zoneOffset,
                endTime = now,
                endZoneOffset = zoneOffset,
                volume = Volume.liters(liters),
                metadata = Metadata.manualEntry()
            )
            client.insertRecords(listOf(record))
        } catch (e: Exception) {
            // handle gracefully
        }
    }

    suspend fun writeNutrition(
        name: String,
        mealType: Int,
        calories: Double,
        proteinGrams: Double,
        carbsGrams: Double,
        fatGrams: Double,
        fiberGrams: Double,
        sugarGrams: Double,
        sodiumMg: Double,
        caffeineMg: Double,
        timestamp: Instant = Instant.now()
    ) {
        val client = healthConnectClient ?: return
        try {
            val zoneOffset = ZoneId.systemDefault().rules.getOffset(timestamp)
            val record = NutritionRecord(
                startTime = timestamp.minus(5, ChronoUnit.MINUTES),
                startZoneOffset = zoneOffset,
                endTime = timestamp,
                endZoneOffset = zoneOffset,
                metadata = Metadata.manualEntry(),
                name = name,
                mealType = mealType,
                energy = Energy.kilocalories(calories),
                protein = Mass.grams(proteinGrams),
                totalCarbohydrate = Mass.grams(carbsGrams),
                totalFat = Mass.grams(fatGrams),
                dietaryFiber = Mass.grams(fiberGrams),
                sugar = Mass.grams(sugarGrams),
                sodium = Mass.milligrams(sodiumMg),
                caffeine = Mass.milligrams(caffeineMg)
            )
            client.insertRecords(listOf(record))
        } catch (e: Exception) {
            // handle gracefully
        }
    }

    suspend fun writeWorkoutSession(
        title: String,
        exerciseType: Int,
        startTime: Instant,
        endTime: Instant,
        calories: Double,
        distanceMeters: Double
    ) {
        val client = healthConnectClient ?: return
        try {
            val zoneOffset = ZoneId.systemDefault().rules.getOffset(startTime)
            val records = mutableListOf<androidx.health.connect.client.records.Record>()
            records += ExerciseSessionRecord(
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = zoneOffset,
                endZoneOffset = ZoneId.systemDefault().rules.getOffset(endTime),
                exerciseType = exerciseType,
                title = title,
                metadata = Metadata.manualEntry()
            )
            if (calories > 0.0) {
                records += ActiveCaloriesBurnedRecord(
                    startTime = startTime,
                    endTime = endTime,
                    startZoneOffset = zoneOffset,
                    endZoneOffset = ZoneId.systemDefault().rules.getOffset(endTime),
                    energy = Energy.kilocalories(calories),
                    metadata = Metadata.manualEntry()
                )
            }
            if (distanceMeters > 0.0) {
                records += DistanceRecord(
                    startTime = startTime,
                    endTime = endTime,
                    startZoneOffset = zoneOffset,
                    endZoneOffset = ZoneId.systemDefault().rules.getOffset(endTime),
                    distance = androidx.health.connect.client.units.Length.meters(distanceMeters),
                    metadata = Metadata.manualEntry()
                )
            }
            client.insertRecords(records)
        } catch (e: Exception) {
            // handle gracefully
        }
    }

    suspend fun readExerciseSessions(startTime: Instant, endTime: Instant): List<ExerciseSessionRecord> {
        val client = healthConnectClient ?: return emptyList()
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun readDistance(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[DistanceRecord.DISTANCE_TOTAL]?.inMeters ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun readHourlySteps(startTime: Instant, endTime: Instant): Map<Instant, Long> {
        val client = healthConnectClient ?: return emptyMap()
        return try {
            val response = client.aggregateGroupByDuration(
                AggregateGroupByDurationRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Duration.ofHours(1)
                )
            )
            response.associate { bucket ->
                bucket.startTime to (bucket.result[StepsRecord.COUNT_TOTAL] ?: 0L)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun readHourlyHeartRate(startTime: Instant, endTime: Instant): Map<Instant, Double> {
        val client = healthConnectClient ?: return emptyMap()
        return try {
            val response = client.aggregateGroupByDuration(
                AggregateGroupByDurationRequest(
                    metrics = setOf(HeartRateRecord.BPM_AVG),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Duration.ofHours(1)
                )
            )
            response.associate { bucket ->
                bucket.startTime to (bucket.result[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun readDailyNutrition(startTime: Instant, endTime: Instant): NutritionTotals {
        val client = healthConnectClient ?: return NutritionTotals()
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        NutritionRecord.ENERGY_TOTAL,
                        NutritionRecord.PROTEIN_TOTAL,
                        NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL,
                        NutritionRecord.TOTAL_FAT_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            NutritionTotals(
                calories = response[NutritionRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0,
                proteinGrams = response[NutritionRecord.PROTEIN_TOTAL]?.inGrams ?: 0.0,
                carbsGrams = response[NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL]?.inGrams ?: 0.0,
                fatGrams = response[NutritionRecord.TOTAL_FAT_TOTAL]?.inGrams ?: 0.0
            )
        } catch (e: Exception) {
            NutritionTotals()
        }
    }

    suspend fun readDailyExerciseMinutes(startTime: Instant, endTime: Instant): Double {
        val client = healthConnectClient ?: return 0.0
        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toMinutes()?.toDouble() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}
