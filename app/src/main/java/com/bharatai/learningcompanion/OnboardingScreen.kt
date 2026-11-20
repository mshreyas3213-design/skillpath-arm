package com.bharatai.learningcompanion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    viewModel: LearningViewModel, // This is the "door" AppNav is looking for
    navigateToGaps: () -> Unit    // This replaces your old "onNext"
) {
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var currentSkills by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to AI Learning Companion",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text("What do you want to learn?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = currentSkills,
            onValueChange = { currentSkills = it },
            label = { Text("Your current skills") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.analyzeGaps(
                    goals = goal,
                    currentSkills = if (currentSkills.isBlank()) "Beginner" else currentSkills
                )
                navigateToGaps()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && goal.isNotBlank()
        ) {
            Text("Analyze & Continue")
        }
    }
}