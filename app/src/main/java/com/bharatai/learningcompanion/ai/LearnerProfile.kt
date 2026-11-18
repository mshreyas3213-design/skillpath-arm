package com.bharatai.learningcompanion.ai

import org.json.JSONObject

// Matches section 3.1 of the team contract.
data class LearnerProfile(
    val roleId: String,
    val hoursPerWeek: Int,
    val weeksAvailable: Int,
    val experience: String,
    val knownSkills: List<String>
)

sealed class ParseResult {
    data class Success(val profile: LearnerProfile) : ParseResult()
    data class Failure(val error: String) : ParseResult()
}

object ProfileValidator {
    // Only data_analyst exists in the database so far. Add roles here when Rafooz adds them.
    val knownRoles = setOf("data_analyst")
    val knownExperience = setOf("beginner", "intermediate", "advanced")

    fun parse(reply: String): ParseResult {
        // The model may wrap the JSON in extra text, so cut out the {...} part.
        val start = reply.indexOf('{')
        val end = reply.lastIndexOf('}')
        if (start == -1 || end <= start) return ParseResult.Failure("No JSON object found")

        val json = try {
            JSONObject(reply.substring(start, end + 1))
        } catch (e: Exception) {
            return ParseResult.Failure("Invalid JSON: ${e.message}")
        }

        val role = json.optString("role_id")
        if (role !in knownRoles) return ParseResult.Failure("role_id must be one of $knownRoles")

        val hours = json.optInt("hours_per_week", -1)
        if (hours !in 1..40) return ParseResult.Failure("hours_per_week must be 1 to 40")

        val weeks = json.optInt("weeks_available", -1)
        if (weeks !in 1..52) return ParseResult.Failure("weeks_available must be 1 to 52")

        val experience = json.optString("experience")
        if (experience !in knownExperience) {
            return ParseResult.Failure("experience must be one of $knownExperience")
        }

        val arr = json.optJSONArray("known_skills")
        val known = if (arr == null) emptyList() else List(arr.length()) { arr.getString(it) }

        return ParseResult.Success(LearnerProfile(role, hours, weeks, experience, known))
    }
}