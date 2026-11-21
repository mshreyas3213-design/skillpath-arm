# PS4 Team Contract (agreed 6 Nov 2026)

Every change to this file must be agreed by all three members.

## 1. Conventions
- Skill IDs: lowercase snake_case, e.g. `sql_basics`. Role IDs the same, e.g. `data_analyst`.
- Levels and mastery: a number from 0 to 1.
- Difficulty: 1 (easy), 2 (medium), 3 (hard).
- Hours: a decimal number.
- The model may only choose `resource_id` and `skill_id` values that the app gives it in the prompt. Code does all the arithmetic.

## 2. Target device
| Item | Decision |
|---|---|
| Design target | Low-end Android, **4 GB RAM**, arm64-v8a, Android 10 or later |
| Test phone (model) | ______________ (Batool to fill in) |
| Test phone RAM | ______ GB |
| Primary model | Llama 3.2 **1B** Instruct, XNNPACK, KV cache, 8da4w |
| 3B model | Benchmark only if the test phone has 8 GB or more. Not a target. |
| Context length | Cap at 1024 tokens to limit KV cache memory |
| Offline | Everything works in airplane mode |

Peak RAM must be measured and reported. The 4 GB claim stays unproven until Batool's benchmark confirms it.

## 3. JSON schemas

### 3.1 LearnerProfile (output of the goal-parser prompt)
```json
{
  "role_id": "data_analyst",
  "hours_per_week": 6,
  "weeks_available": 8,
  "experience": "beginner",
  "known_skills": ["python_basics"]
}
```
Rules: `role_id` must exist in the `roles` table. `hours_per_week` is 1 to 40. `weeks_available` is 1 to 52. `experience` is `beginner`, `intermediate` or `advanced`.

### 3.2 Question and answer
```json
{ "id": "q_sql_basics_01", "skill_id": "sql_basics", "difficulty": 2,
  "text": "Which clause filters rows?", "options": ["WHERE", "GROUP BY", "ORDER BY", "LIMIT"],
  "correct_index": 0 }
```
```json
{ "question_id": "q_sql_basics_01", "chosen_index": 0 }
```

### 3.3 Mastery map
```json
{ "sql_basics": 0.55, "python_basics": 0.8 }
```

### 3.4 Gap
```json
{ "skill_id": "sql_basics", "required": 0.8, "mastery": 0.55, "gap": 0.25 }
```

### 3.5 Pathway item (ordered, prerequisites first)
```json
{ "skill_id": "sql_basics", "gap": 0.25, "resource_id": "r_sql_basics_01", "hours_needed": 4.5 }
```

### 3.6 WeeklyPlan
```json
{
  "weeks": [
    { "week": 1, "total_hours": 6.0,
      "items": [ { "skill_id": "sql_basics", "resource_id": "r_sql_basics_01",
                   "hours": 4.5, "status": "planned" } ] }
  ],
  "overflow": false
}
```
`status` is `planned`, `done`, `stretch` or `revision`. `overflow` is true if some skills are marked `stretch` because they do not fit the deadline.

### 3.7 LLM outputs (each prompt returns only JSON)
Gap explainer:
```json
{ "explanations": [ { "skill_id": "sql_basics", "text": "..." } ] }
```
Pathway justifier:
```json
{ "reasons": [ { "skill_id": "sql_basics", "text": "..." } ] }
```
Weekly coach:
```json
{ "summary": "...", "tips": ["...", "..."] }
```
The app validates every reply, retries up to twice with the error added, then falls back to plain text templates.

## 4. Function names (logic module, owner: Rafooz)
```kotlin
fun nextDifficulty(current: Int, wasCorrect: Boolean): Int          // clamp 1..3
fun scoreQuiz(answers: List<Answer>, questions: List<Question>): Map<String, Double>
fun computeGaps(roleId: String, mastery: Map<String, Double>): List<Gap>
fun orderPathway(gaps: List<Gap>, prereqs: List<Pair<String, String>>): List<PathwayItem>
fun hoursForSkill(resourceHours: Double, gap: Double, required: Double): Double
fun buildSchedule(pathway: List<PathwayItem>, hoursPerWeek: Double, weeksAvailable: Int): WeeklyPlan
fun adaptPlan(plan: WeeklyPlan, week: Int, hoursDone: Double, miniQuiz: Map<String, Double>): WeeklyPlan
fun updateMastery(old: Double, latest: Double): Double              // 0.6*old + 0.4*latest
```
Starting values to tune: 70% hours threshold, 0.6 and 0.4 mastery weights.

## 5. Shared test file
`tests.json` holds about 10 learner profiles with expected gaps, pathway order and plan. Both the Python and Kotlin versions must pass it.

## 6. Owners
| Part | Owner |
|---|---|
| Model, ExecuTorch, prompts, benchmarks | Batool |
| Database, skill graph, logic module, tests | Rafooz |
| App, Room, orchestrator, GitHub repo, APK | Shreyas |
