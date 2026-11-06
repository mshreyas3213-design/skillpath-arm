package com.bharatai.learningcompanion.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalParserTest {

    private val goodJson =
        """{"role":"Android developer","weeks":8,"hoursPerWeek":10,"experience":"beginner"}"""

    @Test
    fun `valid reply returns success on first attempt`() = runTest {
        val parser = GoalParser(FakeLlmEngine(listOf(goodJson)))
        val result = parser.parse("learn Android in 8 weeks, 10h/week, beginner")

        assertTrue(result is GoalParser.Result.Success)
        val profile = (result as GoalParser.Result.Success).profile
        assertEquals("Android developer", profile.role)
        assertEquals(8, profile.weeks)
        assertEquals(10, profile.hoursPerWeek)
    }

    @Test
    fun `invalid then valid reply returns success after retry`() = runTest {
        val parser = GoalParser(FakeLlmEngine(listOf("not json at all", goodJson)))
        val result = parser.parse("same goal")

        assertTrue(result is GoalParser.Result.Success)
        assertEquals("Android developer", (result as GoalParser.Result.Success).profile.role)
    }

    @Test
    fun `three invalid replies return failure`() = runTest {
        val parser = GoalParser(FakeLlmEngine(listOf("nope", "still nope", "{}")))
        val result = parser.parse("same goal")

        assertTrue(result is GoalParser.Result.Failure)
    }
}