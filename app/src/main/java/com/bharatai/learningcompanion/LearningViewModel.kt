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
                val result = aiEngine.analyzeSkillGaps(goals, currentSkills)
                _uiState.update { it.copy(skillGaps = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun createPlan(gaps: String, timeframe: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = aiEngine.generateStudyPlan(gaps, timeframe)
                _uiState.update { it.copy(studyPlan = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun sendChatMessage(message: String, context: String) {
        viewModelScope.launch {
            // Add user message immediately
            _uiState.update {
                it.copy(chatHistory = it.chatHistory + ChatMessage(message, true))
            }

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
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}