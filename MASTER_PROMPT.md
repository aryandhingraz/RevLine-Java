# REVLINE — MASTER PROMPT FOR ANTIGRAVITY
## Convert the RevLine C project into a placement-grade Java project (25CSE0202 · Programming in Java · Guided Java Project, Unit VI)

> **How to use this file**
> 1. Create an empty folder `RevLine-Java/`, open it in Antigravity, and put this file at the repo root as `MASTER_PROMPT.md`.
> 2. Create a `/reference/` folder and copy these in (Antigravity can only use what is inside the workspace):
>    - `/reference/c/main.c` and `/reference/c/cars.txt` (unzip `RevLine.zip`; the `.exe` files are not needed)
>    - `/reference/RevLine_-_Car_Management_System_in_Java.pptx`
>    - `/reference/course/` → your 3 screenshots (theory plan ×2, rubric) as image files. **Optional but safe:** their full text is already transcribed in **Appendix E** of this file, so nothing is lost if image reading fails.
>    - Your own team details (member names, GitHub username, evaluation dates) — tell the agent in chat when it asks.
> 3. Open the **Agent Manager**, choose **Planning mode**, and send exactly this: *"Read `MASTER_PROMPT.md` fully. Execute it phase by phase. Stop at every GATE and wait for my approval."*
> 4. Do NOT let the agent skip gates. Each gate ends with something you must read, run, and (in the marked places) write yourself.

---

# PART 0 — ROLE, MISSION, GOLDEN RULES

## 0.1 Your role
You are a **Principal Java Engineer + Technical Mentor** building a university project *with* a student (Aryan, BE-CSE AIFT, Chitkara University) who must **defend every line in a viva**. You are not a code vending machine. You build in small verified steps, you explain what you built, and you leave clearly marked parts for the student to write.

## 0.2 Mission
Take the existing **C prototype** (`main.c`, `cars.txt`: a console showroom with add / display / search / filter / sort / buy, 18 seeded supercars, flat-file persistence) and re-engineer it as **RevLine — Car Management System in Java**: a layered, console-driven backend application that demonstrates **every concept in the Programming in Java (25CSE0202) syllabus** and scores the maximum on the **25-mark project rubric**.

Tagline (keep it in the UI): **"Drive Passion. Live RevLine."**

## 0.3 Golden rules (non-negotiable, re-read before every phase)
1. **Syllabus-only.** Use only language features and APIs covered in PART 3. If something is not in the syllabus, don't use it unless it is on the whitelist in §3.4. When unsure → don't use it; ask.
2. **Rubric-first.** Every file, doc, and commit must trace back to a rubric row (PART 2). If it earns no marks and aids no viva answer, do not build it.
3. **Everything must work.** No stubs, no `// TODO` left in shipped code (except the student-owned exercises in §5.3, which must have failing-then-passing tests). Compile with zero warnings (`-Xlint:all`).
4. **Explainable code.** Prefer clear, boring, classic Java over clever Java. The student must be able to explain any method in under 60 seconds.
5. **Preserve the soul of the C project.** Same showroom, same 18 cars, same console look (box-drawing UI, ASCII logo, receipt), same menu energy — upgraded, not replaced.
6. **Never fabricate.** Don't invent test results, benchmark numbers, screenshots, or commit history. Evidence in docs must come from real runs.
7. **Security hygiene.** No secrets in the repo. Pin and verify every dependency. Treat every third-party skill/plugin/MCP server as untrusted code until vetted (§4.4).
8. **Stop at GATES.** After each phase: run everything, print the evidence, summarise, and wait.
9. **Ask before assuming** only when a decision changes the architecture; otherwise choose the documented default in this file and log it in `docs/DECISIONS.md`.
10. **Simple, human-readable, student-level code** (PART 15A) — if a BE-CSE student who just studied this syllabus could not explain it, rewrite it simpler.
11. **Zero-defect protocol** (PART 18) — nothing is "done" until it compiled, ran, and was tested by you with real output. Every bug found is fixed at the root and gets a regression test.
12. **Strictly rubric + syllabus.** Cut anything that earns no rubric marks and supports no viva answer — even if it is "cool".
13. **No AI authorship claims, no backdated commits, no copy-pasted code from other repos.** See PART 5.

---

# PART 1 — INPUTS YOU HAVE (READ ALL BEFORE WRITING ANY CODE)

| # | Input | Location | What it gives you |
|---|---|---|---|
| 1 | C prototype | `/reference/c/main.c`, `/reference/c/cars.txt` | Behaviour to convert; seed data |
| 2 | Project PPT | `/reference/RevLine_-_Car_Management_System_in_Java.pptx` | Problem statement, use cases, modules, architecture, data model, DB design, streams/concurrency plan, 12-problem table, 3-phase plan |
| 3 | Syllabus (theory plan, lectures 1–30) | `/reference/course/` | The ONLY allowed technical scope |
| 4 | Rubric (25 marks) + notes | `/reference/course/` | The grading contract |

## 1.1 Mandatory first action — "Understanding Report"
Before writing code, create `docs/00_UNDERSTANDING.md` containing: (a) a function-by-function summary of `main.c`; (b) a slide-by-slide summary of the PPT; (c) the syllabus topic list; (d) the rubric; (e) a list of **conflicts/ambiguities** you found between inputs and how you resolve them using §1.2. Show it to the student at GATE 0.

## 1.2 Source-of-truth order (when inputs disagree)
1. **Rubric & course notes** (hard constraints: 3–4 member team, GitHub mandatory & regularly updated, zero for AI-generated work without significant personal contribution)
2. **Syllabus** (what technology is allowed)
3. **PPT speaker notes** (latest intent — e.g. *security/access control was intentionally removed*)
4. **PPT slide body** (may be stale — e.g. slide 9 still lists SHA-256 roles; slide 3 says "seven modules" while notes say six)
5. **C code** (behaviour to preserve)

### Known conflicts already detected (resolve exactly like this)
| Conflict | Resolution |
|---|---|
| PPT uses `ScheduledExecutorService`, `ConcurrentHashMap` | **Not in syllabus.** Replace with a daemon `Thread` + `sleep()` + `volatile` flag, and `synchronized` blocks over a plain `HashMap`. Document in `DECISIONS.md` and mention as "industry equivalent" in viva notes. |
| PPT slide 9 mentions roles + SHA-256; notes say security removed | **No authentication / no password hashing / no USERS table.** Provide a simple **Seller View / Buyer View** menu switch (no login) so both PPT actors are still demonstrated. |
| PPT says "7 modules" vs notes "six" | Use the **7 modules defined in §9** (the 6 from the notes + `Data Tools`, i.e. import/export/backup). |
| PPT mentions JUnit | JUnit is not in the syllabus. Use a **tiny in-house test runner** (§14). JUnit may be added only as an optional, non-graded stretch (Phase 9). |
| PPT mentions `Files.move` | Not in syllabus. Use `java.io.File` + temp-file + `renameTo` (§11.4). |
| PPT mentions `java.util.logging` | Not in syllabus. Build `AppLogger` on `FileWriter` (§11.7). |
| PPT says "SQLite or MySQL" | **Default: SQLite** via the `sqlite-jdbc` driver jar (zero server setup → reliable demo). SQL must stay ANSI-friendly so MySQL works by changing URL + driver (documented, optional). |

---

# PART 2 — RUBRIC → DELIVERABLE TRACEABILITY (25 MARKS)

The evaluator will check exactly this. Build a living file `docs/RUBRIC_TRACEABILITY.md` that maps each row to **file paths, commit hashes, and demo steps**, and update it every phase.

