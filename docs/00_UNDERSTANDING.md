# 00 — Understanding Report

> **Phase 0 deliverable** — created before writing any feature code.
> This document proves the agent read and understood every input before building.

---

## A. Function-by-Function Summary of `main.c`

The C prototype is a single-file, 624-line console application for a car showroom.

| # | Function | Lines | What It Does |
|---|----------|-------|--------------|
| 1 | `main()` | 75–147 | Entry point. Allocates `Car showroom[100]` via `malloc`. Calls `system("chcp 65001")` for UTF-8. Calls `initDatabase` to load/seed. Prints ASCII "RevLine" logo + tagline. Runs a `while` loop with `switch` on menu choice (1–6, 0=exit). Frees memory on exit. |
| 2 | `initDatabase(showroom, &carCount)` | 41–70 | Prints a box-drawing "SYSTEM BOOT" panel. Tries `fopen("cars.txt","r")` — if missing, creates empty file and sets count=0. If present, calls `loadCarsFromFile`. If count==0, calls `loadDefaultCars` + `saveCarsToFile` to seed 18 cars. Reports status inside the boot panel. |
| 3 | `loadCarsFromFile(showroom)` | 172–198 | Opens `cars.txt`. Uses `fscanf` with format `%d,%49[^,],%49[^,],%lf,%d,%19[^,],%d,%lf,%d\n` to read 9 comma-separated fields per line. Stops at `MAX_CARS=100`. Returns count. No validation — garbage input silently corrupts data. |
| 4 | `saveCarsToFile(showroom, count)` | 200–224 | Opens `cars.txt` with `"w"` (overwrites in-place — **crash-unsafe**). Writes all cars using `fprintf` with `%lf` for doubles (produces 6 decimal places like `3000000.000000`). |
| 5 | `displayMenu()` | 150–167 | Prints a box-drawing menu with options [1] Add, [2] Display, [3] Search, [4] Filter, [5] Sort, [6] Buy, [0] Exit. |
| 6 | `printTableTop(title)` | 229–236 | Prints table header row: `ID | Brand | Model | Price ($) | Speed | HP | Accel | Fuel Type | Status` with box-drawing borders. Fixed-width columns. |
| 7 | `printTableRow(car)` | 238–241 | Prints one data row. Status shown as `Available` or `Sold` based on `isAvailable` flag. Price has no comma formatting (`%-9.0f`). |
| 8 | `printTableBottom(total, available, totalValue)` | 243–252 | Prints footer with total cars, available count, and total showroom value. **Bug: totalValue sums ALL cars including SOLD** — should be AVAILABLE only for "stock value". |
| 9 | `displayAllCars(showroom, count)` | 257–280 | Calls printTableTop/Row/Bottom. Computes `totalValue` by summing ALL prices (including sold). `availableCount` is correct but totalValue is wrong per PPT problem #3. |
| 10 | `addCar(showroom, &count)` | 285–349 | Checks `MAX_CARS` capacity. Auto-generates ID = max existing ID + 1. Reads brand/model via `scanf(" %49[^\n]")`, price/speed/hp/accel via `scanf("%lf"/"%d")` — **no validation on numeric fields** (can enter letters → garbage). Status input validates 0 or 1. Prints success message. |
| 11 | `searchCar(showroom, count)` | 354–414 | Sub-menu: [1] by ID (exact match), [2] by Brand (uses `strstr` for substring — **case-sensitive**, documented as "Case-sensitive" in prompt). Prints results in table. |
| 12 | `filterCars(showroom, count)` | 419–485 | Sub-menu: [1] Available only, [2] By max price. Simple loop with match flag. Prints filter results. |
| 13 | `sortCars(showroom, count)` | 490–539 | Sub-menu: [1] Price low→high, [2] Price high→low, [3] Speed desc, [4] HP desc. Uses **bubble sort** directly on the showroom array — **permanently reorders data** and saves the new order. |
| 14 | `buyCar(showroom, count)` | 544–592 | Asks for car ID. Linear search. If not found → error. If `isAvailable==0` → "already Sold". Otherwise sets `isAvailable=0` and prints a receipt box with "R E V L I N E  M O T O R S", brand, model, price, "CONGRATULATIONS ON YOUR NEW SUPERCAR!". **No buyer name, no date, no sale record, no concurrency protection.** |
| 15 | `loadDefaultCars(showroom, &count)` | 597–624 | Hard-codes 18 cars. **Bug: all 18 have `isAvailable=1`** (all Available), but `cars.txt` has IDs 3, 4, 18 as `0` (Sold). This means if the file is deleted and defaults are loaded, sold cars become available again — inconsistent. |

