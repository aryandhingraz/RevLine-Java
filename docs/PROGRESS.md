# Project Progress and Rubric Traceability — RevLine

**Course:** 25CSE0202 — Programming in Java  
**Project:** RevLine — Car Management System in Java  
**Target:** Java 17 (`--release 17`)

## 1. Rubric traceability

| Rubric component | Marks available | Project deliverable | Evidence |
|---|---:|---|---|
| Problem Statement | 3 | `docs/01_PROBLEM_STATEMENT.md` | Defines the showroom problem, users, proposed Java solution, and project boundaries. |
| Project Objectives and Scope | 2 | `docs/02_OBJECTIVES_SCOPE.md` | Lists objectives, use cases, assumptions, and out-of-scope work. |
| Project Design and Prototype | 9 | `docs/03_DESIGN.md`, diagrams, Java source | Documents architecture, data model, workflows, persistence, and the console prototype. |
| Integration of Course Concepts | 6 | `docs/04_CONCEPT_MAP.md`, Java source, SQL | Maps syllabus topics to implementation examples. |
| Code Quality, Progress Evidence and Documentation | 5 | README, CHANGELOG, source structure, Git history, this file, presentation | Provides setup instructions, structure, milestones, and supporting documentation. |

The evaluator awards marks. This mapping does not predict or guarantee a grade.

## 2. Project milestones

The repository retains historical milestone tags. Their presence records development history; it does not prove that every feature was independently tested at each milestone.

| Tag | Milestone focus |
|---|---|
| `v0.1-prototype` | Initial inventory management and file persistence |
| `v0.3-ca3` | Collections, search, sorting, comparison, and inventory workflows |
| `v0.4-jdbc` | SQLite JDBC persistence |
| `v0.5-analytics` | Stream-based reports and generic/lambda examples |
| `v0.6-concurrency` | Threading, synchronization, and producer-consumer demonstration |
| `v0.7-datatools` | File import/export and data tools |
| `v1.0-final` | Formal documentation and final project structure |
| `cleanup/final-polish` | Cleanup and reliability improvements |

## 3. Application structure

The application is organized into seven packages:

| Package | Responsibility |
|---|---|
| `app` | Startup, storage selection, menu controller, and menu handlers |
| `ui` | Console input, menus, banners, and table formatting |
| `model` | Vehicle, car, sale, reservation, audit entry, and enums |
| `service` | Business rules, search, reports, reservations, undo, and data tools |
| `repository` | CSV and JDBC persistence implementations |
| `concurrency` | Background threads and interactive concurrency demonstrations |
| `util` | Validation, file utilities, paths, logging, and custom exceptions |

## 4. Verification performed for this packaged copy

The following checks were independently repeated against this archive during the final submission audit on Windows:

| Check | Result | Evidence / limitation |
|---|---|---|
| Archive structure | PASS | One `RevLine-Java/` project root; no C source folder; no compiled `.class` files; source, required seed data, SQL, scripts, docs, diagrams, presentation, and SQLite JDBC driver retained. |
| Java source count | PASS | 70 production `.java` files found under `src/main/java`. Every file is <= 300 lines (max: `InventoryService` at 291 lines). |
| Java 17 target compilation | PASS | `scripts/build.bat` completed on Windows with `javac --release 17 -Xlint:all -Werror` and zero warnings. |
| Windows runtime execution | PASS | Both `scripts\build.bat` and `scripts\run.bat` (`--storage=csv` and `--storage=jdbc`) executed directly under Windows PowerShell. |
| CSV-mode full functional regression | PASS | Verified all Buyer workflows (Browse, Search, Sort, Compare, Reserve, Purchase, Receipt, Waitlist, LRU cache), Seller workflows (Inventory list, Add, Update with optional Enter defaults, Delete, Undo, Sales Ledger, Audit log), Analytics reports R1–R15, Concurrency Lab Demos 1–5, and Data Tools (Legacy import, CSV batch import, ScriptTokenizer, ByteStream backup/restore). |
| SQLite JDBC full functional regression | PASS | Verified schema & index initialization, Buyer reserve/purchase transactions, Seller CRUD, Undo, Analytics, and Concurrency Lab with `--storage=jdbc`. |
| Windows SQLite backup & restore with JDBC active | PASS | Verified `closeDatabaseConnection()` before restore, file release retry loop, byte-stream file overwriting, and safe reconnection without Windows `WinError 32` file locks. |
| Persistence & restart recovery | PASS | Verified stale/expired reservations auto-reset to `AVAILABLE` on restart in both CSV and JDBC modes; sales ledger persistence verified across application restarts. |
| Concurrency demonstrations | PASS | All 5 demos verified: race condition vs atomic synchronized counter, 20-thread double-sell with 1 winner, 6 thread states lifecycle, wait/notifyAll bounded buffer producer-consumer, daemon thread clean shutdown. |

All core syllabus workflows and boundaries have been verified on Windows.

## 5. Syllabus concept mapping

See `docs/04_CONCEPT_MAP.md` for lecture-to-code references. The source includes examples of:

- OOP, classes, objects, inheritance, interfaces, abstraction, and polymorphism.
- Arrays, strings, `StringBuilder`, wrappers, and formatted output.
- Checked and unchecked exceptions, custom exceptions, and input validation.
- Collections, maps, sets, queues, deques, iterators, comparators, and sorting.
- `Thread`, `Runnable`, synchronization, intrinsic locks, `wait`, `notifyAll`, and visibility.
- JDBC, SQL, `DriverManager`, `Connection`, prepared statements, CRUD, and `ResultSet`.
- Lambdas, functional interfaces, method references, streams, and generics.
- Byte/character streams, `StreamTokenizer`, CSV handling, and file operations.

A code reference shows where a concept appears; it does not by itself prove that a workflow has been demonstrated successfully. The student should confirm each concept against the actual course syllabus and be prepared to explain the implementation.

## 6. Known scope boundaries

RevLine is a local console project for one showroom. It does not implement authentication, role-based access control, online payments, a GUI, a web API, cloud deployment, or multi-showroom networking.

CSV is the default storage mode. SQLite is an optional JDBC mode. The application includes a Java importer for the retained legacy-format sample data; the original C source code is not part of this repository.

## 7. Student review before submission

- Complete `docs/TEAM_OWNERSHIP.md` using the actual team members and their real contributions.
- Review the problem statement, objectives, and presentation and ensure they reflect your own understanding and work.
- Run the application on the actual submission machine and test every Buyer and Seller menu option.
- Test CSV and JDBC modes, persistence after restart, import/export, reports, reservations, sales, undo, backup/restore, and concurrency demonstrations.
- Verify Windows scripts on Windows if that is the submission platform.
- Be ready to explain the code and the Java concepts without relying only on the presentation.
