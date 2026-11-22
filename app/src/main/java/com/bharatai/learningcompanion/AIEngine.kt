package com.bharatai.learningcompanion

interface AIEngine {
    suspend fun analyzeSkillGaps(goals: String, currentSkills: String): String
    suspend fun generateStudyPlan(gaps: String, timeframe: String): String
    suspend fun getChatResponse(userMessage: String, context: String): String
}