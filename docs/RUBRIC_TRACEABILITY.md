# Rubric Traceability — RevLine-Java

> Updated at every phase gate from real data.

| # | Rubric Component | Marks | Deliverable | Evidence (file paths, commits) | Status |
|---|------------------|-------|-------------|-------------------------------|--------|
| 1 | Problem Statement | 3 | `docs/01_PROBLEM_STATEMENT.md` | Authored and committed personally by Aryan Dhingra. Defines supercar showroom inventory domain, goals, and constraints. | ✅ Completed |
| 2 | Objectives & Scope | 2 | `docs/02_OBJECTIVES_SCOPE.md` | Authored and committed personally by Aryan Dhingra. Defines 6 core objectives, scope boundaries, and academic alignment. | ✅ Completed |
| 3 | Design & Prototype | 9 | `docs/03_DESIGN.md` + working code | Full OOP architecture with 6 Mermaid diagrams. Working Core Features & CA-3 Package: 76 core tests PASS. | ✅ CA-3 Ready |
| 4 | Integration of Concepts | 6 | `docs/04_CONCEPT_MAP.md` + source code | L1 (OOP), L2 (2D arrays, Strings, StringBuffer, wrappers), L3 (Exceptions), L4 (Console IO), L5/6/7 (ArrayList, HashMap, HashSet, TreeSet, Deque, Queue, PriorityQueue, LinkedHashMap LRU, ListIterator), L8 (Comparable, Comparator, thenComparing), L13-14 (synchronized, intrinsic monitors), L21 (Predicate chaining), L24 (Generic Repository), L25/26/27 (File IO, safe-write). | ✅ Working |
| 5 | Code Quality & Docs | 5 | README, PROGRESS, CHANGELOG, Git history | Zero compiler warnings (`-Xlint:all` on Java 17), M1/M2/M3 explainers, CA3 checklist, automated tests (76/76 PASS), demo evidence logs. | ✅ Advanced |

---

## Phase Evidence Log

### Phase -1: Environment & Tooling
- Package skeleton: `src/main/java/com/revline/{app,model,service,repository,concurrency,ui,util/exception}`
- Build scripts: `scripts/{build,run,test,demo}.{bat,sh}`
- Build: **PASS** — 0 errors, 0 warnings (`javac --release 17 -Xlint:all`)
- Files: `.gitignore`, `LICENSE`, `README.md`, `AGENTS.md`, `CHANGELOG.md`, `TOOLING.md`
- Git: repo initialized, files staged

### Phase 0: Understanding & Planning
- `docs/00_UNDERSTANDING.md` — understanding report (function-by-function C summary, real PPT slide XML + notes extraction + differences table, syllabus, rubric, conflicts)
- `docs/01_PROBLEM_STATEMENT.md` — rewritten by student
- `docs/02_OBJECTIVES_SCOPE.md` — rewritten by student
- `docs/DECISIONS.md` — 10 ADRs
- `docs/RUBRIC_TRACEABILITY.md` — this file
- `docs/TEAM_OWNERSHIP.md` — module ownership template

### Phase 1: Design & Data Architecture
- `docs/03_DESIGN.md` — complete design document
- Diagrams in `docs/diagrams/`: `package.mmd`, `class.mmd`, `seq_sell.mmd`, `seq_add.mmd`, `er.mmd`, `state.mmd`
- Domain models: `Vehicle.java`, `Car.java`, `Sale.java`, `Reservation.java`, `AuditEntry.java`, `FuelType.java`, `Status.java`
- Repositories: `Repository.java` (Generic), `CarRepository.java`, `SaleRepository.java`
- Exceptions: `RevLineException`, `RepositoryException`, `DataFormatException`, `CarNotFoundException`, `CarNotAvailableException`, `InvalidCarException`
- SQL & Seed Data: `sql/schema.sql`, `sql/seed.sql`, `sql/sample_queries.sql`, `data/seed/cars.csv`, `data/seed/sales.csv`, `data/legacy/cars.txt`
- Tests: `Assert.java`, `TestRunner.java`, `SeedConsistencyTest.java` (3 checks PASS)
- Build: PASS (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors)