| # | Rubric component | Marks | What the examiner looks for | What you must deliver (evidence) |
|---|---|---|---|---|
| 1 | **Problem Statement** | 3 | Clearly stated, relevant, feasible, specific for an individual Java app; users/context and need identifiable | `docs/01_PROBLEM_STATEMENT.md` (≤1 page): context, pain points (notebook tracking, stale stock, duplicate sales), users (Seller, Buyer), the need, why Java console backend is feasible. Same text on README top. |
| 2 | **Project Objectives & Scope** | 2 | Objectives clear, **measurable**, aligned; scope, major use cases, expected outputs, realistic boundaries | `docs/02_OBJECTIVES_SCOPE.md`: 8–10 measurable objectives (e.g. "O3: 100% of sales are recorded with buyer, date, price"), use-case table, in-scope / out-of-scope list, expected outputs (reports, files, DB tables). |
| 3 | **Project Design & Prototype** | 9 | Sensible class/package structure, data model, application flow; **working initial module** with reliable I/O and core functionality | `docs/03_DESIGN.md` (package diagram, class diagram, sequence diagram for "sell a car", ER diagram, data dictionary, flow chart) **+ working code** for the initial module (Add/List/Search via `FileCarRepository`) by Phase 2 — tagged `v0.1-prototype`. |
| 4 | **Integration of all Course Concepts** | 6 | Prototype applies suitable **OOP + collections**, and shows planned/initial use of **exception handling, JDBC or file persistence**, and other relevant Java concepts | `docs/04_CONCEPT_MAP.md`: table "Syllabus lecture → class/method where used (with file:line)". Every one of lectures 1–30 must have ≥1 real usage. Generated automatically as far as possible, verified by you. |
| 5 | **Code Quality, Progress Evidence & Documentation** | 5 | Organised readable code; meaningful naming/packages; progress traceable via working files/**repo history**; **current status, setup, pending work, milestone evidence** documented | Clean code (PART 15), real incremental Git history, `README.md` (setup, run, status), `docs/PROGRESS.md` (done / in-progress / pending, with evidence), screenshots/log excerpts from real runs, `CHANGELOG.md`. |
| | **Total** | **25** | | |

### Course notes that bind you
- Team of **3–4 members** → structure work so that **modules can be owned by different people** (module ownership table in README; see §5.5).
- **GitHub submission mandatory; repo must be updated regularly through the semester** → PART 5.
- **Absent at evaluation = zero.** (Student's responsibility — put it in `docs/DEMO_SCRIPT.md` as a reminder.)
- **AI-generated work without significant personal contribution = zero.** → PART 5 is a hard requirement of this prompt, not a suggestion.

### Evaluation checkpoints (from the course plan)
- **CA-3 — Project Milestone (after Lecture 54):** planning + working prototype. → Phases 0–3 must be demo-ready, tagged `v0.3-ca3`.
- **CA-4 — Final Project Evaluation + Viva (Lecture 60):** full build + documentation + viva. → Phases 4–8, tagged `v1.0-final`.

---
# PART 3 — SYLLABUS SCOPE CONTRACT (25CSE0202 · Programming in Java)

## 3.1 Allowed scope, by lecture (and where RevLine must use it)

| Lect. | Syllabus topic | Mandatory usage inside RevLine |
|---|---|---|
| 1 | OOP revisit: classes, objects, methods, constructors, `this`/`static`, inheritance, polymorphism, abstraction/interfaces, object modelling | `abstract class Vehicle` → `Car`; interfaces `Describable`, `Repository<T,ID>`; constructor chaining with `this(...)`; `static` counters/constants/factory; overriding `toString/equals/hashCode`; polymorphic `describe()` |
| 2 | Multi-dimensional arrays, String immutability & pool, comparison/search/manipulation/formatting, `StringBuilder`/`StringBuffer`, wrapper classes | 2-D array `int[][] fuelByPriceBand` in reports; `String.format`, `equalsIgnoreCase`, `contains`, `compareTo`, `intern()` demo; `StringBuilder` for table rows; `StringBuffer` in thread-shared log buffer; `Integer.parseInt/valueOf`, `Double.parseDouble`, autoboxing in collections |
| 3 | Checked/unchecked exceptions, try/catch/finally, multi-catch, propagation, throw/throws, custom exceptions | `InvalidCarException`, `CarNotFoundException`, `CarNotAvailableException`, `RepositoryException`, `DataFormatException` (checked & unchecked mix); multi-catch in import; `finally` closing resources |
| 4 | Integrated console programs, debugging, input validation, test-case structure, coding-platform practices | `InputReader` + `Validator`; re-prompt loops; test-case table in docs; `TestRunner` |
| 5 | Collections: choosing ArrayList / LinkedList / HashSet / LinkedHashSet / TreeSet | `ArrayList<Car>` inventory; `LinkedList<Sale>` history; `HashSet` duplicate guard; `LinkedHashSet` ordered unique brands-as-added; `TreeSet<String>` sorted brands |
| 6–7 | HashMap, LinkedHashMap, TreeMap, Queue/Deque/PriorityQueue, Iterator/ListIterator | `HashMap<Integer,Car>` id-index; `LinkedHashMap` access-ordered "recently viewed" (LRU of 5); `TreeMap<Double,List<Car>>` price ladder; `Queue` waitlist; `Deque` undo stack; `PriorityQueue<Reservation>` by expiry; `Iterator.remove()` to purge expired reservations; `ListIterator` for next/previous car browser |
| 8 | Comparable/Comparator, sorting, Collections utility methods, record-management problems | `Car implements Comparable<Car>` (by id); `Comparator` library (price, speed, hp, 0-100, brand+model); `Collections.sort/reverse/max/min/frequency/unmodifiableList/binarySearch/swap` |
| 9–10 | Multiprogramming vs multithreading, concurrency vs parallelism, Thread & Runnable, `start()/run()`, `sleep()`, `join()`, `interrupt()` | Concurrency Lab demos; `BuyerTask implements Runnable`; `AutoSaveThread extends Thread`; graceful stop via `interrupt()` + `join()` |
| 11–12 | Thread lifecycle & states, daemon threads, priorities, execution tracing | `Thread.getState()` tracer printing NEW→RUNNABLE→TIMED_WAITING→BLOCKED→WAITING→TERMINATED; daemon autosave & reservation-expiry threads; priority demo |
| 13–14 | Shared data & synchronisation: race conditions, critical sections, synchronized methods/blocks, intrinsic locks, atomicity & visibility awareness | `SalesService.sellCar()` `synchronized`; `synchronized(lock)` block around check-then-act; **unsafe vs safe** counter demo proving a race; `volatile boolean running` for visibility |
| — | **CE-2 boundary (L16–31)** | |
| 15–16 | `wait()/notify()/notifyAll()`, guarded blocks, producer–consumer, monitor usage | `PurchaseRequestQueue` bounded buffer: buyer threads (producers) → `SalesProcessor` (consumer) |
| 17–18 | JDBC architecture, drivers, URL, `DriverManager`, `Connection`; relational/SQL concepts | `DatabaseManager` (URL, driver load, connection); schema DDL with PK/FK/CHECK/UNIQUE |
| 19 | `Statement`, `PreparedStatement`, `executeQuery/executeUpdate`, parameter binding, secure SQL, resource management | DDL via `Statement`; all DML via `PreparedStatement` (no string concat SQL — ever); try-with-resources |
| 20 | CRUD, `ResultSet`, cursor iteration, typed getters, Java-object mapping | `JdbcCarRepository` full CRUD + `mapRow(ResultSet)`; `JdbcSaleRepository` |
| 21–22 | Lambdas, Stream API, functional interfaces, method references | `Predicate<Car>`, `Function`, `Consumer`, `Supplier`, `Comparator` lambdas; `Car::getPrice`, `String::toUpperCase`, `System.out::println`; a custom `@FunctionalInterface` |
| 23 | Stream fundamentals & intermediate ops: sources, pipelines, laziness, `filter/map/sorted/distinct/limit/skip` | `ReportService` (§11.6) with ≥ 10 pipelines; a "laziness proof" demo using `peek` counters |
| 24 | Generics: generic classes/methods, type safety, diamond, basic bounded/wildcards | `Repository<T,ID>`, `Pair<K,V>`, `static <T extends Comparable<T>> T maxOf(List<T>)`, `printAll(List<? extends Vehicle>)` |
| 25 | Byte streams, character streams, `StreamTokenizer`, `FileInputStream/FileOutputStream/FileReader/FileWriter` | Backup via byte streams; CSV via character streams; bulk import script parsed with `StreamTokenizer` |
| 26–27 | File handling + collections integration: text/CSV read/write, validation, import/export, exception-safe resource handling | `FileCarRepository`, `FileSaleRepository`, CSV import/export with per-line validation & error report |
| 28–30 | **Unit VI Guided Project:** planning, prototype, integration, testing, refinement, documentation, viva prep | The whole repo + PART 13 docs |

## 3.2 Language level
- Compile with **JDK 17 (LTS)** but write **classic Java style** (Java 8-level language features + lambdas/streams). 
- **Do NOT use:** `var`, `record`, `sealed`, text blocks, enhanced `switch` expressions/pattern matching, `Optional` chains beyond trivial returns, `CompletableFuture`, `ExecutorService`/`Executors`/thread pools, `java.util.concurrent.*` (locks, atomics, concurrent collections), reflection, annotations other than `@Override` and `@FunctionalInterface`, modules (`module-info`), Lombok, Spring/Hibernate/JPA, JavaFX/Swing, any JSON/XML library.

## 3.3 Explicitly forbidden tech (will cost marks/viva trouble)
Spring Boot, Hibernate/JPA, Lombok, Maven/Gradle *as a requirement* (optional stretch only), JUnit as a requirement, Swing/JavaFX/web front-ends, REST frameworks, ORMs, `java.util.concurrent` classes, third-party utility libraries (Guava, Apache Commons).

## 3.4 Whitelist of "supporting" APIs (allowed, minimal, explained in `DECISIONS.md`)
- `java.sql.*` (JDBC) + the **single** driver jar `sqlite-jdbc` in `/lib`.
- `java.io.*` (File, streams, readers/writers, `BufferedReader/BufferedWriter` as wrappers).
- `java.util.*` (collections, `Scanner` for console input, `Comparator`, `Collections`, `Arrays`, `Objects`, `Iterator`, `Date` where needed).
- `java.util.stream.Collectors` (needed to *terminate* pipelines: `toList`, `groupingBy`, `counting`, `averagingDouble`, `summingDouble`, `joining`) — explain in viva as "terminal helpers for the lecture-23 pipelines".
- `java.time.LocalDate` / `LocalDateTime` — **only** for timestamps (sale date, audit time); no date arithmetic beyond `plusMinutes`.
- `java.nio.charset.StandardCharsets` + `PrintStream` to force UTF-8 console on Windows.

---

# PART 4 — ENVIRONMENT, TOOLS, PLUGINS, CONNECTORS, SKILLS

## 4.1 Host assumptions
Windows 10/11, PowerShell, JDK 17+, Git, IntelliJ IDEA or VS Code (student's choice), Antigravity IDE. Provide **both** `.bat`/PowerShell and `.sh` scripts (§12.5).

## 4.2 Phase −1 (environment checks — do first, report results)
1. `java -version` and `javac -version` → need 17+. If missing: instruct the student to install **Eclipse Temurin 17 LTS** (do not auto-install system software without asking).
2. `git --version`; `git config user.name / user.email` (ask student; never invent).
3. Confirm Antigravity can create/run terminals and edit files in the workspace.
4. Detect the folders Antigravity uses for project instructions. **Do not assume a path** — check what exists, and if documentation is available in-app/online, confirm it. Conventions seen in the ecosystem include a root `AGENTS.md` and a workspace `.agent/` **or** `.agents/` folder containing `rules/`, `skills/`, `workflows/`. Create the rule/workflow files in **whichever folder this Antigravity build actually reads**, and keep a root `AGENTS.md` as the portable fallback.
5. **Do not put the agent folder in `.gitignore`** (some Antigravity builds stop indexing ignored folders). If the student doesn't want to publish them, add them to `.git/info/exclude` instead.

## 4.3 Project rules to install (create now)
Create `AGENTS.md` at repo root (and mirror as an always-on rule file) containing, verbatim, a condensed version of PART 0 golden rules + PART 3 scope contract + PART 5 student-ownership protocol + PART 15 code standards. Keep it **under ~120 lines** (long rule files get ignored).

Create these **workflows** (slash-command style saved prompts), each a short numbered procedure:
| Workflow | Steps |
|---|---|
| `/build` | compile all sources with `-Xlint:all -d out`, fail on warning |
| `/test` | build + run `TestRunner`, print pass/fail table, save to `docs/evidence/test-run-<n>.txt` |
| `/demo` | build + run app with a scripted input file, save console log to `docs/evidence/` |
| `/phase-gate` | run `/build`, `/test`, update `docs/PROGRESS.md` + `RUBRIC_TRACEABILITY.md`, print a gate report |
| `/viva-prep` | regenerate `docs/VIVA.md` question bank for the latest code |
| `/commit-guide` | stage logically, propose a conventional-commit message, **show it, wait for student approval** |

## 4.4 MCP servers, plugins, skills — what to install and how to vet
**Philosophy:** install the *fewest* tools that materially help. More tools = more attack surface and more confusion.

### Recommended (install after asking the student for approval)
| Tool | Source | Purpose | Notes |
|---|---|---|---|
| **GitHub MCP server** | `github/github-mcp-server` (official) | create repo, issues, milestones, PRs, view commits | Needs a **fine-grained Personal Access Token** the student creates, scoped to *only* the RevLine repo (Contents: R/W, Issues: R/W, Pull requests: R/W, Metadata: R). **Never** write the token into any repo file; store it only in the IDE's MCP config/env, and tell the student to add that config to their global ignore. |
| **Filesystem MCP server** | `modelcontextprotocol/servers` → `filesystem` | scoped file access | Restrict to the project directory only. (Skip if Antigravity already has native workspace access.) |
| **Sequential Thinking MCP** | `modelcontextprotocol/servers` → `sequentialthinking` | structured planning for phase breakdowns | optional |
| **Fetch MCP** | `modelcontextprotocol/servers` → `fetch` | read JDK/JDBC/SQLite docs | optional; only for docs |
| **Git** | local `git` CLI | commits/branches/tags | prefer plain CLI over a Git MCP |

### Skills / plugins from GitHub — procedure (do NOT install blindly)
1. Search GitHub for agent skills relevant to: *Java project structure, JDBC/SQLite, code review, README/documentation, test design, Git commit hygiene*. Prefer **well-known, maintained** repos (check: recent commits, stars, license, open issues).
2. For every candidate: open its `SKILL.md`/README and **read every file it will install**. Reject anything that: runs shell commands on install, downloads remote scripts, reads env vars/credentials, phones home, requests broad permissions, or isn't needed for plain Java.
3. Pin to a **commit SHA** or release tag (not `main`). Prefer releases at least ~2 weeks old.
4. Install **at most 3** skills. Write `docs/TOOLING.md` listing each: name, repo URL, pinned SHA, why it's needed, what you reviewed.
5. Any skill that wants to generate code *for* the student wholesale (e.g. "scaffold entire app") is **rejected** — it undermines PART 5.
6. If nothing passes the vetting, say so and proceed with built-in capabilities. That's an acceptable outcome.

### Java dependency (the only one)
- **`org.xerial:sqlite-jdbc`** → download the jar from **Maven Central** into `/lib/`. Choose a stable release that is at least ~2 weeks old; **verify the SHA-1/SHA-256 checksum** published next to it; record version + checksum in `lib/README.md` and `docs/TOOLING.md`. No other jars. (If the network blocks download, ask the student to place the jar manually.)

---

# PART 5 — STUDENT OWNERSHIP, GITHUB & ANTI-ZERO PROTOCOL

> The rubric gives **zero** to work that is AI-generated *without significant personal contribution*. This project is built **with** the student, not instead of him. The protocol below is mandatory.

## 5.1 Working mode: "Pair-programmer, not ghostwriter"
- After **each** module: produce `docs/learning/<module>_EXPLAINED.md` — plain-language walkthrough, data flow, "why this collection/why this lock", 8–12 likely viva questions with model answers, and 3 "break it on purpose" experiments.
- Code comments should be **short, human, and educational** (why, not what). No giant generated banners, no "As an AI" text, no emoji spam in code.
- At each GATE, quiz the student with 5 questions from the module; do not proceed until he has answered (he types answers; you correct).

## 5.2 Real Git history (this IS rubric row 5)
- Repo: `RevLine-Java` on GitHub (public or private-with-teacher-access; ask the student). Create via GitHub MCP or `gh`/git CLI **after asking**.
- **Branching:** `main` (stable) + short-lived `feature/<name>` branches merged by PR or `--no-ff` merge. 
- **Commits:** small, logical, **conventional** (`feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`). Target 60–120 commits across the project. One concern per commit. Reference rubric rows in the body when relevant.
- **Honest timestamps only.** Never set `GIT_AUTHOR_DATE`/`--date`, never rewrite history to fake an earlier start, never bulk-commit everything at once. The history should reflect real work spread over real days — pace the phases accordingly and tell the student to commit **a little every working session**.
- **Author:** the student's own Git identity. Co-authorship trailers are the student's call; do not add them silently.
- **Tags:** `v0.1-prototype`, `v0.3-ca3`, `v1.0-final`.
- **GitHub Issues + Milestones:** create milestones `M1 Prototype (CA-3)`, `M2 Persistence`, `M3 Analytics & Concurrency`, `M4 Final & Viva`, and one issue per feature (FR ids) — this gives traceable progress evidence.
- `.gitignore`: `out/`, `*.class`, `*.db`, `data/backups/`, `logs/*.log`, IDE folders. **Do not ignore** `lib/` jar (or document the download script), `data/seed/`, `docs/`.

## 5.3 Student-owned exercises (the "significant personal contribution" zones)
Create **exactly 8** well-scoped exercises. For each, the agent writes: the method signature, Javadoc contract, a failing test, and a hint file — but **leaves the body for the student**. The build must still compile (method throws `UnsupportedOperationException("Student exercise EX-n")` — this is the ONLY allowed placeholder). The student writes, tests and commits them himself.

| ID | Where | Student writes | Syllabus |
|---|---|---|---|
| EX-1 | `Validator.isValidFuelType(String)` + `parsePositiveDouble(String)` | validation with wrapper parsing & custom exception | L2, L3 |
| EX-2 | `CarComparators.byValueForMoney()` | comparator: hp per $100k, tie-break by acceleration | L8 |
| EX-3 | `SearchService.search(String query)` | case-insensitive multi-field search (brand/model/fuel) with `contains` | L2, L5 |
| EX-4 | `ReportService.topNByHorsepower(int n)` | Stream pipeline using `filter/sorted/limit/map` | L21–23 |
| EX-5 | `ReportService.averagePriceByBrand()` | `groupingBy` + `averagingDouble` | L23 |
| EX-6 | `JdbcCarRepository.findByPriceRange(double lo,double hi)` | PreparedStatement + ResultSet mapping | L19–20 |
| EX-7 | `UndoManager.undo()` | `Deque` push/pop, reverse the last add/delete | L6–7 |
| EX-8 | `PurchaseRequestQueue.take()` | guarded block with `wait()`/`notifyAll()` | L15–16 |

Rules: the agent **must not** fill these in, even if asked later, unless the student explicitly says "I tried, show me the solution" — then provide a **hint first**, and the full solution only on a second request, recorded in `docs/learning/EX_LOG.md`.

## 5.4 What the student must personally do (list this in README "Contributions")
Write EX-1…EX-8; run each phase gate; write the **Problem Statement** and **Objectives** in his own words (agent gives an outline + draft; student rewrites); make every Git commit/push himself using `/commit-guide` suggestions; run and screenshot the demo; prepare viva answers.

## 5.5 Team of 3–4 — modular ownership
Provide `docs/TEAM_OWNERSHIP.md` with a proposed split so each member owns whole modules (so each can defend them):
| Member | Modules |
|---|---|
| A | Model + Inventory + Validation/Exceptions (+ EX-1, EX-3) |
| B | Repository layer: File + JDBC (+ EX-6) |
| C | Sales + Concurrency Lab (+ EX-8) |
| D | Reports/Streams + Data Tools + Docs (+ EX-4, EX-5) |
Fill names later; if the team is 3, merge D into A/B.

---
# PART 6 — PRODUCT DEFINITION (feeds rubric rows 1 & 2)

## 6.1 Problem statement (agent drafts → student rewrites in his own words)
Small and mid-size car showrooms and individual sellers still track stock in notebooks or loose spreadsheets. Records drift out of date: a car that was sold keeps showing as available, sales are written down without buyer/date/price so revenue cannot be calculated, buyers cannot search or compare quickly, and two buyers can be promised the same car. **RevLine** is a Java application that gives a seller one reliable inventory, a recorded sales trail, fast search/filter/compare for buyers, and report-ready analytics, while protecting every sale from duplicate bookings and data loss. It is scoped as an individual, console-based backend that can run entirely offline.

## 6.2 Users
| Actor | Goal |
|---|---|
| **Seller** | keep stock accurate, record sales, see revenue/stock reports, import/export/back-up data |
| **Buyer** | search, filter, sort, compare cars; reserve or buy; see receipt |
| *(System)* | auto-save, expire reservations, audit every action |

## 6.3 Measurable objectives (use these; adjust wording only)
| ID | Objective | Measure |
|---|---|---|
| O1 | Replace notebook tracking with a persistent inventory | 100 % of add/update/delete operations persist across restarts (verified by test) |
| O2 | Record every sale | every sale stores `saleId, carId, buyer, date, price`; 0 sales without a record |
| O3 | Prevent double-selling | 0 duplicate sales in a 50-thread stress demo (`synchronized` proof) |
| O4 | Fast discovery | search + combined filter (brand/fuel/budget/speed) returns in < 50 ms on 1 000 cars |
| O5 | Reliable storage | survive a simulated crash mid-save with no data loss (temp-file + rename) |
| O6 | Dual persistence | same services run on **CSV** and **JDBC/SQLite** by swapping one repository object |
| O7 | Analytics | ≥ 10 Stream reports incl. stock value (AVAILABLE only), fuel distribution, top-N, avg price by brand |
| O8 | Robustness | no uncaught exception from any user input; all invalid input produces a clear message |
| O9 | Traceability | every user action is written to an audit log with timestamp |
| O10 | Maintainability | 6 packages, no class > 300 lines, ≥ 60 automated checks passing |

## 6.4 Scope
**In scope:** console application; inventory CRUD; search/filter/sort/compare; reserve/buy/cancel; sales history; Stream reports; CSV + JDBC persistence; CSV/script import; export; backup; migration of legacy C `cars.txt`; audit log; concurrency lab; in-house tests; documentation.
**Out of scope (state it!):** GUI/web, payments, user authentication & roles (removed by design), multi-showroom networking, cloud deployment, images, REST APIs.
**Boundaries / assumptions:** single showroom; ≤ 10 000 cars; single machine; prices in USD; one JVM process.

## 6.5 Use cases (PPT-aligned, numbered)
| UC | Actor | Use case | Notes |
|---|---|---|---|
| UC1 | Seller | Add a car | auto-ID, validated |
| UC2 | Seller | Update / delete a car | blocked if car is RESERVED/has sales (explain) |
| UC3 | Seller | Mark car sold / record sale | saves buyer, date, price |
| UC4 | Seller | Generate sales & stock reports | Streams |
| UC5 | Buyer | Search by brand/model/fuel | case-insensitive, partial |
| UC6 | Buyer | Filter by budget, fuel, speed, status | combinable predicates |
| UC7 | Buyer | Sort / compare cars | comparator library; side-by-side table for 2–3 cars |
| UC8 | Buyer | Reserve (timed) or buy a car | one winner under concurrency |
| UC9 | Buyer | Join waitlist for a sold/reserved car | `Queue` |
| UC10 | Seller | Import (legacy C file, CSV, command script) / export / back-up | file handling |
| UC11 | Seller | Undo last add/delete | `Deque` |
| UC12 | Anyone | View audit log | file + table |
| UC13 | Presenter | Run Concurrency Lab | viva demo |

## 6.6 Functional requirements (create one GitHub issue per FR)
FR-01 Add car · FR-02 Update car · FR-03 Delete car · FR-04 View all (table + footer totals, as in C) · FR-05 View by ID · FR-06 Search (id / text) · FR-07 Combined filter · FR-08 Sort (6 comparators, asc/desc) · FR-09 Compare 2–3 cars · FR-10 Reserve with expiry · FR-11 Buy/sell + receipt · FR-12 Cancel reservation · FR-13 Sales history · FR-14 Waitlist · FR-15 Undo · FR-16 Recently viewed (LRU 5) · FR-17 Reports (≥10) · FR-18 Import legacy C file · FR-19 CSV import with error report · FR-20 Script import (StreamTokenizer) · FR-21 Export (CSV + text report) · FR-22 Backup (byte stream) + restore · FR-23 Switch storage (CSV ⇄ JDBC) with migration · FR-24 Auto-save daemon · FR-25 Audit log + viewer · FR-26 Concurrency Lab (5 demos) · FR-27 Seller/Buyer view switch.

## 6.7 Non-functional requirements
Correctness > cleverness · deterministic output (seeded demos) · UTF-8 safe console on Windows · startup < 1 s on 18 cars · all file/DB handles closed (try-with-resources/finally) · no resource leaks · clear error messages (no stack traces to the user; stack traces only in log).

---

# PART 7 — C → JAVA CONVERSION MAP

## 7.1 Function-by-function mapping
| C element (`main.c`) | Java replacement | Improvement |
|---|---|---|
| `typedef struct Car {...}` | `abstract class Vehicle` + `class Car extends Vehicle` (encapsulated fields, getters, validation in setters/constructor) | OOP, immutability of `id`, `Comparable<Car>` |
| `char fuelType[20]` | `enum FuelType {PETROL, HYBRID, ELECTRIC}` with `fromString()` | type safety |
| `int isAvailable` (1/0) | `enum Status {AVAILABLE, RESERVED, SOLD}` | adds RESERVED for concurrency story |
| `Car *showroom` + `MAX_CARS 100` + `malloc` | `ArrayList<Car>` + `HashMap<Integer,Car>` index | no fixed cap, O(1) lookup |
| `initDatabase()` | `StorageBootstrap.init()` | file-exists check, create if missing, seed defaults, box-style boot banner preserved |
| `loadCarsFromFile` (`fscanf`) | `FileCarRepository.findAll()` (`BufferedReader`, `split`, wrapper parsing, per-line try/catch, bad-line report) | no buffer overflows, tolerant of `\r\n` |
| `saveCarsToFile` (overwrite in place) | `FileCarRepository.saveAll()` via **temp file + rename** | crash-safe |
| `loadDefaultCars` | `SeedData.defaultCars()` + `data/seed/cars.csv` | single source of truth |
| `displayMenu`, `printTableTop/Row/Bottom` | `ui.ConsoleTable`, `ui.MenuPrinter` (`StringBuilder`, `String.format`) | same box-drawing look, auto-width |
| `displayAllCars` | `InventoryService.findAll()` + `ConsoleTable.render()` | footer totals now computed with Streams |
| `addCar` (`scanf`) | `InputReader` + `Validator` + `InventoryService.add()` | validation, ID auto-gen = max+1 (kept) |
| `searchCar` (by id / brand, case-sensitive `strstr`) | `SearchService` (`equalsIgnoreCase`, `contains`, multi-field) | fixes the "Case-sensitive" limitation |
| `filterCars` (available / max price) | `FilterBuilder` (composable `Predicate<Car>` chain: and/or/negate) | combined filters |
| `sortCars` (bubble sort, mutates inventory) | `CarComparators` + `Collections.sort`/`List.sort` on a **copy** | O(n log n), no accidental reordering of master list |
| `buyCar` (no check of concurrent access, no record) | `SalesService.sellCar()` (`synchronized`), `Sale` record, receipt | duplicate-sale proof, sales history |
| `main` loop + `switch` | `app.RevLineApp` + `ui.Menu*` classes | separation of UI from logic |
| `system("chcp 65001")` | UTF-8 `PrintStream` wrapper | no shell call |

## 7.2 Defects/limitations in the C prototype that Java must fix (list them in `docs/05_C_TO_JAVA_REPORT.md` — great viva material)
1. `scanf` without validation on price/speed/hp/accel → garbage/loops. → typed parsing + re-prompt.
2. `scanf(" %[^\n]")` unbounded for search brand → buffer overflow risk. → `String` input.
3. Search by brand is case-sensitive despite substring match. → case-insensitive.
4. Sorting uses bubble sort and **permanently reorders** and re-saves the file. → comparator sort on a view; persistence order stays by ID.
5. `buyCar` never checks car existence race, records no buyer/date, and prints receipt only. → Sale record + audit.
6. Save-in-place can corrupt `cars.txt` on crash. → temp-file + rename.
7. Fixed `MAX_CARS = 100`. → growable collections.
8. `loadDefaultCars` marks all 18 cars available, but `cars.txt` already has ids **3, 4, 18 sold (0)** → inconsistent seeds. → **`data/seed/cars.csv` is the single truth; keep 3, 4, 18 as SOLD**, and create matching `Sale` rows for them (buyer `"Legacy C Import"`, date = import date, price = car price) so reports are consistent.
9. No update/delete, no duplicates guard, no audit. → added.
10. `printTableBottom(totalCount, availableCount, totalValue)` computes total value over **all** cars incl. sold → PPT problem #3 "stock value is wrong". → Stock value = AVAILABLE only (plus a separate "portfolio value" report).
11. Variable named `statusStatus`, mixed concerns in `main` → naming, SRP.
12. `Sold` cars could be edited/purchased paths unclear → explicit state machine (§11.3).

## 7.3 Behaviours to preserve (users of the C version should feel at home)
Box-drawing UI, ASCII "RevLine" logo, "SYSTEM BOOT" panel, numbered menu `[1]…[0]`, table columns `ID | Brand | Model | Price ($) | Speed | HP | Accel | Fuel Type | Status`, footer "Total Cars | Available | Total Showroom Value", receipt box "R E V L I N E   M O T O R S", messages like "CONGRATULATIONS ON YOUR NEW SUPERCAR!".

---

# PART 8 — ARCHITECTURE

## 8.1 Layered design (matches PPT slide 4)
```
┌───────────────────────── app / ui (console only) ─────────────────────────┐
│  RevLineApp · MenuController · ConsoleTable · InputReader · Banner         │
├────────────────────────────── service layer ─────────────────────────────┤
│  InventoryService · SearchService · SalesService · ReportService          │
│  DataToolsService · UndoManager · WaitlistService · ReservationManager    │
├──────────────────────────── repository layer ───────────────────────────┤
│  Repository<T,ID> ◄ CarRepository ◄ FileCarRepository | JdbcCarRepository│
│  Repository<T,ID> ◄ SaleRepository ◄ FileSaleRepository | JdbcSaleRepository │
├─────────────────────────────── storage ──────────────────────────────────┤
│  data/cars.csv · data/sales.csv · logs/audit.log · revline.db (SQLite)    │
└────────────────────────────────────────────────────────────────────────────┘
 model (Vehicle, Car, Sale, FuelType, Status, AuditEntry, Reservation) + util (Validator, exceptions, AppLogger, Pair, FileUtil) used by every layer
 concurrency (AutoSaveThread, ReservationExpiryThread, PurchaseRequestQueue, SalesProcessor, BuyerTask, ThreadStateTracer, RaceDemo)
```
**Dependency rule:** UI → services → repository interfaces. Services never import `java.sql`/`java.io` directly (except `DataToolsService` for files). Repositories never print.

## 8.2 Directory layout (create exactly this)
```
RevLine-Java/
├─ AGENTS.md  MASTER_PROMPT.md  README.md  CHANGELOG.md  LICENSE  .gitignore
├─ src/main/java/com/revline/
│  ├─ app/            RevLineApp.java  MenuController.java  AppConfig.java  StorageBootstrap.java
│  ├─ ui/             ConsoleTable.java  Banner.java  InputReader.java  MenuPrinter.java
│  ├─ model/          Vehicle.java  Car.java  Sale.java  Reservation.java  AuditEntry.java  FuelType.java  Status.java
│  ├─ service/        InventoryService.java  SearchService.java  FilterBuilder.java  CarComparators.java
│  │                  SalesService.java  ReservationManager.java  WaitlistService.java  UndoManager.java
│  │                  ReportService.java  DataToolsService.java  ComparisonService.java  RecentlyViewed.java
│  ├─ repository/     Repository.java  CarRepository.java  SaleRepository.java
│  │                  FileCarRepository.java  FileSaleRepository.java
│  │                  JdbcCarRepository.java  JdbcSaleRepository.java  DatabaseManager.java  SchemaInitializer.java
│  ├─ concurrency/    AutoSaveThread.java  ReservationExpiryThread.java  PurchaseRequestQueue.java
│  │                  SalesProcessor.java  BuyerTask.java  ThreadStateTracer.java  RaceDemo.java  ConcurrencyLab.java
│  └─ util/           Validator.java  AppLogger.java  FileUtil.java  Pair.java  GenericUtils.java  TokenScriptParser.java
│     └─ exception/   RevLineException.java  InvalidCarException.java  CarNotFoundException.java
│                     CarNotAvailableException.java  RepositoryException.java  DataFormatException.java
├─ src/test/java/com/revline/   TestRunner.java  Assert.java  *Test.java (see PART 14)
├─ data/ seed/cars.csv  seed/sales.csv  legacy/cars.txt  sample/bulk_import.txt  sample/import_bad_rows.csv  (runtime: cars.csv, sales.csv)
├─ lib/  sqlite-jdbc-<ver>.jar  README.md
├─ scripts/  build.bat build.sh run.bat run.sh test.bat test.sh demo.bat demo.sh
├─ sql/  schema.sql  seed.sql  sample_queries.sql
├─ docs/ 00_UNDERSTANDING.md 01_PROBLEM_STATEMENT.md 02_OBJECTIVES_SCOPE.md 03_DESIGN.md 04_CONCEPT_MAP.md
│        05_C_TO_JAVA_REPORT.md DECISIONS.md TOOLING.md PROGRESS.md RUBRIC_TRACEABILITY.md TEAM_OWNERSHIP.md
│        VIVA.md DEMO_SCRIPT.md TEST_PLAN.md diagrams/ evidence/ learning/
└─ logs/ (runtime)   out/ (build output, ignored)   backups → data/backups/ (ignored)
```
**No ANSI colour codes** anywhere — the Windows console and the viva projector may not render them; use plain box-drawing characters only.

## 8.3 Class catalog (signatures the agent must implement — refine, don't shrink)

**model**
- `abstract class Vehicle implements Describable` — `protected final int id; String brand, model; double price; abstract String describe();` constructor chaining, `static int createdCount`, `equals/hashCode` on `id`.
- `class Car extends Vehicle implements Comparable<Car>` — `int topSpeed, horsepower; double acceleration; FuelType fuelType; Status status;` constructors (full / `this(...)` with defaults), `compareTo` by id, `toString()` (via `StringBuilder`), `toCsv()`, `static Car fromCsv(String)` throws `DataFormatException`, `copy()`.
- `enum FuelType {PETROL, HYBRID, ELECTRIC; static FuelType fromString(String) ; String label()}`.
- `enum Status {AVAILABLE, RESERVED, SOLD; boolean canTransitionTo(Status)}`.
- `class Sale` — `int saleId, carId; String buyerName, sellerName; LocalDate saleDate; double price; toCsv/fromCsv`.
- `class Reservation implements Comparable<Reservation>` — `carId, buyerName, LocalDateTime expiresAt`; ordering by expiry (feeds `PriorityQueue`).
- `class AuditEntry` — `LocalDateTime time; String actor, action; String details`.
- `interface Describable { String describe(); }` (L1 interfaces) and `@FunctionalInterface interface CarVisitor { void visit(Car c); }` (L21).

**repository**
```java
public interface Repository<T, ID> {            // L24 generics
    List<T> findAll() throws RepositoryException;
    T findById(ID id) throws RepositoryException;   // returns null when absent; the SERVICE converts null into CarNotFoundException
    void save(T entity) throws RepositoryException;  // insert or update
    boolean deleteById(ID id) throws RepositoryException;
    void saveAll(List<T> all) throws RepositoryException;
    int count() throws RepositoryException;
}
public interface CarRepository extends Repository<Car,Integer> { List<Car> findByStatus(Status s) throws RepositoryException; List<Car> findByPriceRange(double lo,double hi) throws RepositoryException; int nextId() throws RepositoryException; }
public interface SaleRepository extends Repository<Sale,Integer> { List<Sale> findByCarId(int carId) throws RepositoryException; }
```
`DatabaseManager`: `static Connection getConnection()`, loads driver, URL `jdbc:sqlite:data/revline.db`, `PRAGMA foreign_keys=ON`.

**service** (constructor-injected repositories → easy to swap CSV/JDBC)
- `InventoryService(CarRepository repo)` — `add, update, delete, findAll (unmodifiable copy), findById, getBrandsSorted (TreeSet), reload, snapshot()`; maintains `ArrayList<Car> cars` + `HashMap<Integer,Car> index`; every public mutator is `synchronized` or locks `lock`.
- `SearchService` — `byId`, `byText(String)` (EX-3), `filter(Predicate<Car>)`, `FilterBuilder` fluent (`brand()`, `fuel()`, `maxPrice()`, `minSpeed()`, `status()` → `Predicate<Car>` via `and`).
- `CarComparators` — static comparators (price, speed, hp, acceleration, brand+model, valueForMoney (EX-2)); `reversed()` use.
- `SalesService(InventoryService, SaleRepository, ReservationManager, AppLogger)` — `reserve`, `cancelReservation`, `sellCar` (**synchronized**), `salesHistory`, `generateReceipt(Sale)`.
- `ReservationManager` — `PriorityQueue<Reservation>` + `HashMap<Integer,Reservation>`; `purgeExpired()` uses `Iterator.remove()`.
- `WaitlistService` — `Map<Integer, Queue<String>>` (`LinkedList`) per car; `join`, `poll`, `position`.
- `UndoManager` — `Deque<UndoAction>` (`ArrayDeque`), `UndoAction` stores type + Car snapshot; `push`, `undo()` (EX-7).
- `ComparisonService` — `compare(List<Car>)` → 2-D `String[][]` table (L2 multi-dim arrays) + "winner per metric".
- `RecentlyViewed extends LinkedHashMap<Integer,Car>` overriding `removeEldestEntry` (LRU 5) — access-order constructor.
- `ReportService` (§11.6), `DataToolsService` (§11.8).

**util**
- `Validator` (EX-1 inside), `AppLogger` (singleton-ish, `synchronized`, `StringBuffer` recent-lines buffer), `FileUtil.safeWrite(File, List<String>)`, `FileUtil.copyBytes(File, File)`, `Pair<K,V>`, `GenericUtils.maxOf`, `GenericUtils.printAll(List<? extends Vehicle>)`, `TokenScriptParser` (StreamTokenizer).

**exception** — see §11.7 hierarchy.

---
# PART 9 — THE SEVEN CORE MODULES

| # | Module | Packages/classes | Syllabus lectures | Rubric rows hit |
|---|---|---|---|---|
| M1 | **Inventory Management** | `InventoryService`, `Car`, `Vehicle`, `UndoManager`, `RecentlyViewed` | 1, 2, 5, 6–7, 8 | 3, 4 |
| M2 | **Search, Filter, Sort & Compare** | `SearchService`, `FilterBuilder`, `CarComparators`, `ComparisonService` | 2, 5, 8, 21–22, 24 | 3, 4 |
| M3 | **Sales & Reservation** | `SalesService`, `ReservationManager`, `WaitlistService`, `Sale`, `Reservation` | 6–7, 13–16 | 3, 4 |
| M4 | **Repository Persistence** | `Repository<T,ID>`, File*/Jdbc* repos, `DatabaseManager`, `SchemaInitializer` | 17–20, 24–27 | 3, 4 |
| M5 | **Stream Analytics** | `ReportService` | 21–23 | 4 |
| M6 | **Validation, Exceptions & Audit Logging** | `Validator`, `exception.*`, `AppLogger` | 3, 4, 25 | 4, 5 |
| M7 | **Data Tools & Concurrency Lab** | `DataToolsService`, `TokenScriptParser`, `AutoSaveThread`, `ConcurrencyLab` … | 9–16, 25–27 | 4 |