### Key Data: The 18 Seeded Cars

IDs 1–18, 10 brands (Bugatti, Ferrari, Lamborghini, Porsche, McLaren, Aston Martin, Tesla, Koenigsegg, Pagani, BMW, Audi). Fuel types: Petrol (13), Hybrid (4), Electric (1). In `cars.txt`: IDs 3 (Ferrari LaFerrari), 4 (Ferrari SF90 Stradale), 18 (Audi R8 V10 Plus) are marked Sold (flag=0). Prices range from \$135,990 (Tesla) to \$3,000,000 (Bugatti Chiron, Aston Martin Valkyrie, Koenigsegg Jesko).

---

## B. Slide-by-Slide Summary of the Real PPT (`RevLine_-_Car_Management_System_in_Java.pptx`)

> **Source:** Extracted directly from the 11 slide XML files (`ppt/slides/slide{1..11}.xml`) and 10 speaker notes files (`ppt/notesSlides/notesSlide{1..10}.xml`) within `reference/RevLine_-_Car_Management_System_in_Java.pptx`.

### Slide 1 — Title
- **Slide Content:**
  - Header tags: `JAVA`, `COLLECTIONS`, `STREAMS + JDBC`, `GUIDED JAVA PROJECT`
  - Title: **RevLine — Car Management System in Java**
  - Tagline: *"Drive Passion. Live RevLine."*
- **Speaker Notes:**
  - *"INTRO: RevLine is a Java-based car management system. It replaces manual notebook tracking with a structured backend. The project demonstrates Core Java, Collections, Streams, JDBC, file handling and concurrency. Opening line: 'Our aim is to keep car inventory accurate, make searching easy, and record every sale.' (1)"*

### Slide 2 — 01 / DESIGN: Problem and Use Cases
- **Slide Content:**
  - Metrics banner: `18 seeded cars` | `3 roles` | `4 core packages` | `5 Stream reports`
  - Problem / Users / Goal overview:
    - *The problem:* Manual notebook tracking leaves car stock counts out of sync.
    - *The users:* Sellers manage stock, buyers search inventory, admins control roles.
    - *The goal:* Core Java services with Collections, JDBC persistence, Stream analytics.
  - Actor / Use Case mapping table:
    - `Seller` | Add a car | ID is auto-generated
    - `Seller` | Update or delete a car | Validated state check
    - `Seller` | Mark a car sold | Saves buyer, date, price
    - `Seller` | Generate sales report | Revenue and stock via Streams
    - `Buyer` | Search brand or model | Case-insensitive match
    - `Buyer` | Filter by budget and fuel | Combined in one query
    - `Buyer` | Reserve or buy a car | Only one buyer wins
    - `Admin` | Authenticate and assign roles | Controls modification permissions
- **Speaker Notes:**
  - *"PROBLEM & USE CASES: Manual records can become outdated, so a sold car may still appear available. There are two main actors: Seller and Buyer. Seller: add/update/remove cars, mark a car sold and view reports. Buyer: search by brand/model, filter by budget/fuel and reserve or buy. The project starts with 18 sample cars. The goal is reliable inventory and sales tracking. (2)"*

