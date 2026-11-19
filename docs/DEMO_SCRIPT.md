# RevLine-Java 10-Minute Comprehensive Evaluation Demo Script

> **Course**: 25CSE0202 Programming in Java (Chitkara University)  
> **Milestone**: Final Release (`v1.0-final` / CA-4)  
> **Student Presenter**: Aryan Dhingra (`aryandhingraz`)  
> **Target Duration**: 10.0 Minutes  
> **Environment**: Windows 11 / PowerShell, JDK 17 LTS, SQLite JDBC

---

## 1. Timing Breakdown & Act Structure

| Act | Module / Focus | Key Concepts & Deliverables | Target Time |
|:---:|:---|:---|:---:|
| **Act 0** | Build Quality & Test Suite | Clean compile (`-Xlint:all`), 116 tests, 9/9 exercises | 1.0 min |
| **Act 1** | Showroom Buyer / Client View | Bidirectional `ListIterator`, search, compare, buy, LRU | 2.5 min |
| **Act 2** | Management / Seller Flow | Add/update, O(1) duplicate guard, `UndoManager` stack (EX-7) | 1.5 min |
| **Act 3** | Analytics & Executive Reports | 15 Stream pipelines (R1–R15), price band matrix, export | 1.5 min |
| **Act 4** | Concurrency Lab & Stress Suite | 5 interactive demos, bounded buffer (EX-8), 20 stress rounds | 1.5 min |
| **Act 5** | Enterprise Data Tools & Storage | Legacy C migration, `StreamTokenizer` script, byte backup | 1.0 min |
| **Act 6** | Dual Storage & Fallback Plan | CSV vs SQLite JDBC (`--storage=jdbc`), ACID transactions | 1.0 min |
| **Total** | | | **10.0 min** |

---

## 2. Detailed Execution Walkthrough

### Act 0: Code Quality & Automated Test Verification (1:00)

1. **Clean compilation with `-Xlint:all`**:
   ```powershell
   .\scripts\build.bat
   ```
   - *Examiner cue*: Point to `[PASS] Build succeeded with zero warnings.`
   - *Scripted line*: *"RevLine is built strictly against Java 17 LTS without third-party frameworks. Every class in the system is verified under 275 lines with zero compiler warnings."*

2. **Execute in-house test suite**:
   ```powershell
   .\scripts\test.bat
   ```
   - *Examiner cue*: Point to `Core Suite: 116/116 Passed (~620 ms)` and `Exercises: 9/9 Completed`.
   - *Scripted line*: *"Our zero-dependency in-house test runner executes 116 comprehensive unit, boundary, negative, security, stress, and round-trip checks in ~620 milliseconds."*

---

### Act 1: Showroom Buyer / Client Experience (2:30)

1. **Launch application**:
   ```powershell
   .\scripts\run.bat
   ```
   - *Examiner cue*: Highlight the ASCII boot banner, database verification, and clean dual-view design.

2. **Interactive Showroom Browser (Option `1`)**:
   - Press `N` (Next) to view Car #2 (Bugatti Veyron SS).
   - Press `N` to view Car #3.
   - Press `P` (Previous) to return to Car #2.
   - Press `Q` to exit browsing.
   - *Scripted line*: *"Demonstrating `ListIterator<Car>` (L6–L7) for bidirectional navigation, dynamically registering each viewed car into our LRU cache."*

3. **Search & Composable Filters (Option `2`)**:
   - Select Option `2` (Search Text): enter `Hybrid`.
   - Matches Ferrari LaFerrari, SF90, and McLaren P1 across brand, model, and fuel type.
   - Select Option `3` (Budget Filter): enter min `$100,000`, max `$500,000`.
   - *Scripted line*: *"Powered by `FilterBuilder` (L23) chaining composable `Predicate<Car>` streams."*

4. **Value-for-Money Ranking (Option `3` -> `7`)**:
   - Select Option `7` (Value for Money [EX-2]).
   - *Scripted line*: *"Student-authored EX-2 comparator ranking horsepower-per-$100k ratio with acceleration tie-breakers."*

5. **Side-by-Side Spec Matrix (Option `4`)**:
   - Enter `2` cars: ID `1` (Chiron) and ID `12` (Tesla Model S Plaid).
   - *Examiner cue*: Point out the 8-row comparison matrix highlighting lowest price, highest speed, highest HP, and quickest acceleration with `*BEST`.