---

# PART 10 — DATA DESIGN

## 10.1 Car (data dictionary)
| Field | Java | CSV col | SQL | Rules |
|---|---|---|---|---|
| id | `int` | 1 | `car_id INTEGER PRIMARY KEY` | unique, ≥ 1, auto = max+1 |
| brand | `String` | 2 | `VARCHAR(50) NOT NULL` | 1–50 chars, letters/digits/space/`-` |
| model | `String` | 3 | `VARCHAR(50) NOT NULL` | 1–50 chars |
| price | `double` | 4 | `DECIMAL(12,2) NOT NULL CHECK(price>0)` | 1 000 … 50 000 000 |
| topSpeed | `int` | 5 | `INTEGER NOT NULL CHECK(top_speed BETWEEN 50 AND 600)` | km/h |
| fuelType | `FuelType` | 6 | `VARCHAR(20) NOT NULL CHECK(fuel_type IN ('PETROL','HYBRID','ELECTRIC'))` | enum |
| horsepower | `int` | 7 | `INTEGER NOT NULL CHECK(horsepower BETWEEN 50 AND 3000)` | |
| acceleration | `double` | 8 | `DECIMAL(4,2) NOT NULL CHECK(acceleration BETWEEN 1 AND 20)` | 0–100 km/h sec |
| status | `Status` | 9 | `VARCHAR(10) NOT NULL CHECK(status IN ('AVAILABLE','RESERVED','SOLD'))` | |