### Slide 3 — 02 / MODULES: Core Modules Used
- **Slide Content:**
  - Subtitle: *"The backend application is divided into seven functional Java modules"* / *"Pure backend modular architecture — zero UI coupling"*
  - Six cards displayed on slide:
    1. *Inventory management:* Add, update, remove, index cars via ArrayList plus HashMap.
    2. *Sales and reservation:* Synchronized booking locks prevent duplicate car sales across threads.
    3. *Search and spec comparison:* Predicate filter chains plus Comparator multi-metric vehicle ranking.
    4. *Stream analytics reports:* Real-time stock valuation, fuel distribution, speed leaderboards.
    5. *Repository persistence:* Decoupled FileCarRepository CSV storage plus transactional JdbcCarRepository.
    6. *Validation and audit logging:* Custom InvalidCarException checks alongside timestamped AppLogger trails.
- **Speaker Notes:**
  - *"SIX CORE MODULES: 1. Inventory management — add, update, remove and index cars. ArrayList stores the list; HashMap finds a car by ID. 2. Sales and reservation — controls booking and prevents two buyers from selling the same car. 3. Search and comparison — Predicate filters narrow results; Comparator ranks cars by specifications. 4. Stream analytics — calculates stock value, fuel counts, fastest cars and average brand prices. 5. Repository persistence — saves data using CSV first and JDBC later. Services depend on a repository interface. 6. Validation and audit logging — validates input, throws InvalidCarException for invalid data and writes timestamped logs. Security and access control has been removed from this version. (3)"*

### Slide 4 — 03 / ARCHITECTURE: Architecture and Data Flow
- **Slide Content:**
  - Shared base: `model` (Car, Sale, FuelType, Status) + `util` (Validator, InvalidCarException, AppLogger) used by every layer.
  - *Layer 1 Service:* `InventoryService`, `SalesService`, `ReportService`
  - *Layer 2 Repository:* `CarRepository`, `FileCarRepository`, `JdbcCarRepository`
  - *Storage / Data:* `cars.csv`, `sales.csv`, `SQLite or MySQL (JDBC)`, Action log file
  - *Example Flow: Selling a Car:*
    - 01: Validator checks carId and buyer parameters
    - 02: SalesService locks thread and verifies AVAILABLE
    - 03: Car status becomes SOLD and Sale object initializes
    - 04: Repository commits car update and sale record
    - 05: AppLogger writes audit entry; ReportService updates
- **Speaker Notes:**
  - *"ARCHITECTURE & FLOW: Explain the layers: Model contains Car and Sale data types; Util contains validation, custom exceptions and logging. Service contains InventoryService, SalesService and ReportService. Repository handles saving and loading. Storage is cars.csv and sales.csv initially, with SQLite/MySQL through JDBC as the database option. Selling flow: validation -> check state -> mutate -> persist -> log. Mention that keeping service and repository layers decoupled allows swapping storage without rewriting business logic. (4)"*

### Slide 5 — 04 / DESIGN: Data Model and Collections
- **Slide Content:**
  - `Sale` model: `saleId`, `carId`, `buyerName`, `date`, `price`
  - Architecture decision callout: *"Start with file storage for the initial module, then swap in JDBC without touching service classes."*
  - Data dictionary table (`Car`):
    - `id` (int): Unique car number
    - `brand` (String): Maker, e.g. Ferrari
    - `model` (String): Model name
    - `price` (double): Price in dollars
    - `topSpeed` (int): Top speed in km/h
    - `fuelType` (FuelType enum): Enum, not free text
    - `horsepower` (int): Power in HP
    - `acceleration` (double): 0–100 km/h seconds
    - `status` (Status enum): AVAILABLE, RESERVED, SOLD
  - Collections mapping table:
    - Keep all cars in order: `ArrayList`
    - Find a car by ID: `HashMap`
    - Count cars per fuel: `EnumMap`
    - Unique brand list: `TreeSet`
    - Sales history: `ArrayList`
    - Save to disk: `cars.csv` now, `JDBC` later
