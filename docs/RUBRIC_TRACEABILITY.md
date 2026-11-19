# Rubric Traceability — RevLine-Java

> Updated at every phase gate from real data.

| # | Rubric Component | Marks | Deliverable | Evidence (file paths, commits) | Status |
|---|------------------|-------|-------------|-------------------------------|--------|
| 1 | Problem Statement | 3 | `docs/01_PROBLEM_STATEMENT.md` | Draft outline created. **Student must rewrite in own words.** | Draft |
| 2 | Objectives & Scope | 2 | `docs/02_OBJECTIVES_SCOPE.md` | Draft outline created. **Student must rewrite in own words.** | Draft |
| 3 | Design & Prototype | 9 | `docs/03_DESIGN.md` + working code | Working Initial Module (Prototype v0.1): FileCarRepository, InventoryService, SearchService, ConsoleTable, Banner, MenuController. 34 tests PASS. | Prototype v0.1 |
| 4 | Integration of Concepts | 6 | `docs/04_CONCEPT_MAP.md` | L1 (OOP), L2 (Strings/StringBuffer/wrappers), L3 (Exceptions), L4 (Console IO), L5/6 (ArrayList, HashMap, HashSet, TreeSet), L8 (Comparable), L25/26 (File handling). | Working |
| 5 | Code Quality & Docs | 5 | README, PROGRESS, CHANGELOG, Git history | M1_EXPLAINED.md, EX1_HINT.md, EX3_HINT.md, demo evidence logs, zero compiler warnings. | Advanced |

## Phase Evidence Log

### Phase -1
- Package skeleton: `src/main/java/com/revline/{app,model,service,repository,concurrency,ui,util/exception}`
- Build scripts: `scripts/{build,run,test,demo}.{bat,sh}`
- Build: **PASS** — 0 errors, 0 warnings (`javac --release 17 -Xlint:all`)
- Files: `.gitignore`, `LICENSE`, `README.md`, `AGENTS.md`, `CHANGELOG.md`, `TOOLING.md`
- Git: repo initialized, files staged

### Phase 0
- `docs/00_UNDERSTANDING.md` — understanding report (function-by-function C summary, real PPT slide XML + notes extraction + differences table, syllabus, rubric, conflicts)
- `docs/01_PROBLEM_STATEMENT.md` — rewritten by student
- `docs/02_OBJECTIVES_SCOPE.md` — rewritten by student
- `docs/DECISIONS.md` — 10 ADRs
- `docs/RUBRIC_TRACEABILITY.md` — this file
- `docs/TEAM_OWNERSHIP.md` — module ownership template

### Phase 1
- `docs/03_DESIGN.md` — complete design document
- Diagrams in `docs/diagrams/`: `package.mmd`, `class.mmd`, `seq_sell.mmd`, `seq_add.mmd`, `er.mmd`, `state.mmd`
- Domain models: `Vehicle.java`, `Car.java`, `Sale.java`, `Reservation.java`, `AuditEntry.java`, `FuelType.java`, `Status.java`
- Repositories: `Repository.java` (Generic), `CarRepository.java`, `SaleRepository.java`
- Exceptions: `RevLineException`, `RepositoryException`, `DataFormatException`, `CarNotFoundException`, `CarNotAvailableException`, `InvalidCarException`
- SQL & Seed Data: `sql/schema.sql`, `sql/seed.sql`, `sql/sample_queries.sql`, `data/seed/cars.csv`, `data/seed/sales.csv`, `data/legacy/cars.txt`
- Tests: `Assert.java`, `TestRunner.java`, `SeedConsistencyTest.java` (3 checks PASS)
- Build: PASS (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors)

### Phase 2
- Working Initial Module (Prototype v0.1):
  - Repository: `FileCarRepository.java` with crash-safe atomic saves (`FileUtil.safeWrite`)
  - Services: `InventoryService.java` (addCar, findAll, findById, duplicateGuard, getBrandsSorted), `SearchService.java` (findById, findByStatus)
  - Console UI: `ConsoleTable.java` (box-drawing with stock footer), `Banner.java` (boot panel + logo), `InputReader.java` (line-based Scanner), `MenuPrinter.java`
  - Application: `StorageBootstrap.java` (auto-seeds `data/cars.csv`), `MenuController.java`, `RevLineApp.java`
  - Utilities: `Validator.java`, `AppLogger.java` (FileWriter + StringBuffer buffer), `FileUtil.java`
- Student Exercises Assigned: EX-1 (`Validator.isValidFuelType`, `parsePositiveDouble`), EX-3 (`SearchService.search`)
- Explainer & Guides: `docs/learning/M1_EXPLAINED.md`, `EX1_HINT.md`, `EX3_HINT.md`, `EX_LOG.md`
- Tests: 34 / 34 core checks passing (`TestRunner.java`), EX-1 & EX-3 reported as pending
- Evidence: `docs/evidence/demo_input.txt` -> `docs/evidence/demo_output_v0.1.txt`
- Build: PASS (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors)
