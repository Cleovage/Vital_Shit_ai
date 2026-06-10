package com.example.vitaai.data.model

/**
 * Server-side profile document shape for `users/{uid}`.
 *
 * Lives in `data/model/` so the same class can be used by [com.example.vitaai.data.ProfileRepository]
 * (cloud) and by [com.example.vitaai.data.GoalsRepository] (local mirror) without circular deps.
 */
data class UserProfile(
    val displayName: String = "",
    val email: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val age: Int = 25,
    val gender: String = "male",
    val activityLevel: String = "light",
    val stepGoal: Long = 10000,
    val hydrationGoalLiters: Double = 2.5,
    val exerciseMinutesGoal: Double = 30.0,
    val caloriesBurnGoal: Double = 500.0
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                displayName = (map["displayName"] as? String).orEmpty(),
                email = map["email"] as? String,
                createdAt = (map["createdAt"] as? Number)?.toLong(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong(),
                age = (map["age"] as? Number)?.toInt() ?: 25,
                gender = (map["gender"] as? String) ?: "male",
                activityLevel = (map["activityLevel"] as? String) ?: "light",
                stepGoal = (map["stepGoal"] as? Number)?.toLong() ?: 10000L,
                hydrationGoalLiters = (map["hydrationGoalLiters"] as? Number)?.toDouble() ?: 2.5,
                exerciseMinutesGoal = (map["exerciseMinutesGoal"] as? Number)?.toDouble() ?: 30.0,
                caloriesBurnGoal = (map["caloriesBurnGoal"] as? Number)?.toDouble() ?: 500.0
            )
        }
    }
}
