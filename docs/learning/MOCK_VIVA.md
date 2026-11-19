# RevLine-Java Mock Viva Voce (20 Questions & Evaluation Guide)

This document contains **20 random viva questions** sampled across all 30 syllabus lectures (Units I–VI) for 25CSE0202 Programming in Java. It provides the question, lecture context, model student answer, code pointer, and evaluation criteria.

---

## Question 1 (Lecture 2: Encapsulation & Identity)
- **Question**: Why did you make `Car` extend an abstract class `Vehicle`, and why is `id` declared `protected final`?
- **Model Answer**: `Vehicle` captures core properties common to any motorized transport (`id`, `brand`, `model`, `price`) while preventing instantiation on its own without specific supercar specs (`topSpeed`, `horsepower`). The `id` is marked `final` because entity identity is immutable once assigned by the repository; `protected` allows direct accessor usage in subclasses while hiding it from external packages.
- **Code Pointer**: `src/main/java/com/revline/model/Vehicle.java#id`

---

## Question 2 (Lecture 3: Control Flow & Input Validation)
- **Question**: How does `Validator.requireNonBlank` prevent malformed inputs, and why does it throw an unchecked exception?
- **Model Answer**: It verifies that string arguments are non-null and contain at least one non-whitespace character via `.trim().isEmpty()`. It throws unchecked `InvalidCarException` (subclass of `RuntimeException`) because passing invalid input to domain constructors violates a programmatic precondition, representing an input validation error rather than an unrecoverable system I/O failure.
- **Code Pointer**: `src/main/java/com/revline/util/Validator.java#requireNonBlank`

---

## Question 3 (Lecture 5: Inheritance & Polymorphism)
- **Question**: How does your repository layer use polymorphism and interface segregation?
- **Model Answer**: We define a generic interface `Repository<T, ID>` and sub-interface `CarRepository`. Both `FileCarRepository` (CSV flat-file) and `JdbcCarRepository` (SQLite relational) implement this interface. The service layer interacts strictly with the `CarRepository` interface, allowing the application to swap storage engines dynamically at runtime without modifying business logic.
- **Code Pointer**: `src/main/java/com/revline/repository/CarRepository.java`

---

## Question 4 (Lecture 6: Collections & ListIterator)
- **Question**: In `handleBrowseShowroom()`, why did you use `ListIterator` instead of a standard `for` loop or `Iterator`?
- **Model Answer**: Standard `Iterator` only traverses in the forward direction via `.next()`. `ListIterator` provides bidirectional traversal via `.hasPrevious()` and `.previous()`, allowing the interactive showroom browser to step backward and forward through vehicles upon user keyboard commands (`N` for Next, `P` for Previous).
- **Code Pointer**: `src/main/java/com/revline/app/BuyerMenuHandler.java#handleBrowseShowroom`

---

## Question 5 (Lecture 7: Deque & Stack for Undo Operations)
- **Question**: How does `UndoManager` implement the undo feature, and why choose `ArrayDeque` over `java.util.Stack`?
- **Model Answer**: `UndoManager` pushes an immutable `UndoAction` snapshot onto an `ArrayDeque` acting as a LIFO stack before every mutating operation (ADD, UPDATE, DELETE). `ArrayDeque` is preferred over `Stack` because `Stack` extends the legacy synchronized `Vector` class, incurring unnecessary locking overhead.
- **Code Pointer**: `src/main/java/com/revline/service/UndoManager.java#undo`

---

## Question 6 (Lecture 8: Sets & Hash Collision Invariants)
- **Question**: Explain how `duplicateGuard` works in `InventoryService` and its time complexity.
- **Model Answer**: `duplicateGuard` is a `HashSet<String>` storing composite keys formatted as `brand.toLowerCase().trim() + "|" + model.toLowerCase().trim()`. Checking `contains(key)` takes expected $O(1)$ time, preventing duplicate vehicle entries with zero sensitivity to casing or whitespace.
- **Code Pointer**: `src/main/java/com/revline/service/InventoryService.java#duplicateGuard`

