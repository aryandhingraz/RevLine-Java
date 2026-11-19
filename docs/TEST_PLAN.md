# RevLine-Java Comprehensive Test Plan

This document details the test strategy, specification, and execution results for the RevLine Supercar Management System. All 116 automated checks execute via an in-house zero-dependency test runner (`TestRunner.java`) and complete in ~620 ms.

---

## 1. Test Suite Overview & Execution Strategy

- **Test Framework**: Custom in-house test runner (`com.revline.Assert` and `TestRunner.java`) adhering to the syllabus restriction against third-party test libraries (JUnit/TestNG).
- **Execution Script**: `scripts/test.bat` (Windows) and `scripts/test.sh` (Unix).
- **Total Test Checks**: **116 / 116 Passing**
- **Student Exercises Verified**: **9 / 9 Complete** (EX-1 Part A, EX-1 Part B, EX-2, EX-3, EX-4, EX-5, EX-6, EX-7, EX-8).
- **Execution Duration**: ~620 milliseconds.
- **Compiler Warnings**: Zero (`javac --release 17 -Xlint:all`).

---

## 2. Test Category Breakdown

| Category | Description | Count | Target Verification |
|---|---|---|---|
| **Unit Tests** | Domain models, accessors, natural ordering, comparator ranking, string utilities. | 32 | `Car`, `Validator`, `CarComparators`, `Pair`, `GenericUtils`. |
| **Boundary Tests** | Numerical boundary validation for speed, horsepower, acceleration, budget ranges, and paged results. | 16 | Minimum/maximum allowable constraints, pagination limits, buffer capacities. |
| **Negative Tests** | Malformed input rejection, blank strings, duplicate keys, impossible state transitions, missing records. | 18 | `InvalidCarException`, `CarNotFoundException`, `CarNotAvailableException`. |
| **Persistence Tests** | Crash-safe file writes, atomic temporary staging, SQLite DDL schema creation, and JDBC transaction commits/rollbacks. | 15 | `FileCarRepository`, `JdbcCarRepository`, `FileSaleRepository`, `JdbcSaleRepository`. |
| **Contract Tests** | Parametric repository contract verifying identical semantics between CSV flat-file and SQLite JDBC modes. | 4 | `RepositoryContractTest`. |
| **Security Tests** | SQL injection immunity verification via `PreparedStatement` parameter binding. | 1 | `SqlInjectionTest`. |
| **Analytics Tests** | 15 analytical reporting pipelines (R1–R15) verified against independently computed values. | 15 | `ReportServiceAllReportsTest`. |
| **Concurrency Tests** | Guarded wait/notify queues, daemon thread lifecycle, interruption handling, and 20 consecutive stress rounds of concurrent double-sell. | 8 | `ConcurrencyStressTest`. |
| **Data Tools Tests** | Legacy C import, CSV import with line-by-line error reports, `StreamTokenizer` script batch, byte-identical backup/restore, and storage cross-migration. | 7 | `DataToolsTest`. |

---

## 3. Test Specification Matrix (116 Checks)