- **Speaker Notes:**
  - *"DATA MODEL & COLLECTIONS: Car fields include id, brand, model, price, topSpeed, fuelType, horsepower, acceleration and status. Sale stores saleId, carId, buyerName, date and price. ArrayList: maintains an ordered list of cars and sales. HashMap: quick lookup by car ID. EnumMap: counts cars by FuelType. TreeSet: keeps unique brands in sorted order. Remember: collections manage data in memory; repositories persist it to files or a database. (5)"*

### Slide 6 — 05 / DATABASE: Database Design
- **Slide Content:**
  - Database sections:
    - *Relationships:* `1—N` (cars to sales); `1—N` / `N—1` (user to sales and logs). Slide visual: *"One car maps to sales; one user maps to sales plus logs."*
    - *Integrity:* Enforced PK/FK keys, NOT NULL fields, UNIQUE users, status CHECK.
    - *JDBC:* PreparedStatement queries with atomic commit or rollback per sale.
  - Slide graphical schema (4 tables):
    - `CARS`: `car_id INT PK`, `brand VARCHAR(50)`, `model VARCHAR(50)`, `price DECIMAL(12,2)`, `top_speed INT`, `fuel_type VARCHAR(20)`, `horsepower INT`, `acceleration DECIMAL(4,2)`, `status VARCHAR(10)`
    - `SALES`: `sale_id INT PK`, `car_id INT FK`, `seller_id INT FK`, `buyer_name VARCHAR(60)`, `sale_date DATE`, `price DECIMAL(12,2)`
    - `USERS`: `user_id INT PK`, `username VARCHAR(30) UNIQUE`, `password_hash VARCHAR(64)`, `role VARCHAR(10)`
    - `AUDIT_LOG`: `log_id INT PK`, `user_id INT FK`, `action VARCHAR(100)`, `created_at TIMESTAMP`
- **Speaker Notes:**
  - *"DATABASE TABLES: This revised deck has three tables: CARS — car_id (primary key), brand, model, price, top_speed, fuel_type, horsepower, acceleration, status. SALES — sale_id (primary key), car_id (foreign key), seller_name, buyer_name, sale_date, price. AUDIT_LOG — log_id (primary key), actor_name, action, created_at. CARS to SALES is one-to-many: one car may have a sale record. Audit log stores actions with timestamps. JDBC uses PreparedStatement; a sale should commit as one transaction or roll back if it fails. The USERS table was removed with the security/access module. (6)"*

### Slide 7 — 06 / DESIGN: Streams and Concurrency
- **Slide Content:**
  - *Stream-Based Reports:*
    1. Available cars: `filter(c -> c.getStatus() == AVAILABLE)`
    2. Stock value: `mapToDouble(Car::getPrice).sum()`
    3. Cars per fuel type: `groupingBy(Car::getFuelType, counting())`
    4. Top 3 fastest: `sorted(comparingInt(Car::getTopSpeed).reversed()).limit(3)`
    5. Avg price by brand: `groupingBy(Car::getBrand, averagingDouble(Car::getPrice))`
  - *Concurrency Where it Counts:*
    - Auto-save thread: Persists inventory every 30 seconds without blocking active service threads (`ScheduledExecutorService`).
    - Safe selling: Exclusive method lock ensures no vehicle is sold twice concurrently (`synchronized sellCar()`).
    - Thread-safe data: Concurrent map lookups prevent race conditions during parallel reads (`ConcurrentHashMap`).
- **Speaker Notes:**
  - *"STREAMS & CONCURRENCY: Streams process collections in a readable pipeline. Available cars: filter by AVAILABLE. Stock value: map prices and sum. Fuel distribution: group by fuel type and count. Fastest cars: sort top speed descending and take three. Average price: group by brand and average. Concurrency: ScheduledExecutorService can autosave periodically; synchronized sellCar prevents duplicate sales; ConcurrentHashMap supports concurrent lookups. Key point: synchronization protects the critical sale operation. (7)"*

