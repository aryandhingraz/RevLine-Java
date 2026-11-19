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
| .gitignore | ✅ Done | out/, *.class, *.db, IDE folders |
| LICENSE | ✅ Done | MIT |
| Git repo initialized | ✅ Done | Files staged, awaiting first commit |

## Phase 0: Understanding & Planning

| Item | Status | Evidence |
|------|--------|----------|
| 00_UNDERSTANDING.md | ✅ Done | Function-by-function C summary, real PPT slide-by-slide XML + notes + differences table, syllabus, rubric, conflicts |
| 01_PROBLEM_STATEMENT.md | 📝 Draft | Outline ready — student must rewrite |
| 02_OBJECTIVES_SCOPE.md | 📝 Draft | Outline ready — student must rewrite |
| DECISIONS.md | ✅ Done | 10 ADRs |
| RUBRIC_TRACEABILITY.md | ✅ Done | Skeleton with Phase -1 and 0 evidence |
| TEAM_OWNERSHIP.md | ✅ Done | Template — student fills names |

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
| Student Exercises EX-1 & EX-3 | 📝 Assigned | `Validator.isValidFuelType`, `parsePositiveDouble` (EX-1); `SearchService.search` (EX-3) |
| Console Table & UI | ✅ Done | `ConsoleTable.java`, `Banner.java`, `InputReader.java`, `MenuPrinter.java` |
| Application boot & runtime loop | ✅ Done | `RevLineApp.java`, `MenuController.java`, `StorageBootstrap.java` |
| Automated Test Suite | ✅ Done | 34 / 34 core checks passing (68 ms), EX-1 & EX-3 tracked as pending |
| Scripted Demo & Evidence | ✅ Done | `docs/evidence/demo_input.txt` -> `docs/evidence/demo_output_v0.1.txt` |
| Learning & Explainer Doc | ✅ Done | `docs/learning/M1_EXPLAINED.md`, `EX1_HINT.md`, `EX3_HINT.md`, `EX_LOG.md` |

## Upcoming

| Phase | Description | Status |
|-------|-------------|--------|
| 3 | Core features (v0.3-ca3) | Pending |
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