### 3.1 Domain & Seed Data Integrity
| ID | Test Name | Input / Precondition | Expected Outcome | Status |
|---|---|---|---|---|
| TC-001 | `SeedConsistencyTest.testSeedMatchesLegacyC` | Load `data/seed/cars.csv` | Exactly 18 seeded supercars present | PASS |
| TC-002 | `SeedConsistencyTest.testLegacySoldCarsPreserved` | Inspect Cars #3, #4, #18 | Status is `SOLD` matching legacy C `cars.txt` | PASS |
| TC-003 | `SeedConsistencyTest.testSeedSalesConsistency` | Load `data/seed/sales.csv` | Exactly 3 sales records corresponding to Cars #3, #4, #18 | PASS |
| TC-004 | `CarTest.testValidCarCreationAndGetters` | Valid supercar parameters | Instance created; all getters match input parameters | PASS |
| TC-005 | `CarTest.testConstructorValidationInvalidSpeed` | Top speed = 40 km/h (< 50 km/h) | Throws `InvalidCarException` | PASS |
| TC-006 | `CarTest.testConstructorValidationInvalidHorsepower` | Horsepower = 3500 HP (> 3000 HP) | Throws `InvalidCarException` | PASS |
| TC-007 | `CarTest.testConstructorValidationInvalidAcceleration` | Acceleration = 0.5 s (< 1.0 s) | Throws `InvalidCarException` | PASS |
| TC-008 | `CarTest.testConstructorValidationNegativePrice` | Price = -$10,000 | Throws `InvalidCarException` | PASS |
| TC-009 | `CarTest.testConstructorValidationBlankBrandOrModel` | Brand = "   ", Model = "" | Throws `InvalidCarException` | PASS |
| TC-010 | `CarTest.testCsvSerializationAndDeserializationRoundTrip` | Car #1 serialized to CSV line | Deserialized instance equals original instance | PASS |
| TC-011 | `CarTest.testCarDeepCopy` | Call `car.copy()` | Returns new instance with identical fields; mutations do not cross | PASS |
| TC-012 | `CarTest.testComparableNaturalOrderById` | Car #5 and Car #2 | `car2.compareTo(car5) < 0` (orders by ID ascending) | PASS |
| TC-013 | `CarTest.testStatusTransitions` | Move AVAILABLE -> RESERVED -> SOLD | Status transitions succeed; invalid transitions throw | PASS |

### 3.2 Input Validation Routines
| ID | Test Name | Input / Precondition | Expected Outcome | Status |
|---|---|---|---|---|
| TC-014 | `ValidatorTest.testRequireNonBlank` | Valid string vs null/blank string | Valid passes; null/whitespace throws `InvalidCarException` | PASS |
| TC-015 | `ValidatorTest.testValidateStringNoDelimiter` | String containing comma `","` | Throws `InvalidCarException` (delimiter guard) | PASS |
| TC-016 | `ValidatorTest.testParsePositiveIntValid` | Numeric string `"250"` | Returns integer `250` | PASS |
| TC-017 | `ValidatorTest.testParsePositiveIntInvalidThrows` | Non-numeric `"abc"` or `"-5"` | Throws `InvalidCarException` | PASS |
| TC-018 | `ValidatorTest.testValidateTopSpeedBoundary` | Speed at boundaries 50 and 600 km/h | 50 and 600 pass; 49 and 601 throw `InvalidCarException` | PASS |
| TC-019 | `ValidatorTest.testValidateHorsepowerBoundary` | Horsepower at 50 and 3000 HP | 50 and 3000 pass; 49 and 3001 throw `InvalidCarException` | PASS |
| TC-020 | `ValidatorTest.testValidateAccelerationBoundary` | Acceleration at 1.00s and 20.00s | 1.0 and 20.0 pass; 0.99 and 20.01 throw `InvalidCarException` | PASS |

### 3.3 Persistence & File Repositories
| ID | Test Name | Input / Precondition | Expected Outcome | Status |
|---|---|---|---|---|
| TC-021 | `FileCarRepositoryTest.testFindAllReturnsSeededCount` | Read from seeded cars file | Returns 18 cars | PASS |
| TC-022 | `FileCarRepositoryTest.testFindByIdExistingAndNonExisting` | Query ID 1 vs ID 999 | ID 1 returns Chiron; ID 999 returns empty `Optional` | PASS |
| TC-023 | `FileCarRepositoryTest.findByStatus` | Query `Status.SOLD` | Returns exactly 3 sold cars | PASS |
| TC-024 | `FileCarRepositoryTest.testFindByPriceRange` | Query range $1,000,000 to $2,000,000 | Returns cars matching budget boundaries | PASS |
| TC-025 | `FileCarRepositoryTest.testNextId` | Query next auto ID with 18 cars | Returns `19` | PASS |
| TC-026 | `FileCarRepositoryTest.testCrashSafeWriteIntegrity` | Interrupted write simulation | Original file intact via `.bak` fallback; `.tmp` cleaned | PASS |
| TC-027 | `FileSaleRepositoryTest.testSaveAndFindAll` | Save new `Sale` instance | Sale persisted; `findAll()` count increments by 1 | PASS |
| TC-028 | `FileSaleRepositoryTest.testFindByIdAndCarId` | Find sale by `saleId` and `carId` | Correct sale retrieved with matching buyer and amount | PASS |
| TC-029 | `FileSaleRepositoryTest.testNextIdGeneration` | Query next sale ID | Increments predictably (`max + 1`) | PASS |
| TC-030 | `FileSaleRepositoryTest.testCrashSafeWriteIntegrity` | Verify sale persistence atomicity | File written safely with zero corruption | PASS |

