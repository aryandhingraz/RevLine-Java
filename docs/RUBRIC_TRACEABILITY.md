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
  - `CarComparators.java`: six named comparators plus the EX-2 value-for-money comparator.
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
  - EX-4 and EX-5 (`ReportService`): top horsepower descriptions and average price grouped by brand.
  - EX-6 (`JdbcCarRepository.findByPriceRange()`): inclusive range query using a `PreparedStatement`.
  - EX-7 (`UndoManager.undo()`): L6–7 Deque stack undo for ADD, DELETE, UPDATE. Complete in commit `197baaa`.
- **Automated Tests**:
  - 76 / 76 core checks passing (`TestRunner.java`).
  - 9 / 9 exercise checks complete; EX-1 through EX-8 verified [COMPLETE].
- **CA-3 Package**:
  - `docs/CA3_SUBMISSION_CHECKLIST.md`: comprehensive submission readiness across Rubric Rows 1–5 and 8-minute demo walkthrough.
  - `docs/learning/M2_M3_EXPLAINED.md`: technical explainer and viva defense guide for Modules 2 and 3.
  - `docs/evidence/demo_ca3_input.txt` & `docs/evidence/demo_ca3_output.txt`: scripted execution log verifying full end-to-end functionality.
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).

### Phase 6: Multithreading & Concurrency Architecture (L9–L16) (`v0.6-concurrency`)
- **Background Daemon Threads (L9–L12)**:
  - `AutoSaveThread.java`: low-priority (`MIN_PRIORITY`) daemon thread with 30s interval; executes snapshot persistence via `inventoryService.saveSnapshot()`; cleanly stops on `interrupt()` and `join()`.
  - `ReservationExpiryThread.java`: daemon thread sweeping `ReservationManager` min-heap every 5s; reverts expired holds to `AVAILABLE`.
  - `RevLineApp.java`: boots daemons and hooks graceful termination via `Runtime.getRuntime().addShutdownHook`.
- **Producer-Consumer Architecture (L15–L16)**:
  - `PurchaseRequest.java`: immutable transaction carrier for concurrent sales requests.
  - `PurchaseRequestQueue.java`: bounded buffer (capacity 5) with guarded blocks (`while(condition) wait()` and `notifyAll()`).
  - **EX-8**: `PurchaseRequestQueue.take()` uses a guarded FIFO dequeue with `while`, `wait()`, and `notifyAll()`; verified by `ConcurrencyStressTest`.
  - `SalesProcessor.java`: consumer worker thread processing requests from the bounded queue through `SalesService.sellCar()`.
- **Interactive Concurrency Lab (L9–L16)**:
  - `ConcurrencyLab.java`: 5 deterministic educational demonstrations integrated into Seller Menu `[8]`:
    1. Race Condition Proof: `UnsafeCounter` (lost updates) vs `SafeCounter` (exact 40,000 count).
    2. Double-Sell Showdown: 20 concurrent `BuyerTask` threads race on the same car (exactly 1 winner, 19 rejected).
    3. Thread Lifecycle & Priority Tracer: traces `NEW -> RUNNABLE -> TIMED_WAITING -> BLOCKED -> WAITING -> TERMINATED` with priority and daemon explanation.
    4. Producer-Consumer Pipeline: 3 buyer producers submitting requests to bounded queue and 1 consumer processor.
    5. Thread Interruption & Memory Visibility: clean `interrupt()`/`join()` shutdown and `volatile` memory barrier awareness.
- **GATE 6 Concurrency Stress Suite**:
  - `ConcurrencyStressTest.java`: **20 consecutive stress rounds** of concurrent double-sell with 15 simultaneous threads per round. Zero deadlocks, exactly 1 winner per round, 100% deterministic invariants.
- **Evidence & Traceability**:
  - `docs/evidence/demo_concurrency_output.txt`: real execution output of all 5 Concurrency Lab demonstrations.
  - `docs/evidence/gate6_test_output.txt`: automated test suite execution log.
- **Documentation & Viva Guide**:
  - `docs/learning/M6_EXPLAINED.md`: technical explainer covering multithreading, thread states, monitor synchronization, guarded blocks, and viva prep.
- **Automated Tests**:
  - **109 / 109 core checks passing** (`TestRunner.java`); 9 / 9 exercise checks complete.
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).