---

## Question 7 (Lecture 8: Custom Comparators & EX-2)
- **Question**: How does `CarComparators.byValueForMoney()` determine which car ranks first?
- **Model Answer**: It computes value ratio as `horsepower / (price / 100,000.0)`. The car offering higher horsepower per $100k price ranks first (descending). If ratios are equal within epsilon (`0.0001`), it tie-breaks using acceleration ascending (lower 0–100 km/h time ranks higher).
- **Code Pointer**: `src/main/java/com/revline/service/CarComparators.java#byValueForMoney`

---

## Question 8 (Lecture 9: Map & LRU Cache Eviction)
- **Question**: How does `RecentlyViewed` evict old cars, and what does `accessOrder = true` do?
- **Model Answer**: `RecentlyViewed` extends `LinkedHashMap<Integer, Car>` initialized with `accessOrder = true`. In access-order mode, querying an existing entry moves it to the most-recently-used tail of the doubly-linked list. Overriding `removeEldestEntry()` to return `size() > 5` automatically evicts the least recently accessed vehicle from the head once capacity is exceeded.
- **Code Pointer**: `src/main/java/com/revline/service/RecentlyViewed.java`

---

## Question 9 (Lecture 10: Process vs Thread)
- **Question**: What is the difference between extending `Thread` and implementing `Runnable` in your codebase?
- **Model Answer**: `AutoSaveThread` extends `Thread` because it represents a dedicated lifecycle daemon with specific priority and lifecycle methods (`shutdown()`, `join()`). `ReservationExpiryThread` and `SalesProcessor` implement `Runnable`, separating the task logic from the underlying `Thread` object executing it.
- **Code Pointer**: `src/main/java/com/revline/concurrency/AutoSaveThread.java` vs `SalesProcessor.java`

---

## Question 10 (Lecture 11: Thread Lifecycle & Daemon Threads)
- **Question**: Why is `AutoSaveThread` marked as a daemon thread, and what happens if it is not?
- **Model Answer**: Daemon threads run background tasks without keeping the JVM process alive when all user threads finish. If `AutoSaveThread` were a user thread, its `while (running) sleep(30000)` loop would keep the JVM running indefinitely after the user exits the CLI.
- **Code Pointer**: `src/main/java/com/revline/concurrency/AutoSaveThread.java#AutoSaveThread.<init>`

---

## Question 11 (Lecture 12: Race Conditions & Intrinsic Locks)
- **Question**: Why did `UnsafeCounter` lose updates in Demo 1 of your Concurrency Lab?
- **Model Answer**: `count++` consists of three bytecode instructions: `GETFIELD`, `IADD`, and `PUTFIELD`. Without synchronization, multiple threads interleave during read-modify-write, reading stale values and overwriting updates. Marking the method `synchronized` acquires the monitor lock, enforcing atomicity and memory visibility.
- **Code Pointer**: `src/main/java/com/revline/concurrency/ConcurrencyLab.java#UnsafeCounter`

---

## Question 12 (Lecture 13: Guarded Blocks & EX-8)
- **Question**: In `PurchaseRequestQueue.take()`, why is `wait()` placed inside a `while` loop rather than an `if` block?
- **Model Answer**: A thread can experience spurious wakeups (waking without a corresponding signal) or another consumer thread could interleave and take the newly available item first. Wrapping `wait()` in `while (queue.isEmpty())` guarantees that the queue non-empty invariant is re-verified before proceeding.
- **Code Pointer**: `src/main/java/com/revline/concurrency/PurchaseRequestQueue.java#take`

---

## Question 13 (Lecture 14: Memory Visibility & Interruption)
- **Question**: What is the role of `volatile` in `AutoSaveThread`?
- **Model Answer**: Without `volatile`, thread-local CPU register caching may prevent a background worker thread from observing a write to `running = false` made by the main thread. Marking `running` as `volatile` enforces memory barriers so reads and writes go directly to main memory.
- **Code Pointer**: `src/main/java/com/revline/concurrency/AutoSaveThread.java#running`

