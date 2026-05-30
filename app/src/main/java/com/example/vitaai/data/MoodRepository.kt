package com.example.vitaai.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class MoodEntry(
    val score: Int, // 1-10
    val timestamp: Instant,
    val note: String = ""
)

@Singleton
class MoodRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _currentMood = MutableStateFlow<MoodEntry?>(null)
    val currentMood: StateFlow<MoodEntry?> = _currentMood

    private val prefs = context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)

    init {
        val score = prefs.getInt("last_mood_score", -1)
        if (score != -1) {
            val ts = prefs.getLong("last_mood_ts", 0)
            val note = prefs.getString("last_mood_note", "") ?: ""
            _currentMood.value = MoodEntry(score, Instant.ofEpochMilli(ts), note)
        }
    }

    fun recordMood(score: Int, note: String = "") {
        val now = Instant.now()
        val entry = MoodEntry(score, now, note)
        _currentMood.value = entry
        
        prefs.edit()
            .putInt("last_mood_score", score)
            .putLong("last_mood_ts", now.toEpochMilli())
            .putString("last_mood_note", note)
            .apply()
    }
}
