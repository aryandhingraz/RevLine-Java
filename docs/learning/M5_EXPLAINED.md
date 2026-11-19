# Module 5: Streams, Lambdas & Advanced Analytics (L21–L24)

> **Module 5**: Functional Programming, Stream Processing & Generics  
> **Target Milestone**: Phase 5 (`v0.5-analytics`)  
> **Target Syllabus**: 25CSE0202 Programming in Java (Lectures 21, 22, 23, 24)

---

## 1. Architectural Overview

Phase 5 equips RevLine with declarative stream-processing analytics, robust generic utility structures, and functional pipeline architectures. Instead of relying on imperative multi-nested `for` loops and temporary mutable accumulators, showroom analytics are expressed as composable, type-safe functional pipelines.

Key components introduced:
- **`ReportService` (R1–R15)**: 15 distinct analytical stream pipelines generating comprehensive showroom intelligence (stock valuation, performance leaderboards, fuel distributions, sales revenue, and price band matrices).
- **`LambdaShowcase`**: Systematic demonstration of standard Java functional interfaces (`Predicate`, `Consumer`, `Function`, `Supplier`), custom `@FunctionalInterface`, and all four categories of method references.
- **`Pair<K, V>`**: Type-safe generic pair container demonstrating generic classes with type parameters.
- **`GenericUtils`**: Generic utility library featuring bounded type parameters (`<T extends Comparable<T>> maxOf`), read-only upper-bounded wildcards (`List<? extends Vehicle> printAll`), generic pair conversions (`toMap`), and price ladders backed by `TreeMap`.
- **Reports Menu & Text Export**: Interactive console menu supporting on-demand analytics display and atomic text export to `data/exports/report_summary.txt`.

---

## 2. Functional Interfaces & Lambdas (L21)

A **functional interface** is an interface that contains exactly one abstract method (SAM — Single Abstract Method), annotated with `@FunctionalInterface`.

### Standard Interfaces Used in RevLine:

| Interface | Method Signature | Use Case in RevLine | Example |
| :--- | :--- | :--- | :--- |
| `Predicate<T>` | `boolean test(T t)` | Filtering inventory by status, price, speed | `car -> car.getStatus() == Status.AVAILABLE` |
| `Function<T, R>` | `R apply(T t)` | Mapping vehicles to titles or attributes | `Car::getPrice`, `car -> car.getBrand() + " " + car.getModel()` |
| `Consumer<T>` | `void accept(T t)` | Executing side-effects, printing, logging | `car -> logger.log("INSPECT", car.describe())` |
| `Supplier<T>` | `T get()` | Factory instantiation, deferred creation | `ArrayList::new`, `() -> new CarNotFoundException(id)` |

### Custom Functional Interface:
RevLine defines `VehicleTransformer<T, R>` in `LambdaShowcase`:
```java
@FunctionalInterface
public interface VehicleTransformer<T, R> {
    R transform(T input);
}
```
This demonstrates declaring custom SAM interfaces and passing lambda expressions to fulfill them.

---

## 3. Four Categories of Method References (L22)

Method references provide compact, readable syntax for lambdas that simply invoke an existing method. Java 17 supports four distinct kinds:

```
Method Reference Categories:
 1. Reference to a Static Method                              -> ClassName::staticMethodName
 2. Reference to an Instance Method of a Particular Object   -> instanceRef::instanceMethodName (Bound)
 3. Reference to an Instance Method of an Arbitrary Object   -> ClassName::instanceMethodName   (Unbound)
 4. Reference to a Constructor                                -> ClassName::new
```

### Implementations in `LambdaShowcase.java`:

```java
// 1. Static Method Reference:
Comparator<Integer> comparator = Integer::compare;

// 2. Bound Instance Method Reference (bound to a specific PrintStream instance):
Consumer<String> printer = out::print;

// 3. Unbound Instance Method Reference (applied to whichever Car object is passed):
Function<Car, Double> priceExtractor = Car::getPrice;

// 4. Constructor Reference:
Supplier<List<T>> listFactory = ArrayList::new;
```

