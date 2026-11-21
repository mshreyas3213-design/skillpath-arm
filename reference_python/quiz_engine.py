"""Adaptive quiz logic (reference implementation) + self-test.

Per skill the learner answers 2 questions:
  1st at difficulty 2 -> right: next is 3, wrong: next is 1.
The 3rd question of each skill is kept for the later mini-quiz.
Skill score = sum(difficulty * correct) / sum(difficulty)   (0..1)

Run:  python quiz_engine.py           (self-test)
      python quiz_engine.py demo      (take the quiz in the terminal)
"""
import json, sqlite3, sys

MASTERY_OLD, MASTERY_NEW = 0.6, 0.4      # starting values to tune


def next_difficulty(current: int, was_correct: bool) -> int:
    return max(1, min(3, current + (1 if was_correct else -1)))


def score_skill(history):
    """history: list of (difficulty, was_correct). Returns 0..1."""
    if not history:
        return 0.0
    total = sum(d for d, _ in history)
    got = sum(d for d, ok in history if ok)
    return got / total


def update_mastery(old: float, latest: float) -> float:
    return MASTERY_OLD * old + MASTERY_NEW * latest


def load_questions(db="learning.db"):
    con = sqlite3.connect(db)
    rows = con.execute("SELECT id, skill_id, difficulty, text, options, correct_index FROM questions").fetchall()
    con.close()
    bank = {}
    for qid, sid, d, text, opts, ci in rows:
        bank.setdefault(sid, {})[d] = {"id": qid, "skill_id": sid, "difficulty": d,
                                       "text": text, "options": json.loads(opts), "correct_index": ci}
    return bank


def run_skill_quiz(skill_questions, ask):
    """skill_questions: {difficulty: question}. ask(q) -> chosen index. Returns (score, history)."""
    d, history = 2, []
    for _ in range(2):
        q = skill_questions[d]
        ok = ask(q) == q["correct_index"]
        history.append((d, ok))
        d = next_difficulty(d, ok)
    return score_skill(history), history


def _selftest():
    assert next_difficulty(2, True) == 3 and next_difficulty(2, False) == 1
    assert next_difficulty(3, True) == 3 and next_difficulty(1, False) == 1
    assert score_skill([(2, True), (3, True)]) == 1.0
    assert abs(score_skill([(2, True), (3, False)]) - 0.4) < 1e-9
    assert abs(score_skill([(2, False), (1, True)]) - 1/3) < 1e-9
    assert score_skill([(2, False), (1, False)]) == 0.0
    assert abs(update_mastery(0.5, 1.0) - 0.7) < 1e-9
    bank = {3: {"difficulty": 3, "correct_index": 0}, 2: {"difficulty": 2, "correct_index": 0},
            1: {"difficulty": 1, "correct_index": 0}}
    assert run_skill_quiz(bank, lambda q: 0)[0] == 1.0          # always right
    assert run_skill_quiz(bank, lambda q: 1)[0] == 0.0          # always wrong
    print("all quiz_engine self-tests passed")


def _demo():
    bank = load_questions()
    mastery = {}
    for sid, qs in bank.items():
        def ask(q):
            print(f"\n[{sid}] (difficulty {q['difficulty']}) {q['text']}")
            for i, o in enumerate(q["options"]):
                print(f"  {i+1}. {o}")
            return int(input("Answer (1-4): ")) - 1
        score, _ = run_skill_quiz(qs, ask)
        mastery[sid] = round(score, 2)
    print("\nMastery:", mastery)


if __name__ == "__main__":
    _demo() if len(sys.argv) > 1 and sys.argv[1] == "demo" else _selftest()
