package com.bharatai.learningcompanion

import kotlinx.coroutines.delay

class MockAIEngine : AIEngine {
    override suspend fun analyzeSkillGaps(goals: String, currentSkills: String): String {
        delay(1000)
        return "Mock analysis for $goals based on $currentSkills."
    }
    override suspend fun generateStudyPlan(gaps: String, timeframe: String): String {
        delay(1000)
        return "Mock plan for $gaps over $timeframe."
    }
    override suspend fun getChatResponse(userMessage: String, context: String): String {
        delay(1000)
        return "Mock response to: $userMessage"
    }
}