# 05. Legacy C to Java Migration Report

This report summarizes the migration of the RevLine showroom data model and workflows from a procedural C prototype to the object-oriented Java 17 application. The final Java repository does not include C source files. The legacy-format sample data remains under `data/legacy/` because the Java application can import that data format.

---

## 1. Function-by-Function Translation Map

| C Prototype Element (`main.c`) | Java Replacement (`RevLine-Java`) | Architectural Improvement |
|---|---|---|
| `typedef struct Car { ... }` | `abstract class Vehicle` + `class Car extends Vehicle` | Encapsulated fields, accessors, invariant validation in constructor/setters, `Comparable<Car>` natural ordering. |
| `char fuelType[20]` | `enum FuelType { PETROL, HYBRID, ELECTRIC }` | Type safety, elimination of invalid fuel strings, constant lookup with `fromString()`. |
| `int isAvailable` (`1` / `0`) | `enum Status { AVAILABLE, RESERVED, SOLD }` | Introduces `RESERVED` state enabling concurrency, hold expiries, and state machine transitions. |
| `Car *showroom` + `MAX_CARS 100` + `malloc` | `ArrayList<Car>` + `HashMap<Integer, Car>` index | Dynamic resizing without memory exhaustion or leaks; O(1) primary key lookups. |
| `initDatabase()` | `StorageBootstrap.init()` | File existence verification, idempotent seed loading, boxed boot banner preserved. |
| `loadCarsFromFile` (`fscanf`) | `FileCarRepository.findAll()` (`BufferedReader`, line validation) | Buffer overflow immunity, tolerance of Windows (`\r\n`) and Unix (`\n`) newlines, malformed row isolation. |
| `saveCarsToFile` (in-place rewrite) | `FileUtil.safeWrite()` (temp file `.tmp` + rename `.bak`) | Crash-safe atomic persistence; reduces the risk of truncating the previous file if a write fails; the exact rename guarantees depend on the operating system and filesystem. |
| `loadDefaultCars` | `SeedData.defaultCars()` + `data/seed/cars.csv` | Single source of truth for initial catalog; eliminates hardcoded in-memory divergence. |
| `displayMenu`, `printTableTop/Row/Bottom` | `ConsoleTable.java` & `MenuPrinter.java` | Identical Unicode box-drawing styling with dynamic column autosizing and UTF-8 console output. |
| `displayAllCars` | `InventoryService.findAll()` + `ConsoleTable.render()` | Decoupled UI rendering from storage; footer totals computed via Java Streams. |
| `addCar` (`scanf`) | `InputReader` + `Validator` + `InventoryService.addCar()` | Strict type validation, auto ID generation (`max + 1`), duplicate make/model prevention. |
| `searchCar` (case-sensitive `strstr`) | `SearchService.search()` (`contains`, `equalsIgnoreCase`) | Case-insensitive multi-token query parsing across brand, model, and fuel type. |
| `filterCars` (available / max price) | `FilterBuilder.java` (composable `Predicate<Car>` chains) | Composable predicate filters (`and()`, `negate()`) supporting budget, speed, horsepower, and status. |
| `sortCars` (bubble sort on master array) | `CarComparators` + `List.sort()` on defensive copy | O(n log n) TimSort on a detached copy; master repository ordering remains unmodified. |
| `buyCar` (no synchronization, receipt only) | `SalesService.sellCar()` (`synchronized`) + `Sale` ledger | Check-then-act race immunity, audit logging, persistent sales ledger, printable receipts. |
| `main` loop + `switch` | `RevLineApp` + `MenuController` | Strict separation of UI presentation from business domain services. |
| `system("chcp 65001")` | `PrintStream(System.out, true, StandardCharsets.UTF_8)` | Platform-independent UTF-8 console encoding without shell process execution. |

---

## 2. Comprehensive Defect Resolution Log

