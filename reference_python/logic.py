"""Day 3 logic (reference implementation): gaps, pathway order, hours, weekly schedule.
Pure functions: they take plain dicts/lists, so the Kotlin port can mirror them 1:1.
"""
import sqlite3
from quiz_engine import update_mastery

EPS = 1e-9


def load_catalog(db="learning.db", role_id="data_analyst"):
    """Reads required levels, prerequisites and VERIFIED resources from the database."""
    con = sqlite3.connect(db)
    required = dict(con.execute(
        "SELECT skill_id, required_level FROM role_skills WHERE role_id=?", (role_id,)))
    prereqs = [list(r) for r in con.execute("SELECT skill_id, prereq_id FROM prerequisites")]
    resources = {}
    for rid, sid, hrs in con.execute(
            "SELECT id, skill_id, hours FROM resources WHERE verified=1 ORDER BY id"):
        resources.setdefault(sid, []).append({"id": rid, "hours": hrs})
    con.close()
    return {"required": required, "prereqs": prereqs, "resources": resources}


def compute_gaps(required, mastery):
    """gap = max(0, required - mastery). A missing mastery value counts as 0."""
    gaps = []
    for sid, req in required.items():
        m = mastery.get(sid, 0.0)
        g = round(max(0.0, req - m), 4)
        if g > 0:
            gaps.append({"skill_id": sid, "required": req, "mastery": m, "gap": g})
    return gaps


def hours_for_skill(resource_hours, gap, required):
    """resource hours x (gap / required), rounded to 2 decimals."""
    if required <= 0:
        return 0.0
    return round(resource_hours * gap / required, 2)


def order_pathway(gaps, prereqs, resources):
    """A skill never comes before its prerequisite (only gap skills count).
    Among skills that are ready, the larger gap goes first; ties break by skill_id.
    The resource used is the first verified one (lowest id)."""
    by = {g["skill_id"]: g for g in gaps}
    deps = {s: set() for s in by}
    for skill, pre in prereqs:
        if skill in by and pre in by:
            deps[skill].add(pre)
    done, out = set(), []
    while len(done) < len(by):
        ready = [s for s in by if s not in done and deps[s] <= done]
        if not ready:
            raise ValueError("cycle in prerequisites")
        ready.sort(key=lambda s: (-by[s]["gap"], s))
        s = ready[0]
        res = resources.get(s)
        if not res:
            raise ValueError(f"no verified resource for {s}")
        g = by[s]
        out.append({"skill_id": s, "gap": g["gap"], "resource_id": res[0]["id"],
                    "hours_needed": hours_for_skill(res[0]["hours"], g["gap"], g["required"])})
        done.add(s)
    return out


def _fill(items, hours_per_week, first_week, status, max_hours=None):
    """Packs items into weeks of hours_per_week. A skill may be split across weeks.
    Stops at max_hours; returns (weeks, leftover_items)."""
    weeks, leftover = [], []
    room, used = 0.0, 0.0
    stopped = False
    for it in items:
        remaining = it["hours_needed"]
        while remaining > EPS:
            if stopped or (max_hours is not None and used >= max_hours - EPS):
                stopped = True
                leftover.append({**it, "hours_needed": round(remaining, 2)})
                break
            if room <= EPS:
                weeks.append({"week": first_week + len(weeks), "total_hours": 0.0, "items": []})
                room = hours_per_week
            cap = room if max_hours is None else min(room, max_hours - used)
            take = round(min(remaining, cap), 2)
            weeks[-1]["items"].append({"skill_id": it["skill_id"], "resource_id": it["resource_id"],
                                       "hours": take, "status": it.get("status", status)})
            weeks[-1]["total_hours"] = round(weeks[-1]["total_hours"] + take, 2)
            room = round(room - take, 2)
            used = round(used + take, 2)
            remaining = round(remaining - take, 2)
    return weeks, leftover


def _pack(items, hours_per_week, first_week, last_week):
    """Weeks first_week..last_week are real weeks; what does not fit becomes stretch after last_week."""
    room = max(0, last_week - first_week + 1) * hours_per_week
    planned, leftover = _fill(items, hours_per_week, first_week, "planned", max_hours=room)
    leftover = [{k: v for k, v in it.items() if k != "status"} for it in leftover]
    stretch, _ = _fill(leftover, hours_per_week, last_week + 1, "stretch")
    return planned + stretch, bool(leftover)


