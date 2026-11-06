package com.bharatai.learningcompanion

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LearningCompanionApp()
                }
            }
        }
    }
}

private data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

private val quizQuestions = listOf(
    QuizQuestion(
        "What is 12 × 8?",
        listOf("86", "96", "108", "88"),
        1,
        "12 × 8 = 96."
    ),
    QuizQuestion(
        "Which unit measures electrical resistance?",
        listOf("Volt", "Ampere", "Ohm", "Watt"),
        2,
        "Electrical resistance is measured in ohms."
    ),
    QuizQuestion(
        "What is the derivative of x² with respect to x?",
        listOf("x", "2x", "x³", "2"),
        1,
        "Using the power rule, the derivative is 2x."
    ),
    QuizQuestion(
        "Which component stores energy in a magnetic field?",
        listOf("Resistor", "Capacitor", "Inductor", "Diode"),
        2,
        "An inductor stores energy in its magnetic field."
    ),
    QuizQuestion(
        "What is decimal 5 in binary?",
        listOf("100", "101", "110", "111"),
        1,
        "Binary 101 represents decimal 5."
    )
)

@Composable
private fun LearningCompanionApp() {
    val context = LocalContext.current

    var exam by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("2") }
    var targetDate by remember { mutableStateOf("") }
    var goalsLoaded by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }

    var quizSubmitted by remember { mutableStateOf(false) }
    var quizScore by remember { mutableStateOf<Int?>(null) }

    val selectedAnswers = remember {
        mutableStateMapOf<Int, Int>()
    }

    val tasks = listOf(
        "Study a new concept",
        "Solve practice questions",
        "Revise previous topics",
        "Review mistakes"
    )

    val completedTasks = remember {
        mutableStateMapOf<Int, Boolean>()
    }

    // Load saved goals.
    LaunchedEffect(Unit) {
        val preferences = context.goalDataStore.data.first()

        exam = preferences[GoalKeys.EXAM] ?: ""
        subject = preferences[GoalKeys.SUBJECT] ?: ""
        hours = preferences[GoalKeys.HOURS] ?: "2"
        targetDate = preferences[GoalKeys.TARGET_DATE] ?: ""

        goalsLoaded = true
    }

    // Save goal changes to DataStore.
    LaunchedEffect(
        exam,
        subject,
        hours,
        targetDate,
        goalsLoaded
    ) {
        if (goalsLoaded) {
            context.goalDataStore.edit { preferences ->
                preferences[GoalKeys.EXAM] = exam
                preferences[GoalKeys.SUBJECT] = subject
                preferences[GoalKeys.HOURS] = hours
                preferences[GoalKeys.TARGET_DATE] = targetDate
            }
        }
    }

    val percentage = (quizScore ?: 0) * 100 / quizQuestions.size

    val feedback = when {
        quizScore == null ->
            "Take the quiz to receive personalized feedback."

        percentage == 100 ->
            "Excellent! Every answer is correct. Try a harder quiz."

        percentage >= 80 ->
            "Great work! Review the concepts you missed."

        percentage >= 60 ->
            "Good progress. Review the explanations and practise again."

        percentage >= 40 ->
            "Keep going. Revise the fundamentals before retrying."

        else ->
            "Start with the basics, review the explanations, and try again."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "AI Learning Companion",
            style = MaterialTheme.typography.headlineMedium
        )

        // GOALS
        Text(
            text = "Set Your Learning Goals",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = exam,
            onValueChange = { exam = it },
            label = { Text("Exam or learning goal") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = hours,
            onValueChange = {
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    hours = it
                }
            },
            label = { Text("Daily study hours") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        targetDate = "%04d-%02d-%02d".format(
                            year,
                            month + 1,
                            day
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (targetDate.isBlank()) {
                    "Choose target date"
                } else {
                    "Target date: $targetDate"
                }
            )
        }

        Button(
            onClick = {
                saveMessage = if (
                    exam.isNotBlank() &&
                    subject.isNotBlank() &&
                    (hours.toIntOrNull() ?: 0) > 0 &&
                    targetDate.isNotBlank()
                ) {
                    "Your goals have been saved."
                } else {
                    "Enter your goal, subject, valid hours and target date."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save My Goals")
        }

        if (saveMessage.isNotBlank()) {
            Text(text = saveMessage)
        }

        HorizontalDivider()

        // TODAY'S STUDY PLAN
        Text(
            text = "Today's Study Plan",
            style = MaterialTheme.typography.titleLarge
        )

        Text("Goal: ${exam.ifBlank { "Not set" }}")
        Text("Subject: ${subject.ifBlank { "Not set" }}")
        Text("Available study time: ${hours.ifBlank { "0" }} hours")

        val completedCount = tasks.indices.count {
            completedTasks[it] == true
        }

        val progress = completedCount * 100 / tasks.size

        Text("Daily Progress: $progress%")

        tasks.forEachIndexed { index, task ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = completedTasks[index] == true,
                    onCheckedChange = {
                        completedTasks[index] = it
                    }
                )

                Text(text = task)
            }
        }

        HorizontalDivider()

        // PERSONALIZED STUDY PLAN
        Text(
            text = "Personalized Study Plan",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = when {
                quizScore == null ->
                    "Complete the practice quiz below to get a recommendation."

                percentage >= 80 ->
                    "Focus on advanced practice and timed tests in " +
                            "${subject.ifBlank { "your subject" }}."

                percentage >= 60 ->
                    "Revise important concepts and solve practice questions in " +
                            "${subject.ifBlank { "your subject" }}."

                else ->
                    "Review fundamentals in " +
                            "${subject.ifBlank { "your subject" }} " +
                            "before moving to harder problems."
            }
        )

        HorizontalDivider()

        // INTERACTIVE QUIZ
        Text(
            text = "Interactive Practice Quiz",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Answer all ${quizQuestions.size} questions to calculate your score."
        )

        quizQuestions.forEachIndexed { index, question ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Question ${index + 1}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(text = question.question)

                    question.options.forEachIndexed { optionIndex, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !quizSubmitted) {
                                    selectedAnswers[index] = optionIndex
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAnswers[index] == optionIndex,
                                onClick = {
                                    if (!quizSubmitted) {
                                        selectedAnswers[index] = optionIndex
                                    }
                                },
                                enabled = !quizSubmitted
                            )

                            Text(text = option)
                        }
                    }

                    if (quizSubmitted) {
                        val isCorrect =
                            selectedAnswers[index] == question.correctIndex

                        Text(
                            text = if (isCorrect) {
                                "Correct!"
                            } else {
                                "Correct answer: " +
                                        question.options[question.correctIndex]
                            },
                            color = if (isCorrect) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )

                        Text(text = question.explanation)
                    }
                }
            }
        }

        if (!quizSubmitted) {
            Text(
                text = "${selectedAnswers.size} of " +
                        "${quizQuestions.size} answered"
            )

            Button(
                onClick = {
                    quizScore = quizQuestions.indices.count { index ->
                        selectedAnswers[index] ==
                                quizQuestions[index].correctIndex
                    }

                    quizSubmitted = true
                },
                enabled = selectedAnswers.size == quizQuestions.size,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check Answers")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your Quiz Results",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Score: ${quizScore ?: 0} / ${quizQuestions.size}"
                    )

                    Text(text = "Percentage: $percentage%")
                    Text(text = feedback)
                }
            }

            Button(
                onClick = {
                    selectedAnswers.clear()
                    quizScore = null
                    quizSubmitted = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Try Again")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}