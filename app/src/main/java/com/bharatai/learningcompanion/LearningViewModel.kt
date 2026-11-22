package com.bharatai.learningcompanion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

data class LearningUiState(
    val isLoading: Boolean = false,
    val skillGaps: String = "",
    val studyPlan: String = "",
    val chatHistory: List<ChatMessage> = emptyList(),
    val errorMessage: String? = null
)

class LearningViewModel(private val aiEngine: AIEngine) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    fun analyzeGaps(goals: String, currentSkills: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val required = mapOf(goals to 0.8)
                val mastery = mapOf(currentSkills to 0.3)
                val gaps = LogicEngine.computeGaps(required, mastery)

                val gapsText = if (gaps.isEmpty()) {
                    "Great job! You have no skill gaps for your goal: $goals."
                } else {
                    "Based on your goal ($goals) and current skills ($currentSkills), here are your gaps:\n\n" +
                            gaps.joinToString("\n") { "• ${it.skillId}: needs ${it.gap} more mastery (currently at ${it.mastery})" }
                }
                _uiState.update { it.copy(skillGaps = gapsText, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    fun createPlan(gapsText: String, timeframe: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val pathway = listOf(
                    PathwayItem("Core Fundamentals", 0.5, "resource_1", 4.0),
                    PathwayItem("Advanced Application", 0.3, "resource_2", 6.0)
                )
                val weeks = if (timeframe.contains("4", ignoreCase = true)) 4 else 2
                val schedule = LogicEngine.buildSchedule(pathway, 5.0, weeks)

                val planText = "Your $timeframe Personalized Study Plan:\n\n" +
                        schedule.weeks.joinToString("\n\n") { week ->
                            "📅 Week ${week.week} (${week.totalHours} hours):\n" +
                                    week.items.joinToString("\n") { "  • ${it.skillId}: ${it.hours} hours" }
                        } +
                        if (schedule.overflow) "\n\n⚠️ Note: Some items are scheduled as 'stretch' goals beyond the deadline." else ""

                _uiState.update { it.copy(studyPlan = planText, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    fun sendChatMessage(message: String, context: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(chatHistory = it.chatHistory + ChatMessage(message, true)) }
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = aiEngine.getChatResponse(message, context)
                _uiState.update {
                    it.copy(
                        chatHistory = it.chatHistory + ChatMessage(response, false),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }
}