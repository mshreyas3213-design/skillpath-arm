package com.bharatai.learningcompanion

import kotlin.math.max
import kotlin.math.min

object LogicEngine {
    private const val EPS = 1e-9
    private const val MASTERY_OLD = 0.6
    private const val MASTERY_NEW = 0.4

    private fun r2(x: Double) = Math.round(x * 100.0) / 100.0
    private fun r4(x: Double) = Math.round(x * 10000.0) / 10000.0

    fun computeGaps(required: Map<String, Double>, mastery: Map<String, Double>): List<Gap> =
        required.mapNotNull { (skill, req) ->
            val m = mastery[skill] ?: 0.0
            val g = r4(max(0.0, req - m))
            if (g > 0) Gap(skill, req, m, g) else null
        }

    fun hoursForSkill(resourceHours: Double, gap: Double, required: Double): Double =
        if (required <= 0) 0.0 else r2(resourceHours * gap / required)

    fun orderPathway(
        gaps: List<Gap>,
        prereqs: List<Pair<String, String>>,
        resources: Map<String, List<Resource>>
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
            val res = resources[s]?.firstOrNull() ?: throw IllegalArgumentException("no resource for $s")
            val g = by.getValue(s)
            out.add(PathwayItem(s, g.gap, res.id, hoursForSkill(res.hours, g.gap, g.required)))
            done.add(s)
        }
        return out
    }

    private data class Work(val skillId: String, val resourceId: String?, val hoursNeeded: Double, val status: String? = null)
    private class WeekBuilder(val week: Int) { var total = 0.0; val items = mutableListOf<PlanItem>() }

    private fun fill(items: List<Work>, hoursPerWeek: Double, firstWeek: Int, status: String, maxHours: Double?): Pair<List<WeekBuilder>, List<Work>> {
        val weeks = mutableListOf<WeekBuilder>()
        val leftover = mutableListOf<Work>()
        var room = 0.0; var used = 0.0; var stopped = false
        for (item in items) {
            var remaining = item.hoursNeeded
            while (remaining > EPS) {
                if (stopped || (maxHours != null && used >= maxHours - EPS)) { stopped = true; leftover.add(item.copy(hoursNeeded = r2(remaining))); break }
                if (room <= EPS) { weeks.add(WeekBuilder(firstWeek + weeks.size)); room = hoursPerWeek }
                val cap = if (maxHours == null) room else min(room, maxHours - used)
                val take = r2(min(remaining, cap))
                val wk = weeks.last()
                wk.items.add(PlanItem(item.skillId, item.resourceId, take, item.status ?: status))
                wk.total = r2(wk.total + take); room = r2(room - take); used = r2(used + take); remaining = r2(remaining - take)
            }
        }
        return Pair(weeks, leftover)
    }

    private fun pack(items: List<Work>, hoursPerWeek: Double, firstWeek: Int, lastWeek: Int): WeeklyPlan {
        val room = max(0.0, (lastWeek - firstWeek + 1).toDouble()) * hoursPerWeek
        val (planned, leftover) = fill(items, hoursPerWeek, firstWeek, "planned", room)
        val (stretch, _) = fill(leftover.map { it.copy(status = null) }, hoursPerWeek, lastWeek + 1, "stretch", null)
        val weeks = (planned + stretch).map { PlanWeek(it.week, it.total, it.items.toList()) }
        return WeeklyPlan(weeks, leftover.isNotEmpty())
    }

    fun buildSchedule(pathway: List<PathwayItem>, hoursPerWeek: Double, weeksAvailable: Int): WeeklyPlan =
        pack(pathway.map { Work(it.skillId, it.resourceId, it.hours) }, hoursPerWeek, 1, weeksAvailable)
}