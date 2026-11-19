# RevLine-Java Comprehensive Viva Question Bank (80+ Questions)

This question bank contains **85 categorized viva voce questions** across all six syllabus units (Lectures 1–30) for 25CSE0202 Programming in Java. Each entry provides a crisp, academically rigorous explanation paired with a direct "Show me in code" pointer.

---

## Unit I: Object-Oriented Foundations & Core Java (Lectures 1–5)

### Q1. Why did you migrate the legacy C project to Java?
- **Answer**: The C prototype relied on global arrays, unbounded buffer inputs (`scanf`), direct in-place file overwrites, and lacked type safety and data encapsulation. Java provides memory safety through JVM garbage collection, strong encapsulation via classes and interfaces, robust exception handling, and built-in multithreading.
- **Show me in code**: `src/main/java/com/revline/model/Car.java#Car`

### Q2. Explain the difference between JDK, JRE, and JVM.
- **Answer**: The JVM (Java Virtual Machine) executes compiled bytecode (`.class`). The JRE (Java Runtime Environment) contains the JVM plus core standard class libraries. The JDK (Java Development Kit) includes the JRE plus developer tools: the compiler (`javac`), archiver (`jar`), and debugger.
- **Show me in code**: `scripts/build.bat` (invokes `javac --release 17 -Xlint:all`)

### Q3. Why is `Vehicle` declared abstract instead of a concrete class?
- **Answer**: `Vehicle` defines common properties (`id`, `brand`, `model`, `price`) shared by all automobiles but should never be instantiated on its own without specific supercar performance specifications (top speed, horsepower, fuel type). An abstract class enforces specialization in child classes.
- **Show me in code**: `src/main/java/com/revline/model/Vehicle.java#Vehicle`

### Q4. How does constructor chaining work in your domain model?
- **Answer**: Subclasses invoke `super(id, brand, model, price)` as the first statement in their constructors to initialize inherited base fields before initializing derived fields (`topSpeed`, `fuelType`, `horsepower`, `acceleration`).
- **Show me in code**: `src/main/java/com/revline/model/Car.java#Car.<init>`

### Q5. What is the purpose of the `copy()` method in `Car`?
- **Answer**: It returns a deep, independent copy of the vehicle instance with identical fields. This prevents external mutation leaks when returning entities from services or storing snapshots in undo buffers.
- **Show me in code**: `src/main/java/com/revline/model/Car.java#Car.copy`

### Q6. Why is the vehicle `id` field marked `final`?
- **Answer**: Vehicle identity is immutable. Once assigned by the repository or database sequence, its unique primary key must never be altered during the entity's runtime lifecycle.
- **Show me in code**: `src/main/java/com/revline/model/Vehicle.java#id`

### Q7. How does `FuelType` enforce type safety over strings?
- **Answer**: An enum restricts values to a closed set (`PETROL`, `HYBRID`, `ELECTRIC`). Any invalid string supplied at input is rejected immediately by `FuelType.fromString()`, preventing silent data corruption.
- **Show me in code**: `src/main/java/com/revline/model/FuelType.java#FuelType.fromString`

### Q8. What is the difference between method overriding and overloading?
- **Answer**: Overloading occurs within the same class where methods share a name but differ in parameter count or types (e.g., constructors in `Car`). Overriding occurs when a subclass provides a specific implementation of a method defined in its superclass or interface (e.g., `Car.describe()` overriding `Vehicle.describe()`).
- **Show me in code**: `src/main/java/com/revline/model/Car.java#describe`

### Q9. Why are static fields used for tracking total created vehicles?
- **Answer**: Static fields belong to the class rather than any single instance, allowing shared state across all instantiations (e.g., `Vehicle.createdCount`).
- **Show me in code**: `src/main/java/com/revline/model/Vehicle.java#createdCount`

### Q10. How do you prevent CSV delimiter injection in user text inputs?
- **Answer**: `Validator.validateStringNoDelimiter()` checks that user strings do not contain commas (`","`). If a comma were permitted, saving to a CSV flat-file would misalign subsequent column tokens.
- **Show me in code**: `src/main/java/com/revline/util/Validator.java#validateStringNoDelimiter`

---

## Unit II: Collections Framework, Algorithms & Data Structures (Lectures 6–9)