---

## Question 14 (Lecture 16: Crash-Safe File Writes)
- **Question**: How does `FileUtil.safeWrite()` prevent data loss during power failure or unexpected crashes?
- **Model Answer**: Direct in-place writes truncate existing files before writing. `safeWrite()` writes the new content to a temporary `.tmp` file. Only after writing succeeds does it rename the current file to `.bak` and promote `.tmp` to the target. If promotion fails, it restores the `.bak` file.
- **Code Pointer**: `src/main/java/com/revline/util/FileUtil.java#safeWrite`

---

## Question 15 (Lecture 17: Byte Streams vs Character Streams)
- **Question**: Why did you use `FileInputStream` instead of `FileReader` for database backups?
- **Model Answer**: SQLite database files are binary files. `FileReader` interprets input through a character encoding (like UTF-8), altering byte sequences that do not map to valid characters. `FileInputStream` and `FileOutputStream` transfer raw bytes unchanged via a 4KB buffer.
- **Code Pointer**: `src/main/java/com/revline/service/ByteStreamBackup.java#backupFile`

---

## Question 16 (Lecture 18: StreamTokenizer for Script Parsing)
- **Question**: How does `StreamTokenizer` parse DSL automation commands in `ScriptRunner`?
- **Model Answer**: It splits text into tokens based on syntax table rules. It identifies tokens via `ttype` (`TT_WORD`, `TT_NUMBER`, `TT_EOL`, `TT_EOF`), stores words/strings in `sval`, handles quoted tokens (`"..."`), and skips comment tokens prefixed with `#`.
- **Code Pointer**: `src/main/java/com/revline/service/ScriptRunner.java#run`

---

## Question 17 (Lecture 21: PreparedStatement & SQL Injection)
- **Question**: How does `JdbcCarRepository` prevent SQL injection?
- **Model Answer**: It uses `PreparedStatement` with typed parameter binding (`setString`, `setDouble`, `setInt`). The database parses the SQL template before values are supplied, treating user input strictly as literal data rather than executable SQL syntax.
- **Code Pointer**: `src/main/java/com/revline/repository/JdbcCarRepository.java#findByPriceRange`

---

## Question 18 (Lecture 22: Relational Transactions & ACID)
- **Question**: How does `JdbcSaleRepository.recordSaleAtomic()` achieve atomicity?
- **Model Answer**: It sets `connection.setAutoCommit(false)` to begin a multi-statement transaction. It updates the car status to `SOLD` and inserts the sale record. If both succeed, it calls `commit()`. If any error occurs, it calls `rollback()` in the `catch` block to revert all changes.
- **Code Pointer**: `src/main/java/com/revline/repository/JdbcSaleRepository.java#recordSaleAtomic`

---

## Question 19 (Lecture 24–25: Streams, Lambdas & Downstream Collectors)
- **Question**: In report R7 (`averagePriceByBrand`), explain the stream pipeline and collector structure.
- **Model Answer**: `inventoryService.findAll().stream().collect(Collectors.groupingBy(Car::getBrand, Collectors.averagingDouble(Car::getPrice)))`. The classifier function `Car::getBrand` creates buckets for each brand, and the downstream collector `averagingDouble` computes the arithmetic mean price per bucket.
- **Code Pointer**: `src/main/java/com/revline/service/ReportService.java#averagePriceByBrand`

---

## Question 20 (Lecture 27–28: Generics, Bounded Parameters & Wildcards)
- **Question**: Explain the signature `public static <T extends Comparable<T>> T maxOf(T a, T b)` and why it is type-safe.
- **Model Answer**: `<T extends Comparable<T>>` declares a bounded type parameter ensuring that whatever type `T` is supplied, it implements `Comparable` against its own type. This guarantees that `a.compareTo(b)` is valid at compile time without requiring runtime type casting.
- **Code Pointer**: `src/main/java/com/revline/util/GenericUtils.java#maxOf`
