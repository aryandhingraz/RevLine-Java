# Changelog

All notable changes to the RevLine Supercar Management System (`RevLine-Java`) are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [v1.0-final] - 2026-11-19 (CA-4 Milestone)

### Added
- **Comprehensive Lecture Concept Map**: `docs/04_CONCEPT_MAP.md` mapping every single lecture 1–30 to verified class/method references.
- **Legacy C Migration Report**: `docs/05_C_TO_JAVA_REPORT.md` documenting function translations, 12 C defect resolutions, and preserved UX behaviors.
- **Comprehensive Test Plan**: `docs/TEST_PLAN.md` documenting all 116 tests across unit, boundary, negative, security, stress, and round-trip categories.
- **Viva Defense Master Bank**: `docs/VIVA.md` containing 85 categorized questions with code pointers.
- **Mock Viva Evaluation Guide**: `docs/learning/MOCK_VIVA.md` with 20 representative questions across syllabus units.
- **Final Self-Audit**: `docs/FINAL_AUDIT.md` verifying all 14 criteria of PART 17 with green checks.
- **Modular Controller Architecture**: Extracted `BuyerMenuHandler`, `SellerMenuHandler`, `ReportsMenuHandler`, and `DataToolsMenuHandler` from `MenuController` to enforce strict `<= 300` lines maintainability invariant across all classes.
- **Sub-Service Decompositions**: Extracted `ReportSummaryFormatter`, `ConcurrencyLabDemos`, `ByteStreamBackup`, `ScriptRunner`, and `SyntheticDataGenerator`.

### Changed
- Refactored `InventoryService` and `DataToolsService` to bring all class files in `src/main` under 275 lines.
- Updated `docs/DEMO_SCRIPT.md` to a rehearsed 10-minute presentation flow covering dual-storage, concurrency, analytics, and data tools.
- Upgraded `README.md` with final badges, complete feature guide, quickstart, and AI-assisted development transparency section.

---

## [v0.7-datatools] - 2026-11-19

### Added
- **Legacy C Flat-File Migration (L26–L27)**: `DataToolsService.importLegacyC` importing 9-column comma-separated `cars.txt` with status integer translation (`1` -> AVAILABLE, `0` -> SOLD).
- **Batch CSV Importer with Multi-Catch & Error Reporting (L3, L26–L27)**: `DataToolsService.importCsv` with `catch (NumberFormatException | InvalidCarException ex)` generating structured error reports in `data/reports/import_errors_*.txt`.
- **Command DSL Script Importer via StreamTokenizer (L25)**: `DataToolsService.importScript` lexically parsing `ADD`, `SELL`, and `RESERVE` batch automation commands with quote and comment support.
- **Lossless Byte-Stream Backup & Restore (L25)**: `DataToolsService.backupFile` and `restoreFile` with 4KB buffer binary stream copying and byte-identical verification.
- **Cross-Repository Storage Migration (L24)**: `DataToolsService.migrate` for lossless data migration between CSV and SQLite repositories.
- **Synthetic Supercar Generator (L4)**: `DataToolsService.generateSyntheticCars` generating deterministic valid test fleets.
- **Data Tools Submenu `[9]`**: Integrated batch tools into management menu.
- **Tests**: `DataToolsTest` adding 7 automated tests (116/116 total checks passing).
- **Documentation**: `docs/learning/M7_EXPLAINED.md`.

---

## [v0.6-concurrency] - 2026-11-19

### Added
- **Background Daemon Threads (L9–L12)**: `AutoSaveThread` with 30s interval at `MIN_PRIORITY` and `ReservationExpiryThread` sweeping hold holds every 5s.
- **Producer-Consumer Bounded Buffer Pipeline (L15–L16)**: `PurchaseRequest`, `PurchaseRequestQueue` (bounded 5 slots with guarded `wait()`/`notifyAll()`), and consumer worker `SalesProcessor`.
- **Student Exercise EX-8**: `PurchaseRequestQueue.take()` implemented with while-guarded block and notification signaling.
- **Interactive Concurrency Lab `[8]`**: 5 deterministic educational demonstrations (race condition proof, double-sell showdown, lifecycle tracer, producer-consumer, interruption/visibility).
- **Concurrency Stress Test Suite**: `ConcurrencyStressTest` with 20 consecutive stress rounds of concurrent double-sell (zero deadlocks, exactly 1 winner per round).
- **Documentation**: `docs/learning/M6_EXPLAINED.md` and `EX8_HINT.md`.

---

## [v0.5-analytics] - 2026-11-19

