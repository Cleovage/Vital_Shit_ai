package com.example.vitaai.data

import android.content.Context
import android.provider.Settings
import com.example.vitaai.data.local.LogDao
import com.example.vitaai.data.local.MeditationLogEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

data class MeditationSession(
    val id: String,
    val title: String,
    val durationMinutes: Int,
    val category: String,
    val description: String,
    val accent: Long // ARGB color
)

/**
 * In-memory state for the meditation feature, with durable per-event
 * logging to Room. The Room entries are uploaded to
 * `users/{uid}/meditation_log` by [SyncCoordinator].
 */
@Singleton
class MeditationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logDao: LogDao
) {

    private val _sessions = MutableStateFlow(seedSessions())
    val sessions: StateFlow<List<MeditationSession>> = _sessions.asStateFlow()

    private val _currentlyPlaying = MutableStateFlow<MeditationSession?>(null)
    val currentlyPlaying: StateFlow<MeditationSession?> = _currentlyPlaying.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Wall-clock timestamp of the most recent play() call, so we can
    // compute durationSeconds when the user hits stop().
    @Volatile private var lastPlayStartedAt: Long = 0L

    private val deviceId: String by lazy {
        @Suppress("HardwareIds")
        runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull() ?: "unknown-device"
    }

    fun play(session: MeditationSession) {
        lastPlayStartedAt = System.currentTimeMillis()
        _currentlyPlaying.value = session
        _isPlaying.value = true
    }

    fun togglePlayback() {
        if (_currentlyPlaying.value != null) {
            _isPlaying.value = !_isPlaying.value
        }
    }

    /**
     * Stops the current session and persists a [MeditationLogEntity] so
     * the event survives process death and can be synced to Firestore
     * by [SyncCoordinator.pushPendingLogs].
     */
    fun stop() {
        val session = _currentlyPlaying.value
        val startedAt = lastPlayStartedAt
        if (session != null && startedAt > 0L) {
            val plannedSeconds = session.durationMinutes * 60L
            val elapsed = ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0L)
            // Don't log near-zero plays (likely accidental tap)
            if (elapsed >= 5L) {
                val completed = elapsed >= (plannedSeconds * 0.8).toLong()
                val entry = MeditationLogEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = session.id,
                    title = session.title,
                    category = session.category,
                    plannedDurationSeconds = plannedSeconds,
                    startedAtMillis = startedAt,
                    durationSeconds = elapsed,
                    completed = completed,
                    deviceId = deviceId,
                    syncedToCloud = false
                )
                runBlocking { logDao.insertMeditation(entry) }
            }
        }
        _isPlaying.value = false
        _currentlyPlaying.value = null
        lastPlayStartedAt = 0L
    }

    fun shuffle() {
        _sessions.value = _sessions.value.shuffled()
    }

    private fun seedSessions(): List<MeditationSession> = listOf(
        MeditationSession(
            id = "m1",
            title = "Morning Calm",
            durationMinutes = 10,
            category = "Calm",
            description = "A gentle 10-minute breathing practice to start your day with clarity and ease.",
            accent = 0xFF67E8F9
        ),
        MeditationSession(
            id = "m2",
            title = "Focus Flow",
            durationMinutes = 15,
            category = "Focus",
            description = "Sharpen your attention and steady your mind with this guided focus session.",
            accent = 0xFF06B6D4
        ),
        MeditationSession(
            id = "m3",
            title = "Body Scan",
            durationMinutes = 20,
            category = "Relaxation",
            description = "Release tension by scanning your body from head to toe with mindful awareness.",
            accent = 0xFF8B5CF6
        ),
        MeditationSession(
            id = "m4",
            title = "Sleep Wind-down",
            durationMinutes = 25,
            category = "Sleep",
            description = "Drift into restful sleep with this calming evening practice.",
            accent = 0xFF3B82F6
        ),
        MeditationSession(
            id = "m5",
            title = "Box Breathing",
            durationMinutes = 5,
            category = "Energy",
            description = "A quick 5-minute box-breathing reset for moments of stress or low energy.",
            accent = 0xFFF43F5E
        )
    )
}