### Q11. Explain your Dual-Collection architecture in `InventoryService`.
- **Answer**: `InventoryService` maintains an `ArrayList<Car>` for ordered iteration and indexing, a `HashMap<Integer, Car>` for O(1) primary key lookup, and a `HashSet<String>` for O(1) duplicate brand+model detection.
- **Show me in code**: `src/main/java/com/revline/service/InventoryService.java#InventoryService.cars`

### Q12. Why does `findAll()` return an unmodifiable list of defensive copies?
- **Answer**: Simply returning `Collections.unmodifiableList(cars)` prevents external code from adding or removing items, but callers could still mutate individual `Car` objects. Copying each car (`c.copy()`) guarantees that internal service state cannot be corrupted.
- **Show me in code**: `src/main/java/com/revline/service/InventoryService.java#findAll`

### Q13. How does `ListIterator` support bidirectional showroom browsing?
- **Answer**: Unlike a standard forward-only `Iterator`, `ListIterator` supports both `.next()` and `.previous()`, `.hasNext()` and `.hasPrevious()`, enabling interactive "Next / Previous" navigation in the CLI without re-indexing.
- **Show me in code**: `src/main/java/com/revline/app/BuyerMenuHandler.java#handleBrowseShowroom`

### Q14. How does `UndoManager` implement the undo feature using a Deque?
- **Answer**: An `ArrayDeque<UndoAction>` functions as a LIFO stack. Every ADD, UPDATE, or DELETE pushes a snapshot containing the inverse operation. When `undo()` is invoked, the action is popped and reversed (e.g., an ADD reverses by removing the car; a DELETE reverses by restoring the car).
- **Show me in code**: `src/main/java/com/revline/service/UndoManager.java#undo`

### Q15. Why use `TreeSet` for distinct brand names?
- **Answer**: `TreeSet` is backed by a Red-Black tree that automatically eliminates duplicate brands and maintains them in case-insensitive alphabetical order (`String.CASE_INSENSITIVE_ORDER`).
- **Show me in code**: `src/main/java/com/revline/service/InventoryService.java#getBrandsSorted`

### Q16. How does `RecentlyViewed` implement an LRU cache?
- **Answer**: It extends `LinkedHashMap<Integer, Car>` configured with `accessOrder = true`. Accessing an existing key moves it to the tail. Overriding `removeEldestEntry()` to return `size() > capacity` automatically evicts the oldest entry when capacity (5) is exceeded.
- **Show me in code**: `src/main/java/com/revline/service/RecentlyViewed.java#RecentlyViewed`

### Q17. What is the difference between `Comparable` and `Comparator`?
- **Answer**: `Comparable` defines a class's natural ordering via `compareTo()` inside the class (in `Car`, natural order is by `id` ascending). `Comparator` defines external, alternative ordering strategies (e.g., `BY_PRICE_ASC`, `BY_SPEED_DESC`, `byValueForMoney()`).
- **Show me in code**: `src/main/java/com/revline/model/Car.java#compareTo`, `src/main/java/com/revline/service/CarComparators.java`

### Q18. How does `byValueForMoney()` calculate vehicle ranking in EX-2?
- **Answer**: It calculates value ratio as `horsepower / (price / 100,000.0)`. Cars with higher horsepower per $100k rank first (descending). If value ratios are equal within epsilon (`0.0001`), it tie-breaks by acceleration ascending (lower 0–100 km/h time ranks higher).
- **Show me in code**: `src/main/java/com/revline/service/CarComparators.java#byValueForMoney`

### Q19. How does `WaitlistService` ensure strict FIFO ordering?
- **Answer**: It maps each car ID to an `ArrayDeque<String>` acting as a FIFO queue. `join()` calls `addLast()`, and priority polling calls `pollFirst()`, guaranteeing first-come, first-served customer allocation.
- **Show me in code**: `src/main/java/com/revline/service/WaitlistService.java#WaitlistService.join`

### Q20. Why use `Iterator.remove()` in `ReservationManager.purgeExpired()` instead of a for-each loop?
- **Answer**: Calling `collection.remove()` while traversing inside an enhanced for-loop invalidates the iterator's expected mod-count, throwing `ConcurrentModificationException`. `Iterator.remove()` safely updates the underlying collection during traversal.
- **Show me in code**: `src/main/java/com/revline/service/ReservationManager.java#purgeExpired`

---

## Unit III: Multithreading & Concurrency Architecture (Lectures 10–14)

