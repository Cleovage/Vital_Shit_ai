package com.example.vitaai.data

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class ChatMessage(val role: String, val content: String)
data class ChatRequest(val model: String, val messages: List<ChatMessage>, val temperature: Double = 0.7)
data class ChatChoice(val message: ChatMessage)
data class ChatResponse(val choices: List<ChatChoice>)

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatRequest
    ): ChatResponse
}
