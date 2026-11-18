package com.bharatai.learningcompanion.ai

interface LlmEngine {
    suspend fun generate(prompt: String, maxTokens: Int = 256, temperature: Float = 0.2f): String
}

/** Stand-in until Batool's ExecuTorch model is ready. Returns queued replies in order. */
class FakeLlmEngine(replies: List<String> = emptyList()) : LlmEngine {
    private val queue = ArrayDeque(replies)
    val promptsSeen = mutableListOf<String>()

    override suspend fun generate(prompt: String, maxTokens: Int, temperature: Float): String {
        promptsSeen.add(prompt)
        return queue.removeFirstOrNull()
            ?: """{"role_id":"data_analyst","hours_per_week":6,"weeks_available":8,"experience":"beginner","known_skills":["python_basics"]}"""
    }
}