### Q21. What is the difference between a process and a thread?
- **Answer**: A process is an independent execution unit with its own dedicated virtual memory address space managed by the OS. Threads exist within a process and share its heap, static memory, and file handles, but each thread has its own private call stack and program counter.
- **Show me in code**: `src/main/java/com/revline/concurrency/AutoSaveThread.java`

### Q22. What is a daemon thread and why is `AutoSaveThread` marked daemon?
- **Answer**: A daemon thread provides background support services and does not prevent the JVM from exiting when all user threads finish. `AutoSaveThread` is marked daemon so that when a user quits the CLI application, the JVM terminates cleanly without hanging on background timers.
- **Show me in code**: `src/main/java/com/revline/concurrency/AutoSaveThread.java#AutoSaveThread.<init>`

### Q23. Why did `count++` lose updates in Demo 1 of the Concurrency Lab?
- **Answer**: `count++` is not atomic; it consists of three separate bytecode instructions: read current value from heap, increment register, write back to heap. When multiple threads interleave, they read stale values and overwrite each other's increments.
- **Show me in code**: `src/main/java/com/revline/concurrency/ConcurrencyLab.java#UnsafeCounter.increment`

### Q24. How does `SafeCounter` fix the race condition?
- **Answer**: Declaring `increment()` as `synchronized` acquires the intrinsic monitor lock of the instance. Only one thread can execute inside the synchronized method at a time, enforcing mutual exclusion and memory visibility.
- **Show me in code**: `src/main/java/com/revline/concurrency/ConcurrencyLab.java#SafeCounter.increment`

### Q25. Why must `SalesService.sellCar()` share the monitor lock of `InventoryService`?
- **Answer**: If `SalesService` used its own lock, concurrent inventory mutations (like updating or deleting a car) would not block sales operations. Sharing `inventoryService.getLock()` ensures that status verification and persistence execute atomically across both services.
- **Show me in code**: `src/main/java/com/revline/service/SalesService.java#sellCar`

### Q26. Explain the guarded block in `PurchaseRequestQueue.take()` (EX-8).
- **Answer**: The consumer thread synchronizes on the queue monitor. If the queue is empty, it enters a `while (queue.isEmpty()) wait()` loop. The `while` loop protects against spurious wakeups. Once an item is available, it dequeues it, calls `notifyAll()` to awaken waiting producers, and returns the request.
- **Show me in code**: `src/main/java/com/revline/concurrency/PurchaseRequestQueue.java#take`

### Q27. Why must `wait()` always be called inside a `while` loop rather than an `if` statement?
- **Answer**: Because of spurious wakeups (where the OS wakes a waiting thread without a signal) and condition race changes (another thread could consume the item before this thread resumes execution). A `while` loop re-checks the predicate upon wakeup.
- **Show me in code**: `src/main/java/com/revline/concurrency/PurchaseRequestQueue.java#put`

### Q28. What is the role of `notifyAll()` versus `notify()`?
- **Answer**: `notify()` wakes up an arbitrary single thread from the monitor's wait set, which can lead to missed signals if the wrong type of thread (e.g., another producer instead of a consumer) is awakened. `notifyAll()` wakes all waiting threads, allowing each to re-evaluate its condition predicate safely.
- **Show me in code**: `src/main/java/com/revline/concurrency/PurchaseRequestQueue.java#put`

### Q29. What does the `volatile` keyword do in Java?
- **Answer**: `volatile` guarantees memory visibility and prevents instruction reordering. Writes to a `volatile` variable are flushed immediately to main memory, and reads bypass thread-local CPU register caches, ensuring that all threads observe changes immediately.
- **Show me in code**: `src/main/java/com/revline/concurrency/AutoSaveThread.java#running`

### Q30. How do you safely shut down a background worker thread?
- **Answer**: By signaling interruption (`thread.interrupt()`) and setting a volatile termination flag, followed by calling `thread.join()` in the main thread to wait for it to exit cleanly.
- **Show me in code**: `src/main/java/com/revline/concurrency/AutoSaveThread.java#shutdown`

---

## Unit IV: Exception Handling & File I/O Architecture (Lectures 15–18)

