package com.example.vitaai.data

import com.example.vitaai.data.local.SleepSessionEntity
import com.example.vitaai.data.local.VitaDao
import kotlinx.coroutines.flow.Flow
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val vitaDao: VitaDao
) {

    fun observeRecentSleepSessions(limit: Int): Flow<List<SleepSessionEntity>> {
        return vitaDao.observeRecentSleepSessions(limit)
    }

    fun observeSleepSessions(startMs: Long, endMs: Long): Flow<List<SleepSessionEntity>> {
        return vitaDao.observeSleepSessions(startMs, endMs)
    }

    suspend fun insertSleepSession(session: SleepSessionEntity): Long {
        return vitaDao.insertSleepSession(session)
    }

    /**
     * Calculates rolling sleep debt over the last 14 days.
     * Sleep debt = (Target Sleep Hours - Actual Sleep Hours) accumulated.
     * Capped at positive values (since you can't easily store infinite negative debt, max payoff is limited).
     */
    suspend fun getSleepDebtHours(targetSleepHoursPerNight: Double = 8.0): Double {
        val endMs = System.currentTimeMillis()
        val startMs = endMs - TimeUnit.DAYS.toMillis(14)
        val sessions = vitaDao.getSleepSessions(startMs, endMs)
        
        if (sessions.isEmpty()) return 0.0

        // Group sessions by day (using local date start)
        val daySleepMap = mutableMapOf<Int, Double>()
        val calendar = Calendar.getInstance()
        
        for (session in sessions) {
            calendar.timeInMillis = session.startTimeMillis
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR) + (365 * calendar.get(Calendar.YEAR))
            val durationHours = session.durationMinutes.toDouble() / 60.0
            daySleepMap[dayOfYear] = (daySleepMap[dayOfYear] ?: 0.0) + durationHours
        }

        var accumulatedDebt = 0.0
        // We evaluate each of the last 14 days
        for (i in 0 until 14) {
            val dayTime = endMs - TimeUnit.DAYS.toMillis(i.toLong())
            calendar.timeInMillis = dayTime
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR) + (365 * calendar.get(Calendar.YEAR))
            
            val sleepForDay = daySleepMap[dayOfYear] ?: 0.0
            // If the user did not track, we assume a nominal 7 hours to avoid huge fake debt, or assume 0 sleep if we want true tracking
            val nominalTarget = targetSleepHoursPerNight
            val deficit = nominalTarget - sleepForDay
            accumulatedDebt += deficit
        }

        return accumulatedDebt.coerceIn(-15.0, 30.0) // Clamp between -15 hours (surplus) and 30 hours (high debt)
    }

    /**
     * Sleep Regularity Index (SRI) compares sleep/wake states in 5-minute epochs 24 hours apart.
     * SRI range is 0 to 100.
     * Simplified: We divide the day into 24 1-hour epochs.
     * For the last 7 days, we check if the user's state (asleep/awake) is consistent day-to-day.
     */
    suspend fun getSleepRegularityIndex(): Int {
        val endMs = System.currentTimeMillis()
        val startMs = endMs - TimeUnit.DAYS.toMillis(8) // 8 days to compare 7 pairs
        val sessions = vitaDao.getSleepSessions(startMs, endMs)

        if (sessions.size < 2) return 80 // Default nominal score

        // Map hours of each day to sleep (1) or wake (0)
        // 7 days, 24 hours
        val calendar = Calendar.getInstance()
        val dayStates = Array(8) { IntArray(24) { 0 } }
        val dayIndexMap = mutableMapOf<Int, Int>()

        // Initialize day boundaries
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        for (i in 0..7) {
            val dayCal = today.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val code = dayCal.get(Calendar.DAY_OF_YEAR) + (365 * dayCal.get(Calendar.YEAR))
            dayIndexMap[code] = 7 - i // Map chronologically: index 0 is oldest, 7 is today
        }

        // Fill states
        for (session in sessions) {
            val sessionStart = session.startTimeMillis
            val sessionEnd = session.endTimeMillis

            // Loop hour by hour
            var currentMs = sessionStart
            while (currentMs < sessionEnd) {
                calendar.timeInMillis = currentMs
                val dayCode = calendar.get(Calendar.DAY_OF_YEAR) + (365 * calendar.get(Calendar.YEAR))
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val dayIdx = dayIndexMap[dayCode]
                if (dayIdx != null && dayIdx in 0..7 && hour in 0..23) {
                    dayStates[dayIdx][hour] = 1 // Asleep
                }
                currentMs += TimeUnit.HOURS.toMillis(1)
            }
        }

        var matches = 0
        var totalComparisons = 0

        // Compare consecutive days
        for (d in 0 until 7) {
            for (h in 0 until 24) {
                if (dayStates[d][h] == dayStates[d + 1][h]) {
                    matches++
                }
                totalComparisons++
            }
        }

        return if (totalComparisons > 0) {
            ((matches.toDouble() / totalComparisons) * 100.0).toInt().coerceIn(0, 100)
        } else {
            80
        }
    }

    /**
     * Social Jetlag = Difference between midpoint of sleep on weekends vs weekdays.
     * SJL = |Midpoint_weekend - Midpoint_weekday| in hours.
     */
    suspend fun getSocialJetlagHours(): Double {
        val endMs = System.currentTimeMillis()
        val startMs = endMs - TimeUnit.DAYS.toMillis(14)
        val sessions = vitaDao.getSleepSessions(startMs, endMs)

        if (sessions.isEmpty()) return 0.0

        val weekdayMidpoints = mutableListOf<Double>()
        val weekendMidpoints = mutableListOf<Double>()
        val calendar = Calendar.getInstance()

        for (session in sessions) {
            calendar.timeInMillis = session.startTimeMillis
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            // Sleep midpoint as hours since midnight
            calendar.timeInMillis = session.startTimeMillis
            val startHour = calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.MINUTE) / 60.0)
            val durationHours = session.durationMinutes.toDouble() / 60.0
            val midpoint = (startHour + (durationHours / 2.0)) % 24.0

            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                weekendMidpoints.add(midpoint)
            } else {
                weekdayMidpoints.add(midpoint)
            }
        }

        if (weekdayMidpoints.isEmpty() || weekendMidpoints.isEmpty()) return 0.0

        val avgWeekdayMidpoint = weekdayMidpoints.average()
        val avgWeekendMidpoint = weekendMidpoints.average()

        var diff = Math.abs(avgWeekendMidpoint - avgWeekdayMidpoint)
        if (diff > 12.0) {
            diff = 24.0 - diff // Handle circular midnight wrap-around
        }

        return diff
    }

    /**
     * Circadian Disruption Score (CDS) accumulates light exposure logs during the biological night (10 PM to 6 AM).
     * CDS = sum(ln(1 + lux) * weight)
     */
    suspend fun getCircadianDisruptionScore(): Int {
        val endMs = System.currentTimeMillis()
        val startMs = endMs - TimeUnit.DAYS.toMillis(7)
        val lightLogs = vitaDao.getAmbientLightLogs(startMs, endMs)

        if (lightLogs.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        var totalDisruption = 0.0

        for (log in lightLogs) {
            calendar.timeInMillis = log.timestampMillis
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            // Biological night: 10 PM (22) to 6 AM (6)
            if (hour >= 22 || hour < 6) {
                val weight = if (hour >= 23 || hour < 4) 1.5 else 1.0 // Peak sensitivity in deep night
                totalDisruption += Math.log(1.0 + log.luxValue) * weight
            }
        }

        // Standardize CDS between 0 and 100 (where 0 is no light, and 100 represents high light exposure during night)
        // A single typical day: 8 hours * 60 minutes * 2 (average log density) = 960 logs.
        // If average lux is 50, disruption score = 960 * ln(51) = 3770.
        // We will scale it: score = disruption / 100, capped at 100.
        return (totalDisruption / 20.0).toInt().coerceIn(0, 100)
    }
}
