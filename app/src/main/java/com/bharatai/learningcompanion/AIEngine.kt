package com.bharatai.learningcompanion

// This interface defines how the UI talks to the AI.
// Member 1 (ML Engineer) will create a class that implements this using ExecuTorch.
interface AIEngine {
    suspend fun analyzeSkillGaps(goals: String, currentSkills: String): String
    suspend fun generateStudyPlan(gaps: String, timeframe: String): String
    suspend fun getChatResponse(userMessage: String, context: String): String
}