---

## 4. Stream Pipelines & Intermediate Laziness (L22–L23)

A Java Stream represents a sequence of elements supporting sequential and parallel aggregate operations. Streams do **not** store elements and do **not** modify underlying data structures.

### The Three Pipeline Stages:
1. **Source**: e.g., `inventoryService.findAll().stream()`
2. **Intermediate Operations** (Lazy): `filter`, `map`, `sorted`, `distinct`, `skip`, `limit`, `peek`.
3. **Terminal Operation** (Eager): `collect`, `count`, `sum`, `findFirst`, `forEach`, `reduce`.

### Stream Laziness & Short-Circuit Evaluation (R15):
Intermediate operations are executed **only** when a terminal operation is initiated. If no terminal operation is attached, intermediate operations never execute.

Furthermore, operations like `limit(n)` are **short-circuiting**: they terminate traversal as soon as `n` items have been processed, without scanning the remainder of the dataset:

```java
public Pair<Integer, Integer> demonstrateLaziness(int limit) {
    final int[] unconsumedCounter = new int[1];
    inventoryService.findAll().stream()
            .peek(c -> unconsumedCounter[0]++);
    // Result: unconsumedCounter[0] == 0 (Pipeline was NEVER evaluated!)

    final int[] consumedCounter = new int[1];
    List<Car> sample = inventoryService.findAll().stream()
            .peek(c -> consumedCounter[0]++)
            .limit(limit)
            .collect(Collectors.toList());
    // Result: consumedCounter[0] == limit (Only 3 elements examined out of 18!)

    return Pair.of(unconsumedCounter[0], consumedCounter[0]);
}
```

---

## 5. Advanced Stream Reductions & Collectors (L23)

`ReportService` demonstrates advanced collection and grouping mechanisms:

1. **`groupingBy` with Downstream Collectors**:
   - `counting()`: Grouping vehicle inventory counts per `FuelType` (R4).
   - `averagingDouble()`: Calculating mean showroom vehicle price per manufacturer (R7 / EX-5).
   - `summingDouble()`: Aggregating realized revenue per customer (R11).
   - `minBy()`: Determining quickest 0–100 km/h acceleration per fuel category (R12).
2. **Primitive Stream Specialization**:
   - `mapToDouble(Car::getPrice).sum()`: Avoids boxing/unboxing overhead by operating directly on `DoubleStream` (R2, R3, R10).
3. **Joining Delimited Strings**:
   - `Collectors.joining(", ")`: Concatenates formatted brand/model strings into a single readable display (R14).
4. **2-D Price Band Matrix (`int[3][4]`)**:
   - Maps 3 `FuelType` ordinals (`PETROL`, `HYBRID`, `ELECTRIC`) against 4 price tiers (`< $500k`, `$500k - $1M`, `$1M - $2M`, `>= $2M`), populating a multi-dimensional array directly from stream attributes (R13).

---

## 6. Generics in Depth (L24)

Generics enforce compile-time type safety and eliminate runtime `ClassCastException` hazards.

### A. Generic Classes (`Pair<K, V>`):
Parameterized container storing key-value pairs without loss of type information:
```java
public class Pair<K, V> {
    private final K first;
    private final V second;
    // ...
}
```

### B. Bounded Type Parameters (`<T extends Comparable<T>>`):
`GenericUtils.maxOf` requires that type `T` implement `Comparable<T>`, guaranteeing that `candidate.compareTo(max)` is valid at compile-time:
```java
public static <T extends Comparable<T>> T maxOf(List<T> list) {
    if (list == null || list.isEmpty()) return null;
    T max = list.get(0);
    for (int i = 1; i < list.size(); i++) {
        T candidate = list.get(i);
        if (candidate != null && (max == null || candidate.compareTo(max) > 0)) {
            max = candidate;
        }
    }
    return max;
}
```