### Phase 2: Working Prototype (`v0.1-prototype`)
- Working Initial Module (Prototype v0.1):
  - Repository: `FileCarRepository.java` with crash-safe atomic saves (`FileUtil.safeWrite`)
  - Services: `InventoryService.java` (addCar, findAll, findById, duplicateGuard, getBrandsSorted), `SearchService.java` (findById, findByStatus)
  - Console UI: `ConsoleTable.java` (box-drawing with stock footer), `Banner.java` (boot panel + logo), `InputReader.java` (line-based Scanner), `MenuPrinter.java`
  - Application: `StorageBootstrap.java` (auto-seeds `data/cars.csv`), `MenuController.java`, `RevLineApp.java`
  - Utilities: `Validator.java`, `AppLogger.java` (FileWriter + StringBuffer buffer), `FileUtil.java`
- Student Exercises Assigned & Implemented:
  - EX-1 (`Validator.isValidFuelType`, `parsePositiveDouble`): commit `7fbdfd3`
  - EX-3 (`SearchService.search` multi-field): commit `fc2bb19`
- Tests: 34 / 34 core checks passing (`TestRunner.java`), EX-1 & EX-3 verified as [COMPLETE]
- Evidence: `docs/evidence/demo_input.txt` -> `docs/evidence/demo_output_v0.1.txt`
- Build: PASS (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors)

### Phase 3: Core Features + CA-3 Package (`v0.3-ca3`)
- **Module 2: Search, Filter, Sort & Compare**:
  - `CarComparators.java`: 6 static comparators (`BY_PRICE_ASC`, `BY_PRICE_DESC`, `BY_SPEED_DESC`, `BY_HP_DESC`, `BY_ACCEL_ASC`, `BY_BRAND_MODEL`).
  - `FilterBuilder.java`: composable predicate builder (`brand`, `fuelType`, `minPrice`, `maxPrice`, `minSpeed`, `minHorsepower`, `availableOnly`) chained via `Predicate.and()`.
  - `ComparisonService.java`: 2D `String[][]` matrix comparison table (L2) with `*BEST` winner calculation.
- **Module 3: Sales, Reservation & Management**:
  - `FileSaleRepository.java`: CSV persistence of sales transactions with atomic `safeWrite()`.
  - `ReservationManager.java`: min-heap `PriorityQueue<Reservation>` with `Iterator.remove()` hold expiration.
  - `WaitlistService.java`: FIFO customer waitlist queues (`Queue<String>` / `LinkedList`).
  - `SalesService.java`: synchronized `sellCar()`, atomic rollback guarantee, `reserveCar()`, `cancelReservation()`, C-style ASCII receipt generator.
  - `RecentlyViewed.java`: LRU showroom cache extending `LinkedHashMap` (`accessOrder = true`, capacity 5).
  - `UndoManager.java`: stack-based undo history using `ArrayDeque` (`push`, `peek`, `canUndo`).
- **Interactive UI & Role Switching**:
  - `MenuController.java`: `[S]` command toggles between **Buyer View** and **Seller View**.
  - Buyer View: bidirectional browsing via `ListIterator<Car>`, multi-field search, composable filtering, custom sorting, side-by-side spec comparison, reservation, purchasing, waitlist, recently viewed.
  - Seller View: complete inventory overview, car additions, updates, deletions, undo operation, sales ledger, showroom valuation, audit log viewer.
- **Exercise Implementations**:
  - EX-2 (`CarComparators.byValueForMoney()`): L8 comparator based on HP per $100k with acceleration tie-breaker. Complete in commit `fc78366`.
  - EX-7 (`UndoManager.undo()`): L6–7 Deque stack undo for ADD, DELETE, UPDATE. Complete in commit `197baaa`.
- **Automated Tests**:
  - 76 / 76 core checks passing (`TestRunner.java`).
  - EX-1, EX-2, EX-3, and EX-7 verified [COMPLETE].
- **CA-3 Package**:
  - `docs/CA3_SUBMISSION_CHECKLIST.md`: comprehensive submission readiness across Rubric Rows 1–5 and 8-minute demo walkthrough.
  - `docs/learning/M2_M3_EXPLAINED.md`: technical explainer and viva defense guide for Modules 2 and 3.
  - `docs/evidence/demo_ca3_input.txt` & `docs/evidence/demo_ca3_output.txt`: scripted execution log verifying full end-to-end functionality.
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).
