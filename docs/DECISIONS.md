# Architectural Decision Records — RevLine-Java

## ADR-001: SQLite as Default Database

**Context:** PPT says "SQLite or MySQL". Student must demo JDBC in a viva on a laptop.
**Decision:** Default to SQLite via `sqlite-jdbc` driver jar. Zero server setup = reliable demo.
**Consequence:** SQL kept ANSI-friendly. MySQL works by changing URL + driver in `AppConfig`. Documented as optional.

## ADR-002: No ConcurrentHashMap / No ScheduledExecutorService

**Context:** PPT slides use `ConcurrentHashMap` and `ScheduledExecutorService`, but neither is in the 25CSE0202 syllabus (lectures 1–30).
**Decision:** Use `synchronized` blocks over a plain `HashMap`, and daemon `Thread` + `sleep()` + `volatile boolean` flag.
**Consequence:** Simpler code that maps directly to L9–14. Industry equivalents mentioned in `VIVA.md` as awareness.

## ADR-003: No Authentication / No Password Hashing

**Context:** PPT slide 9 lists roles + SHA-256. Speaker notes explicitly say security was removed.
**Decision:** No USERS table, no login, no SHA-256. Provide a simple Seller View / Buyer View switch (menu option `[S]`).
**Consequence:** Both PPT actors are still demonstrated without out-of-syllabus security code.

## ADR-004: In-House Test Runner (No JUnit)

**Context:** JUnit is not in the syllabus. PPT mentions it.
**Decision:** Build a tiny `TestRunner` + `Assert` class. Each `*Test` class has `runAll()`. Explicit test list (no reflection).
**Consequence:** All testing is in-syllabus. JUnit port is an optional Phase 9 stretch.

## ADR-005: Crash-Safe Saves via temp-file + renameTo

**Context:** PPT mentions `Files.move` (java.nio) which is not in the syllabus. C prototype overwrites in-place (crash-unsafe).
**Decision:** Use `java.io.File` temp-file + `renameTo()`. Algorithm: write to `.tmp` → rename original to `.bak` → rename `.tmp` to target → delete `.bak`.
**Consequence:** File persistence is crash-safe using only `java.io` (L25–27).

## ADR-006: AppLogger Built on FileWriter

**Context:** PPT mentions `java.util.logging` (not in syllabus).
**Decision:** Build `AppLogger` using `FileWriter(file, true)` with `synchronized` logging. `StringBuffer` for recent-lines buffer (demonstrates L2 StringBuffer vs StringBuilder).
**Consequence:** All logging uses in-syllabus APIs.

## ADR-007: Seven Modules (Not Six)

**Context:** PPT slide 3 says "seven modules"; speaker notes say "six".
**Decision:** Use 7 modules as defined in MASTER_PROMPT §9: Inventory, Search/Filter/Sort, Sales, Repository, Stream Analytics, Validation/Exceptions/Audit, Data Tools & Concurrency Lab.
**Consequence:** Data Tools (import/export/backup) is a distinct module, covering L25–27 file handling thoroughly.

## ADR-008: Seed Data — IDs 3, 4, 18 Marked SOLD

**Context:** C `loadDefaultCars()` marks all 18 as Available. C `cars.txt` has IDs 3, 4, 18 as Sold (flag=0). Inconsistent.
**Decision:** Trust `cars.txt`. Seed CSV keeps 3 (Ferrari LaFerrari), 4 (Ferrari SF90 Stradale), 18 (Audi R8 V10 Plus) as SOLD. Create matching Sale rows with buyer "Legacy C Import".
**Consequence:** Reports are consistent; `SeedConsistencyTest` verifies against the C file.

## ADR-009: Stock Value = AVAILABLE Cars Only

**Context:** C `printTableBottom` sums all car prices for "Total Showroom Value" — including sold cars. PPT problem #3 says this is wrong.
**Decision:** "Stock value" report (R2) counts only AVAILABLE cars. Separate "Portfolio value" report (R3) sums all.
**Consequence:** Fixes a documented defect from the C prototype. Great viva material.

## ADR-010: One Lock for Inventory + Sales

**Context:** Need thread safety for `sellCar`, `reserve`, `cancel`, and inventory mutations.
**Decision:** All public mutators in `InventoryService` and `SalesService` synchronize on the same monitor object. Never take two locks. Never call unknown code while holding the lock.
**Consequence:** Simple, deadlock-free. Explained in viva as "one lock strategy".