### C. Wildcards & The PECS Principle (`? extends Vehicle`):
`GenericUtils.printAll` uses an upper-bounded wildcard:
```java
public static void printAll(List<? extends Vehicle> vehicles) {
    for (Vehicle v : vehicles) {
        System.out.println(v.describe());
    }
}
```
- **Upper Bound (`? extends Vehicle`)**: Read-only access ("Producer Extends"). Accepts `List<Car>`, `List<Vehicle>`, or any future subtype of `Vehicle`. You can safely read `Vehicle` from the list, but you cannot insert into it (except `null`).
- Prevents compile-time covariance errors while allowing polymorphism over collections.

### D. Price Ladder with `TreeMap`:
`GenericUtils.buildPriceLadder` groups cars by price into a sorted `TreeMap<Double, List<Car>>`:
- Keys are sorted ascending by price.
- `firstKey()` gives the entry-level price; `lastKey()` gives the flagship supercar tier.

---

## 7. Showroom Reports Catalog (R1–R15)

| # | Report Name | Java 17 Stream Pipeline | Seeded Expected Output |
| :--- | :--- | :--- | :--- |
| **R1** | Available Vehicles | `filter(status == AVAILABLE).toList()` | 15 vehicles |
| **R2** | Available Stock Valuation | `filter(AVAILABLE).mapToDouble(getPrice).sum()` | **$20,633,598.00** |
| **R3** | Total Portfolio Valuation | `mapToDouble(getPrice).sum()` | **$22,737,598.00** |
| **R4** | Vehicles Per Fuel Type | `groupingBy(getFuelType, counting())` | PETROL: 13, HYBRID: 4, ELECTRIC: 1 |
| **R5** | Top-3 Fastest Supercars | `sorted(topSpeed.reversed()).limit(3)` | 1. Jesko (480), 2. Agera RS (447), 3. Veyron SS (431) |
| **R6** | Top-N by Horsepower (EX-4) | `filter(!SOLD).sorted(hp.reversed()).limit(n)` | 1. Jesko (1600), 2. Chiron (1500), 3. Agera RS (1341) |
| **R7** | Average Price by Brand (EX-5) | `groupingBy(getBrand, averagingDouble(getPrice))` | Bugatti: $2.75M, Koenigsegg: $2.75M, Pagani: $1.7M, etc. |
| **R8** | Distinct Brands Sorted | `map(getBrand).distinct().sorted()` | 11 brands (Aston Martin ... Tesla) |
| **R9** | Paged Inventory View | `sorted(byId).skip((p-1)*size).limit(size)` | Page 1 (IDs 1–5), Page 4 (IDs 16–18) |
| **R10** | Total Realized Sales Revenue | `sales.stream().mapToDouble(getPrice).sum()` | **$2,104,000.00** (from 3 seeded sales) |
| **R11** | Revenue by Buyer | `sales.stream().groupingBy(buyer, summingDouble)` | Legacy C Import: $2,104,000.00 |
| **R12** | Quickest 0–100 per Fuel Type | `groupingBy(fuel, minBy(comparingDouble(accel)))` | Petrol: Chiron (2.4s), Hybrid: LaFerrari (2.4s), EV: Model S (1.99s) |
| **R13** | Price Band Matrix `int[3][4]` | Multi-dimensional array mapping fuel vs price | Petrol: [6, 1, 1, 5], Hybrid: [0, 1, 2, 1], EV: [1, 0, 0, 0] |
| **R14** | Showroom Lineup Names | `map(brand+" "+model).collect(joining(", "))` | Single comma-delimited string of 18 vehicle titles |
| **R15** | Stream Laziness Demo | `peek` with counter + `limit(n)` short-circuit | Unconsumed count: 0, Short-circuit count (limit 3): 3 |

---

## 8. Verification & Gate 5 Protocol

All 15 reports and generic algorithms are validated via `ReportServiceAllReportsTest` and `GenericsAndLambdaTest` in the automated test runner:
- Total core checks passing: **105 / 105**
- Assigned exercises complete: **8 / 9**
- Compilation warnings: **0** under `-Xlint:all`
- Export file verified: `data/exports/report_summary.txt`
