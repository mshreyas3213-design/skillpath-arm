package com.bharatai.learningcompanion

data class Gap(
    val skillId: String,
    val required: Double,
    val mastery: Double,
    val gap: Double
)

data class Resource(
    val id: String,
    val hours: Double
)

data class PathwayItem(
    val skillId: String,
    val gap: Double,
    val resourceId: String,
    val hours: Double,
    val status: String = ""
)

data class PlanItem(
    val skillId: String,
    val resourceId: String?,
    val hours: Double,
    val status: String = ""
)

data class PlanWeek(
    val week: Int,
    val totalHours: Double,
    val items: List<PlanItem>
)

data class WeeklyPlan(
    val weeks: List<PlanWeek>,
    val overflow: Boolean
)

data class Work(
    val skillId: String,
    val resourceId: String,
    val hours: Double
)