### Phase 5: Streams, Lambdas & Advanced Analytics (L21–24) (`v0.5-analytics`)
- **Showroom Reporting Pipelines (R1–R15)**:
  - `ReportService.java`: complete implementation of all 15 analytical reporting pipelines (R1 through R15) using Java 17 Streams, intermediate filter/map/sorted/skip/limit operations, and terminal collectors.
  - R1: Available cars list (`filter(Status.AVAILABLE)`).
  - R2: Stock value of available vehicles (`filter -> mapToDouble -> sum`).
  - R3: Total portfolio valuation across entire fleet (`mapToDouble -> sum`).
  - R4: Inventory counts grouped by fuel type (`groupingBy -> counting`, populated into `EnumMap`).
  - R5: Top-3 fastest supercars (`sorted(topSpeed.reversed()) -> limit(3)`).
  - R6: Top-N vehicles by horsepower (EX-4: `filter(!SOLD) -> sorted(hp.reversed()) -> limit(n) -> map(describe)`).
  - R7: Average vehicle price grouped by brand (EX-5: `groupingBy(getBrand, averagingDouble)`).
  - R8: Sorted list of distinct showroom brands (`map -> distinct -> sorted`).
  - R9: Paginated catalog view (`sorted(byId) -> skip((page-1)*size) -> limit(size)`).
  - R10: Total realized sales revenue (`sales.stream().mapToDouble.sum()`).
  - R11: Sales revenue aggregated by buyer (`groupingBy(buyer, summingDouble)`).
  - R12: Quickest 0–100 km/h acceleration per fuel type (`groupingBy(fuel, minBy(comparingDouble(accel)))`).
  - R13: 2-D price band matrix `int[3][4]` mapping fuel categories vs 4 price tiers (`< $500k`, `$500k-$1M`, `$1M-$2M`, `>= $2M`).
  - R14: Formatted comma-delimited vehicle titles string (`map -> collect(joining(", "))`).
  - R15: Intermediate stream laziness and short-circuit demonstration using `peek` counters.
- **Functional Programming & Method References (L21–22)**:
  - `LambdaShowcase.java`: demonstrations of `Predicate`, `Consumer`, `Function`, `Supplier`, custom `@FunctionalInterface VehicleTransformer`, and all 4 kinds of method references (static, bound instance, unbound instance, constructor).
- **Generics in Depth (L24)**:
  - `Pair<K, V>`: type-safe generic pair container.
  - `GenericUtils.java`: `<T extends Comparable<T>> maxOf` bounded method, `List<? extends Vehicle> printAll` upper-bounded wildcard method, and `buildPriceLadder` using `TreeMap`.
- **UI Integration & Text Export**:
  - `MenuPrinter.java` & `MenuController.java`: interactive Reports submenu with on-demand reporting and atomic file export to `data/exports/report_summary.txt`.
- **Gate 5 Verification Suite (GATE 5)**:
  - `ReportServiceAllReportsTest.java`: all 15 reports verified against independently hand-computed values.
  - `GenericsAndLambdaTest.java`: 6 automated tests validating `Pair`, `GenericUtils`, and `LambdaShowcase`.
- **Documentation & Viva Guide**:
  - `docs/learning/M5_EXPLAINED.md`: technical explainer and viva defense guide for Streams, Lambdas, Method References, and Generics.
- **Automated Tests**:
  - **105 / 105 core checks passing** (`TestRunner.java`, 263 ms); 8 / 9 exercises complete.
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).

### Phase 6: Multithreading & Concurrency Architecture (L9–L16) (`v0.6-concurrency`)
- **Background Daemon Threads (L9–L12)**:
  - `AutoSaveThread.java`: low-priority (`MIN_PRIORITY`) daemon thread with 30s interval; executes snapshot persistence via `inventoryService.saveSnapshot()`; cleanly stops on `interrupt()` and `join()`.
  - `ReservationExpiryThread.java`: daemon thread sweeping `ReservationManager` min-heap every 5s; reverts expired holds to `AVAILABLE`.
  - `RevLineApp.java`: boots daemons and hooks graceful termination via `Runtime.getRuntime().addShutdownHook`.
- **Producer-Consumer Architecture (L15–L16)**:
  - `PurchaseRequest.java`: immutable transaction carrier for concurrent sales requests.
  - `PurchaseRequestQueue.java`: bounded buffer (capacity 5) with guarded blocks (`while(condition) wait()` and `notifyAll()`).
  - **EX-8**: `PurchaseRequestQueue.take()` uses a guarded FIFO dequeue with `while`, `wait()`, and `notifyAll()`; verified by `ConcurrencyStressTest`.
  - `SalesProcessor.java`: consumer worker thread processing requests from the bounded queue through `SalesService.sellCar()`.