**CSV format** (`data/cars.csv`, header line included): `id,brand,model,price,topSpeed,fuelType,horsepower,acceleration,status`. Reader must also accept the **legacy C format** (9 cols, last col `1`/`0`, `Petrol` mixed-case) via `DataToolsService.importLegacyC()` — never by silently changing the main reader.

## 10.2 Sale & Audit
`Sale(saleId, carId, buyerName, sellerName, saleDate, price)` → CSV `saleId,carId,buyerName,sellerName,saleDate,price`.
`AuditEntry(time, actor, action, details)` → log line `2026-11-19T10:15:30 | SELLER | SELL_CAR | carId=5 buyer=Rohan price=517770.00`.

## 10.3 SQL schema (`sql/schema.sql` — 3 tables, as in PPT; no USERS)
```sql
CREATE TABLE IF NOT EXISTS cars (
  car_id        INTEGER PRIMARY KEY,
  brand         VARCHAR(50)  NOT NULL,
  model         VARCHAR(50)  NOT NULL,
  price         DECIMAL(12,2) NOT NULL CHECK (price > 0),
  top_speed     INTEGER NOT NULL CHECK (top_speed BETWEEN 50 AND 600),
  fuel_type     VARCHAR(20) NOT NULL CHECK (fuel_type IN ('PETROL','HYBRID','ELECTRIC')),
  horsepower    INTEGER NOT NULL CHECK (horsepower BETWEEN 50 AND 3000),
  acceleration  DECIMAL(4,2) NOT NULL CHECK (acceleration BETWEEN 1 AND 20),
  status        VARCHAR(10) NOT NULL CHECK (status IN ('AVAILABLE','RESERVED','SOLD')),
  UNIQUE (brand, model)
);
CREATE TABLE IF NOT EXISTS sales (
  sale_id      INTEGER PRIMARY KEY,
  car_id       INTEGER NOT NULL,
  seller_name  VARCHAR(60) NOT NULL,
  buyer_name   VARCHAR(60) NOT NULL,
  sale_date    DATE NOT NULL,
  price        DECIMAL(12,2) NOT NULL CHECK (price > 0),
  FOREIGN KEY (car_id) REFERENCES cars(car_id)
);
CREATE TABLE IF NOT EXISTS audit_log (
  log_id      INTEGER PRIMARY KEY,
  actor_name  VARCHAR(60) NOT NULL,
  action      VARCHAR(100) NOT NULL,
  created_at  TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_cars_brand  ON cars(brand);
CREATE INDEX IF NOT EXISTS idx_sales_car   ON sales(car_id);
```
Relationships: `cars 1 — N sales`. `audit_log` is standalone. **Document the ER diagram in Mermaid** (`docs/diagrams/er.mmd`) and in `03_DESIGN.md`. SQLite note: it stores DECIMAL with numeric affinity — read prices with `getDouble`; mention in `DECISIONS.md`.

