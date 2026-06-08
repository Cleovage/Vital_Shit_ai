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

    fun updateWakeSchedule(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(targetWakeHour = hour, targetWakeMinute = minute)
        val prefs = context.getSharedPreferences("vita_circadian_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("sleep_target_wake_hour", hour).putInt("sleep_target_wake_minute", minute).apply()
    }
}