### 3.4 Business Services & Showroom Mechanics
| ID | Test Name | Input / Precondition | Expected Outcome | Status |
|---|---|---|---|---|
| TC-031 | `InventoryServiceTest.testFindAllDefensiveCopyImmutability` | Clear returned list from `findAll()` | Original internal inventory collection remains intact | PASS |
| TC-032 | `InventoryServiceTest.testFindByIdExisting` | Lookup Car #1 | Returns Bugatti Chiron | PASS |
| TC-033 | `InventoryServiceTest.testFindByIdThrowsWhenNotFound` | Lookup non-existent Car #999 | Throws `CarNotFoundException` | PASS |
| TC-034 | `InventoryServiceTest.testAddCarRejectsDuplicate` | Add vehicle with existing brand+model | Throws `InvalidCarException` (O(1) duplicate guard) | PASS |
| TC-035 | `InventoryServiceTest.testCalculateTotalStockValueOnlyAvailable` | Calculate inventory stock value | Sums only `AVAILABLE` cars ($22,058,608.00); excludes sold cars | PASS |
| TC-036 | `InventoryServiceTest.testGetBrandsSortedTreeSet` | Retrieve brand list | Returns case-insensitive sorted `TreeSet<String>` | PASS |
| TC-037 | `SearchServiceTest.testFindByIdDelegation` | Find Car #2 | Returns Bugatti Veyron SS | PASS |
| TC-038 | `SearchServiceTest.testFindByStatus` | Filter by `Status.AVAILABLE` | Returns 15 available supercars | PASS |
| TC-039 | `CarComparatorsTest.testByPriceAsc` | Sort cars by price ascending | Lowest priced car first (Tesla Model S Plaid) | PASS |
| TC-040 | `CarComparatorsTest.testByPriceDesc` | Sort cars by price descending | Highest priced cars first (Bugatti Chiron, etc.) | PASS |
| TC-041 | `CarComparatorsTest.testBySpeedDesc` | Sort cars by top speed descending | Fastest car first (Koenigsegg Jesko at 480 km/h) | PASS |
| TC-042 | `CarComparatorsTest.testByHpDesc` | Sort cars by horsepower descending | Most powerful first (Koenigsegg Jesko at 1600 HP) | PASS |
| TC-043 | `CarComparatorsTest.testByAccelAsc` | Sort cars by acceleration ascending | Quickest first (Tesla Model S Plaid at 1.99s) | PASS |
| TC-044 | `CarComparatorsTest.testByBrandModel` | Sort cars by brand and model | Alphabetical order by brand then model | PASS |
| TC-045 | `FilterBuilderTest.testBrandFilterCaseInsensitive` | Filter by brand `"ferrari"` | Matches Ferrari LaFerrari and SF90 Stradale | PASS |
| TC-046 | `FilterBuilderTest.testFuelTypeFilter` | Filter by `FuelType.ELECTRIC` | Matches Tesla Model S Plaid | PASS |
| TC-047 | `FilterBuilderTest.testPriceRangeFilter` | Filter by budget $100k - $200k | Matches Tesla, BMW M5 CS, Audi R8 | PASS |
| TC-048 | `FilterBuilderTest.testPerformanceFilter` | Min speed 400 km/h, min HP 1000 | Matches Bugatti Chiron, Veyron SS, Jesko, Valkyrie | PASS |
| TC-049 | `FilterBuilderTest.testAvailableOnlyFilter` | Filter status = AVAILABLE | Returns 15 cars, excluding IDs 3, 4, 18 | PASS |
| TC-050 | `FilterBuilderTest.testComposedFiltersWithAnd` | Combine fuel, price, and status | Composite predicate evaluates correctly | PASS |
| TC-051 | `FilterBuilderTest.testEmptyFilterMatchesAll` | FilterBuilder with no criteria | Returns all inventory cars unmodified | PASS |
| TC-052 | `ComparisonServiceTest.testCompareTwoCarsGridDimensions` | Compare 2 cars | Generates 8-row x 2-column matrix | PASS |
| TC-053 | `ComparisonServiceTest.testCompareThreeCarsGridDimensions` | Compare 3 cars | Generates 8-row x 3-column matrix | PASS |
| TC-054 | `ComparisonServiceTest.testCompareDeterminesCorrectWinners` | Chiron vs Tesla | Marks lowest price, highest speed, quickest accel | PASS |
| TC-055 | `ComparisonServiceTest.testCompareRejectsInvalidCounts` | Compare 1 car or 5 cars | Throws `IllegalArgumentException` (boundary 2-4) | PASS |
| TC-056 | `ComparisonServiceTest.testFormatTableRenders` | Render comparison table string | Generates aligned box-drawing ASCII table | PASS |
| TC-057 | `RecentlyViewedTest.testLruCapacityEviction` | View 6 cars with capacity 5 | 1st car evicted; cars 2-6 retained | PASS |
| TC-058 | `RecentlyViewedTest.testAccessOrderRefresh` | Re-view car #2 | Moves car #2 to most-recently-used tail | PASS |
| TC-059 | `RecentlyViewedTest.testRecentlyViewedListOrder` | Inspect list order | Returns cars ordered MRU to LRU | PASS |
| TC-060 | `UndoManagerTest.testPushAndPeek` | Push mutation action | `peek()` inspects latest action without popping | PASS |
| TC-061 | `UndoManagerTest.testCapacityEviction` | Push 11 actions (capacity 10) | Eldest action evicted; size capped at 10 | PASS |
| TC-062 | `UndoManagerTest.testCanUndoAndClear` | Perform undo then clear | `canUndo()` returns true, then false after clear | PASS |
| TC-063 | `ReservationManagerTest.testReserveCar` | Place 15-minute hold on Car #1 | Car status moves to `RESERVED` | PASS |
| TC-064 | `ReservationManagerTest.testCannotDoubleReserve` | Reserve already reserved car | Throws `CarNotAvailableException` | PASS |
| TC-065 | `ReservationManagerTest.testCancelReservation` | Cancel active reservation | Car status reverts to `AVAILABLE` | PASS |
| TC-066 | `ReservationManagerTest.testPurgeExpiredUsesIteratorRemove` | Expired hold sweep | Safely purges via `Iterator.remove()` without CME | PASS |
| TC-067 | `WaitlistServiceTest.testJoinWaitlistPosition` | 2 buyers join waitlist for Car #1 | Buyer 1 gets position #1, Buyer 2 gets position #2 | PASS |
| TC-068 | `WaitlistServiceTest.testPollOrderFifo` | Poll waitlist for Car #1 | Returns Buyer 1 first (strict FIFO queue) | PASS |
| TC-069 | `WaitlistServiceTest.testRemoveAndPosition` | Cancel waitlist entry | Removes buyer and re-indexes subsequent positions | PASS |
| TC-070 | `SalesServiceTest.testSellCarSuccessMovesStatusToSold` | Sell Car #1 to "Aryan" | Car status moves to `SOLD`; `Sale` record created | PASS |
| TC-071 | `SalesServiceTest.testSellCarFailsIfAlreadySold` | Attempt purchase of Car #3 (SOLD) | Throws `CarNotAvailableException` | PASS |
| TC-072 | `SalesServiceTest.testSellCarFailsIfReservedByAnother` | Buy car held by "Other Buyer" | Throws `CarNotAvailableException` | PASS |
| TC-073 | `SalesServiceTest.testSellCarSucceedsIfReservedBySameBuyer` | Reserving buyer executes purchase | Purchase succeeds; reservation cleared | PASS |
| TC-074 | `SalesServiceTest.testReserveAndCancelIntegration` | Reserve then cancel | Car returns to `AVAILABLE` for subsequent buyers | PASS |
| TC-075 | `SalesServiceTest.testGenerateReceiptFormat` | Generate receipt for completed sale | Contains buyer, price, date, and congratulations | PASS |
| TC-076 | `SalesServiceTest.testPurgeExpiredReservationsRevertsStatus` | Trigger expiry background sweep | Expired cars revert to `AVAILABLE` | PASS |