6. **Purchase Vehicle & Receipt Generation (Option `6`)**:
   - Select Car `2` (Bugatti Veyron SS), Buyer: `Aryan Dhingra`, Rep: `RevLine Agent`, Confirm: `y`.
   - *Examiner cue*: Point to the official ASCII receipt and status transition to `SOLD`.

7. **Recently Viewed LRU Cache (Option `8`)**:
   - Show the 5 most recently viewed vehicles ordered MRU to LRU.

---

### Act 2: Management / Seller Flow & Undo Mechanics (1:30)

1. **Switch to Seller Mode**:
   - Enter `S`.
   - *Scripted line*: *"Toggles instantly between Client and Management views."*

2. **Add Vehicle with Duplicate Guard (Option `2`)**:
   - Attempt to add: Brand `Bugatti`, Model `Chiron`.
   - *Examiner cue*: System immediately rejects duplicate via `HashSet<String>` duplicate guard.
   - Add new car: Brand `Aston Martin`, Model `Valhalla`, Price `800000`, Speed `350`, Fuel `2` (Hybrid), HP `950`, Accel `2.5`.
   - Successfully saved as Car #19; undo snapshot pushed.

3. **Undo Last Action (Option `5` [EX-7])**:
   - Select Option `5`.
   - *Examiner cue*: `UndoManager` pops the mutation stack (`ArrayDeque<UndoAction>`) and removes Car #19 from storage and in-memory indices.

4. **Sales History & Receipt Reprint (Option `6`)**:
   - Displays sales transaction ledger with all completed sales.

---

### Act 3: Analytical Streams & Executive Reporting (1:30)

1. **Open Reports Menu (Option `7`)**:
   - Option `1`: Full Executive Summary running all 15 Stream pipelines (R1–R15).
   - Option `2`: Price Band Matrix (`int[3][4]`) mapping fuel types vs 4 price tiers.
   - Option `3`: Top-N by Horsepower (`topNByHorsepower` - EX-4).
   - Option `4`: Average Price by Brand (`averagePriceByBrand` - EX-5).
   - Option `6`: Stream Laziness Proof (unconsumed peek counter = 0; short-circuit limit).
   - Option `7`: Export report to `data/exports/report_summary.txt`.
   - *Scripted line*: *"All 15 reports utilize Java 17 Streams, intermediate pipelines, primitive `DoubleStream`, and downstream collectors."*

---

### Act 4: Concurrency Lab & Deterministic Demos (1:30)

1. **Open Concurrency Lab (Option `8`)**:
   - **Demo 1**: Race condition proof (`UnsafeCounter` loses updates; `SafeCounter` achieves exact 40,000 count).
   - **Demo 2**: Double-Sell Showdown (20 threads race on 1 car; exactly 1 winner, 19 rejected with 0 overselling).
   - **Demo 3**: Thread Lifecycle Tracer (traces `NEW -> RUNNABLE -> TIMED_WAITING -> BLOCKED -> WAITING -> TERMINATED`).
   - **Demo 4**: Producer-Consumer Pipeline (3 producers, 1 consumer, bounded buffer with wait/notify [EX-8]).
   - **Demo 5**: Interruption & Memory Visibility (clean shutdown and `volatile` awareness).

---

### Act 5: Enterprise Data Tools & File Handling (1:00)

1. **Open Data Tools Menu (Option `9`)**:
   - Option `1`: Import legacy C flat-file (`cars.txt`) with 1/0 status flag mapping.
   - Option `2`: Batch CSV import with line-by-line error reports (`data/reports/import_errors_*.txt`).
   - Option `3`: Execute batch DSL commands via `StreamTokenizer` (`data/sample/bulk_import.txt`).
   - Option `4` & `5`: Byte-stream binary backup and restore with byte-identical verification.
   - Option `6`: Generate 50 synthetic test supercars for stress testing.

---

### Act 6: Dual Storage Engine & Fallback Plan (1:00)

1. **SQLite Relational Mode**:
   - Exit application (`0`).
   - Launch in JDBC mode:
     ```powershell
     .\scripts\run.bat --storage=jdbc
     ```
   - *Examiner cue*: Boots with SQLite JDBC driver, enforces `PRAGMA foreign_keys = ON;`, candidate keys, and multi-statement ACID transactions.

2. **Fallback Plan**:
   - If SQLite or foreign key errors occur on examiner machine: launch without flags (`.\scripts\run.bat`), seamlessly falling back to CSV flat-file mode.