### Q31. What is the difference between checked and unchecked exceptions?
- **Answer**: Checked exceptions (subclasses of `Exception` excluding `RuntimeException`) represent recoverable external failures (like `IOException` or `SQLException`) that the compiler forces you to handle with `try-catch` or declare with `throws`. Unchecked exceptions (`RuntimeException` and `Error`) represent programming bugs or invariant violations that do not require mandatory throws clauses.
- **Show me in code**: `src/main/java/com/revline/util/exception/InvalidCarException.java` (unchecked) vs `RepositoryException.java` (checked)

### Q32. Explain the exception hierarchy in RevLine.
- **Answer**: All custom domain exceptions extend `RevLineException`. Data access errors throw checked `RepositoryException`. Domain invariant failures throw unchecked `InvalidCarException`, `CarNotFoundException`, or `CarNotAvailableException`.
- **Show me in code**: `src/main/java/com/revline/util/exception/RevLineException.java`

### Q33. What is multi-catch and how is it used in batch CSV imports?
- **Answer**: Multi-catch allows multiple unrelated exception types to be caught in a single catch block separated by pipe `|` (`catch (NumberFormatException | InvalidCarException ex)`). This consolidates error reporting for malformed CSV rows without duplicating recovery logic.
- **Show me in code**: `src/main/java/com/revline/service/DataToolsService.java#importCsv`

### Q34. How does `try-with-resources` prevent resource leaks?
- **Answer**: Any class implementing `java.lang.AutoCloseable` instantiated in the `try (...)` header is guaranteed to have its `.close()` method invoked automatically when exiting the block, even if an exception or return statement occurs.
- **Show me in code**: `src/main/java/com/revline/repository/FileCarRepository.java#findAll`

### Q35. What is the difference between byte streams and character streams?
- **Answer**: Byte streams (`InputStream`, `OutputStream`) read and write raw 8-bit bytes without character set translation, making them suitable for binary files (SQLite databases, images). Character streams (`Reader`, `Writer`) translate bytes to 16-bit Unicode characters according to a charset encoding (e.g., UTF-8), making them ideal for text files (CSV, reports).
- **Show me in code**: `src/main/java/com/revline/service/ByteStreamBackup.java` (byte stream) vs `FileCarRepository.java` (character stream)

### Q36. How does `FileUtil.safeWrite()` ensure crash-safe atomic file writes?
- **Answer**: It writes new content to a temporary file (`.tmp`). If writing succeeds, it renames the current production file to backup (`.bak`), renames `.tmp` to the target file, and deletes `.bak`. If an error occurs midway, it restores the original from `.bak`, preventing file truncation or corruption.
- **Show me in code**: `src/main/java/com/revline/util/FileUtil.java#safeWrite`

### Q37. How does `StreamTokenizer` parse batch DSL automation scripts?
- **Answer**: `StreamTokenizer` tokenizes text using a configurable syntax table. Tokens are categorized by `ttype` (`TT_WORD`, `TT_NUMBER`, `TT_EOL`, `TT_EOF`), with string payloads in `sval`. We configure quote characters (`"`), comment characters (`#`), and EOL significance to parse commands like `ADD`, `SELL`, `RESERVE`.
- **Show me in code**: `src/main/java/com/revline/service/ScriptRunner.java#run`

### Q38. How does `DataToolsService.areFilesByteIdentical()` verify backups?
- **Answer**: It first verifies file lengths match, then uses two buffered `FileInputStream` instances to compare byte-by-byte. If every byte matches until EOF, the backup is proven 100% byte-identical.
- **Show me in code**: `src/main/java/com/revline/service/ByteStreamBackup.java#areFilesByteIdentical`

### Q39. How do you handle legacy C format imports with status integers?
- **Answer**: `DataToolsService.importLegacyC()` parses the legacy 9-column format from `cars.txt`, mapping status flag `1 -> AVAILABLE` and `0 -> SOLD`, allowing backward compatibility without altering the modern CSV schema.
- **Show me in code**: `src/main/java/com/revline/service/DataToolsService.java#importLegacyC`

### Q40. Why must `BufferedReader` and `BufferedWriter` wrap raw file streams?
- **Answer**: Raw `FileReader` and `FileWriter` make expensive OS disk read/write calls for every character. Wrapping them in buffers batches I/O operations into 8KB in-memory buffers, drastically reducing system call overhead.
- **Show me in code**: `src/main/java/com/revline/repository/FileCarRepository.java#findAll`

---

## Unit V: Relational Databases & JDBC Architecture (Lectures 19–22)