### Slide 8 — 07 / PROBLEMS / SELLER + BUYER: Seller and Buyer Problems
- **Slide Content Table:**
  - `01 SELLER` | Stock lives in notebooks | Paper sheets go stale; sold cars look available | `InventoryService` with `ArrayList` persisted
  - `02 SELLER` | Sales leave no record | Missing buyer, date, price blocks revenue tracking | `Sale` model saved to `sales.csv`; Stream revenue sum
  - `03 SELLER` | Stock value is wrong | Summing every price counts sold cars as stock | `filter AVAILABLE` + `mapToDouble(Car::getPrice).sum()`
  - `04 BUYER` | Hard to find a car | Exact spelling and single filters waste time | `equalsIgnoreCase`, `contains`, `Predicate` filter chains
  - `05 BUYER` | Two buyers, one car | Concurrent requests can sell the same car twice | `synchronized sellCar` plus `RESERVED` state lock
  - `06 BUYER` | No way to compare cars | Manual spec checking across records is slow | Comparison service using `Comparator.comparing`
- **Speaker Notes:**
  - *"SELLER & BUYER PROBLEMS: Seller problems: outdated stock, missing sales records and incorrect stock valuation. Solutions: InventoryService, Sale records and a Stream sum over AVAILABLE cars only. Buyer problems: difficult searching, duplicate sale attempts and slow manual comparisons. Solutions: case-insensitive matching, Predicate filter chains, synchronized selling and Comparator ranking. Give one example: filter cars under a budget and with a selected fuel type. (8)"*

### Slide 9 — 08 / PROBLEMS / DEVELOPER + DATA: Developer and Security Problems
- **Slide Content Table:**
  - `07 DEVELOPER` | Invalid data crashes app | Malformed numbers throw NumberFormatException | `Validator` with try-catch plus `InvalidCarException`
  - `08 DEVELOPER` | Code tied to one storage | Moving from file to DB rewrites services | `CarRepository` interface: file now, JDBC later
  - `09 DEVELOPER` | One giant class | Everything in Main is hard to maintain or test | Packages (`model`, `service`, `repository`, `util`) + JUnit
  - `10 DATA` | A crash can wipe data | Overwriting cars.csv in place loses stock mid-save | Write temp file, atomic `Files.move`, JDBC transactions
  - `11 DATA` | Anyone can do anything | Unauthenticated callers could delete car stock | Admin, Seller, Customer roles; SHA-256 hashes
  - `12 DATA` | No audit trail | Unrecorded price edits or sales lack accountability | `java.util.logging` writes action, timestamp, user
- **Speaker Notes:**
  - *"DEVELOPER & DATA PROBLEMS: Invalid input is handled by Validator and InvalidCarException. Repository interface avoids tying service code to CSV or JDBC. Packages separate model, service, repository and util code, making testing and maintenance easier. Safe file replacement and JDBC transactions reduce the risk of partial saves. AppLogger records action, timestamp and actor for an audit trail. Security/access control is intentionally excluded from this version. (9)"*

### Slide 10 — 09 / NEXT: Project Plan (Unit VI)
- **Slide Content:**
  - Subtitle: *"Iterative delivery from file-backed prototype to concurrent JDBC service"*
  - Three Phases:
    - *Phase 1 (Plan):* Scope (Problem definition), Actors (Core use cases), Structure (Backend packages), Schema (Data model + DB)
    - *Phase 2 (Initial module):* Domain (Car, FuelType, Status), Storage (FileCarRepository), Service (Add and list cars), Indexing (ArrayList + HashMap)
    - *Phase 3 (Full build):* Workflows (Search, filter, sell), Analytics (Stream reports), Database (JDBC persistence), Safety (Concurrency + logging)
- **Speaker Notes:**
  - *"PROJECT PLAN: Phase 1: define the problem, actors, packages, data model and database schema. Phase 2: build the initial file-backed module with Car, FuelType, Status, FileCarRepository, add/list operations and collections. Phase 3: add search, filtering, selling, Stream reports, JDBC persistence, concurrency and logging. Explain this as an iterative plan: first make the core work, then add persistence and reliability. (10)"*

