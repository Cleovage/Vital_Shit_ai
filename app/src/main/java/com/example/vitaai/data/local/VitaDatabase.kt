package com.example.vitaai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

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
        ScreenStateEventEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VitaDatabase : RoomDatabase() {
    abstract fun vitaDao(): VitaDao
}