### Q41. What are the four core interfaces of the JDBC API?
- **Answer**: `Driver` (database driver implementation), `Connection` (active session with the database), `Statement` / `PreparedStatement` (SQL execution container), and `ResultSet` (cursor-based tabular query result).
- **Show me in code**: `src/main/java/com/revline/repository/DatabaseManager.java`

### Q42. Why is `PRAGMA foreign_keys = ON;` required in SQLite JDBC?
- **Answer**: SQLite disables foreign key constraint enforcement by default for backward compatibility. Enabling it on every opened connection ensures referential integrity between `sales(car_id)` and `cars(id)`.
- **Show me in code**: `src/main/java/com/revline/repository/DatabaseManager.java#getConnection`

### Q43. What is SQL injection and how does `PreparedStatement` prevent it?
- **Answer**: SQL injection occurs when untrusted user strings are concatenated directly into SQL queries, altering the command structure. `PreparedStatement` pre-compiles the SQL template in the database engine and transmits user inputs as typed parameters via protocol placeholders (`?`), ensuring they are treated strictly as data literals.
- **Show me in code**: `src/test/java/com/revline/SqlInjectionTest.java`

### Q44. How does `JdbcSaleRepository.recordSaleAtomic()` manage transactions?
- **Answer**: It saves the connection's original auto-commit status and sets `connection.setAutoCommit(false)`. It executes both the vehicle status update (`UPDATE cars SET status = 'SOLD'`) and the sales ledger insertion (`INSERT INTO sales...`). If both succeed, it calls `connection.commit()`. If an exception occurs, it calls `connection.rollback()` in the catch block to guarantee ACID atomicity.
- **Show me in code**: `src/main/java/com/revline/repository/JdbcSaleRepository.java#recordSaleAtomic`

### Q45. Explain the ACID properties in relational persistence.
- **Answer**:
  - **Atomicity**: All statements in a transaction succeed, or all roll back.
  - **Consistency**: The database transitions from one valid state to another, upholding all constraints (foreign keys, checks, uniqueness).
  - **Isolation**: Concurrent transactions execute without cross-contamination.
  - **Durability**: Committed data survives crashes and power loss.
- **Show me in code**: `src/main/java/com/revline/repository/JdbcSaleRepository.java#recordSaleAtomic`

### Q46. What is the difference between `Statement` and `PreparedStatement`?
- **Answer**: `Statement` parses and compiles SQL every time it is executed, offering no protection against SQL injection. `PreparedStatement` pre-compiles the query template once, caches the execution plan, and binds parameters safely using typed setters (`setInt`, `setString`, `setDouble`).
- **Show me in code**: `src/main/java/com/revline/repository/JdbcCarRepository.java#findByPriceRange`

### Q47. How do candidate keys in SQL protect catalog integrity?
- **Answer**: A candidate key defined as `UNIQUE (brand, model)` in the `cars` table schema prevents duplicate vehicles from ever being inserted into the database.
- **Show me in code**: `src/main/java/com/revline/repository/SchemaInitializer.java#initialize`

### Q48. How does `ResultSet` cursor navigation work?
- **Answer**: A `ResultSet` cursor initially points before the first row. Calling `rs.next()` advances the cursor to the next row and returns `true` if a row exists, allowing iterative data extraction using typed getters (`rs.getInt("id")`, `rs.getString("brand")`).
- **Show me in code**: `src/main/java/com/revline/repository/JdbcCarRepository.java#mapRow`

### Q49. What is the purpose of `RepositoryContractTest`?
- **Answer**: It is a parametric integration test that runs the exact same test assertions against both `FileCarRepository` (CSV) and `JdbcCarRepository` (SQLite) to verify behavioral parity across storage engines.
- **Show me in code**: `src/test/java/com/revline/RepositoryContractTest.java`

### Q50. How does runtime storage mode selection work in RevLine?
- **Answer**: `RevLineApp` parses the launch CLI arguments. Passing `--storage=jdbc` initializes SQLite database repositories, while the default `--storage=csv` initializes flat-file CSV repositories.
- **Show me in code**: `src/main/java/com/revline/app/RevLineApp.java#main`

---

## Unit VI: Streams, Lambdas, Generics & Architecture (Lectures 23–30)

### Q51. What is a functional interface?
- **Answer**: An interface containing exactly one abstract method (Single Abstract Method - SAM), optionally marked with `@FunctionalInterface`. Instances of functional interfaces can be implemented using lambda expressions or method references.
- **Show me in code**: `src/main/java/com/revline/service/LambdaShowcase.java#VehicleTransformer`

