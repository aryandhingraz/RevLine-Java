# RevLine — CA-3 Submission & Viva Readiness Checklist

> **Course**: 25CSE0202 Programming in Java  
> **Candidate**: Aryan Dhingra (`aryandhingraz`)  
> **Repository**: [https://github.com/aryandhingraz/RevLine-Java](https://github.com/aryandhingraz/RevLine-Java)  
> **Milestone Tag**: `v0.3-ca3`

---

## 1. Submission Overview

| Requirement | Artifact / Evidence | Status |
|-------------|---------------------|--------|
| **Public GitHub Repo** | [https://github.com/aryandhingraz/RevLine-Java](https://github.com/aryandhingraz/RevLine-Java) | Ready to push |
| **Clean Build Script** | `scripts/build.bat` / `scripts/build.sh` (Java 17 target, `-Xlint:all`, 0 warnings) | ✅ PASS (0 warnings) |
| **Automated Test Suite**| `scripts/test.bat` (76 core tests pass, in-house runner) | ✅ PASS (76/76 core tests) |
| **Exercise Implementations** | EX-1, EX-2, EX-3, and EX-7 complete; EX-4, EX-5, EX-6, and EX-8 remain for later phases | ✅ Tracked in `EX_LOG.md` |
| **Demo Script & Evidence** | `docs/DEMO_SCRIPT.md` + `docs/evidence/demo_ca3_output.txt` | ✅ Complete |
| **Rubric Rows 1–5 Evidence**| Traceability matrix in `docs/RUBRIC_TRACEABILITY.md` | ✅ Complete |

---

## 2. Rubric Rows 1–5 Traceability Verification

| Rubric Row | Max Marks | Evaluated Criteria | Implemented RevLine Evidence |
|------------|-----------|--------------------|------------------------------|
| **Row 1: Problem Definition & Scope** | **3** | Clear, measurable objectives; defined boundaries; thorough real-world supercar domain. | `docs/01_PROBLEM_STATEMENT.md`, `docs/02_OBJECTIVES_SCOPE.md` (authored personally by student). |
| **Row 2: Architecture & Design** | **2** | Clean package design; OOP decomposition; data dictionary; UML diagrams. | `docs/03_DESIGN.md` containing 6 Mermaid diagrams (Package, Class, Sequence Sell/Add, ER, State), `docs/DECISIONS.md` (10 ADRs). |
| **Row 3: Java OOP Implementation** | **9** | Encapsulation, inheritance, polymorphism, abstract classes, interfaces, custom exceptions. | `Vehicle` abstract base, `Car` subclass, `Describable`, `CarVisitor`, `Repository<T,ID>`, `RevLineException` hierarchy, `Validator` wrapper parsing. |
| **Row 4: Data Structures & Algorithms** | **6** | Appropriate collection choices, custom sorting, searching, stack/queue operations, algorithmic complexity. | `ArrayList` (fleet), `HashMap` (O(1) index), `HashSet` (duplicate guard), `TreeSet` (sorted brands), `CarComparators` (L8), `FilterBuilder` (L21), `ComparisonService` (L2 2D matrix), `RecentlyViewed` (L6-7 LRU), `ReservationManager` (min-heap `PriorityQueue`), `WaitlistService` (FIFO `Queue`), `UndoManager` (LIFO `Deque`). |
| **Row 5: Robustness & Code Quality** | **5** | Zero warnings on `-Xlint:all`; atomic crash-safe persistence; input validation; test coverage. | `FileUtil.safeWrite()` (temp file + backup + rename), `InputReader` (whole line reading, bounds check), 76 automated core unit tests with 100% pass rate. |

---

## 3. Rehearsed 8-Minute Live Demo Walkthrough

### Act 1: Clean Build & Test Suite Verification (1.5 min)
1. Run `.\scripts\clean.bat`
2. Run `.\scripts\build.bat` (show `javac --release 17 -Xlint:all` outputting zero warnings).
3. Run `.\scripts\test.bat` (show 76 core tests passing and exercise status).

### Act 2: Showroom Buyer Flow (3.5 min)
1. Launch `.\scripts\run.bat`. Show boot panel and ASCII banner.
2. Select `[1]` to enter **Interactive Showroom Browser**:
   - Press `[N]` and `[P]` to navigate cars bidirectionally using `ListIterator`.
   - Exit back to menu with `[Q]`.
3. Select `[2]` -> Option `[2]` to demonstrate **EX-3 Multi-Field Search** (search `"Hybrid"` or `"Ferrari"`).
4. Select `[3]` -> Option `[1]` to demonstrate **Custom Sorting** (Price Low to High).
5. Select `[4]` to demonstrate **Side-by-Side Spec Comparison**:
   - Select 2 cars (e.g. Car #1 Bugatti Chiron and Car #2 Ferrari SF90).
   - Point out the 2-D comparison matrix and `*BEST` indicators.
6. Select `[5]` to **Reserve a Car** (e.g. Car #5 for 30 minutes). Show status changes to `RESERVED`.
7. Select `[6]` to **Purchase a Car**:
   - Purchase Car #1. Show generated formal ASCII receipt box and status transition to `SOLD`.
8. Select `[8]` to inspect **Recently Viewed Cars** (demonstrate LRU cache).

### Act 3: Seller Management Flow & Undo (2 min)
1. Press `[S]` to toggle into **Seller / Management View**.
2. Select `[1]` to view complete inventory and stock valuation.
3. Select `[2]` to **Add a New Car** (e.g. Aston Martin Valkyrie). Show assigned ID.
4. Select `[5]` to show **Undo Action** (`UndoManager`).
5. Select `[6]` to inspect the **Sales Transaction Ledger** and reprint a past receipt.
6. Select `[8]` to view the **System Audit Log**.

### Act 4: Code Architecture & Viva Defense (1 min)
1. Open `docs/learning/M2_M3_EXPLAINED.md` to answer examiner questions on:
   - Why `inventoryService.getLock()` is shared with `SalesService`.
   - How `PriorityQueue` min-heap and `Iterator.remove()` manage reservation expiration.
   - Why `LinkedHashMap(16, 0.75f, true)` provides an LRU cache.
