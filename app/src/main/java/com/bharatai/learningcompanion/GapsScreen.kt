package com.bharatai.learningcompanion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// ---- Data for the Data Analyst role (from build_db.py and tests.json) ----
private val SKILL_NAMES = mapOf(
    "spreadsheets" to "Spreadsheets",
    "stats_basics" to "Statistics Basics",
    "sql_basics" to "SQL Basics",
    "python_basics" to "Python Basics",
    "pandas_cleaning" to "Data Cleaning with Pandas",
    "sql_advanced" to "Advanced SQL",
    "data_viz" to "Data Visualization",
    "hypothesis_testing" to "Hypothesis Testing",
    "eda" to "Exploratory Data Analysis",
    "dashboards" to "Dashboards"
)

private val REQUIRED_LEVELS = mapOf(
    "spreadsheets" to 0.6, "stats_basics" to 0.7, "sql_basics" to 0.8,
    "python_basics" to 0.6, "pandas_cleaning" to 0.7, "sql_advanced" to 0.7,
    "data_viz" to 0.7, "hypothesis_testing" to 0.6, "eda" to 0.7, "dashboards" to 0.6
)

// (skill, prerequisite)
private val PREREQUISITES = listOf(
    Pair("pandas_cleaning", "python_basics"),
    Pair("sql_advanced", "sql_basics"),
    Pair("data_viz", "pandas_cleaning"),
    Pair("hypothesis_testing", "stats_basics"),
    Pair("eda", "pandas_cleaning"),
    Pair("eda", "stats_basics"),
    Pair("dashboards", "data_viz"),
    Pair("dashboards", "sql_basics")
)

private val RESOURCES: Map<String, List<Resource>> = mapOf(
    "spreadsheets" to listOf(Resource("r_spreadsheets_01", 5.0)),
    "stats_basics" to listOf(Resource("r_stats_basics_01", 10.0)),
    "sql_basics" to listOf(Resource("r_sql_basics_01", 3.0)),
    "python_basics" to listOf(Resource("r_python_basics_01", 5.0)),
    "pandas_cleaning" to listOf(Resource("r_pandas_cleaning_01", 4.0)),
    "sql_advanced" to listOf(Resource("r_sql_advanced_01", 4.0)),
    "data_viz" to listOf(Resource("r_data_viz_01", 4.0)),
    "hypothesis_testing" to listOf(Resource("r_hypothesis_testing_01", 6.0)),
    "eda" to listOf(Resource("r_eda_01", 10.0)),
    "dashboards" to listOf(Resource("r_dashboards_01", 3.0))
)

// Sample learner. Replaced by real quiz results later; a skill that is missing counts as 0.
private val SAMPLE_MASTERY = mapOf(
    "spreadsheets" to 0.6,
    "stats_basics" to 0.2,
    "sql_basics" to 0.4,
    "python_basics" to 0.3
)

private fun skillName(id: String) = SKILL_NAMES[id] ?: id
private fun hours(x: Double) = "%.1f".format(x)

@Composable
fun GapsScreen() {
    var hoursPerWeek by remember { mutableStateOf(6f) }
    var weeksAvailable by remember { mutableStateOf(8f) }

    val gaps = remember { LogicEngine.computeGaps(REQUIRED_LEVELS, SAMPLE_MASTERY) }
    val pathway = remember(gaps) { LogicEngine.orderPathway(gaps, PREREQUISITES, RESOURCES) }
    val plan = remember(pathway, hoursPerWeek, weeksAvailable) {
        LogicEngine.buildSchedule(pathway, hoursPerWeek.roundToInt().toDouble(), weeksAvailable.roundToInt())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Your learning pathway", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Text(
                "Sample learner for now. This will use the real quiz results later.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Text("Hours per week: ${hoursPerWeek.roundToInt()}")
            Slider(
                value = hoursPerWeek,
                onValueChange = { hoursPerWeek = it },
                valueRange = 2f..20f,
                steps = 17
            )
            Text("Weeks available: ${weeksAvailable.roundToInt()}")
            Slider(
                value = weeksAvailable,
                onValueChange = { weeksAvailable = it },
                valueRange = 1f..16f,
                steps = 14
            )
        }

        item {
            Text("Skill gaps, in learning order", style = MaterialTheme.typography.titleMedium)
        }
        if (pathway.isEmpty()) {
            item { Text("No gaps. This learner already meets every requirement.") }
        }
        itemsIndexed(pathway) { index, p ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "${index + 1}. ${skillName(p.skillId)}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Gap ${"%.2f".format(p.gap)}  |  ${hours(p.hoursNeeded)} h  |  ${p.resourceId}")
                }
            }
        }

        item {
            Text("Weekly plan", style = MaterialTheme.typography.titleMedium)
        }
        if (plan.overflow) {
            item {
                Text(
                    "Not everything fits before the deadline. Items marked stretch fall after it.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        items(plan.weeks) { w ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Week ${w.week}  (${hours(w.totalHours)} h)",
                        fontWeight = FontWeight.Bold
                    )
                    w.items.forEach { item ->
                        val tag = if (item.status == "planned") "" else "  [${item.status}]"
                        Text("${skillName(item.skillId)}: ${hours(item.hours)} h$tag")
                    }
                }
            }
        }
    }
}