- **Interactive Concurrency Lab (L9–L16)**:
  - `ConcurrencyLab.java`: 5 deterministic educational demonstrations integrated into Seller Menu `[8]`:
    1. Race Condition Proof: `UnsafeCounter` (lost updates) vs `SafeCounter` (exact 40,000 count).
    2. Double-Sell Showdown: 20 concurrent `BuyerTask` threads race on the same car (exactly 1 winner, 19 rejected).
    3. Thread Lifecycle & Priority Tracer: traces `NEW -> RUNNABLE -> TIMED_WAITING -> BLOCKED -> WAITING -> TERMINATED` with priority and daemon explanation.
    4. Producer-Consumer Pipeline: 3 buyer producers submitting requests to bounded queue and 1 consumer processor.
    5. Thread Interruption & Memory Visibility: clean `interrupt()`/`join()` shutdown and `volatile` memory barrier awareness.
- **GATE 6 Concurrency Stress Suite**:
  - `ConcurrencyStressTest.java`: **20 consecutive stress rounds** of concurrent double-sell with 15 simultaneous threads per round. Zero deadlocks, exactly 1 winner per round, 100% deterministic invariants.
- **Evidence & Traceability**:
  - `docs/evidence/demo_concurrency_output.txt`: real execution output of all 5 Concurrency Lab demonstrations.
  - `docs/evidence/gate6_test_output.txt`: automated test suite execution log.
- **Documentation & Viva Guide**:
  - `docs/learning/M6_EXPLAINED.md`: technical explainer covering multithreading, thread states, monitor synchronization, guarded blocks, and viva prep.
- **Automated Tests**:
  - **109 / 109 core checks passing** (`TestRunner.java`); 9 / 9 exercise checks complete.
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).
### Phase 4: JDBC Persistence (L17–20) (`v0.4-jdbc`)
- **Relational Persistence Infrastructure**:
  - `DatabaseManager.java`: connection factory loading `org.sqlite.JDBC` driver, enabling `PRAGMA foreign_keys = ON;`.
  - `SchemaInitializer.java`: DDL schema execution creating `cars`, `sales`, `audit_log`, candidate keys `UNIQUE(brand, model)`, checks, indexes, and idempotent auto-seeding.
- **JDBC Repositories**:
  - `JdbcCarRepository.java`: full CRUD implementation using `PreparedStatement` only, cursor iteration over `ResultSet`, typed getter mapping.
  - `JdbcSaleRepository.java`: sales ledger persistence with transactional `recordSaleAtomic` using `setAutoCommit(false)`, `commit()`, and `rollback()`.
- **Relational Transactions in Sales**:
  - `SalesService.java` delegates to `JdbcSaleRepository.recordSaleAtomic` when operating in JDBC mode, ensuring multi-statement ACID safety.
- **SQL Injection Security Demonstration**:
  - `SqlInjectionTest.java`: proves parameter binding neutralizes `' OR '1'='1` and prevents unauthorized data exposure.
- **Parametric Dual-Storage Contract (GATE 4)**:
  - `RepositoryContractTest.java`: runs identical assertions across BOTH CSV and SQLite JDBC modes (`FileCarRepository` vs `JdbcCarRepository`, `FileSaleRepository` vs `JdbcSaleRepository`).
- **Runtime Dual-Storage Switch**:
  - `RevLineApp.java` supports launching with `--storage=jdbc` or default `--storage=csv`.
  - `StorageBootstrap.java` provides `migrateCsvToJdbc` for cross-engine data migration.
- **Documentation & Viva Guide**:
  - `docs/learning/M4_EXPLAINED.md`: architecture document, ACID mechanics, and 5 viva defense questions.
- **Automated Tests**:
  - 84 / 84 core checks passing (`TestRunner.java`, 252 ms).
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).

### Phase 6: Multithreading & Concurrency Architecture (L9–L16) (`v0.6-concurrency`)
- **Background Daemon Threads (L9–L12)**:
  - `AutoSaveThread.java`: low-priority (`MIN_PRIORITY`) daemon thread with 30s interval; executes snapshot persistence via `inventoryService.saveSnapshot()`; cleanly stops on `interrupt()` and `join()`.
  - `ReservationExpiryThread.java`: daemon thread sweeping `ReservationManager` min-heap every 5s; reverts expired holds to `AVAILABLE`.
  - `RevLineApp.java`: boots daemons and hooks graceful termination via `Runtime.getRuntime().addShutdownHook`.
