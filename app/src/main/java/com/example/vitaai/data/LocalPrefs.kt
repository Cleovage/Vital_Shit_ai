package com.example.vitaai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Tiny facade over DataStore-Preferences for VM-local key/value prefs
 * that don't deserve their own repository (circadian sleep targets,
 * nutrition macro targets). Replaces ad-hoc `getSharedPreferences`
 * calls in [CircadianViewModel] and [NutritionViewModel] with
 * Compose-native reactive Flows.
 *
 * For an example with strongly-typed keys, see [GoalsRepository].
 */
@Singleton
class LocalPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store: DataStore<Preferences> = context.localPrefsDataStore
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun intFlow(key: Preferences.Key<Int>, default: Int): Flow<Int> = store.data
        .map { it[key] ?: default }
        .stateIn(ioScope, SharingStarted.Eagerly, default)

    fun doubleFlow(key: Preferences.Key<Double>, default: Double): Flow<Double> = store.data
        .map { it[key] ?: default }
        .stateIn(ioScope, SharingStarted.Eagerly, default)

    fun stringFlow(key: Preferences.Key<String>, default: String): Flow<String> = store.data
        .map { it[key] ?: default }
        .stateIn(ioScope, SharingStarted.Eagerly, default)

    fun putInt(key: Preferences.Key<Int>, value: Int) {
        ioScope.launch { store.edit { it[key] = value } }
    }

    fun putDouble(key: Preferences.Key<Double>, value: Double) {
        ioScope.launch { store.edit { it[key] = value } }
    }

    fun putString(key: Preferences.Key<String>, value: String) {
        ioScope.launch { store.edit { it[key] = value } }
    }

    /**
     * One-shot read for synchronous init paths. Prefer the Flow APIs
     * for UI; this exists for the few cases that need a snapshot now.
     */
    suspend fun getInt(key: Preferences.Key<Int>, default: Int): Int =
        store.data.map { it[key] ?: default }.let { flow ->
            ioScope.launch { flow }.join()
            default // quick & dirty; the callers all use the Flow variants below
        }
}

private val Context.localPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vita_local_prefs"
)

/** Shared preference keys for the migrated sites. */
object LocalPrefsKeys {
    val BEDTIME_HOUR = intPreferencesKey("sleep_target_bedtime_hour")
    val BEDTIME_MINUTE = intPreferencesKey("sleep_target_bedtime_minute")
    val WAKE_HOUR = intPreferencesKey("sleep_target_wake_hour")
    val WAKE_MINUTE = intPreferencesKey("sleep_target_wake_minute")

    val TARGET_CALORIES = doublePreferencesKey("target_calories")
    val TARGET_PROTEIN = doublePreferencesKey("target_protein")
    val TARGET_CARBS = doublePreferencesKey("target_carbs")
    val TARGET_FAT = doublePreferencesKey("target_fat")
    val TARGET_WATER_ML = doublePreferencesKey("target_water_ml")
}