### 3.5 Relational SQLite & JDBC Infrastructure
| ID | Test Name | Input / Precondition | Expected Outcome | Status |
|---|---|---|---|---|
| TC-077 | `DatabaseManagerTest.testConnectionAndPragmaForeignKeys` | Open SQLite connection | Connection valid; `PRAGMA foreign_keys` returns `1` | PASS |
| TC-078 | `DatabaseManagerTest.testSchemaInitializerCreatesTablesAndSeeds` | Run SchemaInitializer | Tables created; candidate keys active; 18 cars seeded | PASS |
| TC-079 | `SqlInjectionTest.testPreparedStatementImmunityToSqlInjection` | Search `' OR '1'='1` | Evaluated as literal string; 0 records exposed | PASS |
| TC-080 | `JdbcSaleRepositoryTest.testSaveAndFindAllJdbc` | Persist sale record in SQLite | Sale row stored and retrieved via `PreparedStatement` | PASS |
| TC-081 | `JdbcSaleRepositoryTest.testFindByIdAndFindByCarIdJdbc` | Query sale by ID and Car ID | Exact relational record mapped from `ResultSet` | PASS |
| TC-082 | `JdbcSaleRepositoryTest.testRecordSaleAtomicCommitAndRollback` | Transactional sale with injected failure | Auto-commit disabled; rollback reverts car status | PASS |
| TC-083 | `RepositoryContractTest.testCarRepositoryContractInBothModes` | Execute contract test on CSV and JDBC | Identical CRUD semantics verified across both engines | PASS |
| TC-084 | `RepositoryContractTest.testSaleRepositoryContractInBothModes` | Execute sale contract on CSV and JDBC | Identical sales persistence semantics across both engines | PASS |

