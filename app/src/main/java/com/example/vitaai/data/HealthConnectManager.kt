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
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    val permissions = requiredPermissions + setOf(
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
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
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
    }

    suspend fun getGrantedPermissions(): Set<String> {
        return healthConnectClient.permissionController.getGrantedPermissions()
    }

    suspend fun readDailySteps(startTime: Instant, endTime: Instant): Long {
        return try {
            val response = healthConnectClient.aggregate(
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
        // Since we want to let HC do calculations, we can still read raw for specific lists,
        // but for snapshot we'll use an aggregate BPM_AVG if needed.
        // Keeping this for now as it's used for averaging in Repository, 
        // but adding an aggregate method too.
        return try {
            val response = healthConnectClient.readRecords(
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
        return try {
            val response = healthConnectClient.aggregate(
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

    suspend fun readSleepDuration(startTime: Instant, endTime: Instant): Double {
        return try {
            val response = healthConnectClient.aggregate(
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
        return try {
            val response = healthConnectClient.readRecords(
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
        return try {
            val response = healthConnectClient.aggregate(
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

    suspend fun readDailyHydration(startTime: Instant, endTime: Instant): Double {
        return try {
            val response = healthConnectClient.aggregate(
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
        healthConnectClient.insertRecords(listOf(record))
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
        healthConnectClient.insertRecords(listOf(record))
    }

    suspend fun writeWorkoutSession(
        title: String,
        exerciseType: Int,
        startTime: Instant,
        endTime: Instant,
        calories: Double,
        distanceMeters: Double
    ) {
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
        healthConnectClient.insertRecords(records)
    }

    suspend fun readExerciseSessions(startTime: Instant, endTime: Instant): List<ExerciseSessionRecord> {
        return try {
            val response = healthConnectClient.readRecords(
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
        return try {
            val response = healthConnectClient.aggregate(
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
        return try {
            val response = healthConnectClient.aggregateGroupByDuration(
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
        return try {
            val response = healthConnectClient.aggregateGroupByDuration(
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
        return try {
            val response = healthConnectClient.aggregate(
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
        return try {
            val response = healthConnectClient.aggregate(
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
