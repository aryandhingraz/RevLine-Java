# Progress — RevLine-Java

> Updated at every phase gate from real data. All evidence comes from actual runs.

## Phase -1: Environment & Tooling

| Item | Status | Evidence |
|------|--------|----------|
| JDK 17+ installed | ✅ Done | OpenJDK 25.0.4.1 (Temurin) |
| Git installed & configured | ✅ Done | Git 2.54.0, user: Aryan dhingra |
| Project skeleton compiles | ✅ Done | `javac --release 17 -Xlint:all` → 0 warnings |
| Build/run/test/demo scripts | ✅ Done | `scripts/*.bat` and `scripts/*.sh` (configured for `--release 17`) |
| AGENTS.md | ✅ Done | Condensed golden rules + scope |
| README skeleton | ✅ Done | Setup instructions, feature table |
| TOOLING.md | ✅ Done | Environment + dependency docs |
| .gitignore | ✅ Done | out/, build/, *.class, *.db, IDE folders |
| LICENSE | ✅ Done | MIT |
| Git repo initialized | ✅ Done | Branch `main`, origin configured |

## Phase 0: Understanding & Planning

| Item | Status | Evidence |
|------|--------|----------|
| 00_UNDERSTANDING.md | ✅ Done | Function-by-function C summary, real PPT slide-by-slide XML + notes + differences table, syllabus, rubric, conflicts |
| 01_PROBLEM_STATEMENT.md | ✅ Done | Authored personally by Aryan Dhingra |
| 02_OBJECTIVES_SCOPE.md | ✅ Done | Authored personally by Aryan Dhingra |
| DECISIONS.md | ✅ Done | 10 ADRs |
| RUBRIC_TRACEABILITY.md | ✅ Done | Comprehensive trace across Rubric Rows 1–5 |
| TEAM_OWNERSHIP.md | ✅ Done | Authored and committed by Aryan |

## Phase 1: Design & Data Architecture

| Item | Status | Evidence |
|------|--------|----------|
| Mermaid diagrams | ✅ Done | `docs/diagrams/{package,class,seq_sell,seq_add,er,state}.mmd` |
| docs/03_DESIGN.md | ✅ Done | Complete architecture, diagrams, data dictionary, collection-choice table, trade-offs |
| Compiling interfaces & models | ✅ Done | `model/` (Vehicle, Car, Sale, Reservation, AuditEntry, FuelType, Status, Describable, CarVisitor), `repository/` (Repository, CarRepository, SaleRepository) |
| Exception hierarchy | ✅ Done | `util/exception/` (RevLineException, RepositoryException, DataFormatException, CarNotFoundException, CarNotAvailableException, InvalidCarException) |
| Relational schema & queries | ✅ Done | `sql/schema.sql`, `sql/seed.sql`, `sql/sample_queries.sql` |
| Seed CSV data | ✅ Done | `data/seed/cars.csv`, `data/seed/sales.csv`, `data/legacy/cars.txt` |
| In-house Test Framework | ✅ Done | `src/test/java/com/revline/Assert.java`, `TestRunner.java` |
| SeedConsistencyTest | ✅ Done | 3 checks passing: 18 cars match legacy C, IDs 3, 4, 18 marked SOLD, sales.csv consistent |
| Build & test status | ✅ Done | 0 errors, 0 warnings with `javac --release 17 -Xlint:all` |

## Phase 2: Initial Module / Working Prototype (`v0.1-prototype`)

| Item | Status | Evidence |
|------|--------|----------|
| Working prototype compiled | ✅ Done | `javac --release 17 -Xlint:all` (0 warnings, 0 errors) |
| FileCarRepository & safe writes | ✅ Done | `FileCarRepository.java`, `FileUtil.safeWrite()` (temp file + renameTo) |
| InventoryService CRUD | ✅ Done | `InventoryService.java` (add, findAll unmodifiable, findById O(1), duplicate guard, sorted brands) |
| SearchService & ID lookup | ✅ Done | `SearchService.java` (findById, findByStatus) |
| Student Exercises EX-1 & EX-3 | ✅ Done | EX-1 commit `7fbdfd3`, EX-3 commit `fc2bb19` |
| Console Table & UI | ✅ Done | `ConsoleTable.java`, `Banner.java`, `InputReader.java`, `MenuPrinter.java` |
| Application boot & runtime loop | ✅ Done | `RevLineApp.java`, `MenuController.java`, `StorageBootstrap.java` |
| Automated Test Suite | ✅ Done | 34 / 34 core checks passing (68 ms), EX-1 & EX-3 completed |
| Scripted Demo & Evidence | ✅ Done | `docs/evidence/demo_input.txt` -> `docs/evidence/demo_output_v0.1.txt` |
| Learning & Explainer Doc | ✅ Done | `docs/learning/M1_EXPLAINED.md`, `EX1_HINT.md`, `EX3_HINT.md`, `EX_LOG.md` |

