package com.bharatai.learningcompanion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GapsScreen(
    skillGaps: String,
    isLoading: Boolean,
    errorMessage: String?,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Your Skill Gaps", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("AI is analyzing your profile...")
        } else if (errorMessage != null) {
            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(text = skillGaps.ifBlank { "No gaps identified yet." }, modifier = Modifier.padding(16.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth(), enabled = !isLoading && skillGaps.isNotBlank()) {
            Text("Generate My Study Plan")
        }
    }
}