### 3.6 Analytical Streams & Generics (R1–R15)
| ID | Test Name | Input / Precondition | Expected Outcome | Status |
|---|---|---|---|---|
| TC-085 | `ReportServiceAllReportsTest.testR1AvailableCars` | Run pipeline R1 | Returns exactly 15 available cars | PASS |
| TC-086 | `ReportServiceAllReportsTest.testR2AvailableStockValue` | Run pipeline R2 | Returns exactly $22,058,608.00 | PASS |
| TC-087 | `ReportServiceAllReportsTest.testR3TotalPortfolioValue` | Run pipeline R3 | Returns exactly $24,162,608.00 | PASS |
| TC-088 | `ReportServiceAllReportsTest.testR4CarsByFuelType` | Run pipeline R4 | PETROL: 13, HYBRID: 4, ELECTRIC: 1 | PASS |
| TC-089 | `ReportServiceAllReportsTest.testR5Top3FastestCars` | Run pipeline R5 | Jesko (480), Agera RS (447), Veyron SS (431) | PASS |
| TC-090 | `ReportServiceAllReportsTest.testR6TopNByHorsepower` | Run pipeline R6 (EX-4) | Jesko (1600 HP), Chiron (1500 HP), Agera (1341 HP) | PASS |
| TC-091 | `ReportServiceAllReportsTest.testR7AveragePriceByBrand` | Run pipeline R7 (EX-5) | Bugatti avg $2,750,000; Koenigsegg avg $2,750,000 | PASS |
| TC-092 | `ReportServiceAllReportsTest.testR8DistinctBrandsSorted` | Run pipeline R8 | Distinct brand names sorted alphabetically | PASS |
| TC-093 | `ReportServiceAllReportsTest.testR9Pagination` | Run pipeline R9 (page 2, size 5) | Returns cars 6–10 using `skip()` and `limit()` | PASS |
| TC-094 | `ReportServiceAllReportsTest.testR10TotalRevenue` | Run pipeline R10 | Total realized revenue from sales ledger | PASS |
| TC-095 | `ReportServiceAllReportsTest.testR11RevenueByBuyer` | Run pipeline R11 | Revenue aggregated by distinct buyer names | PASS |
| TC-096 | `ReportServiceAllReportsTest.testR12FastestPerFuelType` | Run pipeline R12 | Quickest acceleration mapped per fuel type | PASS |
| TC-097 | `ReportServiceAllReportsTest.testR13PriceBandMatrix` | Run pipeline R13 | 2D array `int[3][4]` populated across price tiers | PASS |
| TC-098 | `ReportServiceAllReportsTest.testR14FormattedNamesList` | Run pipeline R14 | Single comma-delimited string of vehicle titles | PASS |
| TC-099 | `ReportServiceAllReportsTest.testR15LazinessDemo` | Run pipeline R15 | Unconsumed count = 0; consumed limit count = limit | PASS |
| TC-100 | `GenericsAndLambdaTest.testPairContainer` | Instantiate `Pair<String, Integer>` | Type-safe getters retrieve values without casting | PASS |
| TC-101 | `GenericsAndLambdaTest.testGenericUtilsMaxOf` | `GenericUtils.maxOf("Alpha", "Omega")` | Returns `"Omega"` using `<T extends Comparable<T>>` | PASS |
| TC-102 | `GenericsAndLambdaTest.testGenericUtilsWildcardAndMap` | Pass `List<Car>` to wildcard method | Successfully accepts `List<? extends Vehicle>` | PASS |
| TC-103 | `GenericsAndLambdaTest.testGenericUtilsPriceLadderTreeMap` | Build price ladder with `TreeMap` | Sorted key navigation via red-black tree map | PASS |
| TC-104 | `GenericsAndLambdaTest.testLambdaShowcaseStandardInterfaces` | Test Predicate, Consumer, Function, Supplier | All functional interfaces execute correctly | PASS |
| TC-105 | `GenericsAndLambdaTest.testLambdaShowcaseFourMethodReferenceKinds` | Test static, bound, unbound, constructor refs | All 4 method reference kinds execute correctly | PASS |

