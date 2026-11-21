"""Day 1: builds learning.db with schema + Data Analyst skill graph.
Run:  python build_db.py
"""
import os
import sqlite3
from collections import defaultdict, deque

DB = "learning.db"

SCHEMA = """
CREATE TABLE skills (id TEXT PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE prerequisites (
    skill_id  TEXT NOT NULL REFERENCES skills(id),
    prereq_id TEXT NOT NULL REFERENCES skills(id),
    PRIMARY KEY (skill_id, prereq_id)
);
CREATE TABLE roles (id TEXT PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE role_skills (
    role_id TEXT NOT NULL REFERENCES roles(id),
    skill_id TEXT NOT NULL REFERENCES skills(id),
    required_level REAL NOT NULL CHECK (required_level BETWEEN 0 AND 1),
    PRIMARY KEY (role_id, skill_id)
);
CREATE TABLE resources (
    id TEXT PRIMARY KEY,
    skill_id TEXT NOT NULL REFERENCES skills(id),
    title TEXT NOT NULL,
    link TEXT NOT NULL,
    level TEXT,
    hours REAL NOT NULL
);
CREATE TABLE questions (
    id TEXT PRIMARY KEY,
    skill_id TEXT NOT NULL REFERENCES skills(id),
    difficulty INTEGER NOT NULL CHECK (difficulty IN (1,2,3)),
    text TEXT NOT NULL,
    options TEXT NOT NULL,          -- JSON list of 4 strings
    correct_index INTEGER NOT NULL  -- 0..3
);
CREATE TABLE mastery (
    skill_id TEXT PRIMARY KEY REFERENCES skills(id),
    score REAL NOT NULL DEFAULT 0
);
CREATE TABLE weekly_plan (
    week INTEGER NOT NULL,
    skill_id TEXT NOT NULL REFERENCES skills(id),
    resource_id TEXT,
    hours REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'planned'  -- planned/done/stretch/revision
);
"""

SKILLS = [
    ("spreadsheets", "Spreadsheets"),
    ("stats_basics", "Statistics Basics"),
    ("sql_basics", "SQL Basics"),
    ("python_basics", "Python Basics"),
    ("pandas_cleaning", "Data Cleaning with Pandas"),
    ("sql_advanced", "Advanced SQL"),
    ("data_viz", "Data Visualization"),
    ("hypothesis_testing", "Hypothesis Testing"),
    ("eda", "Exploratory Data Analysis"),
    ("dashboards", "Dashboards"),
]

# (skill, prerequisite)
PREREQS = [
    ("pandas_cleaning", "python_basics"),
    ("sql_advanced", "sql_basics"),
    ("data_viz", "pandas_cleaning"),
    ("hypothesis_testing", "stats_basics"),
    ("eda", "pandas_cleaning"),
    ("eda", "stats_basics"),
    ("dashboards", "data_viz"),
    ("dashboards", "sql_basics"),
]

ROLE = ("data_analyst", "Data Analyst")

# (skill, required level 0..1)
REQUIRED = {
    "spreadsheets": 0.6, "stats_basics": 0.7, "sql_basics": 0.8,
    "python_basics": 0.6, "pandas_cleaning": 0.7, "sql_advanced": 0.7,
    "data_viz": 0.7, "hypothesis_testing": 0.6, "eda": 0.7, "dashboards": 0.6,
}


def topo_order(skills, prereqs):
    """Kahn's algorithm. Returns a valid order, or raises if a cycle exists."""
    indeg = {s: 0 for s in skills}
    nxt = defaultdict(list)
    for skill, pre in prereqs:
        nxt[pre].append(skill)
        indeg[skill] += 1
    q = deque(sorted(s for s, d in indeg.items() if d == 0))
    order = []
    while q:
        s = q.popleft()
        order.append(s)
        for t in sorted(nxt[s]):
            indeg[t] -= 1
            if indeg[t] == 0:
                q.append(t)
    if len(order) != len(skills):
        stuck = [s for s, d in indeg.items() if d > 0]
        raise ValueError(f"Cycle in prerequisites involving: {stuck}")
    return order


def main():
    ids = [s for s, _ in SKILLS]
    assert set(REQUIRED) == set(ids), "REQUIRED must cover every skill"
    order = topo_order(ids, PREREQS)  # fails early if there is a loop

    if os.path.exists(DB):
        os.remove(DB)
    con = sqlite3.connect(DB)
    con.execute("PRAGMA foreign_keys = ON")
    con.executescript(SCHEMA)
    con.executemany("INSERT INTO skills VALUES (?,?)", SKILLS)
    con.executemany("INSERT INTO prerequisites VALUES (?,?)", PREREQS)
    con.execute("INSERT INTO roles VALUES (?,?)", ROLE)
    con.executemany("INSERT INTO role_skills VALUES (?,?,?)",
                    [(ROLE[0], s, lvl) for s, lvl in REQUIRED.items()])
    con.commit()

    print("Role requirements:")
    rows = con.execute("""
        SELECT s.name, rs.required_level
        FROM role_skills rs JOIN skills s ON s.id = rs.skill_id
        WHERE rs.role_id = ? ORDER BY rs.required_level DESC, s.name
    """, (ROLE[0],))
    for name, lvl in rows:
        print(f"  {name:30s} {lvl:.1f}")

    print("\nPrerequisites:")
    for s, p in con.execute("SELECT skill_id, prereq_id FROM prerequisites"):
        print(f"  {s} <- {p}")

    print("\nValid learning order:", " -> ".join(order))
    con.close()
    print(f"\nCreated {DB}")


if __name__ == "__main__":
    main()