- **Producer-Consumer Architecture (L15–L16)**:
  - `PurchaseRequest.java`: immutable transaction carrier for concurrent sales requests.
  - `PurchaseRequestQueue.java`: bounded buffer (capacity 5) with guarded blocks (`while(condition) wait()` and `notifyAll()`).
  - **EX-8**: `PurchaseRequestQueue.take()` uses a guarded FIFO dequeue with `while`, `wait()`, and `notifyAll()`; verified by `ConcurrencyStressTest`.
  - `SalesProcessor.java`: consumer worker thread processing requests from the bounded queue through `SalesService.sellCar()`.
- **Interactive Concurrency Lab (L9–L16)**:
  - `ConcurrencyLab.java`: 5 deterministic educational demonstrations integrated into Seller Menu `[8]`:
    1. Race Condition Proof: `UnsafeCounter` (lost updates) vs `SafeCounter` (exact 40,000 count).
    2. Double-Sell Showdown: 20 concurrent `BuyerTask` threads race on the same car (exactly 1 winner, 19 rejected).
    3. Thread Lifecycle & Priority Tracer: traces `NEW -> RUNNABLE -> TIMED_WAITING -> BLOCKED -> WAITING -> TERMINATED` with priority and daemon explanation.
    4. Producer-Consumer Pipeline: 3 buyer producers submitting requests to bounded queue and 1 consumer processor.
    5. Thread Interruption & Memory Visibility: clean `interrupt()`/`join()` shutdown and `volatile` memory barrier awareness.
- **GATE 6 Concurrency Stress Suite**:
  - `ConcurrencyStressTest.java`: **20 consecutive stress rounds** of concurrent double-sell with 15 simultaneous threads per round. Zero deadlocks, exactly 1 winner per round, 100% deterministic invariants.
- **Evidence & Traceability**:
  - `docs/evidence/demo_concurrency_output.txt`: real execution output of all 5 Concurrency Lab demonstrations.
  - `docs/evidence/gate6_test_output.txt`: automated test suite execution log.
- **Documentation & Viva Guide**:
  - `docs/learning/M6_EXPLAINED.md`: technical explainer covering multithreading, thread states, monitor synchronization, guarded blocks, and viva prep.
- **Automated Tests**:
  - **109 / 109 core checks passing** (`TestRunner.java`); 9 / 9 exercise checks complete.
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).

### Phase 5: Streams, Lambdas & Advanced Analytics (L21–24) (`v0.5-analytics`)
- **Showroom Reporting Pipelines (R1–R15)**:
  - `ReportService.java`: complete implementation of all 15 analytical reporting pipelines (R1 through R15) using Java 17 Streams, intermediate filter/map/sorted/skip/limit operations, and terminal collectors.
  - R1: Available cars list (`filter(Status.AVAILABLE)`).
  - R2: Stock value of available vehicles (`filter -> mapToDouble -> sum`).
  - R3: Total portfolio valuation across entire fleet (`mapToDouble -> sum`).
  - R4: Inventory counts grouped by fuel type (`groupingBy -> counting`, populated into `EnumMap`).
  - R5: Top-3 fastest supercars (`sorted(topSpeed.reversed()) -> limit(3)`).
  - R6: Top-N vehicles by horsepower (EX-4: `filter(!SOLD) -> sorted(hp.reversed()) -> limit(n) -> map(describe)`).
  - R7: Average vehicle price grouped by brand (EX-5: `groupingBy(getBrand, averagingDouble)`).
  - R8: Sorted list of distinct showroom brands (`map -> distinct -> sorted`).
  - R9: Paginated catalog view (`sorted(byId) -> skip((page-1)*size) -> limit(size)`).
  - R10: Total realized sales revenue (`sales.stream().mapToDouble.sum()`).
  - R11: Sales revenue aggregated by buyer (`groupingBy(buyer, summingDouble)`).
  - R12: Quickest 0–100 km/h acceleration per fuel type (`groupingBy(fuel, minBy(comparingDouble(accel)))`).
  - R13: 2-D price band matrix `int[3][4]` mapping fuel categories vs 4 price tiers (`< $500k`, `$500k-$1M`, `$1M-$2M`, `>= $2M`).
  - R14: Formatted comma-delimited vehicle titles string (`map -> collect(joining(", "))`).
  - R15: Intermediate stream laziness and short-circuit demonstration using `peek` counters.