### Added
- **Analytical Reporting Pipelines (R1–R15)**: `ReportService` implementing 15 stream pipelines using Java 17 Streams, intermediate filter/map/sorted/skip/limit operations, and terminal collectors.
- **Student Exercise EX-4**: `ReportService.topNByHorsepower(int)`.
- **Student Exercise EX-5**: `ReportService.averagePriceByBrand()`.
- **Functional Programming Showcase (L21–L22)**: `LambdaShowcase` demonstrating `Predicate`, `Consumer`, `Function`, `Supplier`, custom `@FunctionalInterface VehicleTransformer`, and all 4 method reference kinds (static, bound, unbound, constructor).
- **Generics in Depth (L24)**: Generic container `Pair<K, V>`, `<T extends Comparable<T>> maxOf`, `List<? extends Vehicle> printAll` wildcard method, and `TreeMap` price ladder.
- **Reports Submenu `[7]`**: Interactive analytical reporting and atomic text export to `data/exports/report_summary.txt`.
- **Tests**: `ReportServiceAllReportsTest` and `GenericsAndLambdaTest` (105 total checks).
- **Documentation**: `docs/learning/M5_EXPLAINED.md`.

---

## [v0.4-jdbc] - 2026-11-19

### Added
- **Relational Persistence Infrastructure (L17–L20)**: `DatabaseManager` connection factory loading SQLite JDBC driver with `PRAGMA foreign_keys = ON;`.
- **DDL Schema Initializer**: `SchemaInitializer` creating `cars`, `sales`, and `audit_log` tables with candidate keys `UNIQUE (brand, model)`, checks, and idempotent auto-seeding.
- **JDBC Repositories**: `JdbcCarRepository` using `PreparedStatement` parameter binding and typed `ResultSet` mapping; `JdbcSaleRepository` with atomic transactions (`setAutoCommit(false)`, `commit()`, `rollback()`).
- **Student Exercise EX-6**: `JdbcCarRepository.findByPriceRange(double, double)` with parameterized query binding.
- **SQL Injection Security Suite**: `SqlInjectionTest` verifying parameter binding neutralizes `' OR '1'='1` injection attacks.
- **Dual-Storage Contract**: `RepositoryContractTest` verifying identical CRUD semantics across CSV and SQLite modes.
- **Runtime Switch**: `--storage=jdbc` CLI argument support.
- **Documentation**: `docs/learning/M4_EXPLAINED.md`.

---

## [v0.3-ca3] - 2026-11-19 (CA-3 Milestone)

### Added
- **Vehicle Comparators (L8)**: `CarComparators` with sort strategies and student-authored `byValueForMoney()` (EX-2).
- **Composable Filter Builder (L21)**: `FilterBuilder` chaining `Predicate<Car>` filters with `and()` and `negate()`.
- **Side-by-Side Spec Matrix (L2)**: `ComparisonService` rendering multi-car comparisons with `*BEST` winner markers.
- **LRU Recently Viewed Cache (L9)**: `RecentlyViewed` extending `LinkedHashMap` with access-order eviction.
- **Sales Transaction Ledger (L16)**: `FileSaleRepository` persisting completed sales records and printable ASCII receipts.
- **Priority Holds & Waitlist (L7, L8)**: `ReservationManager` with min-heap priority queue and `WaitlistService` FIFO queue.
- **Undo Mutation Stack (L6–L7)**: `UndoManager` with `ArrayDeque<UndoAction>` LIFO stack (EX-7).
- **Thread-Safe Sales Service (L13)**: `SalesService.sellCar()` synchronized on shared inventory lock.
- **Dual View CLI Navigation**: `MenuController` supporting instant `[S]` toggling between Client and Management views.
- **Student Exercises**: Completed EX-2 and EX-7.
- **Tests**: 76 automated core checks passing.
- **Documentation**: `docs/CA3_SUBMISSION_CHECKLIST.md` and `docs/learning/M2_M3_EXPLAINED.md`.

---

## [v0.1-prototype] - 2026-11-19

### Added
- Domain Model: `Vehicle` (abstract), `Car`, `Sale`, `Reservation`, `AuditEntry`, `FuelType`, `Status`, `Describable`.
- Repository Layer: generic `Repository<T, ID>`, `CarRepository`, `SaleRepository`, `FileCarRepository`.
- Crash-safe CSV persistence via temp file + atomic rename in `FileUtil.safeWrite`.
- Service Layer: `InventoryService` (dual collection ArrayList + HashMap, duplicate guard, sorted brands TreeSet, defensive copy), `SearchService`.
- UI: box-drawing table renderer (`ConsoleTable`), ASCII brand logo and boot panel (`Banner`), exception-safe line reader (`InputReader`), menus (`MenuPrinter`).
- Storage auto-bootstrap: automatically initializes `data/cars.csv` from verified seed data.
- In-house testing framework: `Assert` and `TestRunner` with 34 automated unit checks.
- Student exercise specifications: EX-1 (`Validator`) and EX-3 (`SearchService`).
- Relational SQL schema, seed data, and queries in `sql/`.
- Documentation: `docs/00_UNDERSTANDING.md`, `docs/03_DESIGN.md` (6 Mermaid diagrams, data dictionary, collection-choice table), `docs/learning/M1_EXPLAINED.md`, `EX1_HINT.md`, `EX3_HINT.md`, `EX_LOG.md`.