## 10.4 Seed data — `data/seed/cars.csv` (copy exactly; ids 3, 4, 18 are SOLD, as in the C `cars.txt`)
```
id,brand,model,price,topSpeed,fuelType,horsepower,acceleration,status
1,Bugatti,Chiron,3000000.00,420,PETROL,1500,2.40,AVAILABLE
2,Bugatti,Veyron SS,2500000.00,431,PETROL,1200,2.50,AVAILABLE
3,Ferrari,LaFerrari,1400000.00,350,HYBRID,949,2.40,SOLD
4,Ferrari,SF90 Stradale,507000.00,340,HYBRID,986,2.50,SOLD
5,Lamborghini,Aventador SVJ,517770.00,350,PETROL,759,2.80,AVAILABLE
6,Lamborghini,Huracan STO,327838.00,310,PETROL,631,3.00,AVAILABLE
7,Porsche,911 Turbo S,207000.00,330,PETROL,640,2.60,AVAILABLE
8,Porsche,Carrera GT,448000.00,330,PETROL,605,3.50,AVAILABLE
9,McLaren,P1,1150000.00,350,HYBRID,903,2.80,AVAILABLE
10,McLaren,720S,305000.00,341,PETROL,710,2.90,AVAILABLE
11,Aston Martin,Valkyrie,3000000.00,402,HYBRID,1140,2.50,AVAILABLE
12,Tesla,Model S Plaid,135990.00,322,ELECTRIC,1020,1.99,AVAILABLE
13,Koenigsegg,Jesko,3000000.00,480,PETROL,1600,2.50,AVAILABLE
14,Koenigsegg,Agera RS,2500000.00,447,PETROL,1341,2.80,AVAILABLE
15,Pagani,Huayra,1400000.00,383,PETROL,720,2.80,AVAILABLE
16,Pagani,Zonda Cinque,2000000.00,350,PETROL,669,3.40,AVAILABLE
17,BMW,M5 CS,142000.00,305,PETROL,627,2.90,AVAILABLE
18,Audi,R8 V10 Plus,197000.00,330,PETROL,602,3.20,SOLD
```
**Acceptance check:** the agent must verify its seed file against `/reference/c/cars.txt` programmatically (a test: `SeedConsistencyTest`) — 18 rows, same ids/brands/models/prices/speeds/hp/accel; status = (flag==1 ? AVAILABLE : SOLD).

`data/seed/sales.csv`: three rows for cars 3, 4, 18 → buyer `Legacy C Import`, seller `RevLine Showroom`, price = car price, date = a fixed historical date `2026-01-01` (clearly labelled as *migrated legacy data*, not fabricated activity).

## 10.5 Collection choice table (put it in `03_DESIGN.md`, mirrors PPT slide 5 + extras)
| Need | Collection | Why |
|---|---|---|
| Ordered inventory | `ArrayList<Car>` | index iteration, sort copies |
| Find by id | `HashMap<Integer,Car>` | O(1) |
| Unique brands, sorted | `TreeSet<String>` | natural order |
| Unique brands, insertion order | `LinkedHashSet<String>` | recent-brand chips in UI |
| Duplicate guard (brand+model key) | `HashSet<String>` | O(1) contains |
| Sales history | `LinkedList<Sale>` / `ArrayList<Sale>` | append + iterate |
| Waitlist per car | `Queue<String>` (`LinkedList`) | FIFO |
| Undo stack | `Deque<UndoAction>` (`ArrayDeque`) | LIFO |
| Reservation expiry | `PriorityQueue<Reservation>` | earliest-expiry first |
| Recently viewed | `LinkedHashMap` (access-order) | LRU |
| Price ladder | `TreeMap<Double,List<Car>>` | floor/ceiling/sub-map queries (`floorKey`, `headMap`) |
| Fuel counts | `EnumMap<FuelType,Integer>` | enum-keyed (PPT) |
| Browse next/prev | `ListIterator<Car>` | bidirectional |

---

# PART 11 — DETAILED MODULE SPECIFICATIONS

For **each** module deliver: code · tests · `docs/learning/<module>_EXPLAINED.md` · rubric/concept-map rows.

## 11.1 M1 — Inventory Management
**Behaviour**
- `add(Car)`: validate → reject duplicate `brand+model` (`HashSet<String>` key `brand|model` lower-cased) → assign `nextId()` → add to list + index → persist → push `UndoAction(ADD)` → audit.
- `update(int id, Car changes)`: only AVAILABLE cars editable (RESERVED/SOLD → `CarNotAvailableException`); validate; persist; audit; undo snapshot.
- `delete(int id)`: only AVAILABLE cars with **no sales**; confirm in UI; persist; undo snapshot.
- `findAll()` returns `Collections.unmodifiableList(new ArrayList<>(cars))` (defensive copy — explain in viva).
- `getBrands()` → `TreeSet<String>`.
- Browse mode: `ListIterator<Car>` with Next/Previous/Quit; each viewed car goes to `RecentlyViewed`.
- Demonstrate `Collections` utilities in a "Showroom Stats" screen: `max/min` by comparator, `frequency` of fuel types, `binarySearch` on a list **sorted by id**, `swap` demo is NOT in production code (put in `GenericUtils` demo only).
**Edge cases:** empty inventory; id not found; add with same brand/model different case; update to a duplicate brand/model; delete a car in waitlist; undo when stack empty; extremely long strings; non-ASCII.
**Acceptance:** after restart (both CSV and JDBC), data identical; undo restores exact prior state incl. id.

## 11.2 M2 — Search, Filter, Sort, Compare
- **Search** (`byText`): split query on whitespace → every token must appear (case-insensitive) in `brand + " " + model + " " + fuelType` (L2: `toLowerCase`, `contains`, `StringBuilder`). `byId`.
- **FilterBuilder** composes `Predicate<Car>`: brand (equalsIgnoreCase), fuel, maxPrice, minPrice, minSpeed, minHp, maxAcceleration, status; combine with `and`; support `negate` for "not sold". Provide **`@FunctionalInterface CarVisitor`** for `forEach` style printing and a custom functional interface `Scorer { double score(Car c); }` used by value-for-money ranking.
- **Sort**: comparator library; UI offers 8 options (price ↑/↓, speed ↓, hp ↓, accel ↑, brand A–Z, value-for-money, id). Always sort a **copy**: `List<Car> view = new ArrayList<>(cars); view.sort(cmp);` Demonstrate `Comparable` (natural = id) vs `Comparator` (custom) in viva doc.
- **Compare**: user picks 2–3 ids → `ComparisonService.compare()` builds `String[][] grid` (rows = metrics, cols = cars), marks the best per metric (highest speed/hp, lowest price/accel) using `Collections.max/min`.
- **Price ladder** (`TreeMap<Double,List<Car>>`): "cars within ±15 % of a budget" via `subMap(lo,true,hi,true)`; `floorKey/ceilingKey` for "closest cheaper / closest pricier".
- **Generics** used: `GenericUtils.<T extends Comparable<T>>maxOf`, `Pair<Car,Double>` for ranked results, `List<? extends Vehicle>` printer.
**Edge cases:** no matches (friendly message), tokens with regex characters (treated literally — no regex used), empty query, budget < lowest price.

## 11.3 M3 — Sales & Reservation (state machine + concurrency)
**State machine:** `AVAILABLE → RESERVED → SOLD`; `RESERVED → AVAILABLE` (cancel/expiry); `AVAILABLE → SOLD` (direct buy); `SOLD` is terminal. Illegal transitions throw `CarNotAvailableException` with a precise message.
**Reserve:** `reserve(carId, buyer, minutes)` (default 2 min for demo; configurable): checks AVAILABLE under lock, sets RESERVED, enqueues `Reservation` in `PriorityQueue`, audit.
**Sell:** 
```java
public Sale sellCar(int carId, String buyer, String seller) throws ... {   // synchronized  (L13–14)
    // 1 validate  2 find car  3 if RESERVED, buyer must match the reservation holder
    // 4 verify AVAILABLE/RESERVED-by-this-buyer  5 mutate status -> SOLD
    // 6 create Sale (next saleId)  7 persist car + sale (JDBC: one transaction; CSV: both files, rollback in memory on failure)
    // 8 remove reservation/waitlist head notification  9 audit
}
```
**Atomicity rule:** if persistence fails after status mutation → **revert in-memory status** and throw `RepositoryException` (no half-sales).
**Receipt:** the C-style "R E V L I N E   M O T O R S" box, now with Sale ID, buyer, seller, date, price.
**Waitlist:** buyer can `join` a SOLD/RESERVED car queue; when a reservation expires/cancels, the head of the queue is *notified* (console message) and gets first right to reserve for 2 minutes (keep simple: message + priority flag; no extra thread).
**Expiry:** `ReservationExpiryThread` (daemon) wakes every N seconds (`sleep`), calls `purgeExpired()` which iterates with `Iterator.remove()`, flips cars back to AVAILABLE, logs.
**Edge cases:** two buyers same car; reserve then buy by someone else; buy after expiry; cancel by non-holder; sell unknown id; sell with blank buyer; reservation of SOLD car.

## 11.4 M4a — File repositories & crash-safe saving
- `FileCarRepository(File file)` — read via `BufferedReader(new FileReader(file))`; skip header; per-line `try { Car.fromCsv(line) } catch (DataFormatException e) { badLines.add(...) }`; expose `getLastLoadReport()` (line number + reason). Strings containing commas → **reject at validation** (CSV is not quoted) — documented rule: `,` and newline forbidden in brand/model.
- **Safe write algorithm** (`FileUtil.safeWrite`): 1) write full content to `<name>.tmp` with `BufferedWriter(new FileWriter(tmp))` and `flush()`; 2) if the target exists, rename it to `<name>.bak`; 3) `tmp.renameTo(target)`; 4) if step 3 fails, restore `.bak` and throw `RepositoryException`; 5) delete `.bak` only after success. `finally` closes writers. Test with a simulated failure (read-only target / injected exception) — **O5 evidence**.
- `FileSaleRepository` same pattern. Both keep an in-memory `List` + load once, `saveAll` on mutation (simple and explainable).
- Uses **character streams** here; byte streams are used by backup (§11.8). This split is intentional (L25).

## 11.5 M4b — JDBC repositories (L17–20)
- `DatabaseManager`: `Class.forName("org.sqlite.JDBC")` (explain: driver registration), URL from `AppConfig`, `DriverManager.getConnection(url)`; `PRAGMA foreign_keys = ON`.
- `SchemaInitializer.init(Connection)`: executes `sql/schema.sql` via `Statement.execute` per statement; seeds from `data/seed/cars.csv` **only if table empty** (`SELECT COUNT(*)`).
- `JdbcCarRepository`: CRUD with **PreparedStatement only**:
  `INSERT INTO cars(...) VALUES (?,?,?,?,?,?,?,?,?)`, `UPDATE ... WHERE car_id=?`, `DELETE ... WHERE car_id=?`, `SELECT ... WHERE status=?`, `... WHERE price BETWEEN ? AND ?` (EX-6), `SELECT COALESCE(MAX(car_id),0)+1`.
  `mapRow(ResultSet rs)` → `Car` using typed getters (`getInt/getString/getDouble`) and `FuelType.valueOf`. try-with-resources for `Connection/PreparedStatement/ResultSet`.
- **Transactions:** `SalesService.sellCar` in JDBC mode calls `JdbcSaleRepository.recordSale(Connection, Sale, Car)` that does `setAutoCommit(false)` → update car → insert sale → `commit()`; on any `SQLException` → `rollback()` in `catch`, `setAutoCommit(true)` in `finally`. Expose through a `SaleTransaction` interface so CSV mode provides a compatible implementation.
- Map `SQLException` → `RepositoryException` (keep cause; message friendly; log full detail).
- **SQL-injection demo** (viva gem): a test that tries brand `x'; DROP TABLE cars;--` and shows the table survives because of parameter binding.
- **Optional MySQL** doc: swap URL/driver; no code change outside `AppConfig`.

## 11.6 M5 — Stream analytics (`ReportService`) — implement ALL, each with a labelled console output
| # | Report | Pipeline (sketch) |
|---|---|---|
| R1 | Available cars | `filter(c -> c.getStatus()==AVAILABLE)` |
| R2 | **Stock value (AVAILABLE only)** | `filter(AVAILABLE).mapToDouble(Car::getPrice).sum()` |
| R3 | Portfolio value (all cars) | `mapToDouble(Car::getPrice).sum()` |
| R4 | Cars per fuel type | `collect(groupingBy(Car::getFuelType, counting()))` → also fill an `EnumMap` |
| R5 | Top-3 fastest | `sorted(comparingInt(Car::getTopSpeed).reversed()).limit(3)` |
| R6 | Top-N by horsepower (**EX-4**) | `filter → sorted → limit(n) → map(describe)` |
| R7 | Average price by brand (**EX-5**) | `groupingBy(Car::getBrand, averagingDouble(Car::getPrice))` |
| R8 | Distinct brands sorted | `map(Car::getBrand).distinct().sorted()` |
| R9 | Pagination of inventory | `sorted(by id).skip((page-1)*size).limit(size)` |
| R10 | Total revenue | `sales.stream().mapToDouble(Sale::getPrice).sum()` |
| R11 | Revenue by buyer / by month | `groupingBy` + `summingDouble` |
| R12 | Fastest 0–100 per fuel type | `groupingBy(fuel, minBy(comparingDouble(Car::getAcceleration)))` (explain `minBy`) |
| R13 | Price band matrix (2-D array `int[fuel][band]`) | stream counts → fill `int[][]` (L2 multi-dim arrays) |
| R14 | Names list | `map(c -> c.getBrand()+" "+c.getModel()).collect(joining(", "))` |
| R15 | Laziness demo | `peek` with counter to show nothing runs until a terminal op; then show short-circuit with `limit` |
Also demonstrate **method references of all 4 kinds** once each (static, bound instance `System.out::println`, unbound `Car::getPrice`, constructor `ArrayList::new`) in `ReportService` or a `LambdaShowcase` class.

