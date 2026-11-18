package com.bharatal.learningcompanion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlanScreen() {
    val weeklyPlan = listOf(
        "Week 1: Statistics Basics & Hypothesis Testing (6h)",
        "Week 2: SQL Basics & Advanced SQL (6h)",
        "Week 3: Python Basics (2.5h) + Data Cleaning with Pandas (3.5h)",
        "Week 4: Pandas Deep Dive (6h)",
        "Week 5: Practice Quiz & Review (6h)",
        "Week 6: Data Visualization (6h)",
        "Week 7: Mini-Project (6h)",
        "Week 8: Final Review & Assessment (6h)"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Your Weekly Pathway",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Based on 8 weeks available and 6 hours/week.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(weeklyPlan) { week ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = week,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}