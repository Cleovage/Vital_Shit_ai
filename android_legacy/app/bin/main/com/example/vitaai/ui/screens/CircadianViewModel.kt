package com.example.vitaai.ui.screens

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.SleepRepository
import com.example.vitaai.data.local.AmbientLightLogEntity
import com.example.vitaai.data.local.SleepSessionEntity
import com.example.vitaai.data.local.VitaDao
import com.example.vitaai.notifications.BedtimeAlarmScheduler
import com.example.vitaai.tracking.SleepTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CircadianUiState(
    val isTrackingSleep: Boolean = false,
    val sleepRegularityIndex: Int = 85,
    val sleepDebtHours: Double = 0.0,
    val socialJetlagHours: Double = 0.0,
    val circadianDisruptionScore: Int = 0,
    val targetBedtimeHour: Int = 22,
    val targetBedtimeMinute: Int = 30,
    val targetWakeHour: Int = 6,
    val targetWakeMinute: Int = 30,
    val recentSleepSessions: List<SleepSessionEntity> = emptyList(),
    val todayLightLogs: List<AmbientLightLogEntity> = emptyList()
)

@HiltViewModel
class CircadianViewModel @Inject constructor(
    private val sleepRepository: SleepRepository,
    private val vitaDao: VitaDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircadianUiState())
    val uiState: StateFlow<CircadianUiState> = _uiState.asStateFlow()

    init {
        // Load targets from SharedPreferences
        val prefs = context.getSharedPreferences("vita_circadian_prefs", Context.MODE_PRIVATE)
        val bedtimeHour = prefs.getInt("sleep_target_bedtime_hour", 22)
        val bedtimeMinute = prefs.getInt("sleep_target_bedtime_minute", 30)
        val wakeHour = prefs.getInt("sleep_target_wake_hour", 6)
        val wakeMinute = prefs.getInt("sleep_target_wake_minute", 30)
        _uiState.value = _uiState.value.copy(
            targetBedtimeHour = bedtimeHour,
            targetBedtimeMinute = bedtimeMinute,
            targetWakeHour = wakeHour,
            targetWakeMinute = wakeMinute
        )

        // Schedule initial alarms
        val scheduler = BedtimeAlarmScheduler(context)
        scheduler.scheduleBedtimeAlarm(bedtimeHour, bedtimeMinute)
        scheduler.scheduleWakeUpAlarm(wakeHour, wakeMinute)

        // Observe service running state
        viewModelScope.launch {
            SleepTrackingService.isServiceRunning.collect { isRunning ->
                _uiState.value = _uiState.value.copy(isTrackingSleep = isRunning)
            }
        }

        // Observe sleep sessions (last 10)
        viewModelScope.launch {
            sleepRepository.observeRecentSleepSessions(10).collect { sessions ->
                _uiState.value = _uiState.value.copy(recentSleepSessions = sessions)
                // Re-calculate stats when sessions change
                refreshMetrics()
            }
        }

        // Observe today's light logs reactively
        viewModelScope.launch {
            val startOfDay = getStartOfDayMillis()
            val tomorrowStart = startOfDay + 24 * 3600 * 1000L
            vitaDao.observeAmbientLightLogs(startOfDay, tomorrowStart).collect { logs ->
                _uiState.value = _uiState.value.copy(todayLightLogs = logs)
            }
        }
    }

    private fun getStartOfDayMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun refreshMetrics() {
        viewModelScope.launch {
            val sri = sleepRepository.getSleepRegularityIndex()
            val debt = sleepRepository.getSleepDebtHours()
            val jetlag = sleepRepository.getSocialJetlagHours()
            val cds = sleepRepository.getCircadianDisruptionScore()
            
            _uiState.value = _uiState.value.copy(
                sleepRegularityIndex = sri,
                sleepDebtHours = debt,
                socialJetlagHours = jetlag,
                circadianDisruptionScore = cds
            )
        }
    }

    fun toggleSleepTracking(context: Context) {
        val intent = Intent(context, SleepTrackingService::class.java)
        if (_uiState.value.isTrackingSleep) {
            context.stopService(intent)
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun updateBedtimeSchedule(context: Context, hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(targetBedtimeHour = hour, targetBedtimeMinute = minute)
        val prefs = context.getSharedPreferences("vita_circadian_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("sleep_target_bedtime_hour", hour).putInt("sleep_target_bedtime_minute", minute).apply()
        val scheduler = BedtimeAlarmScheduler(context)
        scheduler.scheduleBedtimeAlarm(hour, minute)
    }

    fun updateWakeSchedule(hour: Int, minute: Int, context: Context = this.context) {
        _uiState.value = _uiState.value.copy(targetWakeHour = hour, targetWakeMinute = minute)
        val prefs = context.getSharedPreferences("vita_circadian_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("sleep_target_wake_hour", hour).putInt("sleep_target_wake_minute", minute).apply()
        val scheduler = BedtimeAlarmScheduler(context)
        scheduler.scheduleWakeUpAlarm(hour, minute)
    }

    fun getTimeUntilBedtime(): Pair<Int, Int> {
        val now = java.util.Calendar.getInstance()
        val bedtime = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, _uiState.value.targetBedtimeHour)
            set(java.util.Calendar.MINUTE, _uiState.value.targetBedtimeMinute)
            set(java.util.Calendar.SECOND, 0)
            if (this.timeInMillis <= now.timeInMillis) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        val diffMillis = bedtime.timeInMillis - now.timeInMillis
        val hours = (diffMillis / (1000 * 60 * 60)).toInt()
        val minutes = ((diffMillis / (1000 * 60)) % 60).toInt()
        return Pair(hours, minutes)
    }

    fun getTimeUntilWakeUp(): Pair<Int, Int> {
        val now = java.util.Calendar.getInstance()
        val wakeup = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, _uiState.value.targetWakeHour)
            set(java.util.Calendar.MINUTE, _uiState.value.targetWakeMinute)
            set(java.util.Calendar.SECOND, 0)
            if (this.timeInMillis <= now.timeInMillis) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        val diffMillis = wakeup.timeInMillis - now.timeInMillis
        val hours = (diffMillis / (1000 * 60 * 60)).toInt()
        val minutes = ((diffMillis / (1000 * 60)) % 60).toInt()
        return Pair(hours, minutes)
    }

    fun getBedtimeContextMessage(): String {
        val (hoursUntil, minutesUntil) = getTimeUntilBedtime()
        val state = _uiState.value
        
        return when {
            state.isTrackingSleep -> "Sleep tracking active — Rest well"
            hoursUntil == 0 && minutesUntil <= 30 -> "⏰ Bedtime in ${minutesUntil}m — Wind down now"
            hoursUntil == 0 -> "🌙 Bedtime in ~1h — Reduce screen light"
            hoursUntil <= 2 -> "🌙 ${hoursUntil}h ${minutesUntil}m until bedtime"
            state.sleepDebtHours > 2.0 -> "💤 You have sleep debt — ${state.sleepDebtHours.toInt()}h to catch up"
            state.socialJetlagHours > 1.5 -> "🔄 High irregular sleep pattern detected"
            else -> "😴 Target bedtime at ${String.format("%02d:%02d", state.targetBedtimeHour, state.targetBedtimeMinute)}"
        }
    }

    fun getWakeUpContextMessage(): String {
        val (hoursUntil, minutesUntil) = getTimeUntilWakeUp()
        val state = _uiState.value
        
        return when {
            state.isTrackingSleep -> "Tracking sleep until wake-up"
            hoursUntil == 0 && minutesUntil <= 30 -> "⏰ Wake-up in ${minutesUntil}m"
            hoursUntil == 0 -> "☀️ Morning in ~1h"
            state.sleepRegularityIndex < 70 -> "⚠️ Irregular wake pattern — Target: ${String.format("%02d:%02d", state.targetWakeHour, state.targetWakeMinute)}"
            else -> "☀️ Target wake-up at ${String.format("%02d:%02d", state.targetWakeHour, state.targetWakeMinute)}"
        }
    }

    fun getSleepTrackingStatusMessage(): String {
        val state = _uiState.value
        return if (state.isTrackingSleep) {
            "Active — tracking in progress"
        } else {
            val (hours, minutes) = getTimeUntilBedtime()
            when {
                hours == 0 && minutes <= 30 -> "⚡ Start now to track tonight"
                hours <= 2 -> "⏰ Recommended to start in ${hours}h"
                else -> "Tap to start tonight's session"
            }
        }
    }
}
