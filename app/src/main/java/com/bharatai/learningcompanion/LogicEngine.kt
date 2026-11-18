package com.bharatai.learningcompanion

import kotlin.math.max
import kotlin.math.min

data class Gap(val skillId: String, val required: Double, val mastery: Double, val gap: Double)
data class Resource(val id: String, val hours: Double)
data class PathwayItem(val skillId: String, val gap: Double, val resourceId: String, val hoursNeeded: Double)
data class PlanItem(val skillId: String, val resourceId: String?, val hours: Double, val status: String)
data class WeekPlan(val week: Int, val totalHours: Double, val items: List<PlanItem>)
data class WeeklyPlan(val weeks: List<WeekPlan>, val overflow: Boolean)

object LogicEngine {
    private const val EPS = 1e-9
    private const val MASTERY_OLD = 0.6   // starting values to tune
    private const val MASTERY_NEW = 0.4

    private fun r2(x: Double) = Math.round(x * 100.0) / 100.0
    private fun r4(x: Double) = Math.round(x * 10000.0) / 10000.0

    fun nextDifficulty(current: Int, wasCorrect: Boolean): Int =
        (current + if (wasCorrect) 1 else -1).coerceIn(1, 3)

    /** history: list of (difficulty, wasCorrect). Returns 0..1. */
    fun scoreSkill(history: List<Pair<Int, Boolean>>): Double {
        if (history.isEmpty()) return 0.0
        val total = history.sumOf { it.first }
        val got = history.filter { it.second }.sumOf { it.first }
        return got.toDouble() / total
    }

    fun updateMastery(old: Double, latest: Double): Double =
        MASTERY_OLD * old + MASTERY_NEW * latest

    /** gap = max(0, required - mastery). A missing mastery value counts as 0. */
    fun computeGaps(required: Map<String, Double>, mastery: Map<String, Double>): List<Gap> =
        required.mapNotNull { (skill, req) ->
            val m = mastery[skill] ?: 0.0
            val g = r4(max(0.0, req - m))
            if (g > 0) Gap(skill, req, m, g) else null
        }

    fun hoursForSkill(resourceHours: Double, gap: Double, required: Double): Double =
        if (required <= 0) 0.0 else r2(resourceHours * gap / required)

    /** Prerequisites first; among ready skills the larger gap goes first, ties by skill id. */
    fun orderPathway(
        gaps: List<Gap>,
        prereqs: List<Pair<String, String>>,   // (skill, prerequisite)
        resources: Map<String, List<Resource>> // verified resources only
    ): List<PathwayItem> {
        val by = gaps.associateBy { it.skillId }
        val deps = by.keys.associateWith { mutableSetOf<String>() }
        for ((skill, pre) in prereqs) {
            if (skill in by && pre in by) deps.getValue(skill).add(pre)
        }
        val done = mutableSetOf<String>()
        val out = mutableListOf<PathwayItem>()
        while (done.size < by.size) {
            val ready = by.keys.filter { it !in done && done.containsAll(deps.getValue(it)) }
            require(ready.isNotEmpty()) { "cycle in prerequisites" }
            val s = ready.sortedWith(compareBy<String>({ -by.getValue(it).gap }, { it })).first()
            val res = resources[s]?.firstOrNull()
                ?: throw IllegalArgumentException("no verified resource for $s")
            val g = by.getValue(s)
            out.add(PathwayItem(s, g.gap, res.id, hoursForSkill(res.hours, g.gap, g.required)))
            done.add(s)
        }
        return out
    }

    // ---------- scheduling ----------
    private data class Work(
        val skillId: String, val resourceId: String?, val hoursNeeded: Double, val status: String? = null
    )

    private class WeekBuilder(val week: Int) {
        var total = 0.0
        val items = mutableListOf<PlanItem>()
    }

    private fun fill(
        items: List<Work>, hoursPerWeek: Double, firstWeek: Int, status: String, maxHours: Double?
    ): Pair<List<WeekBuilder>, List<Work>> {
        val weeks = mutableListOf<WeekBuilder>()
        val leftover = mutableListOf<Work>()
        var room = 0.0
        var used = 0.0
        var stopped = false
        for (item in items) {
            var remaining = item.hoursNeeded
            while (remaining > EPS) {
                if (stopped || (maxHours != null && used >= maxHours - EPS)) {
                    stopped = true
                    leftover.add(item.copy(hoursNeeded = r2(remaining)))
                    break
                }
                if (room <= EPS) {
                    weeks.add(WeekBuilder(firstWeek + weeks.size))
                    room = hoursPerWeek
                }
                val cap = if (maxHours == null) room else min(room, maxHours - used)
                val take = r2(min(remaining, cap))
                val wk = weeks.last()
                wk.items.add(PlanItem(item.skillId, item.resourceId, take, item.status ?: status))
                wk.total = r2(wk.total + take)
                room = r2(room - take)
                used = r2(used + take)
                remaining = r2(remaining - take)
            }
        }
        return Pair(weeks, leftover)
    }

    private fun pack(items: List<Work>, hoursPerWeek: Double, firstWeek: Int, lastWeek: Int): WeeklyPlan {
        val room = max(0, lastWeek - firstWeek + 1) * hoursPerWeek
        val (planned, leftover) = fill(items, hoursPerWeek, firstWeek, "planned", room)
        val stripped = leftover.map { it.copy(status = null) }
        val (stretch, _) = fill(stripped, hoursPerWeek, lastWeek + 1, "stretch", null)
        val weeks = (planned + stretch).map { WeekPlan(it.week, it.total, it.items.toList()) }
        return WeeklyPlan(weeks, leftover.isNotEmpty())
    }

    /** Fills week 1, 2, ... up to hours per week and the deadline; the rest becomes "stretch". */
    fun buildSchedule(pathway: List<PathwayItem>, hoursPerWeek: Double, weeksAvailable: Int): WeeklyPlan =
        pack(pathway.map { Work(it.skillId, it.resourceId, it.hoursNeeded) }, hoursPerWeek, 1, weeksAvailable)
}