package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitaai.data.ChatRepository
import com.example.vitaai.data.VitaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.vitaai.data.HydrationRepository
import com.example.vitaai.data.NutritionRepository
import com.example.vitaai.data.WorkoutRepository
import com.example.vitaai.data.local.WorkoutTemplateEntity
import org.json.JSONObject
import com.example.vitaai.data.WorkoutSetDraft

data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionProposal: ActionProposal? = null,
    val visualization: Visualization? = null,
    var actionStatus: ActionStatus = ActionStatus.PENDING
)

enum class ActionStatus { PENDING, CONFIRMED, REFUSED }

sealed class ActionProposal {
    data class LogNutrition(
        val foodName: String,
        val meal: String,
        val calories: Double,
        val protein: Double,
        val carbs: Double,
        val fat: Double
    ) : ActionProposal()

    data class LogWorkout(
        val workoutName: String,
        val category: String,
        val durationMinutes: Int,
        val caloriesBurned: Double,
        val sets: List<WorkoutSetDraft> = emptyList()
    ) : ActionProposal()

    data class LogHydration(val volumeMl: Double) : ActionProposal()
}

sealed class Visualization {
    data class ProgressRing(val label: String, val current: Double, val goal: Double, val unit: String) : Visualization()
    data class BarChart(val label: String, val data: List<Float>, val days: List<String>) : Visualization()
    data class MacroPie(val protein: Double, val carbs: Double, val fat: Double) : Visualization()
}

