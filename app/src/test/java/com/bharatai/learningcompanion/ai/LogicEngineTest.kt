package com.bharatai.learningcompanion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicEngineTest {

    @Test
    fun testComputeGaps_FindsMissingMastery() {
        val required = mapOf("kotlin" to 0.8, "compose" to 0.6)
        val mastery = mapOf("kotlin" to 0.5, "compose" to 0.6)

        val gaps = LogicEngine.computeGaps(required, mastery)

        assertEquals(1, gaps.size)
        assertEquals("kotlin", gaps[0].skillId)
        assertEquals(0.3, gaps[0].gap, 0.001)
    }

    @Test
    fun testComputeGaps_NoGapsWhenMasteryHigh() {
        val required = mapOf("kotlin" to 0.8)
        val mastery = mapOf("kotlin" to 0.9)

        val gaps = LogicEngine.computeGaps(required, mastery)
        assertTrue(gaps.isEmpty())
    }

    @Test
    fun testBuildSchedule_DistributesHoursCorrectly() {
        val pathway = listOf(
            PathwayItem("kotlin", 0.3, "res1", 5.0),
            PathwayItem("compose", 0.2, "res2", 3.0)
        )

        // 8 hours total work, 4 hours per week capacity, 2 weeks available
        val schedule = LogicEngine.buildSchedule(pathway, 4.0, 2)

        assertTrue(schedule.weeks.isNotEmpty())
        assertEquals(2, schedule.weeks.size)
        assertEquals(4.0, schedule.weeks[0].totalHours, 0.001)
        assertEquals(4.0, schedule.weeks[1].totalHours, 0.001)
    }
}