package com.example.vitaai.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.vitaai.data.local.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val auth: FirebaseAuth?,
    private val firestore: FirebaseFirestore?
) {
    private val userId: String?
        get() = auth?.currentUser?.uid

    suspend fun saveHealthSnapshot(snapshot: HealthSnapshot) {
        val uid = userId ?: throw IllegalStateException("User must be logged in to save data")
        val db = firestore ?: throw IllegalStateException("Firestore is not available")
        db.collection("users")
            .document(uid)
            .collection("snapshots")
            .document(snapshot.timestamp.toString())
            .set(snapshot)
            .await()
    }

    suspend fun getLatestSnapshot(): HealthSnapshot? {
        val uid = userId ?: return null
        val db = firestore ?: return null
        return try {
            val snapshots = db.collection("users")
                .document(uid)
                .collection("snapshots")
            
            val query = snapshots.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
            
            val result = query.get().await()
            result.toObjects(HealthSnapshot::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProfileGoals(
        age: Int,
        gender: String,
        activityLevel: String,
        stepGoal: Long,
        hydrationGoalLiters: Double,
        exerciseMinutesGoal: Double,
        caloriesBurnGoal: Double
    ) {
        val uid = userId ?: return
        val db = firestore ?: return
        val profileData = mapOf(
            "age" to age,
            "gender" to gender,
            "activityLevel" to activityLevel,
            "stepGoal" to stepGoal,
            "hydrationGoalLiters" to hydrationGoalLiters,
            "exerciseMinutesGoal" to exerciseMinutesGoal,
            "caloriesBurnGoal" to caloriesBurnGoal
        )
        // Use SetOptions.merge() so we don't clobber displayName, email, createdAt, etc.
        db.collection("users")
            .document(uid)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    suspend fun getProfileData(): Map<String, Any>? {
        val uid = userId ?: return null
        val db = firestore ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.data
        } catch (e: Exception) {
            null
        }
    }

    suspend fun uploadLocalData(dao: VitaDao) {
        val uid = userId ?: return
        val db = firestore ?: return

        // 1. Sync Food Entries
        val foodEntries = dao.getFoodEntries(0, System.currentTimeMillis())
        foodEntries.forEach { entry ->
            db.collection("users").document(uid)
                .collection("food_entries")
                .document(entry.timestampMillis.toString())
                .set(entry)
                .await()
        }

        // 2. Sync Drink Entries
        val drinkEntries = dao.getDrinkEntries(0, System.currentTimeMillis())
        drinkEntries.forEach { entry ->
            db.collection("users").document(uid)
                .collection("drink_entries")
                .document(entry.timestampMillis.toString())
                .set(entry)
                .await()
        }

        // 3. Sync Sleep Sessions
        val sleepSessions = dao.getSleepSessions(0, System.currentTimeMillis())
        sleepSessions.forEach { session ->
            db.collection("users").document(uid)
                .collection("sleep_sessions")
                .document(session.startTimeMillis.toString())
                .set(session)
                .await()
        }

        // 4. Sync Workout Sessions
        val workoutSessions = dao.getWorkoutSessions(0, System.currentTimeMillis())
        workoutSessions.forEach { session ->
            db.collection("users").document(uid)
                .collection("workout_sessions")
                .document(session.startTimeMillis.toString())
                .set(session)
                .await()

            // Also upload associated sets
            val sets = dao.observeExerciseSets(session.id).first()
            sets.forEach { set ->
                db.collection("users").document(uid)
                    .collection("exercise_sets")
                    .document(set.timestampMillis.toString())
                    .set(set)
                    .await()
            }
        }

        // 5. Sync Ambient Light Logs
        val lightLogs = dao.getAmbientLightLogs(0, System.currentTimeMillis())
        lightLogs.forEach { log ->
            db.collection("users").document(uid)
                .collection("ambient_light_logs")
                .document(log.timestampMillis.toString())
                .set(log)
                .await()
        }
    }

    suspend fun downloadCloudData(dao: VitaDao) {
        val uid = userId ?: return
        val db = firestore ?: return

        try {
            // 1. Food Entries
            val foodSnapshot = db.collection("users").document(uid).collection("food_entries").get().await()
            foodSnapshot.documents.forEach { doc ->
                val name = doc.getString("name") ?: ""
                val meal = doc.getString("meal") ?: ""
                val servingLabel = doc.getString("servingLabel") ?: ""
                val servingMultiplier = doc.getDouble("servingMultiplier") ?: 1.0
                val calories = doc.getDouble("calories") ?: 0.0
                val proteinGrams = doc.getDouble("proteinGrams") ?: 0.0
                val carbsGrams = doc.getDouble("carbsGrams") ?: 0.0
                val fatGrams = doc.getDouble("fatGrams") ?: 0.0
                val fiberGrams = doc.getDouble("fiberGrams") ?: 0.0
                val sugarGrams = doc.getDouble("sugarGrams") ?: 0.0
                val sodiumMg = doc.getDouble("sodiumMg") ?: 0.0
                val caffeineMg = doc.getDouble("caffeineMg") ?: 0.0
                val timestampMillis = doc.getLong("timestampMillis") ?: 0L

                val existing = dao.getFoodEntries(timestampMillis, timestampMillis)
                if (existing.isEmpty()) {
                    dao.insertFoodEntry(
                        FoodEntryEntity(
                            name = name, meal = meal, servingLabel = servingLabel,
                            servingMultiplier = servingMultiplier, calories = calories,
                            proteinGrams = proteinGrams, carbsGrams = carbsGrams, fatGrams = fatGrams,
                            fiberGrams = fiberGrams, sugarGrams = sugarGrams, sodiumMg = sodiumMg,
                            caffeineMg = caffeineMg, timestampMillis = timestampMillis
                        )
                    )
                }
            }

            // 2. Drink Entries
            val drinkSnapshot = db.collection("users").document(uid).collection("drink_entries").get().await()
            drinkSnapshot.documents.forEach { doc ->
                val name = doc.getString("name") ?: ""
                val drinkType = doc.getString("drinkType") ?: ""
                val volumeMl = doc.getDouble("volumeMl") ?: 0.0
                val hydrationMl = doc.getDouble("hydrationMl") ?: 0.0
                val caffeineMg = doc.getDouble("caffeineMg") ?: 0.0
                val sugarGrams = doc.getDouble("sugarGrams") ?: 0.0
                val sodiumMg = doc.getDouble("sodiumMg") ?: 0.0
                val benefit = doc.getString("benefit") ?: ""
                val timestampMillis = doc.getLong("timestampMillis") ?: 0L

                val existing = dao.getDrinkEntries(timestampMillis, timestampMillis)
                if (existing.isEmpty()) {
                    dao.insertDrinkEntry(
                        DrinkEntryEntity(
                            name = name, drinkType = drinkType, volumeMl = volumeMl,
                            hydrationMl = hydrationMl, caffeineMg = caffeineMg,
                            sugarGrams = sugarGrams, sodiumMg = sodiumMg,
                            benefit = benefit, timestampMillis = timestampMillis
                        )
                    )
                }
            }

            // 3. Sleep Sessions
            val sleepSnapshot = db.collection("users").document(uid).collection("sleep_sessions").get().await()
            sleepSnapshot.documents.forEach { doc ->
                val startTimeMillis = doc.getLong("startTimeMillis") ?: 0L
                val endTimeMillis = doc.getLong("endTimeMillis") ?: 0L
                val durationMinutes = doc.getLong("durationMinutes") ?: 0L
                val sleepQualityScore = doc.getLong("sleepQualityScore")?.toInt() ?: 0
                val source = doc.getString("source") ?: ""
                val notes = doc.getString("notes")

                val existing = dao.getSleepSessions(startTimeMillis, startTimeMillis)
                if (existing.isEmpty()) {
                    dao.insertSleepSession(
                        SleepSessionEntity(
                            startTimeMillis = startTimeMillis, endTimeMillis = endTimeMillis,
                            durationMinutes = durationMinutes, sleepQualityScore = sleepQualityScore,
                            source = source, notes = notes
                        )
                    )
                }
            }

            // 4. Workout Sessions & Sets
            val workoutSnapshot = db.collection("users").document(uid).collection("workout_sessions").get().await()
            workoutSnapshot.documents.forEach { doc ->
                val templateId = doc.getString("templateId") ?: ""
                val title = doc.getString("title") ?: ""
                val category = doc.getString("category") ?: ""
                val startTimeMillis = doc.getLong("startTimeMillis") ?: 0L
                val endTimeMillis = doc.getLong("endTimeMillis") ?: 0L
                val durationSeconds = doc.getLong("durationSeconds") ?: 0L
                val totalSets = doc.getLong("totalSets")?.toInt() ?: 0
                val totalReps = doc.getLong("totalReps")?.toInt() ?: 0
                val calories = doc.getDouble("calories") ?: 0.0
                val avgHeartRate = doc.getDouble("avgHeartRate") ?: 0.0
                val distanceMeters = doc.getDouble("distanceMeters") ?: 0.0
                val notes = doc.getString("notes") ?: ""
                val completed = doc.getBoolean("completed") ?: true

                val existing = dao.getWorkoutSessions(startTimeMillis, startTimeMillis)
                if (existing.isEmpty()) {
                    val sessionId = dao.insertWorkoutSession(
                        WorkoutSessionEntity(
                            templateId = templateId, title = title, category = category,
                            startTimeMillis = startTimeMillis, endTimeMillis = endTimeMillis,
                            durationSeconds = durationSeconds, totalSets = totalSets,
                            totalReps = totalReps, calories = calories, avgHeartRate = avgHeartRate,
                            distanceMeters = distanceMeters, notes = notes, completed = completed
                        )
                    )

                    // Download associated sets from cloud and save locally
                    val setsSnapshot = db.collection("users").document(uid).collection("exercise_sets")
                        .whereEqualTo("sessionId", doc.getLong("id")).get().await() // Fallback query
                    
                    val allSetsSnapshot = db.collection("users").document(uid).collection("exercise_sets").get().await()
                    allSetsSnapshot.documents.forEach { setDoc ->
                        // Match sets where exercise is logged around the same workout duration
                        val setTimestamp = setDoc.getLong("timestampMillis") ?: 0L
                        if (setTimestamp in startTimeMillis..endTimeMillis) {
                            val exerciseName = setDoc.getString("exerciseName") ?: ""
                            val setNumber = setDoc.getLong("setNumber")?.toInt() ?: 1
                            val reps = setDoc.getLong("reps")?.toInt() ?: 0
                            val weightKg = setDoc.getDouble("weightKg") ?: 0.0
                            val durationSecs = setDoc.getLong("durationSeconds") ?: 0L
                            
                            dao.insertExerciseSet(
                                ExerciseSetEntity(
                                    sessionId = sessionId, exerciseName = exerciseName,
                                    setNumber = setNumber, reps = reps, weightKg = weightKg,
                                    durationSeconds = durationSecs, timestampMillis = setTimestamp
                                )
                            )
                        }
                    }
                }
            }

            // 5. Ambient Light Logs
            val lightSnapshot = db.collection("users").document(uid).collection("ambient_light_logs").get().await()
            lightSnapshot.documents.forEach { doc ->
                val timestampMillis = doc.getLong("timestampMillis") ?: 0L
                val luxValue = doc.getDouble("luxValue")?.toFloat() ?: 0f

                val existing = dao.getAmbientLightLogs(timestampMillis, timestampMillis)
                if (existing.isEmpty()) {
                    dao.insertAmbientLightLog(
                        AmbientLightLogEntity(
                            timestampMillis = timestampMillis, luxValue = luxValue
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Download cloud data error", e)
        }
    }
}