- **Functional Programming & Method References (L21–22)**:
  - `LambdaShowcase.java`: demonstrations of `Predicate`, `Consumer`, `Function`, `Supplier`, custom `@FunctionalInterface VehicleTransformer`, and all 4 kinds of method references (static, bound instance, unbound instance, constructor).
- **Generics in Depth (L24)**:
  - `Pair<K, V>`: type-safe generic pair container.
  - `GenericUtils.java`: `<T extends Comparable<T>> maxOf` bounded method, `List<? extends Vehicle> printAll` upper-bounded wildcard method, and `buildPriceLadder` using `TreeMap`.
- **UI Integration & Text Export**:
  - `MenuPrinter.java` & `MenuController.java`: interactive Reports submenu with on-demand reporting and atomic file export to `data/exports/report_summary.txt`.
- **Gate 5 Verification Suite (GATE 5)**:
  - `ReportServiceAllReportsTest.java`: all 15 reports verified against independently hand-computed values.
  - `GenericsAndLambdaTest.java`: 6 automated tests validating `Pair`, `GenericUtils`, and `LambdaShowcase`.
- **Documentation & Viva Guide**:
  - `docs/learning/M5_EXPLAINED.md`: technical explainer and viva defense guide for Streams, Lambdas, Method References, and Generics.
- **Automated Tests**:
  - **105 / 105 core checks passing** (`TestRunner.java`, 263 ms); 8 / 9 exercises complete.
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).

### Phase 6: Multithreading & Concurrency Architecture (L9–L16) (`v0.6-concurrency`)
- **Background Daemon Threads (L9–L12)**:
  - `AutoSaveThread.java`: low-priority (`MIN_PRIORITY`) daemon thread with 30s interval; executes snapshot persistence via `inventoryService.saveSnapshot()`; cleanly stops on `interrupt()` and `join()`.
  - `ReservationExpiryThread.java`: daemon thread sweeping `ReservationManager` min-heap every 5s; reverts expired holds to `AVAILABLE`.
  - `RevLineApp.java`: boots daemons and hooks graceful termination via `Runtime.getRuntime().addShutdownHook`.
- **Producer-Consumer Architecture (L15–L16)**:
  - `PurchaseRequest.java`: immutable transaction carrier for concurrent sales requests.
  - `PurchaseRequestQueue.java`: bounded buffer (capacity 5) with guarded blocks (`while(condition) wait()` and `notifyAll()`).
  - **EX-8**: `PurchaseRequestQueue.take()` uses a guarded FIFO dequeue with `while`, `wait()`, and `notifyAll()`; verified by `ConcurrencyStressTest`.
  - `SalesProcessor.java`: consumer worker thread processing requests from the bounded queue through `SalesService.sellCar()`.
- **Interactive Concurrency Lab (L9–L16)**:
  - `ConcurrencyLab.java`: 5 deterministic educational demonstrations integrated into Seller Menu `[8]`:
    1. Race Condition Proof: `UnsafeCounter` (lost updates) vs `SafeCounter` (exact 40,000 count).
    2. Double-Sell Showdown: 20 concurrent `BuyerTask` threads race on the same car (exactly 1 winner, 19 rejected).
    3. Thread Lifecycle & Priority Tracer: traces `NEW -> RUNNABLE -> TIMED_WAITING -> BLOCKED -> WAITING -> TERMINATED` with priority and daemon explanation.
    4. Producer-Consumer Pipeline: 3 buyer producers submitting requests to bounded queue and 1 consumer processor.
    5. Thread Interruption & Memory Visibility: clean `interrupt()`/`join()` shutdown and `volatile` memory barrier awareness.
- **GATE 6 Concurrency Stress Suite**:
  - `ConcurrencyStressTest.java`: **20 consecutive stress rounds** of concurrent double-sell with 15 simultaneous threads per round. Zero deadlocks, exactly 1 winner per round, 100% deterministic invariants.
- **Evidence & Traceability**:
  - `docs/evidence/demo_concurrency_output.txt`: real execution output of all 5 Concurrency Lab demonstrations.
  - `docs/evidence/gate6_test_output.txt`: automated test suite execution log.
- **Documentation & Viva Guide**:
  - `docs/learning/M6_EXPLAINED.md`: technical explainer covering multithreading, thread states, monitor synchronization, guarded blocks, and viva prep.
- **Automated Tests**:
  - **109 / 109 core checks passing** (`TestRunner.java`); 9 / 9 exercise checks complete.
- **Build Status**: **PASS** (`javac --release 17 -Xlint:all`, 0 warnings, 0 errors).
