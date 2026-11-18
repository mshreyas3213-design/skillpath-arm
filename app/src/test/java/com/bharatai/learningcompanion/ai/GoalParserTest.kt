package com.bharatai.learningcompanion.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalParserTest {
    @Test fun goodReplyWorks() = runBlocking {
        val result = GoalParser(FakeLlmEngine()).parse("I want to be a data analyst")
        assertTrue(result is ParseResult.Success)
        val profile = (result as ParseResult.Success).profile
        assertEquals("data_analyst", profile.roleId)
        assertEquals(6, profile.hoursPerWeek)
    }

    @Test fun badThenGoodRetries() = runBlocking {
        val engine = FakeLlmEngine(listOf("sorry, here you go!"))  // first reply is broken
        val result = GoalParser(engine).parse("anything")
        assertTrue(result is ParseResult.Success)
        assertEquals(2, engine.promptsSeen.size)
    }

    @Test fun threeBadRepliesFail() = runBlocking {
        val engine = FakeLlmEngine(listOf("no", "still no", "nope"))
        assertTrue(GoalParser(engine).parse("x") is ParseResult.Failure)
    }
}