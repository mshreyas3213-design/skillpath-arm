package com.bharatai.learningcompanion

// Gap, Resource, PathwayItem, PlanItem, WeekPlan and WeeklyPlan are defined in LogicEngine.kt.
// Do not define them here again, or the build fails with "Redeclaration".

// Quiz question and answer, as in the team contract (section 3.2).
data class Question(
    val id: String,
    val skillId: String,
    val difficulty: Int,
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

data class Answer(
    val questionId: String,
    val chosenIndex: Int
)