def build_schedule(pathway, hours_per_week, weeks_available):
    """Fills week 1, 2, ... up to the learner's hours per week and deadline.
    What does not fit is marked 'stretch' in extra weeks after the deadline, and overflow=True."""
    weeks, overflow = _pack(pathway, hours_per_week, 1, weeks_available)
    return {"weeks": weeks, "overflow": overflow}


# ---------------- adaptation (check-in) ----------------
ON_TRACK = 0.70          # starting values to tune
REVISION_MARGIN = 0.20   # latest score below required - 0.2  -> add a revision block
REVISION_HOURS = 1.0


def adapt_plan(plan, week, hours_done, mini_quiz, mastery, required, hours_per_week, weeks_available):
    """Check-in for `week`.
    1. hours_done >= 70% of the week's hours -> week counts as done.
       Otherwise only hours_done hours are done and the rest is pushed to later weeks.
    2. Mini-quiz per skill: latest >= required -> skill complete (its remaining hours are dropped,
       the plan is pulled forward); latest < required - 0.2 -> a 1 h revision block next week.
    3. Mastery = 0.6*old + 0.4*latest (never below `required` for a passed skill).
    Returns {"plan", "mastery", "completed", "revision"}."""
    weeks = [{**w, "items": [dict(i) for i in w["items"]]} for w in plan["weeks"]]
    cur = next((w for w in weeks if w["week"] == week), None)
    if cur is None:
        raise ValueError(f"week {week} is not in the plan")

    planned_this = sum(i["hours"] for i in cur["items"] if i["status"] in ("planned", "revision"))
    carry = []                                   # unfinished work of this week
    if planned_this > EPS and hours_done >= ON_TRACK * planned_this - EPS:
        for i in cur["items"]:
            i["status"] = "done"
    else:
        left, kept = hours_done, []
        for i in cur["items"]:
            if i["status"] not in ("planned", "revision"):
                kept.append(i)
            elif left >= i["hours"] - EPS:
                kept.append({**i, "status": "done"})
                left = round(left - i["hours"], 2)
            elif left > EPS:
                kept.append({**i, "hours": round(left, 2), "status": "done"})
                carry.append({"skill_id": i["skill_id"], "resource_id": i["resource_id"],
                              "hours_needed": round(i["hours"] - left, 2), "status": i.get("status")})
                left = 0.0
            else:
                carry.append({"skill_id": i["skill_id"], "resource_id": i["resource_id"],
                              "hours_needed": i["hours"], "status": i.get("status")})
        cur["items"] = kept
        cur["total_hours"] = round(sum(i["hours"] for i in kept), 2)

    new_mastery, completed, revision = dict(mastery), [], []
    for sid, latest in mini_quiz.items():
        req, old = required[sid], mastery.get(sid, 0.0)
        m = update_mastery(old, latest)
        if latest >= req - EPS:
            completed.append(sid)
            m = max(m, req)
        elif latest < req - REVISION_MARGIN - EPS:
            revision.append(sid)
        new_mastery[sid] = round(m, 4)

    remaining = []                               # merge pieces of the same skill, keep order
    index = {}
    for i in carry + [i for w in weeks if w["week"] > week for i in w["items"]]:
        sid = i["skill_id"]
        if sid in completed:
            continue
        hrs = i["hours_needed"] if "hours_needed" in i else i["hours"]
        st = "revision" if i.get("status") == "revision" else None
        key = (sid, i["resource_id"], st)
        if key in index:
            index[key]["hours_needed"] = round(index[key]["hours_needed"] + hrs, 2)
        else:
            index[key] = {"skill_id": sid, "resource_id": i["resource_id"], "hours_needed": hrs}
            if st:
                index[key]["status"] = st
            remaining.append(index[key])

    rev_items = []
    for sid in revision:
        rid = next((i["resource_id"] for w in weeks for i in w["items"] if i["skill_id"] == sid), None)
        rev_items.append({"skill_id": sid, "resource_id": rid,
                          "hours_needed": REVISION_HOURS, "status": "revision"})

    packed, overflow = _pack(rev_items + remaining, hours_per_week, week + 1, weeks_available)
    new_weeks = [w for w in weeks if w["week"] <= week] + packed
    return {"plan": {"weeks": new_weeks, "overflow": overflow},
            "mastery": new_mastery, "completed": completed, "revision": revision}