### Q52. Explain the four core built-in functional interfaces in `java.util.function`.
- **Answer**:
  - `Predicate<T>`: accepts `T`, returns `boolean` (`test()`).
  - `Consumer<T>`: accepts `T`, returns `void` (`accept()`).
  - `Function<T, R>`: accepts `T`, returns `R` (`apply()`).
  - `Supplier<T>`: accepts nothing, returns `T` (`get()`).
- **Show me in code**: `src/main/java/com/revline/service/LambdaShowcase.java#demonstrateStandardInterfaces`

### Q53. Name and explain the four kinds of method references in Java.
- **Answer**:
  1. **Static method reference**: `ContainingClass::staticMethod` (e.g., `CarComparators::compareByHp`).
  2. **Bound instance method reference**: `instance::instanceMethod` (e.g., `System.out::println`).
  3. **Unbound instance method reference**: `ContainingType::instanceMethod` (e.g., `Car::getPrice`).
  4. **Constructor reference**: `ClassName::new` (e.g., `ArrayList::new`).
- **Show me in code**: `src/main/java/com/revline/service/LambdaShowcase.java#demonstrateMethodReferences`

### Q54. What is the difference between intermediate and terminal stream operations?
- **Answer**: Intermediate operations (`filter`, `map`, `sorted`, `limit`, `skip`, `peek`) transform a stream into another stream and are evaluated lazily. Terminal operations (`collect`, `sum`, `count`, `forEach`) trigger consumption of the pipeline and produce a non-stream result.
- **Show me in code**: `src/main/java/com/revline/service/ReportService.java#getAvailableCars`

### Q55. How did pipeline R15 prove stream laziness?
- **Answer**: In `demonstrateLaziness()`, an intermediate `.peek(counter::increment)` is attached to a stream without a terminal operation. The counter remains `0` because intermediate operations never execute until a terminal operation is called.
- **Show me in code**: `src/main/java/com/revline/service/ReportService.java#demonstrateLaziness`

### Q56. How does `DoubleStream` avoid primitive boxing overhead in pipeline R2?
- **Answer**: `mapToDouble(Car::getPrice)` transforms the object stream into a specialized primitive `DoubleStream`. Calling `.sum()` operates on primitive `double` values directly without boxing them into `java.lang.Double` wrapper objects.
- **Show me in code**: `src/main/java/com/revline/service/ReportService.java#calculateAvailableStockValue`

### Q57. How does pipeline R4 count cars by fuel type using downstream collectors?
- **Answer**: `countCarsByFuelType()` groups vehicles using `Collectors.groupingBy(Car::getFuelType, Collectors.counting())`, populating a map where each fuel type maps to the count of matching vehicles.
- **Show me in code**: `src/main/java/com/revline/service/ReportService.java#countCarsByFuelType`

### Q58. How is pagination implemented with Streams in pipeline R9?
- **Answer**: Pagination uses `stream().skip((page - 1) * pageSize).limit(pageSize).collect(Collectors.toList())`. `skip()` discards preceding records, and `limit()` stops traversal once the page size is fulfilled.
- **Show me in code**: `src/main/java/com/revline/service/ReportService.java#getCarsPaged`

### Q59. What is a bounded type parameter in Generics?
- **Answer**: A generic parameter constrained by an upper bound using `extends`, such as `<T extends Comparable<T>> T maxOf(T a, T b)`. This ensures that type `T` can only be replaced by types that implement `Comparable`.
- **Show me in code**: `src/main/java/com/revline/util/GenericUtils.java#maxOf`

### Q60. What is an upper-bounded wildcard and why use `List<? extends Vehicle>`?
- **Answer**: `List<? extends Vehicle>` accepts a list of `Vehicle` or any of its subclasses (such as `List<Car>`). Because `List<Car>` is not a subtype of `List<Vehicle>` in Java generics, the wildcard is necessary to allow read-only polymorphic traversal.
- **Show me in code**: `src/main/java/com/revline/util/GenericUtils.java#printAll`

### Q61. What is type erasure in Java generics?
- **Answer**: The Java compiler replaces all generic type parameters with their bounding types (or `Object` if unbounded) and inserts appropriate casts in the compiled bytecode. This maintains full binary compatibility with pre-generic Java versions.
- **Show me in code**: `src/main/java/com/revline/util/Pair.java`

