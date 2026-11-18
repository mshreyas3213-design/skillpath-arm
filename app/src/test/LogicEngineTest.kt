package com.bharatai.learningcompanion.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class LogicEngineTest {

    @Test
    fun `test 04 - single gap calculation`() {
        val required = mapOf("sql_basics" to 0.8)
        val mastery = mapOf("sql_basics" to 0.4)

        val gaps = LogicEngine.computeGaps(required, mastery)

        assertEquals(1, gaps.size)
        assertEquals("sql_basics", gaps[0].skillId)
        assertEquals(0.8, gaps[0].required, 0.001)
        assertEquals(0.4, gaps[0].mastery, 0.001)
        assertEquals(0.4, gaps[0].gap, 0.001)
    }

    @Test
    fun `test 08 - exact fit schedule`() {
        val pathway = listOf(
            PathwayItem("sql_basics", 0.8, "r_sql_basics_01", 3.0),
            PathwayItem("sql_advanced", 0.7, "r_sql_advanced_01", 4.0),
            PathwayItem("stats_basics", 0.7, "r_stats_basics_01", 10.0),
            PathwayItem("hypothesis_testing", 0.6, "r_hypothesis_testing_01", 6.0),
            PathwayItem("python_basics", 0.6, "r_python_basics_01", 5.0),
            PathwayItem("pandas_cleaning", 0.7, "r_pandas_cleaning_01", 4.0),
            PathwayItem("data_viz", 0.7, "r_data_viz_01", 4.0),
            PathwayItem("eda", 0.7, "r_eda_01", 10.0),
            PathwayItem("dashboards", 0.6, "r_dashboards_01", 3.0),
            PathwayItem("spreadsheets", 0.6, "r_spreadsheets_01", 5.0)
        )

        val result = LogicEngine.buildSchedule(pathway, hoursPerWeek = 9.0, weeksAvailable = 6)

        assertEquals(false, result.overflow)
        assertEquals(6, result.weeks.size)
        assertEquals(9.0, result.weeks[0].totalHours, 0.01)

        assertEquals("sql_basics", result.weeks[0].items[0].skillId)
        assertEquals(3.0, result.weeks[0].items[0].hours, 0.01)
        assertEquals("stats_basics", result.weeks[0].items[2].skillId)
        assertEquals(2.0, result.weeks[0].items[2].hours, 0.01)
    }
}