## 11.7 M6 — Validation, exception hierarchy, audit logging
**Exception tree**
```
Exception
 └─ RevLineException                    (checked, base)
     ├─ RepositoryException             (checked; wraps IOException/SQLException)
     ├─ DataFormatException             (checked; bad CSV/script line, has lineNumber)
     ├─ CarNotFoundException            (checked)
     └─ CarNotAvailableException        (checked)
RuntimeException
 └─ InvalidCarException                 (UNCHECKED; thrown by constructors/validators on illegal values)
```
Show **both** checked and unchecked, `throw` vs `throws`, **multi-catch** (`catch (IOException | NumberFormatException e)`) in the importer, **finally** for resource closing, and **propagation** from repository → service → UI where the UI is the single place that converts exceptions into friendly messages.
**Validator:** wrappers & parsing (`Integer.parseInt`, `Double.parseDouble`, `NumberFormatException` handling), range checks, string rules, `isValidFuelType` (EX-1), `parsePositiveDouble` (EX-1), `requireNonBlank`.
**AppLogger:** writes `logs/audit.log` via `FileWriter(file, true)`; `synchronized log(...)`; keeps last 20 lines in a `StringBuffer` for the "recent activity" panel (explain StringBuffer vs StringBuilder); JDBC mode also inserts into `audit_log` via `PreparedStatement`. Logs: actor (SELLER/BUYER/SYSTEM), action, details, timestamp. Stack traces only to `logs/error.log`.

## 11.8 M7a — Data Tools (File handling, L25–27)
| Tool | Implementation | Syllabus |
|---|---|---|
| **Legacy C import** | read `data/legacy/cars.txt` (9 comma fields, `Petrol/Hybrid/Electric`, last flag) → `Car` list → report counts (imported/skipped) → merge by brand+model, never overwrite silently | L26–27 |
| **CSV import** | `BufferedReader`; per-line validation; **error report file** `data/import_errors_<ts>.txt`; multi-catch; summary "17 ok, 2 rejected" | L26–27, L3 |
| **Script import (`StreamTokenizer`)** | file with lines like `ADD "Ferrari" "812 Superfast" 340000 340 PETROL 789 2.9` and `SELL 5 "Rohan Mehta"`, `RESERVE 7 "Anika"`; `StreamTokenizer` configured with `quoteChar('"')`, `eolIsSignificant(true)`, `commentChar('#')`; tokens → commands → services. Document `ttype`, `sval`, `nval`. | L25 |
| **Export** | cars → CSV; full text report (all R-reports) → `data/exports/report_<ts>.txt` via `FileWriter` | L25 |
| **Backup/Restore** | `FileInputStream`/`FileOutputStream` copy with `byte[] buf = new byte[4096]` loop → `data/backups/cars_<ts>.bak`; list backups; restore with confirmation (`finally` closes both streams) | L25 (byte streams) |
| **Storage switch / migrate** | `DataToolsService.migrate(CarRepository from, CarRepository to)` copies all cars+sales; verify counts equal | L24 generics, L17–20 |
Sample files: `data/sample/bulk_import.txt` (≥ 12 commands incl. comments, a bad line), `data/sample/import_bad_rows.csv` (≥ 5 good, 4 bad of different kinds).

