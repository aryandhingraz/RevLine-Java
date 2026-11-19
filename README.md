# RevLine — Car Management System in Java

> **"Drive Passion. Live RevLine."**

A layered, enterprise-grade, console-driven backend application managing a supercar showroom — built as the guided project for **25CSE0202 · Programming in Java** (Unit VI), Chitkara University.

[![Java 17](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Build Status](https://img.shields.io/badge/Build-Passing%20(0%20warnings)-brightgreen.svg)]()
[![Tests Passing](https://img.shields.io/badge/Tests-116%2F116%20Passed-brightgreen.svg)]()
[![Exercises](https://img.shields.io/badge/Exercises-9%2F9%20Complete-brightgreen.svg)]()
[![Storage](https://img.shields.io/badge/Storage-CSV%20%7C%20SQLite%20JDBC-blue.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 1. Problem Statement & Objectives

RevLine transforms a legacy procedural C prototype into a robust, object-oriented, concurrent showroom management system. It eliminates fatal memory crashes, unbounded input buffer overflows, race conditions in vehicle purchasing, and direct in-place file corruptions.

- **Problem Statement**: Detailed specification in `docs/01_PROBLEM_STATEMENT.md`.
- **Objectives & Scope**: 10 SMART objectives (O1–O10) and boundary assumptions in `docs/02_OBJECTIVES_SCOPE.md`.
- **C to Java Conversion Report**: Full mapping and 12 defect resolutions in `docs/05_C_TO_JAVA_REPORT.md`.

---

## 2. Complete Feature Matrix

| # | Feature Domain | Concrete Implementation | Syllabus Alignment | Status |
|---|----------------|-------------------------|--------------------|:------:|
| 1 | **Inventory CRUD** | Add, update, delete, view, restore via `InventoryService` with O(1) duplicate guard. | L5, L6–L8 | ✅ **Complete** |
| 2 | **Interactive Browser** | Bidirectional Next / Previous showroom browsing via `ListIterator<Car>`. | L6, L7 | ✅ **Complete** |
| 3 | **Multi-Field Search** | Case-insensitive multi-token querying across brand, model, and fuel (EX-3). | L6, L8 | ✅ **Complete** |
| 4 | **Composable Filters** | `FilterBuilder` chaining `Predicate<Car>` filters (`and()`, `negate()`). | L21, L23 | ✅ **Complete** |
| 5 | **Multi-Spec Sorting** | 7 sorting strategies via `CarComparators` + EX-2 `byValueForMoney()`. | L8 | ✅ **Complete** |
| 6 | **Side-by-Side Matrix** | 2–4 vehicle specification comparison matrix with dynamic `*BEST` markers. | L2 | ✅ **Complete** |
| 7 | **Sales & Atomic Ledger** | Synchronized `sellCar()`, ASCII receipt generation, rollback safety, `Sale` record. | L13, L16, L22 | ✅ **Complete** |
| 8 | **Priority Holds & Waitlist** | Min-heap `ReservationManager` with sweep, FIFO `WaitlistService` queue. | L7, L8 | ✅ **Complete** |
| 9 | **LIFO Undo Stack** | `UndoManager` stack (`ArrayDeque<UndoAction>`) reversing ADD, UPDATE, DELETE (EX-7). | L6, L7 | ✅ **Complete** |
| 10 | **LRU Showroom Cache** | `RecentlyViewed` extending `LinkedHashMap` (`accessOrder = true`, capacity 5). | L9 | ✅ **Complete** |
| 11 | **Analytical Streams (R1–R15)**| 15 stream pipelines, primitive `DoubleStream`, downstream collectors (EX-4, EX-5). | L21–L23 | ✅ **Complete** |
| 12 | **Functional Programming** | `LambdaShowcase` demonstrating all 4 method reference kinds and standard interfaces. | L23 | ✅ **Complete** |
| 13 | **Generics in Depth** | Type-safe `Pair<K,V>`, bounded `<T extends Comparable<T>> maxOf`, wildcard traversal. | L24 | ✅ **Complete** |
| 14 | **Dual-Storage Engine** | Dynamic runtime switching between CSV flat-file and SQLite JDBC (`--storage=jdbc`). | L17–L20, L24 | ✅ **Complete** |
| 15 | **SQL Injection Immunity** | Complete parameter binding via `PreparedStatement`; verified with `' OR '1'='1`. | L21 | ✅ **Complete** |
| 16 | **Relational Transactions** | Multi-statement ACID transactions (`setAutoCommit(false)`, `commit()`, `rollback()`). | L22 | ✅ **Complete** |
| 17 | **Background Daemons** | `AutoSaveThread` (`MIN_PRIORITY`, daemon, 30s) and `ReservationExpiryThread`. | L10–L12 | ✅ **Complete** |
| 18 | **Producer-Consumer Buffer** | `PurchaseRequestQueue` (capacity 5, guarded wait/notifyAll, EX-8) and `SalesProcessor`. | L13–L16 | ✅ **Complete** |
| 19 | **Concurrency Lab & Stress** | 5 deterministic educational demos + **20 stress rounds** of concurrent double-sell. | L9–L16 | ✅ **Complete** |
| 20 | **Enterprise Data Tools** | Legacy C import, CSV import with line-by-line error reports, `StreamTokenizer` script batch, byte-identical backup/restore, synthetic generator. | L25–L27 | ✅ **Complete** |
| 21 | **Crash-Safe File Writes** | Atomic `.tmp` file write, `.bak` backup promotion, and safe rollback via `safeWrite`. | L16, L25 | ✅ **Complete** |
| 22 | **Operational Audit Logging**| Persistent, timestamped audit log saved to `logs/audit.log` and inspectable in CLI. | L2, L25 | ✅ **Complete** |

---

## 3. Quick Start (Windows & Unix)

### Prerequisites
- **JDK 17 LTS** (Eclipse Temurin 17+ recommended)
- **Git**

### Clone & Build
```powershell
# Windows (PowerShell)
git clone https://github.com/aryandhingraz/RevLine-Java.git
cd RevLine-Java
.\scripts\build.bat
```
```bash
# Unix (Linux / macOS)
git clone https://github.com/aryandhingraz/RevLine-Java.git
cd RevLine-Java
chmod +x scripts/*.sh
./scripts/build.sh
```

### Run Application
```powershell
# Standard CSV Flat-File Storage (Default)
.\scripts\run.bat

# Relational SQLite JDBC Storage Mode
.\scripts\run.bat --storage=jdbc
```

### Run In-House Test Suite
```powershell
.\scripts\test.bat
```
*Executes all 116 automated checks in ~620 ms with zero dependencies.*

---

## 4. Project Directory Layout

```
RevLine-Java/
├─ src/main/java/com/revline/
│  ├─ app/            Entry point (RevLineApp), StorageBootstrap, MenuController, View Handlers
│  ├─ ui/             ConsoleTable (box renderer), MenuPrinter, InputReader, Banner
│  ├─ model/          Vehicle (abstract), Car, Sale, Reservation, AuditEntry, enums (FuelType, Status)
│  ├─ service/        InventoryService, SearchService, SalesService, Comparators, UndoManager,
│  │                  RecentlyViewed (LRU), ReportService, DataToolsService, LambdaShowcase
│  ├─ repository/     Repository (generic), CarRepository, SaleRepository, File & JDBC implementations
│  ├─ concurrency/    AutoSaveThread, ReservationExpiryThread, PurchaseRequestQueue,
│  │                  SalesProcessor, ConcurrencyLab (5 demos), ConcurrencyLabDemos
│  └─ util/           Validator, AppLogger, FileUtil, Pair, GenericUtils, exceptions/
├─ src/test/java/com/revline/
│  ├─ Assert.java     Custom assertion framework (zero third-party dependencies)
│  ├─ TestRunner.java Suite orchestrator executing 116 automated checks
│  └─ *Test.java      22 test classes covering unit, boundary, stress, security, contract
├─ data/              Seed data (cars.csv, sales.csv), legacy files, samples, backups, exports
├─ lib/               sqlite-jdbc-3.54.1.0.jar (single permitted driver dependency)
├─ scripts/           build, test, run, demo scripts (.bat and .sh)
├─ sql/               Relational DDL schema, seed data, sample queries
└─ docs/              All coursework deliverables, architecture diagrams, test plan, viva guides
```

---

## 5. Student Contributions & Academic Integrity

This project follows the **pair-programmer and technical mentor model**, adhering strictly to academic integrity standards. All 9 student exercises were implemented and owned by the student:

| Exercise | Component & Method | Syllabus | Status | Evidence |
|---|---|---|:---:|---|
| **EX-1 (Part A)** | `Validator.isValidFuelType(String)` | L3 | ✅ Complete | Commit `7fbdfd3` |
| **EX-1 (Part B)** | `Validator.parsePositiveDouble(String, String)` | L3 | ✅ Complete | Commit `7fbdfd3` |
| **EX-2** | `CarComparators.byValueForMoney()` | L8 | ✅ Complete | Commit `fc78366` |
| **EX-3** | `SearchService.search(String query)` | L6, L8 | ✅ Complete | Commit `fc2bb19` |
| **EX-4** | `ReportService.topNByHorsepower(int)` | L21–L23 | ✅ Complete | Commit `3448e62` |
| **EX-5** | `ReportService.averagePriceByBrand()` | L21–L23 | ✅ Complete | Commit `3448e62` |
| **EX-6** | `JdbcCarRepository.findByPriceRange(double, double)` | L17–L20 | ✅ Complete | Commit `3448e62` |
| **EX-7** | `UndoManager.undo(InventoryService)` | L6–L7 | ✅ Complete | Commit `197baaa` |
| **EX-8** | `PurchaseRequestQueue.take()` | L13–L16 | ✅ Complete | Commit `e5e433e` |

---

## 6. Comprehensive Documentation Index

All coursework deliverables are located in the `docs/` folder:
- **`docs/01_PROBLEM_STATEMENT.md`**: Student problem statement.
- **`docs/02_OBJECTIVES_SCOPE.md`**: SMART objectives and scope.
- **`docs/03_DESIGN.md`**: Complete system architecture and 6 Mermaid diagrams.
- **`docs/04_CONCEPT_MAP.md`**: Syllabus lecture mapping for Lectures 1–30.
- **`docs/05_C_TO_JAVA_REPORT.md`**: Legacy C translation and defect resolution report.
- **`docs/TEST_PLAN.md`**: Specification of all 116 automated checks.
- **`docs/VIVA.md`**: 85 categorized viva questions with code pointers.
- **`docs/learning/MOCK_VIVA.md`**: 20 representative mock viva practice questions.
- **`docs/DEMO_SCRIPT.md`**: 10-minute live evaluation presentation script.
- **`docs/FINAL_AUDIT.md`**: Final self-audit checklist (all 14 items green).
- **`docs/RUBRIC_TRACEABILITY.md`**: Traceability table across Rubric Rows 1–5 (25/25 target).

---

## 7. License

This project is licensed under the [MIT License](LICENSE).