| Defect ID | Defect in C Prototype | Severity | Java Resolution | Verification Mechanism |
|---|---|---|---|---|
| **DEF-01** | `scanf` without numeric validation produces infinite input loops on character input. | High | `InputReader` reads entire line via `Scanner.nextLine()` and parses with typed loops (`Integer.parseInt`, `Double.parseDouble`). | Interactive rejection of malformed numeric tokens |
| **DEF-02** | `scanf(" %[^\n]")` lacks buffer limits, causing stack buffer overflows on long strings. | Critical | `Validator.requireNonBlank` operates on immutable `java.lang.String` with length and delimiter checks. | Boundary enforcement in `Validator.requireNonBlank` |
| **DEF-03** | Brand search uses case-sensitive `strstr`, failing searches like `"ferrari"` or `"porsche"`. | Medium | `SearchService` converts queries and targets to `.toLowerCase().trim()` for case-insensitive matching. | Multi-field search token matching in `SearchService` |
| **DEF-04** | In-place bubble sort permutes master memory and overwrites `cars.txt` in sorted order. | High | `MenuController` sorts a defensive shallow copy (`new ArrayList<>(service.findAll())`), preserving repository ID ordering. | Shallow-copy sorting preservation in `BuyerMenuHandler` |
| **DEF-05** | `buyCar` has no mutual exclusion, causing double-sell race conditions under concurrent access. | Critical | `SalesService.sellCar()` synchronizes on `inventoryService.getLock()`, ensuring atomic check-then-act execution. | Concurrency Lab Demo 2 double-sell showdown verification |
| **DEF-06** | File writes overwrite `cars.txt` directly; an interruption leaves an empty or corrupt file. | High | `FileUtil.safeWrite()` stages output to `.tmp`, renames existing file to `.bak`, and promotes `.tmp` atomically. | Atomic write and backup rotation in `FileUtil.safeWrite` |
| **DEF-07** | Fixed buffer capacity `MAX_CARS 100` crashes or rejects additions beyond 100 cars. | Medium | Dynamic `ArrayList<Car>` collection scales dynamically to memory capacity without hard limits. | Dynamic capacity in `ArrayList` collection |
| **DEF-08** | Inconsistent seed data: `loadDefaultCars` marked all cars AVAILABLE, while `cars.txt` had 3, 4, 18 as SOLD. | Medium | `data/seed/cars.csv` established as single truth; cars 3, 4, 18 marked SOLD with corresponding historical sales rows. | Seed dataset integrity verification across CSV and legacy C |
| **DEF-09** | Showroom footer calculates stock value over ALL cars including SOLD vehicles. | High | `InventoryService.calculateTotalStockValue()` sums price over `AVAILABLE` cars only; `calculateTotalPortfolioValue()` sums entire fleet. | Stream filtering on `Status.AVAILABLE` in `InventorySupport` |
| **DEF-10** | No ability to update or delete inventory vehicles; no duplicate guard. | Medium | `InventoryService.updateCar()` and `deleteCar()` added with O(1) `duplicateGuard` (`HashSet<String>`) on `brand|model`. | Uniqueness checks via `duplicateGuard` in `InventoryService` |
| **DEF-11** | No undo mechanism; accidental deletions or edits were unrecoverable. | Medium | `UndoManager` stack (`ArrayDeque<UndoAction>`) captures pre-mutation snapshots and supports undo for ADD, UPDATE, DELETE. | LIFO undo operation stack in `UndoManager` |
| **DEF-12** | State transitions undefined; sold cars could be sold again or edited. | High | Explicit vehicle state machine enforces `AVAILABLE -> RESERVED -> SOLD`; updates/deletions rejected if not `AVAILABLE`. | State machine validation in `Car.setStatus` and `SalesService` |

---

## 3. Preserved Behavioral and UX Fidelity

RevLine-Java was engineered to preserve 100% of the look, feel, and terminal aesthetics that users appreciated in the C prototype:

1. **Terminal Box-Drawing Aesthetics**: Exact ASCII and Unicode box borders (`╔`, `═`, `╗`, `║`, `╠`, `╣`, `╚`, `╝`) are preserved across all screens.
2. **RevLine Brand Identity**: ASCII art logo and "Drive Passion. Live RevLine." tagline rendered identically upon startup.
3. **Numbered Menu Workflow**: Familiar numeric hotkeys `[1]` through `[8]`, `[S]` to toggle views, and `[0]` to exit.
4. **Official Sales Receipt**: Printable ASCII receipt with "R E V L I N E   M O T O R S", vehicle specifications, buyer/seller metadata, and congratulations banner.
5. **Real-Time Catalog Footers**: Summary line rendering `Total Supercars`, `Available for Purchase`, and `Total Showroom Stock Value`.