## Phase 3: Core Features & CA-3 Package (`v0.3-ca3`)

| Item | Status | Evidence |
|------|--------|----------|
| Sorting & Comparators (L8) | ✅ Done | `CarComparators.java` (6 comparators, asc/desc, tie-breakers) |
| Student Exercise EX-2 | ⏳ Assigned | `CarComparators.byValueForMoney()`, `docs/learning/EX2_HINT.md` |
| Composable Filters (L21) | ✅ Done | `FilterBuilder.java` (Predicate chaining with and()) |
| Side-by-Side Spec Matrix (L2) | ✅ Done | `ComparisonService.java` (2D String[][] grid, *BEST indicators) |
| LRU Browsing Cache (L6-7) | ✅ Done | `RecentlyViewed.java` (LinkedHashMap access-order eviction at size > 5) |
| Bidirectional Browsing (L6-7)| ✅ Done | `MenuController.java` (`ListIterator<Car>` with Next/Previous/Quit) |
| CSV Sales Persistence (L26-27)| ✅ Done | `FileSaleRepository.java` (safe-write atomic persistence, load/save) |
| Priority Reservations (L6-7) | ✅ Done | `ReservationManager.java` (PriorityQueue min-heap, Iterator.remove() purge) |
| FIFO Waitlist (L6-7) | ✅ Done | `WaitlistService.java` (Queue / LinkedList FIFO) |
| Sales & Atomic State Machine | ✅ Done | `SalesService.java` (synchronized sellCar, rollback guarantee, ASCII receipt) |
| Stack-Based Undo (L6-7) | ✅ Done | `UndoManager.java` (ArrayDeque LIFO history for ADD/DELETE/UPDATE) |
| Student Exercise EX-7 | ⏳ Assigned | `UndoManager.undo()`, `docs/learning/EX7_HINT.md` |
| Seller / Buyer View Toggle | ✅ Done | `[S]` command in `MenuController.java` |
| Automated Test Suite | ✅ Done | 76 / 76 core checks passing (127 ms), EX-1 & EX-3 complete, EX-2 & EX-7 pending |
| Scripted CA-3 Demo Output | ✅ Done | `docs/evidence/demo_ca3_input.txt` -> `docs/evidence/demo_ca3_output.txt` |
| Explainer & Viva Preparation | ✅ Done | `docs/learning/M2_M3_EXPLAINED.md` (architecture + 5 viva Q&As) |
| CA-3 Submission Checklist | ✅ Done | `docs/CA3_SUBMISSION_CHECKLIST.md` (Rubric Rows 1–5 green) |

## Upcoming

| Phase | Description | Status |
|-------|-------------|--------|
| 4 | JDBC persistence | Pending |
| 5 | Streams, lambdas, generics | Pending |
| 6 | Concurrency | Pending |
| 7 | File handling & data tools | Pending |
| 8 | Hardening & viva v1.0 | Pending |

## Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Evaluator flags AI-generated work | Med | Critical | EX-1…8, own docs, real commit history, viva prep |
| Out-of-syllabus code in viva | Med | High | Scope contract + grep audit at every gate |
| JDBC driver/DB fails on demo laptop | Low | High | CSV mode fallback; demo script plan B |
| Windows console garbles box chars | Med | Low | UTF-8 PrintStream + --ascii fallback |
| Repo not updated regularly | Med | High | Commit-every-session rule |
| Scope creep | Med | Med | Rubric-first rule; stretch branch only |
| Thread demo non-deterministic | Med | Med | Assertions on invariants; repeat rounds; watchdog |
| Teammate absent at evaluation | Low | Critical | Module ownership; everyone rehearses viva |
