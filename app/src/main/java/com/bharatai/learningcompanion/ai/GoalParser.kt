package com.bharatai.learningcompanion.ai

// The final prompts belong to Batool (team contract section 6). This is a working placeholder.
class GoalParser(
    private val engine: LlmEngine,
    private val maxRetries: Int = 2
) {
    suspend fun parse(userText: String): ParseResult {
        var lastError = ""
        repeat(maxRetries + 1) { attempt ->
            val prompt = buildPrompt(userText, if (attempt == 0) null else lastError)
            when (val result = ProfileValidator.parse(engine.generate(prompt))) {
                is ParseResult.Success -> return result
                is ParseResult.Failure -> lastError = result.error
            }
        }
        // All attempts failed: the UI should now show a simple form instead.
        return ParseResult.Failure(lastError)
    }

    private fun buildPrompt(userText: String, previousError: String?): String = buildString {
        appendLine("You extract a learner profile. Reply with ONLY a JSON object, nothing else.")
        appendLine("Keys: role_id (data_analyst), hours_per_week (1 to 40), weeks_available (1 to 52),")
        appendLine("experience (beginner | intermediate | advanced), known_skills (list of skill ids).")
        appendLine("Skill ids: spreadsheets, stats_basics, sql_basics, python_basics, pandas_cleaning,")
        appendLine("sql_advanced, data_viz, hypothesis_testing, eda, dashboards.")
        appendLine()
        appendLine("Example input: I want to be a data analyst in 8 weeks, 6 hours a week. I already know Python.")
        appendLine("""Example output: {"role_id":"data_analyst","hours_per_week":6,"weeks_available":8,"experience":"beginner","known_skills":["python_basics"]}""")
        appendLine()
        if (previousError != null) appendLine("Your last reply was rejected: $previousError. Fix it.")
        appendLine("Input: $userText")
        append("Output:")
    }
}