### Q62. Explain the 3-Tier Layered Architecture of RevLine.
- **Answer**:
  1. **Presentation Layer (`app`, `ui`)**: Handles console rendering, CLI menus, user prompts, and tables.
  2. **Service Layer (`service`, `concurrency`)**: Implements business rules, validation, synchronization, transactions, and analytics.
  3. **Repository Layer (`repository`)**: Manages physical persistence across flat-file CSV and SQLite JDBC storage engines.
- **Show me in code**: `docs/03_DESIGN.md`

### Q63. What is the Repository Pattern and why is it useful?
- **Answer**: The Repository Pattern mediates between the domain service layer and data mapping layers by providing a collection-like interface (`CarRepository`). Services remain agnostic of whether data is stored in memory, flat files, or a relational database.
- **Show me in code**: `src/main/java/com/revline/repository/CarRepository.java`

### Q64. How does RevLine satisfy the syllabus restriction against JUnit?
- **Answer**: RevLine features a custom, zero-dependency automated test runner (`Assert.java` and `TestRunner.java`) that executes 116 test methods, prints timing diagnostics, and reports assertions using standard Java reflection and primitives.
- **Show me in code**: `src/test/java/com/revline/TestRunner.java`

### Q65. What is the purpose of `AppLogger`?
- **Answer**: It records an immutable, timestamped operational audit log of every major business action (`ADD_CAR`, `SELL_CAR`, `SNAPSHOT_SAVED`, `BACKUP`) to disk at `logs/audit.log` and in a circular in-memory buffer for menu inspection.
- **Show me in code**: `src/main/java/com/revline/util/AppLogger.java`

---

## Unit VII: Deep-Dive Defense Questions (Q66–Q85)

### Q66. What happens if a thread is sleeping and receives an `interrupt()`?
- **Answer**: The thread wakes up immediately and throws `InterruptedException`, clearing its interrupted status flag. The handling code must either handle the shutdown or restore the interrupt flag via `Thread.currentThread().interrupt()`.
- **Show me in code**: `src/main/java/com/revline/concurrency/AutoSaveThread.java#run`

### Q67. Why does `ReservationExpiryThread` sleep for 5 seconds between sweeps?
- **Answer**: Sweeping every 5 seconds provides timely expiration of holds while minimizing CPU utilization and lock contention on the reservation queue.
- **Show me in code**: `src/main/java/com/revline/concurrency/ReservationExpiryThread.java#run`

### Q68. What would happen if `PurchaseRequestQueue` had no capacity bound?
- **Answer**: If producers generated requests faster than the consumer could process them, an unbounded queue would consume heap memory indefinitely, eventually causing an `OutOfMemoryError`. A bounded buffer of 5 slots applies backpressure on producers.
- **Show me in code**: `src/main/java/com/revline/concurrency/PurchaseRequestQueue.java`

### Q69. Why is `SalesProcessor` decoupled from the UI thread?
- **Answer**: Decoupling sales processing into a separate consumer worker thread prevents the CLI user interface from blocking during long-running disk or database transactions.
- **Show me in code**: `src/main/java/com/revline/concurrency/SalesProcessor.java`

### Q70. How does `MenuPrinter` achieve uniform table borders?
- **Answer**: It uses `String.format()` with fixed column widths and Unicode box-drawing characters, ensuring that strings and numbers align predictably across any terminal.
- **Show me in code**: `src/main/java/com/revline/ui/ConsoleTable.java`

### Q71. How do you ensure that `recentlyViewed` does not throw concurrent access exceptions?
- **Answer**: `RecentlyViewed` synchronizes all operations (`recordView`, `getRecentlyViewedList`) on `this`, protecting its internal `LinkedHashMap` when multiple views occur.
- **Show me in code**: `src/main/java/com/revline/service/RecentlyViewed.java`

### Q72. Why does `ReportService` take both `InventoryService` and `SaleRepository`?
- **Answer**: Showroom inventory queries (R1–R9, R12–R15) require live inventory data, while revenue and sales analytics (R10–R11) require historical completed transactions from the sales repository.
- **Show me in code**: `src/main/java/com/revline/service/ReportService.java#ReportService.<init>`

