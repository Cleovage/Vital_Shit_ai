package com.example.vitaai.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.Metadata
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
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.count }
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun readHeartRate(startTime: Instant, endTime: Instant): List<Long> {
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
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.energy.inKilocalories }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun readDailyHydration(startTime: Instant, endTime: Instant): Double {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HydrationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.volume.inLiters }
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
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.sumOf { it.distance.inMeters }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun readHourlySteps(startTime: Instant, endTime: Instant): Map<Instant, Long> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records
                .groupBy { it.startTime.truncatedTo(ChronoUnit.HOURS) }
                .mapValues { (_, records) -> records.sumOf { it.count } }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun readHourlyHeartRate(startTime: Instant, endTime: Instant): Map<Instant, Double> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records
                .flatMap { record -> record.samples }
                .groupBy { sample -> sample.time.truncatedTo(ChronoUnit.HOURS) }
                .mapValues { (_, samples) -> samples.map { it.beatsPerMinute.toDouble() }.average() }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun readDailyNutrition(startTime: Instant, endTime: Instant): NutritionTotals {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.fold(NutritionTotals()) { acc, record ->
                NutritionTotals(
                    calories = acc.calories + (record.energy?.inKilocalories ?: 0.0),
                    proteinGrams = acc.proteinGrams + (record.protein?.inGrams ?: 0.0),
                    carbsGrams = acc.carbsGrams + (record.totalCarbohydrate?.inGrams ?: 0.0),
                    fatGrams = acc.fatGrams + (record.totalFat?.inGrams ?: 0.0)
                )
            }
        } catch (e: Exception) {
            NutritionTotals()
        }
    }

    suspend fun readDailyExerciseMinutes(startTime: Instant, endTime: Instant): Double {
        val sessions = readExerciseSessions(startTime, endTime)
        return sessions.sumOf { session ->
            Duration.between(session.startTime, session.endTime).toMinutes().toDouble()
        }
    }
}
