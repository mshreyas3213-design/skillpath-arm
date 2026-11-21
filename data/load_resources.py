"""Day 2: loads resources.csv into learning.db (run build_db.py first).
Adds a 'verified' column. The app must recommend ONLY rows with verified = 1.
After you open each link by hand, set verified to 1 in resources.csv and rerun.
"""
import csv, sqlite3

con = sqlite3.connect("learning.db")
cols = [r[1] for r in con.execute("PRAGMA table_info(resources)")]
if "verified" not in cols:
    con.execute("ALTER TABLE resources ADD COLUMN verified INTEGER NOT NULL DEFAULT 0")
con.execute("DELETE FROM resources")

with open("resources.csv", newline="", encoding="utf-8") as f:
    rows = list(csv.DictReader(f))

skills = {r[0] for r in con.execute("SELECT id FROM skills")}
for r in rows:
    assert r["skill_id"] in skills, f"unknown skill {r['skill_id']}"
    assert r["link"].startswith("https://"), f"bad link {r['id']}"
    con.execute(
        "INSERT INTO resources (id, skill_id, title, link, level, hours, verified) "
        "VALUES (?,?,?,?,?,?,?)",
        (r["id"], r["skill_id"], r["title"], r["link"], r["level"],
         float(r["hours"]), int(r.get("verified") or 0)),
    )
con.commit()

# every skill needs at least 2 resources
for sid, n in con.execute(
    "SELECT s.id, COUNT(r.id) FROM skills s LEFT JOIN resources r ON r.skill_id = s.id GROUP BY s.id"
):
    flag = "" if n >= 2 else "   <-- NEEDS MORE"
    print(f"{sid:20s} {n}{flag}")
print("verified:", con.execute("SELECT COUNT(*) FROM resources WHERE verified=1").fetchone()[0], "/", len(rows))
con.close()