### Q73. What is the time complexity of `SearchService.search()`?
- **Answer**: It iterates through the list of $N$ cars and checks matching tokens across brand, model, and fuel type, operating in $O(N \cdot M)$ time where $M$ is query length.
- **Show me in code**: `src/main/java/com/revline/service/SearchService.java#search`

### Q74. How does `SyntheticDataGenerator` ensure reproducible benchmark data?
- **Answer**: It seeds `java.util.Random` with a fixed seed (`new Random(42)`), guaranteeing that identical pseudorandom cars are generated across every test run.
- **Show me in code**: `src/main/java/com/revline/service/SyntheticDataGenerator.java#generate`

### Q75. How does `DataToolsService.migrate()` move data between storage engines?
- **Answer**: It reads all cars from the source repository via `from.findAll()` and iteratively saves them to the destination repository via `to.save(car)`.
- **Show me in code**: `src/main/java/com/revline/service/DataToolsService.java#migrate`

### Q76. What does `SchemaInitializer` do if database tables already exist?
- **Answer**: All DDL commands use `CREATE TABLE IF NOT EXISTS`, and seed data insertion checks if the table count is zero before populating defaults, making execution fully idempotent.
- **Show me in code**: `src/main/java/com/revline/repository/SchemaInitializer.java#initialize`

### Q77. Why is `AutoSaveThread` priority set to `Thread.MIN_PRIORITY`?
- **Answer**: `MIN_PRIORITY` gives the operating system scheduler a hint that user-facing requests and transaction processing should take precedence over background disk persistence.
- **Show me in code**: `src/main/java/com/revline/concurrency/AutoSaveThread.java#AutoSaveThread.<init>`

### Q78. How does `ComparisonService` format table headers dynamically?
- **Answer**: It builds dynamic format strings (`"| %-16s |"` and repeated column specifiers) based on the number of selected cars (2, 3, or 4).
- **Show me in code**: `src/main/java/com/revline/service/ComparisonService.java#formatTable`

### Q79. How does `FilterBuilder` support multiple composed filters?
- **Answer**: It chains `Predicate<Car>` instances using `.and()`. An initially neutral predicate (`car -> true`) is progressively combined with price, performance, and status predicates.
- **Show me in code**: `src/main/java/com/revline/service/FilterBuilder.java#filter`

### Q80. How is deadlock prevented in RevLine?
- **Answer**: All services acquire intrinsic locks in a strict global order. Because sales operations always acquire `inventoryService.getLock()` before proceeding, conflicting cross-lock acquisitions cannot occur.
- **Show me in code**: `src/main/java/com/revline/service/SalesService.java#sellCar`

### Q81. Why was `ArrayDeque` preferred over `java.util.Stack` for the undo manager?
- **Answer**: `java.util.Stack` extends `Vector` and synchronizes every single method call, incurring unnecessary legacy overhead. `ArrayDeque` provides a faster, unsynchronized LIFO deque implementation.
- **Show me in code**: `src/main/java/com/revline/service/UndoManager.java`

### Q82. What does `EnumMap` offer over a general `HashMap` in report R4?
- **Answer**: `EnumMap` is internally represented as a compact array indexed by enum ordinals, offering superior memory efficiency and faster key access than a hashing-based `HashMap`.
- **Show me in code**: `src/main/java/com/revline/service/ReportService.java#countCarsByFuelType`

### Q83. What is the difference between `System.exit()` and clean shutdown hooks?
- **Answer**: `System.exit()` terminates the JVM immediately. A shutdown hook registered via `Runtime.getRuntime().addShutdownHook()` allows the application to cleanly interrupt daemons, flush file buffers, and close open database connections before process termination.
- **Show me in code**: `src/main/java/com/revline/app/RevLineApp.java#main`

### Q84. Why does `CarComparators` provide both static constants and factory methods?
- **Answer**: Stateless comparators (like `BY_PRICE_ASC` or `BY_SPEED_DESC`) are instantiated once as reusable singleton constants, while configurable comparators (like `byValueForMoney()`) use factory methods.
- **Show me in code**: `src/main/java/com/revline/service/CarComparators.java`

### Q85. How do you prove that every syllabus lecture is covered?
- **Answer**: `docs/04_CONCEPT_MAP.md` contains an exhaustive matrix verifying every lecture 1–30 with specific class and method pointers that have been verified in the compiled codebase.
- **Show me in code**: `docs/04_CONCEPT_MAP.md`