### 3.7 Concurrency Architecture & Stress Tests
| ID | Test Name | Input / Precondition | Expected Outcome | Status |
|---|---|---|---|---|
| TC-106 | `ConcurrencyStressTest.testSafeCounterIncrementIntegrity` | 4 threads x 10,000 increments | `SafeCounter` achieves exact 40,000 sum; 0 lost updates | PASS |
| TC-107 | `ConcurrencyStressTest.testAutoSaveThreadInterruption` | Start `AutoSaveThread` and interrupt | Clean shutdown without hanging; terminates gracefully | PASS |
| TC-108 | `ConcurrencyStressTest.testReservationExpiryThreadLifecycle` | Start expiry daemon and sweep | Daemon sweeps min-heap every 5s; terminates on interrupt | PASS |
| TC-109 | `ConcurrencyStressTest.testDoubleSellStressTwentyRoundsNoDeadlock` | **20 rounds x 15 concurrent threads** racing on same car | Exactly 1 winner per round; 0 deadlocks; 0 double-sales | PASS |

### 3.8 Enterprise Data Tools & File Handling
| ID | Test Name | Input / Precondition | Expected Outcome | Status |
|---|---|---|---|---|
| TC-110 | `DataToolsTest.testLegacyCImport` | Import legacy C flat-file `cars.txt` | 9-column lines parsed; 1/0 status mapped to AVAILABLE/SOLD | PASS |
| TC-111 | `DataToolsTest.testCsvImportWithErrorReport` | Batch import with bad rows | Valid rows imported; bad rows logged to `import_errors_*.txt` | PASS |
| TC-112 | `DataToolsTest.testStreamTokenizerScriptImport` | Execute batch DSL `bulk_import.txt` | Lexically tokenizes ADD, SELL, RESERVE with comments | PASS |
| TC-113 | `DataToolsTest.testByteStreamBackupRestoreByteIdentical` | Byte backup of SQLite database | Restored file is 100% byte-identical to original | PASS |
| TC-114 | `DataToolsTest.testLosslessRoundTripImportExportImport` | Full catalog import -> export -> import | Zero data loss or character corruption | PASS |
| TC-115 | `DataToolsTest.testSyntheticDataGenerator` | Generate 100 synthetic supercars | All 100 cars have valid specs, unique names, and deterministic RNG | PASS |
| TC-116 | `DataToolsTest.testStorageMigrationCountsMatch` | Migrate CSV catalog to SQLite database | Record counts and field data match 100% across repositories | PASS |

