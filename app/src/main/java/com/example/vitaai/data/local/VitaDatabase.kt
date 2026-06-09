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
        HydrationLogEntity::class,
        ChatConversationEntity::class,
        ChatMessageEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class VitaDatabase : RoomDatabase() {
    abstract fun vitaDao(): VitaDao
    abstract fun logDao(): LogDao
    abstract fun chatDao(): ChatDao

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

        /**
         * v3 → v4: add the chats and chat_messages tables so the coach
         * chat can persist a history of past conversations and mirror
         * them to Firestore (`users/{uid}/chats/{chatId}/messages`).
         */
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chats` (" +
                        "`id` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "`messageCount` INTEGER NOT NULL, " +
                        "`syncedToCloud` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chat_messages` (" +
                        "`id` TEXT NOT NULL, " +
                        "`chatId` TEXT NOT NULL, " +
                        "`text` TEXT NOT NULL, " +
                        "`isUser` INTEGER NOT NULL, " +
                        "`timestampMillis` INTEGER NOT NULL, " +
                        "`syncedToCloud` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_chat_messages_chatId` " +
                        "ON `chat_messages`(`chatId`)"
                )
            }
        }
    }
}