/**
 * Drives the coach chat. Each call to [sendMessage] either starts a new
 * conversation (if [startNewConversation] hasn't been called) or appends
 * to the active one. Every persisted row has `syncedToCloud = 0` until
 * the next [SyncCoordinator.fullSync] tick.
 *
 * UI can swap to a saved conversation via [openConversation]; that
 * replaces the active message stream and updates the header.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: VitaRepository,
    private val chatRepository: ChatRepository,
    private val nutritionRepository: NutritionRepository,
    private val workoutRepository: WorkoutRepository,
    private val hydrationRepository: HydrationRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    init {
        // Seed with the welcome bubble only if there is no active chat.
        _messages.value = listOf(
            Message("Hello! I'm **VitaAI**, your elite health coach. How can I help you optimize your wellness today?", false)
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // First user message in this session -> create a new
            // conversation row in Room and persist the message.
            val chatId = _activeChatId.value ?: run {
                val newId = chatRepository.startNewConversation(text)
                _activeChatId.value = newId
                newId
            }

            _messages.value = _messages.value + Message(text, true)
            chatRepository.appendMessage(chatId, text, isUser = true)

            _isThinking.value = true
            try {
                val aiResponse = runCatching {
                    val snapshot = repository.getDailySnapshot()
                    val history = _messages.value.map { com.example.vitaai.data.ChatMessage(if (it.isUser) "user" else "assistant", it.text) }
                    repository.getChatbotResponse(history, snapshot)
                }.getOrElse { e ->
                    "I'm sorry, I encountered an issue retrieving your health snapshot or connecting to the AI: ${e.localizedMessage ?: "Unknown error"}"
                }

                val parsedMessage = parseAiResponse(aiResponse)
                _messages.value = _messages.value + parsedMessage
                chatRepository.appendMessage(chatId, parsedMessage.text, isUser = false)
            } finally {
                _isThinking.value = false
            }
        }
    }

    private fun parseAiResponse(rawText: String): Message {
        var cleanText = rawText
        var action: ActionProposal? = null
        var viz: Visualization? = null

        // Extraction logic for <action> and <viz> tags
        try {
            val actionMatch = Regex("<action>(.*?)</action>", RegexOption.DOT_MATCHES_ALL).find(rawText)
            if (actionMatch != null) {
                val jsonStr = actionMatch.groupValues[1]
                val json = JSONObject(jsonStr)
                action = when (json.optString("type")) {
                    "nutrition" -> ActionProposal.LogNutrition(
                        foodName = json.optString("name", "Food"),
                        meal = json.optString("meal", "snack"),
                        calories = json.optDouble("calories", 0.0),
                        protein = json.optDouble("protein", 0.0),
                        carbs = json.optDouble("carbs", 0.0),
                        fat = json.optDouble("fat", 0.0)
                    )
                    "hydration" -> ActionProposal.LogHydration(
                        volumeMl = json.optDouble("volume_ml", 250.0)
                    )
                    "workout" -> ActionProposal.LogWorkout(
                        workoutName = json.optString("name", "Workout"),
                        category = json.optString("category", "Strength"),
                        durationMinutes = json.optInt("duration_min", 30),
                        caloriesBurned = json.optDouble("calories", 0.0)
                    )
                    else -> null
                }
                cleanText = cleanText.replace(actionMatch.value, "").trim()
            }

            val vizMatch = Regex("<viz>(.*?)</viz>", RegexOption.DOT_MATCHES_ALL).find(rawText)
            if (vizMatch != null) {
                val jsonStr = vizMatch.groupValues[1]
                val json = JSONObject(jsonStr)
                viz = when (json.optString("type")) {
                    "progress" -> Visualization.ProgressRing(
                        label = json.optString("label", ""),
                        current = json.optDouble("current", 0.0),
                        goal = json.optDouble("goal", 100.0),
                        unit = json.optString("unit", "")
                    )
                    "macros" -> Visualization.MacroPie(
                        protein = json.optDouble("protein", 30.0),
                        carbs = json.optDouble("carbs", 40.0),
                        fat = json.optDouble("fat", 30.0)
                    )
                    else -> null
                }
                cleanText = cleanText.replace(vizMatch.value, "").trim()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatViewModel", "Parsing failed", e)
        }

        return Message(text = cleanText, isUser = false, actionProposal = action, visualization = viz)
    }

    fun handleAction(message: Message, confirm: Boolean) {
        if (message.actionStatus != ActionStatus.PENDING) return
        
        viewModelScope.launch {
            if (confirm && message.actionProposal != null) {
                message.actionStatus = ActionStatus.CONFIRMED
                when (val p = message.actionProposal) {
                    is ActionProposal.LogNutrition -> {
                        nutritionRepository.quickAddMacros(p.foodName, p.meal, p.calories, p.protein, p.carbs, p.fat)
                    }
                    is ActionProposal.LogHydration -> {
                        // Use HydrationRepository to handle streak and local logs properly
                        hydrationRepository.addWater(p.volumeMl.toInt())
                        // Also push to Health Connect via VitaRepository
                        repository.logWater(p.volumeMl / 1000.0)
                    }
                    is ActionProposal.LogWorkout -> {
                        val template = workoutRepository.getTemplate("hiit") ?: WorkoutTemplateEntity("manual", p.workoutName, p.category, 0, "cardio", false, 0, "", "time")
                        workoutRepository.saveWorkoutSession(
                            template = template,
                            startTime = java.time.Instant.now().minusSeconds(p.durationMinutes.toLong() * 60),
                            endTime = java.time.Instant.now(),
                            totalReps = 0,
                            sets = emptyList(),
                            route = emptyList(),
                            avgHeartRate = 0.0,
                            calories = p.caloriesBurned,
                            notes = "Logged via VitaAI Coach"
                        )
                    }
                    null -> {}
                }
                _messages.value = _messages.value.toList() // Trigger state update
            } else {
                message.actionStatus = ActionStatus.REFUSED
                _messages.value = _messages.value.toList()
            }
        }
    }

    /**
     * Switch the active conversation to a saved one. The messages list
     * is replaced with the loaded scrollback so the UI updates
     * immediately. Used by [ChatHistoryScreen] when a row is tapped.
     */
    fun openConversation(chatId: String) {
        viewModelScope.launch {
            val rows = chatRepository.loadConversation(chatId)
            _activeChatId.value = chatId
            _messages.value = rows.map { Message(it.text, it.isUser, it.timestampMillis) }
        }
    }

    /** Reset to the welcome-only state for a fresh "New chat" action. */
    fun startNewConversation() {
        _activeChatId.value = null
        _messages.value = listOf(
            Message("Hello! I'm VitaAI, your health coach. How can I help you today?", false)
        )
    }

    fun deleteConversation(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteConversation(chatId)
            if (_activeChatId.value == chatId) {
                startNewConversation()
            }
        }
    }
}