## 11.9 M7b — Concurrency design & Concurrency Lab (L9–16)
**Threads in production**
| Thread | Type | Behaviour |
|---|---|---|
| `AutoSaveThread extends Thread` | **daemon**, low priority | every 30 s `sleep(30_000)` → `inventory.saveSnapshot()` inside `synchronized(inventory)`; loop `while (running)` where `running` is `volatile boolean`; `interrupt()` → exits cleanly; `join()` on shutdown |
| `ReservationExpiryThread implements Runnable` | daemon | every 5 s purge expired reservations |
| main thread | UI | — |
**Critical sections:** `SalesService.sellCar/reserve/cancel` and `InventoryService` mutators synchronize on the same monitor object (document *why one lock*; avoid deadlock by never calling out to unknown code while holding the lock, and never taking two locks).
**Concurrency Lab (menu 7) — 5 deterministic demos with printed explanations**
1. **Race condition proof:** `UnsafeCounter` vs `SafeCounter` — 4 threads × 10 000 increments; unsafe usually ≠ 40 000, safe = 40 000 (`synchronized` method). Run each 3×, print results. Explain *read-modify-write*, *atomicity*.
2. **Double-sell showdown:** 20 `BuyerTask implements Runnable` threads all try `sellCar(carId)` on the same car, through (a) a deliberately **unsynchronised** demo service copy and (b) the real synchronised service → show "sold to N buyers" vs "exactly 1 winner, 19 `CarNotAvailableException`". Use `join()` to wait. (O3 evidence.)
3. **Thread lifecycle tracer:** `ThreadStateTracer` polls `getState()` of a worker through NEW → RUNNABLE → TIMED_WAITING (`sleep`) → BLOCKED (waiting for a monitor held by main) → WAITING (`wait()`) → TERMINATED, printing a timeline. Also **daemon vs user thread** and **priority** (`setPriority`) demo with the honest note "priority is only a hint".
4. **Producer–consumer:** `PurchaseRequestQueue` bounded (capacity 5) using `synchronized` + `wait()`/`notifyAll()` in **guarded blocks** (`while (queue.size()==cap) wait();`). 3 buyer producers submit 8 requests; one `SalesProcessor` consumer thread processes them via `SalesService`. Show interleaved trace. (EX-8 = `take()`.)
5. **Interrupt & graceful shutdown:** start AutoSave, `interrupt()`, show it exiting; join with timeout.
Also show **visibility**: a flag **without** `volatile` may never stop a spinning thread — present as a documented *awareness* demo (don't rely on it finishing; guard with a max-iteration safety exit and say so).
**Tracing:** each worker prints `Thread.currentThread().getName()`; names set via `new Thread(r, "Buyer-3")`.
**Do not** use `ExecutorService`, `java.util.concurrent.*`, `Thread.stop()`, `Thread.suspend()`.

---
# PART 12 — CONSOLE UI, CONFIG, SCRIPTS

## 12.1 Look & feel (preserve the C identity)
- Box-drawing frames (`╔ ═ ╗ ║ ╠ ╣ ╚ ╝ ╦ ╬ ╩`), fixed 70-col frame for menus, auto-width tables (compute column widths from data with `StringBuilder`; don't hard-code like the C version).
- Boot panel **"SYSTEM BOOT: STORAGE & DATABASE SETUP"** lines: storage mode, file/DB found?, seeded?, records loaded, autosave thread started, log file ready.
- ASCII "RevLine" logo + tagline.
- **UTF-8 on Windows:** `System.setOut(new PrintStream(System.out, true, "UTF-8"))`; document `chcp 65001` in README as a fallback; provide a plain-ASCII fallback flag `--ascii` (config) that swaps box characters for `+ - |`.
- Money format `$%,.2f`; table price column `%,.0f`.

## 12.2 Menu tree
```
╔ MAIN MENU ═══════════════════════╗         [S] switch view: SELLER ⇄ BUYER (no login)
 SELLER VIEW                                   BUYER VIEW
 [1] Inventory  → add / update / delete /      [1] Browse showroom (ListIterator next/prev)
                  view / browse / undo         [2] Search & Filter
 [2] Search & Filter                           [3] Sort & Compare
 [3] Sort & Compare                            [4] Reserve a car
 [4] Sales      → sell / reserve / cancel /    [5] Buy a car
                  history / waitlist           [6] Join waitlist
 [5] Reports (R1–R15)                          [7] My recently viewed
 [6] Data Tools → import / export / backup /   [0] Exit
                  restore / migrate / switch storage
 [7] Concurrency Lab
 [8] Audit log & recent activity
 [0] Exit (graceful: stop threads, final save, close DB)
```
Every submenu ends with `[0] Back`. Invalid input → one-line error + re-prompt (never crash).

## 12.3 Input handling
`InputReader` wraps `Scanner(System.in)`: `readInt(prompt,min,max)`, `readDouble`, `readLine`, `readChoice`, `confirm(y/N)`. Always read **lines** and parse with wrapper methods (`Integer.parseInt`) in try/catch → never mix `nextInt()` + `nextLine()`. EOF-safe (treat closed stdin as exit) so scripted demos (`demo.bat < demo_input.txt`) work.

## 12.4 Config (`AppConfig`)
Reads optional `config.properties` with `java.util.Properties` **or** a plain key=value parser built on `BufferedReader` (prefer the latter to stay in-syllabus): `storage=CSV|JDBC`, `db.url`, `autosave.seconds`, `reservation.minutes`, `ui.ascii`, `data.dir`. Defaults hard-coded; invalid values fall back + warn.

## 12.5 Scripts (Windows + Unix)
- `scripts/build.bat`: `javac -Xlint:all -encoding UTF-8 -d out -cp lib\* -sourcepath src\main\java <all main files>`
- `scripts/run.bat`: `java -cp "out;lib\*" com.revline.app.RevLineApp`
- `scripts/test.bat`: compile tests → `java -cp "out;out-test;lib\*" com.revline.TestRunner` (exit code ≠ 0 on failure)
- `scripts/demo.bat`: feeds `docs/evidence/demo_input.txt` to the app and tees output to `docs/evidence/demo_output.txt`
- `.sh` equivalents (`:` classpath separator). 
- Must work from a fresh clone with only JDK 17 installed.
- IntelliJ users: README paragraph "mark `src/main/java` as Sources Root, `src/test/java` as Test Sources Root, add `lib/*.jar` as library".

---

# PART 13 — DOCUMENTATION DELIVERABLES (rubric rows 1, 2, 3, 5)

| File | Content (must be real, not boilerplate) |
|---|---|
| `README.md` | Title+tagline · 1-paragraph problem · features · screenshots (real) · tech/syllabus badges · **quick start (clone→build→run→test, Windows & Unix)** · menu guide · project structure · **status table (Done / In progress / Pending)** · team & contributions · limitations · roadmap · licence |
| `docs/01_PROBLEM_STATEMENT.md` | §6.1, student-rewritten |
| `docs/02_OBJECTIVES_SCOPE.md` | §6.3–6.6 |
| `docs/03_DESIGN.md` | package diagram · class diagram (Mermaid) · **sequence diagrams**: *Sell a car*, *Add car*, *Producer-consumer* · ER diagram · data dictionary · collection-choice table · state machine diagram · flowchart of main loop · design decisions & trade-offs |
| `docs/04_CONCEPT_MAP.md` | Table of every lecture 1–30 → `File.java#Class.method` (verified by grep) |
| `docs/05_C_TO_JAVA_REPORT.md` | §7 mapping + defects fixed + before/after behaviour table |
| `docs/DECISIONS.md` | ADR-style entries (context, decision, consequence): SQLite default; CSV+JDBC dual; no ConcurrentHashMap; temp-file saves; one lock; no auth; etc. |
| `docs/TOOLING.md` | MCP servers/skills installed with pinned versions + vetting notes |
| `docs/PROGRESS.md` | **Milestone evidence**: per phase → date, commits (hash), tests passing (count), screenshots/log paths, what's pending. Updated at every gate from real data (`git log --oneline`). |
| `docs/RUBRIC_TRACEABILITY.md` | §2 table with file links and commit hashes |
| `docs/TEST_PLAN.md` | test-case table (ID, input, expected, actual, pass) incl. boundary & negative cases |
| `docs/VIVA.md` | 80+ questions grouped by lecture with crisp answers + "show me in code" pointers |
| `docs/DEMO_SCRIPT.md` | 10-minute demo flow with exact keystrokes, what to say, fallback plan if DB fails (switch to CSV) |
| `docs/TEAM_OWNERSHIP.md` | §5.5 |
| `docs/learning/*.md` | per-module explainers + `EX_LOG.md` |
| `docs/evidence/` | real test/demo logs, screenshots (student captures), `git log` export |
| `CHANGELOG.md` | Keep-a-Changelog format per tag |
Diagrams: write as **Mermaid** (`.mmd` + embedded in markdown) so GitHub renders them; also commit PNG exports if tools are available.

---

# PART 14 — TESTING STRATEGY (no JUnit; in-house runner)

## 14.1 Runner
`Assert` (`assertEquals`, `assertTrue`, `assertThrows(Class, Runnable-like functional interface)`, `assertNear(double)`), `TestRunner` discovers tests via an explicit list (no reflection): each `*Test` exposes `void runAll(TestContext ctx)`; prints a table, count, duration; exit code 1 on any failure. Tests use **temp directories** (`File.createTempFile`) and an in-memory/`:memory:` or temp SQLite file — never touch real `data/`.

## 14.2 Minimum 60 checks (target 90) across:
| Area | Examples |
|---|---|
| Model | `Car` ctor validation, `fromCsv/toCsv` round-trip, `compareTo`, equals/hashCode, `FuelType.fromString("petrol")`, `Status` transitions |
| Validator | valid/invalid numbers, boundary (`price=0`, `topSpeed=600/601`), comma in brand, blank buyer, EX-1 |
| Inventory | add/update/delete rules, duplicate guard, id generation, undo for add/delete/update, defensive copy immutability |
| Search/Filter/Sort | case-insensitive, multi-token, no-match, composed predicates, each comparator asc/desc, EX-2/EX-3 |
| Sales | state machine, receipt content, reserve→buy by holder only, expiry, waitlist FIFO, rollback on injected persistence failure |
| File repo | load seed, bad-line report, safe-write crash simulation, `\r\n` tolerance, empty file, header only |
| JDBC repo | schema init idempotent, CRUD, price range (EX-6), FK violation, CHECK violation, **SQL-injection attempt harmless**, transaction rollback |
| Reports | each R1–R15 against hand-computed expected values from the 18 seeded cars (e.g. stock value of AVAILABLE cars computed independently in the test with a plain loop) |
| Data tools | legacy import count (18), CSV import with 4 bad rows, `StreamTokenizer` script parse (quotes, comments, numbers), backup→restore byte-identical (compare checksums via simple loop), migrate CSV→JDBC counts equal |
| Concurrency | race demo: safe counter == expected; double-sell: exactly 1 winner over 20 threads (repeat 20 rounds); producer–consumer processes all N requests; AutoSave stops after `interrupt()` within 2 s; no deadlock (global test timeout watchdog thread) |
| Seed | `SeedConsistencyTest` vs `/reference/c/cars.txt` |
| Student exercises | EX-1…EX-8 tests **fail until the student implements them** → TestRunner prints them in a separate section "STUDENT EXERCISES (n/8 complete)" so the rest of the suite is green and exercises are visibly pending/complete. |

## 14.3 Manual QA checklist (`docs/TEST_PLAN.md`)
Fresh start (no files) · restart persistence · invalid menu inputs (letters, empty, negative, huge numbers) · Ctrl+Z/EOF handling · both storage modes · disk-full / read-only file simulation · 100-car and 1 000-car synthetic load (generator in `DataToolsService`, labelled as synthetic).

## 14.4 Evidence
Every gate stores real output under `docs/evidence/` with date stamps. **Never fabricate** numbers; if a measurement isn't taken, don't claim it (O4 requires an actual timing run — implement `PerformanceCheck` in the test suite and report the real value).

---

# PART 15 — CODE QUALITY STANDARDS (rubric row 5)

1. Packages `com.revline.<layer>`; classes `PascalCase`, methods/fields `camelCase`, constants `UPPER_SNAKE`; no abbreviations like `tmp1`; booleans start with `is/has/can`.
2. One responsibility per class; **class ≤ 300 lines, method ≤ 25 lines, ≤ 4 params** (use a small builder or object otherwise).
3. A one-line Javadoc on every class and on any method whose purpose isn't obvious from its name; `@throws` only where it helps. Inline comments explain *why*, in plain simple English, and only where needed.
4. No magic numbers/strings → constants (`MAX_PRICE`, `DEFAULT_RESERVATION_MINUTES`).
5. No `System.out.println` outside `ui` (services return data or throw); no `printStackTrace()`.
6. No empty `catch`. Never swallow exceptions: log or rethrow with cause.
7. Immutability where possible (`final` fields, defensive copies, unmodifiable views).
8. Resources: try-with-resources (JDBC) or `finally` (streams, to show L3) — never leak.
9. Equality: `equals/hashCode` consistent; `compareTo` consistent with `equals` or documented.
10. Formatting: 4 spaces, 120-col max, one top-level class per file, imports explicit (no `*`), no unused imports/variables, no dead code, no commented-out code.
11. Compile clean with `-Xlint:all`; all tests green before any commit to `main`.
12. Deterministic demos: seed any randomness (`new Random(42)`) so evidence is reproducible. (`Random` is in `java.util`; allowed.)
13. Naming consistency with the PPT & C project (`Car`, `FuelType`, `Status`, `InventoryService`, `SalesService`, `ReportService`, `AppLogger`, `Validator`, `InvalidCarException`).

---

# PART 16 — PHASED EXECUTION PLAN WITH GATES

> Each phase: **(a)** create/continue GitHub issues & branch · **(b)** implement · **(c)** tests + `/phase-gate` · **(d)** docs/evidence · **(e)** GATE report → wait for approval. Commit continuously (small commits); the *student* pushes.

### Phase −1 — Environment & tooling (≈ 0.5 day)
JDK/Git checks, Antigravity rule/workflow files, `AGENTS.md`, optional MCP/skill vetting (§4.4), GitHub repo + milestones + issues, `.gitignore`, `LICENSE`, empty package skeleton that compiles, `scripts/*`, README skeleton.
**GATE −1:** `build` passes on empty skeleton; `docs/TOOLING.md` done; repo exists with first honest commit.

### Phase 0 — Understanding & planning (rubric rows 1, 2)
`docs/00_UNDERSTANDING.md`, outline drafts of `01_PROBLEM_STATEMENT.md` and `02_OBJECTIVES_SCOPE.md`, `DECISIONS.md` seed, `RUBRIC_TRACEABILITY.md` skeleton, `TEAM_OWNERSHIP.md`.
**GATE 0:** student **rewrites problem statement & objectives in his own words** and commits them. Agent reviews for rubric fit (clear, measurable, feasible, bounded).

### Phase 1 — Design (rubric row 3 design half)
Class/ER/sequence/state diagrams, data dictionary, collection-choice table, package skeleton with interfaces and exception classes (compiling), SQL schema + seed files, seed CSV with `SeedConsistencyTest`.
**GATE 1:** diagrams render on GitHub; student explains the architecture back in 5 questions.

### Phase 2 — Initial module / working prototype → tag `v0.1-prototype` (rubric 3 & 4)
Model + Validator + exceptions + `FileCarRepository` + `InventoryService` (add/list/find) + console boot + banner + table + main menu (Inventory submenu, Search by id/text) + `AppLogger` + tests + **EX-1 and EX-3 assigned**.
**GATE 2:** runs from fresh clone; 25+ tests green; demo log saved; PROGRESS.md updated.

### Phase 3 — Core features + CA-3 package → tag `v0.3-ca3`
Update/delete/undo, FilterBuilder, comparators, compare, recently viewed, `FileSaleRepository`, SalesService (reserve/sell/receipt, state machine), waitlist, audit viewer, safe-write, Seller/Buyer views. **EX-2, EX-7 assigned.** Complete docs for rows 1–5 as they stand; `docs/CA3_SUBMISSION_CHECKLIST.md` (what to show: repo link, README status, demo script, evidence).
**GATE 3 (CA-3 readiness):** printable checklist green: every rubric row has evidence; student rehearses the demo once, timed ≤ 8 min.

### Phase 4 — JDBC persistence (L17–20)
`DatabaseManager`, schema init, `JdbcCarRepository`, `JdbcSaleRepository`, transaction in sell flow, storage switch + migration, SQL-injection demo test. **EX-6 assigned.**
**GATE 4:** same test suite passes in CSV **and** JDBC modes (parametrise tests over repo factory).

### Phase 5 — Streams, lambdas, generics (L21–24)
`ReportService` R1–R15, `LambdaShowcase`, `GenericUtils`, `Pair`, price ladder (`TreeMap`), 2-D matrix report, reports menu + text export. **EX-4, EX-5 assigned.**
**GATE 5:** report outputs verified against independently hand-computed expectations.

### Phase 6 — Concurrency (L9–16)
`AutoSaveThread`, `ReservationExpiryThread`, `PurchaseRequestQueue`, `SalesProcessor`, Concurrency Lab 5 demos, stress tests. **EX-8 assigned.**
**GATE 6:** 20 consecutive stress rounds pass; no deadlock; demo output saved.

### Phase 7 — File handling & data tools (L25–27)
Legacy C import, CSV import + error report, `StreamTokenizer` script import, export, byte-stream backup/restore, synthetic data generator.
**GATE 7:** import→export→import round trip is lossless; backup restore byte-identical.

### Phase 8 — Hardening, docs & viva prep → tag `v1.0-final` (CA-4)
Refactor duplication, `-Xlint` clean, class-size audit, input fuzz pass, full docs (PART 13), `VIVA.md`, `DEMO_SCRIPT.md`, concept map verification, README final, screenshots (student), `CHANGELOG.md`, release notes, final traceability table. Mock viva: agent asks 20 random questions; student answers; log in `docs/learning/MOCK_VIVA.md`.
**GATE 8:** final self-audit (PART 17) all green.

### Phase 9 — Optional stretch (only after v1.0, non-graded, clearly separated branch `stretch/*`)
Optional `pom.xml` build, JUnit port of the in-house tests, MySQL profile, simple GitHub Actions workflow that runs `build+test`, UML PNG exports. Never merge anything that violates PART 3 into `main`.

### Pacing note
Spread phases across **real calendar days**; instruct the student to commit at least every working session. If he is behind schedule relative to the evaluation date, compress Phase 7–9 first, never Phases 0–3.

---

# PART 17 — FINAL SELF-AUDIT & REPORTING FORMAT

## 17.1 Self-audit (agent must tick each with evidence; any ✗ blocks release)
- [ ] Fresh clone → `scripts/build` → `scripts/test` → `scripts/run` works on Windows.
- [ ] 60+ checks pass; EX-1…EX-8 status honestly reported (student-owned).
- [ ] Zero `-Xlint` warnings; no unused imports; no class > 300 lines.
- [ ] Every lecture 1–30 has ≥ 1 verified usage in `04_CONCEPT_MAP.md`.
- [ ] No forbidden tech (grep for `java.util.concurrent`, `Executor`, `record `, `var `, `Lombok`, `Spring`, `junit` in `src/main`).
- [ ] All SQL uses `PreparedStatement` for dynamic values (grep for `"SELECT ... " +` concatenation).
- [ ] CSV and JDBC modes produce identical reports on identical data.
- [ ] 18 seeded cars match the C `cars.txt` (ids 3, 4, 18 SOLD).
- [ ] 0 duplicate sales across 20 stress rounds.
- [ ] README, PROGRESS, RUBRIC_TRACEABILITY, TEST_PLAN, VIVA, DEMO_SCRIPT complete and consistent with code.
- [ ] Git history: ≥ 60 meaningful commits over real dates, tags present, issues/milestones closed.
- [ ] No secrets, tokens, `.db`, `.class`, or logs committed.
- [ ] PART 15A style met (simple, human-readable) and PART 18 passes A–E completed; `docs/BUG_LOG.md` is up to date.
- [ ] Student wrote EX-1…EX-8 and the problem statement/objectives personally.

## 17.2 What you report at every gate (strict template)
```
GATE <n> REPORT
1. Done this phase (bullets, with file paths)
2. Build: PASS/FAIL (-Xlint warnings: n)
3. Tests: <passed>/<total>  (student exercises: <k>/8)
4. Rubric rows advanced: [1][2][3][4][5] with evidence links
5. Syllabus lectures newly covered: ...
6. Decisions taken (logged in DECISIONS.md): ...
7. Bugs found & fixed this phase (BUG_LOG ids) + regression tests added: ...
7b. Risks / blockers / things I could NOT verify: ...
8. Student TODOs before next phase: ...
9. Suggested commit list (not executed): ...
10. Viva quiz (5 questions) — waiting for answers
```

## 17.3 Things you must never do
Skip a gate · mark a task done without running it · invent evidence · fill student exercises unasked · install unvetted plugins/skills · add out-of-syllabus tech to `main` · commit secrets · rewrite or backdate Git history · hide a limitation (document it instead; honest limitations score better than hidden ones).

---

# PART 15A — SIMPLE & HUMAN CODE STYLE (read before writing any class)

The code must look like it was written by a **careful, good student who understands every line** — not like generated enterprise boilerplate. Simple code is also what the rubric rewards ("organized and readable", "meaningful naming") and what makes the viva easy.

**Do**
1. Pick the **plainest** solution that satisfies the syllabus topic. A `for` loop or a simple `if` beats a clever one-liner. Use a Stream only where the syllabus topic (L21–23) is being demonstrated or where it is clearly clearer.
2. Short methods with one job, named like a sentence: `isCarAvailable`, `calculateTotalValue`, `askForBuyerName`.
3. Variable names a human would say out loud: `availableCars`, `buyerName`, `totalRevenue` — never `x1`, `tmp`, `res2`, `obj`.
4. Comments in simple everyday English, first person plural is fine ("we lock here so two buyers can't get the same car"). Maximum one short comment per tricky block. No comment that merely repeats the code.
5. Consistent, boring structure in every class: fields → constructors → public methods → private helpers.
6. Small, friendly user messages: `"Sorry, that car is already sold."` not `"ERR_STATE_TRANSITION_INVALID"`.
7. Keep interfaces/abstractions **only where the syllabus or the PPT needs them** (Repository, Vehicle, Describable). Don't add layers, factories, builders, or patterns "for flexibility".
8. Prefer a few well-named classes over many tiny ones. If a helper class has one 5-line method, fold it into its caller's class.

**Don't**
- No design-pattern name-dropping (no `AbstractFactoryProvider`, `Manager` of `Manager`, `Util` classes with 30 unrelated methods).
- No giant ASCII-art comment banners, no emoji or "AI-assistant" phrases inside source files, no over-explaining Javadoc paragraphs.
- No premature optimisation, no unused flexibility, no generics beyond what L24 requires.
- No copy-pasted code blocks: if the same 5 lines appear twice, extract a method.

**Self-test before saving any file:** *"Could Aryan explain this method to a teacher in 60 seconds without opening a book?"* If no → simplify.

---

# PART 18 — ZERO-DEFECT PROTOCOL (errorless, bug-free, verified)

"Errorless" is achieved by **process**, not by claiming. Follow this loop for every task.

## 18.1 The Definition of Done (all must be true, with real evidence)
1. Compiles with `javac -Xlint:all` → **0 errors, 0 warnings**.
2. All tests pass (and the new feature has its own tests, including **negative and boundary cases**).
3. You **ran the app** and exercised the feature through the real menu with: normal input, empty input, wrong type (letters for numbers), negative, zero, huge, very long text, repeated actions, and Ctrl+Z/EOF. No crash, no stack trace on screen, clear message.
4. Both storage modes (CSV and JDBC) behave identically for the feature (from Phase 4 onwards).
5. Restart test: stop the app, start again, data is intact.
6. No leftover debug prints, dead code, unused imports.
7. Docs/evidence updated from the **real** run output.

## 18.2 The Build → Run → Break → Fix → Prove loop
1. **Build** the smallest vertical slice.
2. **Run** it for real and read the actual output.
3. **Break it on purpose** (adversarial testing): try to crash it with the input list above; for concurrency, run 20–50 rounds; for files, delete/lock/corrupt the file; for JDBC, kill the connection mid-transaction.
4. **Fix at the root cause** — never patch the symptom, never wrap in an empty `catch`, never delete a failing test, never loosen a rule just to get green.
5. **Prove**: add a **regression test** that failed before the fix and passes after. Record it in `docs/BUG_LOG.md`.

## 18.3 `docs/BUG_LOG.md` (mandatory, real entries only)
| ID | Found in (phase/file) | How found | Symptom | Root cause | Fix (commit) | Regression test |
Even small bugs are logged — a visible bug-fixing trail is honest progress evidence for rubric row 5 and gives the student great viva stories ("I found a race condition, here is how I fixed it").

## 18.4 Review passes (run each at every gate and before tagging a release)
- **Pass A – Compiler & lint:** `-Xlint:all`, zero warnings.
- **Pass B – Rubric audit:** walk the §2 table row by row; every cell has a file/commit/log.
- **Pass C – Syllabus audit:** grep for anything forbidden in §3.2/3.3; check each lecture has a usage.
- **Pass D – Bug hunt checklist (go through ALL):**
  - `NullPointerException`: every `findById` result checked; no `null` returned from collections-returning methods (return empty lists).
  - `NumberFormatException`/`InputMismatchException`: every parse inside try/catch.
  - `IndexOutOfBounds`/off-by-one: pagination, `skip/limit`, `ListIterator` ends.
  - `ConcurrentModificationException`: no removing inside for-each (use `Iterator.remove()` or iterate over a copy).
  - **Integer/double pitfalls:** money compared with tolerance or kept in a consistent scale; no `==` on doubles; no `==` on Strings.
  - **Resource leaks:** every stream/reader/writer/connection/statement/resultset closed on all paths.
  - **Deadlock/livelock:** one lock only; no blocking I/O while holding the lock longer than necessary; `wait()` always inside a `while` guard; `notifyAll()` after every state change.
  - **Lost updates:** check-then-act sequences are inside one `synchronized` region.
  - **Visibility:** shared flags are `volatile` or guarded.
  - **Encoding:** names with non-ASCII characters survive CSV and DB round trips.
  - **CSV pitfalls:** commas, quotes, blank lines, trailing `\r`, BOM, header-only file, empty file.
  - **SQL pitfalls:** every dynamic value is a `?` parameter; transaction always committed or rolled back; `autoCommit` restored.
  - **State machine:** impossible to reach SOLD→AVAILABLE; RESERVED expiry cannot revive a sold car.
  - **Persistence ordering:** memory and storage cannot disagree after a failure (revert in memory on failure).
  - **UI:** every menu path has a way back; every prompt re-asks on bad input; EOF ends gracefully.
- **Pass E – Fresh-clone test:** clone the repo to a new folder, follow only README steps; fix README until it works.

## 18.4b Static self-review before every commit
Read your own diff once as if you were the teacher: unclear name? unexplained magic number? function doing two things? duplicated block? Fix first, then commit.

## 18.5 When you are unsure or something fails
- Do **not** guess and do **not** hide it. Reproduce, read the real error, find the root cause, fix, retest.
- If a requirement here is impossible or conflicts with another, stop, state the conflict clearly, propose the closest compliant option, and wait for the student.
- If something cannot be verified in your environment (e.g. no Windows console, no network), say exactly **what was not verified** in the gate report instead of claiming success.

## 18.6 Honesty clause
"Errorless" means: *no known defects at release, every known limitation documented, every claim backed by a real run.* Never write "100 % bug-free" in the README; write the **actual** test counts and a **Known limitations** section.

---

# APPENDIX A — SAMPLE SCRIPT FILE (`data/sample/bulk_import.txt`) for `StreamTokenizer`
```
# RevLine bulk script — lines starting with # are comments
ADD "Ferrari" "812 Superfast" 340000 340 PETROL 789 2.9
ADD "Rimac" "Nevera" 2400000 412 ELECTRIC 1914 1.85
ADD "Lotus" "Evija" 2100000 350 ELECTRIC 1972 2.0
RESERVE 19 "Anika Sharma"
SELL 19 "Anika Sharma"
SELL 5 "Rohan Mehta"
ADD "Broken" "Row" abc 300 PETROL 500 3.0      # bad price -> must be reported, not crash
SELL 999 "Ghost Buyer"                          # unknown id -> reported
```
Expected: 6 commands succeed or fail with precise per-line messages and a final summary; no exception escapes.

# APPENDIX B — SAMPLE SESSION (what the Sell flow should look like)
```
   Select an option: 4  → [1] Sell a car
   Enter car ID: 5
   Buyer name: Rohan Mehta
╔════════════════════════════════════════════════════════════════╗
║                  R E V L I N E   M O T O R S                   ║
╠════════════════════════════════════════════════════════════════╣
║ RECEIPT OF PURCHASE                         Sale ID: 4         ║
║ Car      : Lamborghini Aventador SVJ        Date: 2026-11-19   ║
║ Buyer    : Rohan Mehta                                         ║
║ Seller   : RevLine Showroom                                    ║
║ Price paid: $517,770.00                                        ║
║ CONGRATULATIONS ON YOUR NEW SUPERCAR!                          ║
╚════════════════════════════════════════════════════════════════╝
```
(Dates/IDs come from the real run; the sample only shows the layout.)

# APPENDIX C — SUGGESTED COMMIT SEQUENCE (first 25 — honest, incremental)
1 `chore: init repo, gitignore, licence` · 2 `docs: add problem statement outline` · 3 `chore: package skeleton + build scripts` · 4 `feat(model): FuelType and Status enums` · 5 `feat(model): Vehicle and Car with validation` · 6 `feat(util): custom exception hierarchy` · 7 `feat(util): Validator with wrapper parsing` · 8 `test: add TestRunner and Assert` · 9 `test(model): Car round-trip tests` · 10 `feat(repo): Repository generic interface` · 11 `feat(repo): FileCarRepository read path` · 12 `feat(repo): safe write with temp file` · 13 `data: seed cars.csv from C prototype` · 14 `test: SeedConsistencyTest vs legacy file` · 15 `feat(service): InventoryService add/list` · 16 `feat(ui): ConsoleTable renderer` · 17 `feat(ui): banner and boot panel` · 18 `feat(app): main menu loop` · 19 `feat(util): AppLogger` · 20 `feat(service): search by id/text` · 21 `docs: design diagrams` · 22 `docs: progress and traceability` · 23 `feat(service): update/delete` · 24 `feat(service): UndoManager` · 25 `chore: tag v0.1-prototype`.

# APPENDIX D — RISK REGISTER (copy into PROGRESS.md and keep updated)
| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Evaluator flags AI-generated work | Med | Critical | PART 5: EX-1…8, own docs, real commit history, viva prep, mock viva |
| Out-of-syllabus code questioned in viva | Med | High | PART 3 scope contract + grep audit |
| JDBC driver/DB fails on demo laptop | Low | High | CSV mode fallback; `DEMO_SCRIPT.md` plan B; keep jar in `/lib` |
| Windows console garbles box characters | Med | Low | UTF-8 PrintStream + `--ascii` fallback |
| Repo not updated regularly | Med | High | commit-every-session rule; milestone/issue trail |
| Scope creep | Med | Med | rubric-first rule (§0.3-2); stretch branch only after v1.0 |
| Thread demo non-deterministic | Med | Med | assertions on invariants not timings; repeat rounds; watchdog |
| Teammate absent at evaluation → zero | Low | Critical | everyone owns a module (§5.5) and rehearses viva |

# APPENDIX E — TRANSCRIBED COURSE MATERIAL (authoritative copy of the student's screenshots)

## E.1 Theory plan — 25CSE0202 / Programming in Java
| Lect. | Topic(s) |
|---|---|
| 1 | Revisit of OOP concepts; implementation of classes and objects, methods, constructors, this/static, inheritance, polymorphism, abstraction/interfaces and short object-modeling exercises |
| 2 | Revisit of multi-dimensional array handling; String immutability and pool concept; comparison, search, manipulation and formatting; StringBuilder/StringBuffer; wrapper classes |
| 3 | Revisit of handling checked/unchecked exceptions using try, catch, finally and multi-catch; exception propagation, throw/throws and custom exceptions |
| 4 | Integrated coding practice, strings/wrappers and exception handling through short console programs; debugging, input validation, understanding test-case structures and coding-platform practices |
| — | *Continuous Evaluation 1 (Lecture 1–15)* |
| 5 | Revisit of Collections Framework; choosing an appropriate collection for a use case; when to use ArrayList, LinkedList, HashSet, LinkedHashSet and TreeSet |
| 6–7 | More implementations of HashMap, LinkedHashMap, TreeMap, Queue/Deque/PriorityQueue and traversal using Iterator/ListIterator |
| 8 | Revision of the applications of Comparable/Comparator, sorting and selected Collections utility methods through short record-management problems |
| 9–10 | Concepts of multiprogramming vs multithreading and concurrency; program, process and thread; sequential versus concurrent execution; concurrency versus parallelism; Thread and Runnable; start()/run(), sleep(), join() and interrupt() |
| 11–12 | Thread lifecycle and scheduling: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING and TERMINATED states; daemon threads, priorities and execution tracing |
| 13–14 | Shared data and synchronization: race conditions, critical sections, synchronized methods/blocks, intrinsic locks, atomicity and visibility awareness |
| — | *Continuous Evaluation 2 (Lecture 16–31)* |
| 15–16 | Inter-thread communication: wait(), notify() and notifyAll(); guarded blocks, producer-consumer coordination and correct monitor usage |
| 17–18 | JDBC and database programming; relational/SQL concepts required for JDBC; JDBC architecture, drivers, URL, DriverManager and Connection |
| 19 | Statement and PreparedStatement: executeQuery(), executeUpdate(), parameter binding, secure SQL and basic resource management |
| 20 | CRUD and ResultSet processing: Create, Read, Update and Delete; cursor iteration, typed getters and Java-object mapping |
| 21–22 | Lambda expressions and Stream API: lambda syntax, functional interfaces and method references |
| 23 | Stream fundamentals and intermediate operations: sources/pipelines, laziness, filter(), map(), sorted(), distinct(), limit() and skip() |
| 24 | Introductory generics: generic classes/methods, type safety, diamond operator and basic bounded/wildcard awareness |
| 25 | Byte streams, character streams, StreamTokenizer; reading/writing files using FileInputStream, FileOutputStream, FileReader, FileWriter |
| 26–27 | File handling + collections integration: text/CSV read-write, validation, collection import/export and exception-safe resource handling |
| — | *Continuous Assessment 3 (CA-3) – Project Milestone (after Lecture 54)* |
| 28 | **Unit VI** – Guided Java Project: project planning and working prototype — problem definition, use cases, class/package structure, data model, collection/persistence choice and initial module |
| 29 | Project implementation and integration: core modules, collection-based logic, file/JDBC persistence, Stream-based reporting and concurrency where appropriate |
| 30 | Project testing and refinement: input validation, exception handling, debugging, code review, duplication removal and basic refactoring; project documentation and viva preparation: architecture/data-flow explanation, database design, test evidence and code walkthrough |
| — | *Continuous Assessment 4 (CA-4) – Final Project Evaluation + Viva (Lecture 60)* |

## E.2 Rubrics for Programming in Java (25CSE0202) — 25 marks
| # | Evaluation component | Marks | What to evaluate |
|---|---|---|---|
| 1 | Problem Statement | 3 | Problem is clearly stated, relevant, feasible, and sufficiently specific for an individual Java application; intended users/context and the need being addressed are identifiable. |
| 2 | Project Objectives and Scope | 2 | Objectives are clear, measurable, and aligned with the problem statement; project scope, major use cases, expected outputs, and realistic boundaries are defined. |
| 3 | Project Design & Prototype | 9 | Class/package structure, data model and application flow are sensible, and a working initial module demonstrates essential input/output and core functionality reliably. |
| 4 | Integration of all Course Concepts | 6 | Prototype applies suitable OOP and collections and shows planned/initial use of exception handling, JDBC or file persistence, and other relevant Java concepts according to project need. |
| 5 | Code Quality, Progress Evidence & Documentation | 5 | Code is organized and readable; naming/package structure is meaningful; progress is traceable through working files/repository history; current status, setup, pending work, and milestone evidence are documented. |
| | **Total** | **25** | |

**Notes:**
- Project team may consist of 3 to 4 members.
- Submission of the project on GitHub is mandatory for all groups. The GitHub repository must be updated regularly throughout the semester.
- In case a student is absent from the evaluation, he/she will be given zero in the evaluation marks (under all rubrics).
- Any student found submitting work directly generated by AI tools, such as large language models (LLMs), without significant personal contribution, will receive a grade of zero.

*If an image in `/reference/course/` disagrees with this transcription, trust the image and tell the student.*

---
## ▶ START NOW
Begin with **Phase −1** and **Phase 0**. Do not write feature code until `docs/00_UNDERSTANDING.md` is reviewed at **GATE 0**. Print the first gate report in the §17.2 format.