### Slide 11 — 10 / Conclusion
- **Slide Content:**
  - Summary metrics: `12 problems` | `7 core modules` | `4 viewpoints` | `3 phases`
  - Closing: **THANK YOU** | Questions? Ask away. | *"Drive Passion. Live RevLine."*
- **Speaker Notes:** *(No notes)*

---

### B.1 Differences Between the Real PPT and MASTER_PROMPT.md

| # | Topic | Real PPT (Slide / Notes) | MASTER_PROMPT.md Specification | Difference / Resolution |
|---|-------|--------------------------|--------------------------------|-------------------------|
| 1 | **Total Slide Count** | Exactly 11 slides (with notes on slides 1–10). | Assumed 12 slides based on problem/phase outlines. | Corrected in this report: the presentation is exactly 11 slides. |
| 2 | **Actors & Roles** | Slide 2 shows `Admin` (authenticate, roles). Slide 9 shows SHA-256 roles. | Notes 2, 3, 9 explicitly state: *"There are two main actors: Seller and Buyer"* and *"Security and access control has been removed from this version."* | **No auth / No USERS table.** A simple `[S]` menu switch toggles Seller View / Buyer View without login (ADR-003). |
| 3 | **Module Count** | Slide 3 title says "seven functional Java modules", but displays 6 cards. Note 3 says "SIX CORE MODULES". | Defines 7 modules: the 6 PPT modules + `Data Tools` (import/export/backup). | Adding `Data Tools` as the 7th module provides full coverage for syllabus Lectures 25–27 (file handling, StreamTokenizer, backup/restore) without altering the 6 core business modules (ADR-007). |
| 4 | **Database Schema** | Slide 6 diagram displays 4 tables: `CARS`, `SALES` (with `seller_id`), `USERS`, `AUDIT_LOG` (with `user_id`). | Note 6 explicitly clarifies: *"This revised deck has three tables: CARS, SALES (with seller_name, buyer_name), AUDIT_LOG (with actor_name)... The USERS table was removed."* | MASTER_PROMPT adopted the 3-table schema (`cars`, `sales`, `audit_log`) per the speaker notes, which reflect the author's final intent (ADR-001, ADR-003). |
| 5 | **Concurrency API** | Slide 7 & Note 7 mention `ScheduledExecutorService` and `ConcurrentHashMap`. | Replaces them with daemon `Thread` + `sleep()` + `volatile` flag + `synchronized` over `HashMap`. | `ScheduledExecutorService` and `ConcurrentHashMap` are not in the 25CSE0202 syllabus (Lectures 9–16 cover `Thread`, `Runnable`, `synchronized`, `wait`/`notify`). In-syllabus primitives are used and industry equivalents documented for viva (ADR-002). |
| 6 | **Testing & File Utilities** | Slide 9 lists `JUnit` (problem 09) and `Files.move` (problem 10). | Replaces with in-house `TestRunner` and `File.renameTo`. | JUnit and NIO `Files.move` are out of syllabus. Safe saving uses `java.io.File` temp-file + `renameTo` (ADR-004, ADR-005). |
| 7 | **Logging** | Slide 9 lists `java.util.logging`. | Builds in-house `AppLogger` using `FileWriter`. | `java.util.logging` is not in the syllabus; custom `AppLogger` demonstrates file I/O (L25) and `StringBuffer` (L2) (ADR-006). |
| 8 | **Execution Phases** | Slide 10 defines 3 high-level phases (Plan, Initial module, Full build) for Unit VI. | Expands into 10 detailed engineering phases (Phases −1 to 8 + Phase 9 stretch) with explicit milestone gates. | High-level PPT phases map directly: PPT Phase 1 = Phases −1, 0, 1; PPT Phase 2 = Phase 2 (v0.1 prototype); PPT Phase 3 = Phases 3–8 (v0.3-ca3 through v1.0-final). |

---

