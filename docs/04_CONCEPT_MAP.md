# 04. Syllabus Concept Map & Lecture Traceability

This document maps the relevant topics in the supplied **25CSE0202 Programming in Java** syllabus (Lectures 1–30, Unit VI Guided Project) to concrete RevLine source-code examples. A code reference shows where a concept is used; it does not, by itself, prove that the related application workflow has been manually verified.

---

## Lecture-to-Code Mapping (Lectures 1–30)

| Lecture | Official Syllabus Topic | Concrete Implementation in RevLine | Verified Code Reference |
|---|---|---|---|
| **L1** | OOP Revisit (classes, constructors, this/static, inheritance, polymorphism, abstraction/interfaces) | Abstract class `Vehicle` extended by `Car`; static factories; interface-driven repository design (`Repository`, `CarRepository`, `SaleRepository`). | `model/Vehicle.java`<br>`model/Car.java`<br>`repository/Repository.java` |
| **L2** | Multi-dimensional arrays, String immutability/pool, formatting, StringBuilder/StringBuffer, wrappers | 2D array comparison matrix (`String[rows][cols]`); 2D price-band matrix (`int[fuel][band]`); dynamic ASCII receipt and table rendering via `StringBuilder`; `Double.parseDouble()`. | `service/ComparisonService.java`<br>`service/ReportService.java`<br>`service/SalesService.java` |
| **L3** | Checked & unchecked exceptions, try/catch/finally/multi-catch, propagation, throw/throws, custom exceptions | Domain exception hierarchy rooted in `RevLineException`, `InvalidCarException` (unchecked), `CarNotFoundException`, `CarNotAvailableException`, `RepositoryException` (checked), multi-catch error handling. | `util/exception/`<br>`util/Validator.java`<br>`service/DataToolsService.java` |
| **L4** | Integrated console practice, debugging, input validation, debugging and structured input validation | Interactive console loop with menu dispatchers; defensive boundary checks for price, horsepower, speed, acceleration. | `ui/InputReader.java`<br>`app/MenuController.java`<br>`util/Validator.java` |
| **L5** | Collections: ArrayList, LinkedList, HashSet, LinkedHashSet, TreeSet | In-memory fleet storage via `ArrayList<Car>`; O(1) duplicate brand+model prevention via `HashSet<String>`; alphabetical unique brand extraction via `TreeSet<String>`; insertion-order unique element preservation via `LinkedHashSet` in `GenericUtils.uniquePreservingOrder()`. | `service/InventoryService.java`<br>`util/GenericUtils.java`<br>`service/ReportService.java` |
| **L6–L7** | HashMap, LinkedHashMap, TreeMap, Queue/Deque/PriorityQueue, Iterator/ListIterator | Primary key indexing via `HashMap<Integer, Car>`; LRU cache via `RecentlyViewed` (`LinkedHashMap`, accessOrder=true); price ladder in `TreeMap`; mutation undo stack in `ArrayDeque<UndoAction>` (`Deque`); FIFO waitlist in `LinkedList<String>` (`Queue`); min-heap reservation hold in `PriorityQueue<Reservation>`; bidirectional browsing via `ListIterator<Car>`. | `service/InventoryService.java`<br>`service/RecentlyViewed.java`<br>`service/UndoManager.java`<br>`service/WaitlistService.java`<br>`service/ReservationManager.java`<br>`app/BuyerShowroomBrowser.java` |
| **L8** | Comparable, Comparator, sorting, Collections utility methods | Natural ordering by ID via `Comparable<Car>`; 7 sorting strategies via `CarComparators` (price, top speed, horsepower, acceleration, brand/model, and value-for-money); defensive copies via `Collections.unmodifiableList()`. | `model/Car.java`<br>`service/CarComparators.java` |
| **L9–L10** | Multithreading fundamentals: Thread/Runnable, start/run, sleep, join, interrupt | Background thread creation via `Thread` extension (`AutoSaveThread`) and `Runnable` implementation (`ReservationExpiryThread`, `SalesProcessor`, `BuyerTask`); interrupt status handling. | `concurrency/AutoSaveThread.java`<br>`concurrency/ReservationExpiryThread.java`<br>`concurrency/ConcurrencyLab.java` |
| **L11–L12** | Thread lifecycle and states, daemon threads, priorities, execution tracing | Educational thread lifecycle tracer inspecting `Thread.State` (`NEW`, `RUNNABLE`, `TIMED_WAITING`, `BLOCKED`, `WAITING`, `TERMINATED`); `MIN_PRIORITY` daemon threads for background autosave. | `concurrency/ConcurrencyLabDemos.java`<br>`concurrency/AutoSaveThread.java` |
| **L13–L14** | Race conditions, critical sections, synchronized methods/blocks, intrinsic locks, atomicity and visibility | Race condition proof (`UnsafeCounter` lost updates vs `SafeCounter`); synchronized monitor lock coordinating showroom state between inventory and sales; `volatile boolean running` ensuring cross-thread cache visibility. | `concurrency/ConcurrencyLab.java`<br>`service/SalesService.java`<br>`concurrency/AutoSaveThread.java` |
| **L15–L16** | wait/notify/notifyAll, guarded blocks, producer-consumer, monitors | Bounded buffer queue (`PurchaseRequestQueue`, capacity 5) using while-loop guarded blocks, `wait()` when full/empty, and `notifyAll()` signaling between producers and consumers. | `concurrency/PurchaseRequestQueue.java`<br>`concurrency/SalesProcessor.java` |
| **L17–L18** | JDBC architecture, drivers, URL, DriverManager, Connection, SQL basics | SQLite JDBC connection bootstrap (`org.sqlite.JDBC`), `DriverManager.getConnection()`, foreign key enforcement (`PRAGMA foreign_keys = ON;`), relational schema creation. | `repository/DatabaseManager.java`<br>`repository/SchemaInitializer.java`<br>`sql/schema.sql` |
| **L19** | Statement/PreparedStatement, executeQuery/executeUpdate, parameter binding, secure SQL, resource management | Secure parameter binding via `PreparedStatement` eliminating SQL injection; try-with-resources connection and statement closure. | `repository/JdbcCarRepository.java`<br>`repository/JdbcSaleRepository.java` |
| **L20** | CRUD, ResultSet processing, typed getters, object mapping | Complete relational CRUD operations; `ResultSet` traversal mapping rows to `Car` and `Sale` domain entities using typed getters (`getInt`, `getString`, `getDouble`). | `repository/JdbcCarRepository.java`<br>`repository/JdbcSaleRepository.java` |
| **L21–L22** | Lambdas, functional interfaces, method references | Standard functional interfaces (`Predicate`, `Consumer`, `Function`, `Supplier`); custom `@FunctionalInterface VehicleTransformer`; all 4 method reference kinds (static, bound, unbound, constructor). | `service/LambdaShowcase.java`<br>`service/FilterBuilder.java` |
| **L23** | Stream pipelines, laziness, filter/map/sorted/distinct/limit/skip | 15 analytical queries (R1–R15); intermediate laziness demonstration proving `.peek()` counters are zero without terminal operation and short-circuited with `.limit(n)`; downstream collectors (`groupingBy`, `averagingDouble`, `summingDouble`, `joining`). | `service/ReportService.java`<br>`service/ReportSummaryFormatter.java` |
| **L24** | Generics: classes/methods, type safety, diamond, bounded/wildcard awareness | Generic key-value container `Pair<K, V>`; generic repository interface `Repository<T, ID>`; bounded type method `<T extends Comparable<T>> maxOf`; wildcard traversal `List<? extends Vehicle>`. | `util/Pair.java`<br>`repository/Repository.java`<br>`util/GenericUtils.java` |
| **L25** | Byte and character streams, StreamTokenizer, FileInputStream/FileOutputStream/FileReader/FileWriter | Raw byte-stream database backup/restore using 4KB buffers; byte-by-byte file comparison; batch script DSL parsing via `StreamTokenizer` (`quoteChar`, `wordChars`, `eolIsSignificant`). | `service/DataToolsService.java`<br>`util/FileUtil.java` |
| **L26–L27** | File handling + collections: CSV read/write, validation, import/export, exception-safe resource handling | Flat-file CSV repositories parsing and formatting entities; atomic crash-safe writing via `.tmp` staging, `.bak` backup, and rename; CSV batch import with line-by-line error reports. | `repository/FileCarRepository.java`<br>`repository/FileSaleRepository.java`<br>`util/FileUtil.java#safeWrite`<br>`service/DataToolsService.java#importCsv` |
| **L28–L30** | Unit VI project: planning, prototype, integration, implementation, refinement, and documentation | Layered decoupled architecture (UI -> Service -> Repository); dual-mode runtime switching (CSV flat-file vs SQLite JDBC); zero compiler warnings with Java 17; clean project deliverables. | `app/RevLineApp.java`<br>`docs/` |

