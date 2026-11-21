"""Loads questions.json into learning.db (run build_db.py first)."""
import json, sqlite3

con = sqlite3.connect("learning.db")
skills = {r[0] for r in con.execute("SELECT id FROM skills")}
qs = json.load(open("questions.json", encoding="utf-8"))
con.execute("DELETE FROM questions")
for q in qs:
    assert q["skill_id"] in skills, q["id"]
    con.execute("INSERT INTO questions VALUES (?,?,?,?,?,?)",
                (q["id"], q["skill_id"], q["difficulty"], q["text"],
                 json.dumps(q["options"], ensure_ascii=False), q["correct_index"]))
con.commit()
for sid, n in con.execute("SELECT skill_id, COUNT(*) FROM questions GROUP BY skill_id"):
    print(f"{sid:20s} {n}" + ("" if n == 3 else "   <-- expected 3"))
print("total:", con.execute("SELECT COUNT(*) FROM questions").fetchone()[0])
