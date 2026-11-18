package com.bharatal.learningcompanion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bharatai.learningcompanion.GapsScreen

sealed class Screen(val route: String, val label: String) {
    object Chat : Screen("chat", "Chat")
    object Profile : Screen("profile", "Profile")
    object Quiz : Screen("quiz", "Quiz")
    object Gaps : Screen("gaps", "Gaps")
    object Plan : Screen("plan", "Plan")
    object CheckIn : Screen("checkin", "Check-in")
}

val bottomNavItems = listOf(
    Screen.Chat, Screen.Profile, Screen.Quiz,
    Screen.Gaps, Screen.Plan, Screen.CheckIn
)

@Composable
fun AppNav() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { /* Add icons later if time permits */ },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Gaps.route, // Starts on Gaps screen
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Gaps.route) { GapsScreen() }
            composable(Screen.Plan.route) { PlanScreen() }
            composable(Screen.Quiz.route) { QuizScreen() } // NEW: Real Quiz Screen

            // Clean, intentional stubs for the rest
            composable(Screen.Chat.route) {
                StubScreen("Onboarding Chat", "The AI chat assistant will be available in Phase 2.")
            }
            composable(Screen.Profile.route) {
                StubScreen("Profile", "User settings and progress tracking coming soon.")
            }
            composable(Screen.CheckIn.route) {
                StubScreen("Weekly Check-in", "Track your hours and update your learning pathway here.")
            }
        }
    }
}

@Composable
fun QuizScreen() {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }

    val questions = listOf(
        QuizQuestion(
            "What does SQL stand for?",
            listOf("Structured Query Language", "Simple Query Logic", "Standard Query Language"),
            0
        ),
        QuizQuestion(
            "Which Python library is used for data manipulation?",
            listOf("NumPy", "Pandas", "Matplotlib"),
            1
        ),
        QuizQuestion(
            "What is the primary purpose of Hypothesis Testing?",
            listOf("To make inferences about a population", "To visualize data", "To clean data"),
            0
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showResult) {
            Text(text = "Quiz Complete!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Your Score: $score / ${questions.size}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                currentQuestionIndex = 0
                score = 0
                showResult = false
            }) {
                Text("Retake Quiz")
            }
        } else {
            val q = questions[currentQuestionIndex]
            Text(text = "Question ${currentQuestionIndex + 1} of ${questions.size}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = q.question, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            q.options.forEachIndexed { index, option ->
                Button(
                    onClick = {
                        if (index == q.correctAnswer) {
                            score++
                        }
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                        } else {
                            showResult = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(text = option)
                }
            }
        }
    }
}

@Composable
fun StubScreen(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)