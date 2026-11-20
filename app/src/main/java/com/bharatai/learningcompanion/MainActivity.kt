package com.bharatai.learningcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bharatai.learningcompanion.ui.theme.AILearningCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mockEngine = MockAIEngine()

        setContent {
            AILearningCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: LearningViewModel = viewModel(
                        factory = LearningViewModelFactory(mockEngine)
                    )

                    AppNav(viewModel = viewModel)
                }
            }
        }
    }
}

class LearningViewModelFactory(private val aiEngine: AIEngine) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearningViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LearningViewModel(aiEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}