## C. Syllabus Topic List (25CSE0202 · Programming in Java)

> Source: Appendix E.1 of MASTER_PROMPT, verified against `/reference/course/` images.

| Lecture(s) | Topic |
|-----------|-------|
| 1 | OOP revisit: classes, objects, methods, constructors, this/static, inheritance, polymorphism, abstraction/interfaces, object modelling |
| 2 | Multi-dimensional arrays, String immutability & pool, comparison/search/manipulation/formatting, StringBuilder/StringBuffer, wrapper classes |
| 3 | Checked/unchecked exceptions, try/catch/finally, multi-catch, propagation, throw/throws, custom exceptions |
| 4 | Integrated coding practice: console programs, debugging, input validation, test-case structures |
| — | *CE-1 (Lectures 1–15)* |
| 5 | Collections Framework: ArrayList, LinkedList, HashSet, LinkedHashSet, TreeSet |
| 6–7 | HashMap, LinkedHashMap, TreeMap, Queue/Deque/PriorityQueue, Iterator/ListIterator |
| 8 | Comparable/Comparator, sorting, Collections utility methods, record-management problems |
| 9–10 | Multiprogramming vs multithreading, concurrency, Thread & Runnable, start/run/sleep/join/interrupt |
| 11–12 | Thread lifecycle & states, daemon threads, priorities, execution tracing |
| 13–14 | Shared data & synchronisation: race conditions, synchronized methods/blocks, intrinsic locks, atomicity & visibility |
| — | *CE-2 (Lectures 16–31)* |
| 15–16 | wait()/notify()/notifyAll(), guarded blocks, producer-consumer, monitor usage |
| 17–18 | JDBC architecture, drivers, URL, DriverManager, Connection; relational/SQL concepts |
| 19 | Statement, PreparedStatement, executeQuery/executeUpdate, parameter binding, secure SQL |
| 20 | CRUD, ResultSet, cursor iteration, typed getters, Java-object mapping |
| 21–22 | Lambda expressions, Stream API, functional interfaces, method references |
| 23 | Stream fundamentals & intermediate ops: sources, pipelines, laziness, filter/map/sorted/distinct/limit/skip |
| 24 | Generics: generic classes/methods, type safety, diamond, bounded/wildcards |
| 25 | Byte streams, character streams, StreamTokenizer, FileInputStream/FileOutputStream/FileReader/FileWriter |
| 26–27 | File handling + collections: text/CSV read-write, validation, import/export, exception-safe resource handling |
| 28 | **Unit VI** Guided Project: planning and working prototype |
| 29 | Project implementation and integration |
| 30 | Project testing, refinement, documentation, viva preparation |
| — | *CA-3: Project Milestone (after Lecture 54)* |
| — | *CA-4: Final Project Evaluation + Viva (Lecture 60)* |

---

## D. Rubric (25 marks)

| # | Component | Marks | What the Examiner Evaluates |
|---|-----------|-------|-----------------------------|
| 1 | **Problem Statement** | 3 | Clearly stated, relevant, feasible, specific for a Java app; users/context and need identifiable |
| 2 | **Project Objectives & Scope** | 2 | Clear, measurable objectives aligned with problem; scope, use cases, expected outputs, realistic boundaries |
| 3 | **Project Design & Prototype** | 9 | Sensible class/package structure, data model, app flow; working initial module with reliable I/O and core functionality |
| 4 | **Integration of all Course Concepts** | 6 | OOP + collections applied; planned/initial use of exception handling, JDBC or file persistence, other concepts |
| 5 | **Code Quality, Progress Evidence & Documentation** | 5 | Organised readable code; meaningful naming; progress via files/repo history; status, setup, pending, milestone evidence |
| | **Total** | **25** | |

### Binding Notes
- Team of 3–4 members.
- GitHub submission mandatory; repo updated regularly.
- Absent at evaluation = zero.
- AI-generated without significant personal contribution = zero.

---

## E. Conflicts and Ambiguities — Resolution Table

