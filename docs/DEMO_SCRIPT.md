# RevLine — 8-Minute Live Evaluation Demo Script

> **Course**: 25CSE0202 Programming in Java (Unit VI)  
> **Target Milestone**: CA-3 Submission (`v0.3-ca3`)  
> **Presenter**: Aryan Dhingra (`aryandhingraz`)  
> **Target Duration**: $\le 8$ Minutes

---

## Timing Breakdown

| Act | Phase | Focus | Allocated Time |
|:---:|:------|:------|:--------------:|
| **Act 0** | Code Quality & Verification | Zero-warning build & 76-test suite | 1.0 min |
| **Act 1** | Showroom Client / Buyer Flow | Browse, Search, Sort, Compare, Reserve, Buy, LRU | 3.5 min |
| **Act 2** | Showroom Management / Seller Flow | Inventory CRUD, Undo Stack (EX-7), Sales Ledger, Audit | 2.0 min |
| **Act 3** | Architectural Viva Defense | Thread safety, Collections, Rubric Rows 1–5 | 1.5 min |
| **Total** | | | **8.0 min** |

---

## Detailed Step-by-Step Script

### Act 0: Clean Build & Test Verification (1:00)
1. **Clean prior artifacts**:
   ```powershell
   .\scripts\clean.bat
   ```
   *Say to Examiner*: *"RevLine is built strictly against Java 17 LTS without third-party frameworks. I will first demonstrate a completely clean compile."*
2. **Compile with zero warnings**:
   ```powershell
   .\scripts\build.bat
   ```
   *Point out*: `[PASS] Build succeeded with zero warnings.` using `javac --release 17 -Xlint:all`.
3. **Execute in-house automated test suite**:
   ```powershell
   .\scripts\test.bat
   ```
   *Point out*:
   - `Core Suite: 76/76 Passed`
   - `Exercises: 5/8 Completed` (EX-1, EX-2, EX-3, and EX-7 complete).

---

### Act 1: Showroom Client / Buyer Flow (3:30)
1. **Launch the application**:
   ```powershell
   .\scripts\run.bat
   ```
   *Point out*: The C-style ASCII Boot Panel verifying storage file integrity (`data\cars.csv`), seed validation, and the RevLine banner.
2. **Interactive Showroom Browser (Option `1`)**:
   - Press `N` (Next) to advance to Car #2.
   - Press `N` (Next) to advance to Car #3.
   - Press `P` (Previous) to navigate back to Car #2.
   - Press `Q` to return to menu.  
   *Say to Examiner*: *"Here we demonstrate `ListIterator<Car>` (Lecture 6–7) enabling bidirectional traversal, dynamically logging each viewed vehicle into an LRU cache."*
3. **Multi-Field Search & Filter (Option `2`)**:
   - Choose Sub-option `2` (Multi-Field Search [EX-3]).
   - Type: `Hybrid`
   - *Point out*: It matches Ferraris, McLarens, and Aston Martins across brand, model, and fuel type.
   - Return to menu and select Option `2` -> Sub-option `3` (Budget Filter: min `$200,000`, max `$500,000`) to show `FilterBuilder` composable predicates (Lecture 21).
4. **Showroom Sorting (Option `3`)**:
   - Select Option `1` (Price: Low to High).
   - Select Option `7` (Value-for-Money [EX-2]).
   - *Point out*: Custom `Comparator<Car>` sorting vehicles by horsepower-per-$100k ratio with 0-100 acceleration tie-breaker (Lecture 8).
5. **Side-by-Side Spec Comparison (Option `4`)**:
   - Number of cars: `2`
   - Enter Car IDs: `1` (Bugatti Chiron) and `2` (Bugatti Veyron SS).
   - *Point out*: The 2-D multi-dimensional matrix `String[8][2]` (Lecture 2) highlighting winning specs with `*BEST`.
6. **Vehicle Hold Reservation (Option `5`)**:
   - Car ID: `5` (Lamborghini Aventador SVJ)
   - Buyer: `Bruce Wayne`
   - Duration: `30` minutes.
   - *Point out*: The vehicle status transitions to `RESERVED` and a priority hold is registered in `ReservationManager`.
7. **Purchase Vehicle & Receipt Generation (Option `6`)**:
   - Car ID: `2` (Bugatti Veyron SS)
   - Buyer: `Tony Stark`
   - Seller: `RevLine Showroom`
   - Confirm: `y`
   - *Point out*: The formal ASCII receipt generated on console, the vehicle status transitioning from `AVAILABLE` to `SOLD`, and atomic persistence in `FileSaleRepository`.
8. **Recently Viewed Cache (Option `8`)**:
   - *Point out*: `RecentlyViewed` (extending `LinkedHashMap` with `accessOrder = true`) displaying the last 5 viewed cars in reverse chronological order.

---

### Act 2: Management / Seller Flow & Undo (2:00)
1. **Toggle to Seller View**:
   - Enter `S`.
   - *Point out*: Clean role-based navigation toggle without complex security framework.
2. **View Full Inventory (Option `1`)**:
   - Show complete fleet table with updated statuses (`SOLD` for Car #2, `RESERVED` for Car #5).
3. **Add New Supercar (Option `2`)**:
   - Brand: `Aston Martin` | Model: `Valhalla` | Price: `800000` | Speed: `350` | Fuel: `2` (Hybrid) | HP: `950` | Accel: `2.5`
   - *Point out*: Assigned ID generated automatically, duplicate-guard verification (`HashSet`), and snapshot pushed onto `UndoManager` stack.
4. **Undo Last Action (Option `5` [EX-7])**:
   - Select Option `5`.
   - *Point out*: `UndoManager` pops the `ArrayDeque` LIFO stack and cleanly removes the newly added car from inventory.
5. **Sales Ledger & Receipt Reprint (Option `6`)**:
   - Show the recorded transaction ledger (`data/sales.csv`).
   - Reprint receipt for Sale #4.
6. **Showroom Valuation & Audit Log (Options `7` & `8`)**:
   - Option `7`: Active stock valuation excluding sold cars ($O(N)$ calculation).
   - Option `8`: View system audit trail in `logs/audit.log` showing timestamps and actors.
7. **Exit**:
   - Enter `0`.

---

### Act 3: Architectural Defense & Viva Readiness (1:30)
*Briefly summarize to Examiner*:
1. **Thread Synchronization**: `SalesService` and `InventoryService` share the same monitor lock (`inventoryService.getLock()`), preventing race conditions and deadlocks.
2. **Safe Traversal**: `ReservationManager` uses `PriorityQueue<Reservation>` (min-heap) and `Iterator.remove()` to eliminate `ConcurrentModificationException` during expiration sweeps.
3. **Data Structure Variety**: 10 distinct collection types utilized purposefully (`ArrayList`, `HashMap`, `HashSet`, `TreeSet`, `LinkedList`, `PriorityQueue`, `ArrayDeque`, `LinkedHashMap`, 2D Array `String[][]`).
4. **Rubric Alignment**: Point to `docs/CA3_SUBMISSION_CHECKLIST.md` confirming full readiness across Rubric Rows 1 through 5.