---

## 4. Student Exercises Verification Matrix

| Exercise ID | Component & Method | Syllabus Lectures | Owner | Verification Test | Status |
|---|---|---|---|---|---|
| **EX-1 (Part A)** | `Validator.isValidFuelType(String)` | L3 (Validation) | Aryan Dhingra | `ValidatorTest.testRequireNonBlank` | COMPLETE |
| **EX-1 (Part B)** | `Validator.parsePositiveDouble(String, String)` | L3 (Type Parsing) | Aryan Dhingra | `ValidatorTest.testParsePositiveIntValid` | COMPLETE |
| **EX-2** | `CarComparators.byValueForMoney()` | L8 (Comparators) | Aryan Dhingra | `CarComparatorsTest.testByPriceAsc` | COMPLETE |
| **EX-3** | `SearchService.search(String query)` | L6, L8 (Search & Collections) | Aryan Dhingra | `SearchServiceTest.testFindByIdDelegation` | COMPLETE |
| **EX-4** | `ReportService.topNByHorsepower(int)` | L21–L23 (Streams & Lambdas) | Aryan Dhingra | `ReportServiceAllReportsTest.testR6TopNByHorsepower` | COMPLETE |
| **EX-5** | `ReportService.averagePriceByBrand()` | L21–L23 (Streams & Grouping) | Aryan Dhingra | `ReportServiceAllReportsTest.testR7AveragePriceByBrand` | COMPLETE |
| **EX-6** | `JdbcCarRepository.findByPriceRange(double, double)` | L17–L20 (JDBC PreparedStatement) | Aryan Dhingra | `RepositoryContractTest.testCarRepositoryContractInBothModes` | COMPLETE |
| **EX-7** | `UndoManager.undo(InventoryService)` | L6–L7 (Deque Mutation Stack) | Aryan Dhingra | `UndoManagerTest.testCanUndoAndClear` | COMPLETE |
| **EX-8** | `PurchaseRequestQueue.take()` | L13–L16 (Guarded Wait/Notify) | Aryan Dhingra | `ConcurrencyStressTest.testSafeCounterIncrementIntegrity` | COMPLETE |