| # | Conflict | Source A | Source B | Resolution | Logged in |
|---|----------|---------|---------|------------|-----------|
| 1 | PPT uses `ScheduledExecutorService`, `ConcurrentHashMap` | PPT slide 10 | Syllabus (not covered) | Replace with daemon `Thread` + `sleep()` + `volatile` flag + `synchronized` blocks over plain `HashMap`. Mention industry equivalents in viva notes. | DECISIONS.md |
| 2 | PPT slide 9 mentions roles + SHA-256 passwords; speaker notes say security removed | PPT slide body | PPT speaker notes | No authentication, no password hashing, no USERS table. Simple Seller View / Buyer View menu switch (no login). | DECISIONS.md |
| 3 | PPT says "7 modules" vs notes say "six" | PPT slide 3 | PPT speaker notes | Use 7 modules as defined in §9: the 6 from notes + Data Tools (import/export/backup). | DECISIONS.md |
| 4 | PPT mentions JUnit | PPT | Syllabus (not covered) | Use in-house test runner (§14). JUnit only as optional stretch in Phase 9. | DECISIONS.md |
| 5 | PPT mentions `Files.move` | PPT | Syllabus (not covered) | Use `java.io.File` + temp-file + `renameTo()` for crash-safe saves. | DECISIONS.md |
| 6 | PPT mentions `java.util.logging` | PPT | Syllabus (not covered) | Build `AppLogger` on `FileWriter`. | DECISIONS.md |
| 7 | PPT says "SQLite or MySQL" | PPT | Practical (zero-setup demo) | Default: SQLite via `sqlite-jdbc` jar. SQL kept ANSI-friendly for optional MySQL swap. | DECISIONS.md |
| 8 | C default cars all `isAvailable=1`; `cars.txt` has 3,4,18 as `0` | `loadDefaultCars()` | `cars.txt` | Trust `cars.txt`. Seed CSV keeps IDs 3, 4, 18 as SOLD. Create matching Sale rows for them. | DECISIONS.md |
| 9 | C `printTableBottom` sums ALL cars for "Total Showroom Value" | C code | PPT problem #3 ("stock value wrong") | Stock value = AVAILABLE cars only. Separate "portfolio value" report for all cars. | DECISIONS.md |
| 10 | C search is case-sensitive | C code | Java best practice | Java version uses case-insensitive `equalsIgnoreCase`/`toLowerCase` + `contains`. | 05_C_TO_JAVA_REPORT.md |
| 11 | C sort permanently reorders and saves | C code | Java best practice | Java sorts a copy; persistence order stays by ID. | 05_C_TO_JAVA_REPORT.md |
| 12 | C `buyCar` records no buyer/date, no concurrency protection | C code | PPT requirements | Java records `Sale(saleId, carId, buyer, seller, date, price)` with `synchronized` protection. | 05_C_TO_JAVA_REPORT.md |

### Source-of-truth priority (when inputs disagree)
1. **Rubric & course notes** (hard constraints)
2. **Syllabus** (what technology is allowed)
3. **PPT speaker notes** (latest intent)
4. **PPT slide body** (may be stale)
5. **C code** (behaviour to preserve)

---

## F. What This Means for the Build Plan

1. **Every lecture 1–30** must have at least one real usage in the code, traceable in `04_CONCEPT_MAP.md`.
2. **No technology outside the syllabus** in `main` branch — forbidden tech is listed in §3.2/3.3.
3. **Working prototype by CA-3** (Phases 0–3): Add/List/Search + FileCarRepository + tests.
4. **Full build by CA-4** (Phases 4–8): JDBC, Streams, Concurrency, File handling, all docs, viva prep.
5. **8 student exercises** (EX-1 to EX-8) left for the student — these are the "significant personal contribution".
6. **Real Git history** with 60–120 small commits over real days. Never fabricated.
7. **The 18 cars from the C prototype** are the seed data, with IDs 3, 4, 18 correctly marked SOLD.
