package com.bharatai.learningcompanion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bharatai.learningcompanion.ai.FakeLlmEngine
import com.bharatai.learningcompanion.ai.GoalParser
import com.bharatai.learningcompanion.ai.ParseResult
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen() {
    // FakeLlmEngine is the stand-in until Batool's real model is plugged in.
    val parser = remember { GoalParser(FakeLlmEngine()) }
    val scope = rememberCoroutineScope()

    var goalText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Tell me your learning goal",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = goalText,
            onValueChange = { goalText = it },
            label = { Text("For example: I want to become a data analyst in 8 weeks, 6 hours a week") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    loading = true
                    resultText = ""
                    val result = parser.parse(goalText)
                    resultText = when (result) {
                        is ParseResult.Success -> "Profile found:\n${result.profile}"
                        is ParseResult.Failure -> "Could not understand that: ${result.error}"
                    }
                    loading = false
                }
            },
            enabled = goalText.isNotBlank() && !loading
        ) {
            Text("Continue")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (loading) {
            CircularProgressIndicator()
        }

        if (resultText.isNotEmpty()) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}