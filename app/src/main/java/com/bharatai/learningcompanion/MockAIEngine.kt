package com.bharatai.learningcompanion

import kotlinx.coroutines.delay

class MockAIEngine : AIEngine {
    override suspend fun analyzeSkillGaps(goals: String, currentSkills: String): String {
        delay(1500) // Simulate AI thinking time
        return "Based on your goal to learn '$goals' and your current skills in '$currentSkills', " +
                "you need to focus on: 1. Advanced Kotlin Coroutines, 2. Jetpack Compose State Management, " +
                "3. Basic Machine Learning concepts."
    }

    override suspend fun generateStudyPlan(gaps: String, timeframe: String): String {
        delay(1500)
        return "Here is your $timeframe study plan:\n" +
                "- Week 1: Focus on $gaps.\n" +
                "- Week 2: Build a small project.\n" +
                "- Week 3: Review and optimize."
    }

    override suspend fun getChatResponse(userMessage: String, context: String): String {
        delay(1000)
        return "I understand you're asking about '$userMessage'. Based on your learning context ($context), " +
                "I recommend checking out the official Android documentation for this topic."
    }
}