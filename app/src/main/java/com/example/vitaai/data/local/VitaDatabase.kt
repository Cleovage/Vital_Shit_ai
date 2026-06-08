package com.example.vitaai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FoodEntryEntity::class,
        DrinkEntryEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        ExerciseSetEntity::class,
        RoutePointEntity::class,
        SleepSessionEntity::class,
        AmbientLightLogEntity::class,
        ScreenStateEventEntity::class,
        MeditationLogEntity::class,
        HydrationLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class VitaDatabase : RoomDatabase() {
    abstract fun vitaDao(): VitaDao
    abstract fun logDao(): LogDao

    companion object {
        /**
         * v2 → v3: add the meditation_log and hydration_log tables for
         * offline-durable log entries that sync to Firestore.
         */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `meditation_log` (" +
                        "`id` TEXT NOT NULL, " +
                        "`sessionId` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`category` TEXT NOT NULL, " +
                        "`plannedDurationSeconds` INTEGER NOT NULL, " +
                        "`startedAtMillis` INTEGER NOT NULL, " +
                        "`durationSeconds` INTEGER NOT NULL, " +
                        "`completed` INTEGER NOT NULL, " +
                        "`deviceId` TEXT NOT NULL, " +
                        "`syncedToCloud` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `hydration_log` (" +
                        "`id` TEXT NOT NULL, " +
                        "`timestampMillis` INTEGER NOT NULL, " +
                        "`ml` INTEGER NOT NULL, " +
                        "`hour` INTEGER NOT NULL, " +
                        "`source` TEXT NOT NULL, " +
                        "`deleted` INTEGER NOT NULL, " +
                        "`syncedToCloud` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
            }
        }
    }
}
