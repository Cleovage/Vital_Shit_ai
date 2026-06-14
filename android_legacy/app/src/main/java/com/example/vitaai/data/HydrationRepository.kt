package com.example.vitaai.data

import com.example.vitaai.data.local.HydrationLogEntity
import com.example.vitaai.data.local.LogDao
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

@Singleton
class HydrationRepository @Inject constructor(
    private val logDao: LogDao
) {

    data class DayEntry(val date: LocalDate, val totalMl: Int)

    data class HydrationState(
        val todayMl: Int = 0,
        val goalMl: Int = 2500,
        val weeklyData: List<DayEntry> = emptyList(),
        val hourlyMask: List<Boolean> = List(24) { false },
        val streak: Int = 0,
        val log: List<LogEntry> = emptyList()
    )

    data class LogEntry(
        val id: String = UUID.randomUUID().toString(),
        val hour: Int,
        val ml: Int,
        val timestamp: Long = System.currentTimeMillis(),
        val source: String = "manual"
    )

    private val _state = MutableStateFlow(seed())
    val state: StateFlow<HydrationState> = _state.asStateFlow()

    fun addWater(ml: Int, hour: Int = currentHour()) {
        val s = _state.value
        val entry = LogEntry(hour = hour, ml = ml, source = "quickAdd")
        val newLog = s.log + entry
        val newHourly = s.hourlyMask.toMutableList().also { it[hour] = true }
        val newToday = s.todayMl + ml
        val newStreak = computeStreak(newToday, s.goalMl, s.weeklyData)
        val newWeek = s.weeklyData.toMutableList().also {
            val idx = it.indexOfFirst { e -> e.date == LocalDate.now() }
            if (idx >= 0) it[idx] = DayEntry(LocalDate.now(), newToday)
            else it.add(DayEntry(LocalDate.now(), newToday))
        }
        // Persist to Room so the event survives process death and syncs later.
        runBlocking {
            logDao.insertHydration(
                HydrationLogEntity(
                    id = entry.id,
                    timestampMillis = entry.timestamp,
                    ml = entry.ml,
                    hour = entry.hour,
                    source = entry.source,
                    deleted = false,
                    syncedToCloud = false
                )
            )
        }
        _state.value = s.copy(
            todayMl = newToday,
            hourlyMask = newHourly,
            streak = newStreak,
            weeklyData = newWeek,
            log = newLog
        )
    }

    fun removeLastEntry() {
        val s = _state.value
        val last = s.log.lastOrNull() ?: return
        val newLog = s.log.dropLast(1)
        val newToday = (s.todayMl - last.ml).coerceAtLeast(0)
        val newHourly = s.hourlyMask.toMutableList().also {
            // recompute hour mask from log
            for (i in it.indices) it[i] = newLog.any { e -> e.hour == i }
        }
        val newStreak = computeStreak(newToday, s.goalMl, s.weeklyData)
        // Mark the row as deleted (not hard-deleted) so the sync layer
        // can replicate the deletion in Firestore.
        runBlocking { logDao.markHydrationDeleted(last.id) }
        _state.value = s.copy(
            todayMl = newToday,
            hourlyMask = newHourly,
            streak = newStreak,
            log = newLog
        )
    }

    private fun computeStreak(todayMl: Int, goalMl: Int, week: List<DayEntry>): Int {
        val dayMet = todayMl >= goalMl
        if (!dayMet && week.isEmpty()) return 0
        return if (dayMet) 1 else 0
    }

    private fun currentHour(): Int = java.time.LocalTime.now().hour

    private fun seed(): HydrationState {
        val today = LocalDate.now()
        val week = (0..6).reversed().map { offset ->
            val d = today.minusDays(offset.toLong())
            DayEntry(d, 1200 + (offset * 173) % 1500)
        }
        val nowHour = currentHour()
        val mask = (0..23).map { it <= nowHour && it % 3 == 0 }
        val seedLog = listOf(
            LogEntry(id = "seed-1", hour = 8, ml = 250),
            LogEntry(id = "seed-2", hour = 11, ml = 500),
            LogEntry(id = "seed-3", hour = 14, ml = 750)
        )
        return HydrationState(
            todayMl = week.last().totalMl,
            goalMl = 2500,
            weeklyData = week,
            hourlyMask = mask,
            streak = 3,
            log = seedLog
        )
    }
}