---

## Core Concept Distribution Across Layers

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      UI PRESENTATION LAYER                              │
│   MenuController (L4), BuyerMenuHandler (L6), SellerMenuHandler (L4),   │
│   ReportsMenuHandler (L23), DataToolsMenuHandler (L25), ConsoleTable (L2)│
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                       BUSINESS SERVICE LAYER                            │
│   InventoryService (L5, L6-L8)        RecentlyViewed LRU Cache (L6-L7) │
│   SalesService (L13, L14)             UndoManager LIFO Stack (L6-L7)   │
│   SearchService (L6, L8)              WaitlistService FIFO Queue (L6-L7)│
│   ComparisonService (L2 Matrix)       ReportService Streams (L23)      │
│   DataToolsService (L25-L27)          LambdaShowcase (L21-L22)         │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼────────────────────────────────────┐
│                    DATA ACCESS / REPOSITORY LAYER                       │
│   Repository<T, ID> (L24)             DatabaseManager (L17-L18)        │
│   FileCarRepository (L26-L27)         JdbcCarRepository (L19-L20)      │
│   FileSaleRepository (L26-L27)        JdbcSaleRepository (L19-L20)     │
│   FileUtil.safeWrite (L26-L27)        SchemaInitializer (L17, L20)     │
└─────────────────────────────────────────────────────────────────────────┘
```
