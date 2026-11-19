# Modules 2 & 3 Technical Architecture & Viva Guide

> **Module 2**: Search, Filter, Sort & Compare  
> **Module 3**: Sales & Reservation Management  
> **Target Syllabus**: 25CSE0202 Programming in Java (L2, L5, L6–7, L8, L13–14, L21, L26–27)

---

## 1. Module 2: Search, Filter, Sort & Compare

### 1.1 Custom Sorting with `Comparator<Car>` (L8)
- **Natural Ordering (`Comparable<Car>`)**: `Car` implements `Comparable<Car>` based on `id` ascending (`Integer.compare(this.id, other.id)`). This defines default repository and database ordering.
- **Custom Ordering (`CarComparators`)**: Showroom buyers have different criteria (budget, performance, acceleration). `CarComparators` provides static, reusable `Comparator<Car>` instances:
  - `BY_PRICE_ASC`: `Comparator.comparingDouble(Car::getPrice)`
  - `BY_PRICE_DESC`: `BY_PRICE_ASC.reversed()`
  - `BY_SPEED_DESC`: `(c1, c2) -> Integer.compare(c2.getTopSpeed(), c1.getTopSpeed())`
  - `BY_HP_DESC`: `(c1, c2) -> Integer.compare(c2.getHorsepower(), c1.getHorsepower())`
  - `BY_ACCEL_ASC`: `Comparator.comparingDouble(Car::getAcceleration)`
  - `BY_BRAND_MODEL`: `Comparator.comparing(Car::getBrand, String.CASE_INSENSITIVE_ORDER).thenComparing(Car::getModel, String.CASE_INSENSITIVE_ORDER)`
  - **EX-2 (`byValueForMoney()`)**: Computes horsepower per \$100,000 ratio and tie-breaks with acceleration ascending.

### 1.2 Fluent Predicate Chaining (`FilterBuilder`) (L21)
- Demonstrates `java.util.function.Predicate<T>` and higher-order functional composition:
  - Each builder method (`brand()`, `fuelType()`, `minPrice()`, `maxPrice()`, `availableOnly()`) adds a `Predicate<Car>` to an internal list.
  - `build()` chains predicates using `Predicate.and()`:
    ```java
    Predicate<Car> combined = predicates.get(0);
    for (int i = 1; i < predicates.size(); i++) {
        combined = combined.and(predicates.get(i));
    }
    ```
  - This allows buyers to dynamically compose multi-dimensional searches (e.g. "Ferrari AND Hybrid AND under \$700,000 AND Available") without writing combinatorial if-else logic.

### 1.3 Multi-Dimensional Matrix Comparison (`ComparisonService`) (L2)
- Takes 2 to 4 cars and constructs a 2-D array `String[8][N]` (rows = metrics, columns = vehicles).
- Analyzes each quantitative row to determine the best specification:
  - Lowest price
  - Highest top speed
  - Highest horsepower
  - Quickest 0-100 km/h acceleration
- Renders an ASCII comparison grid highlighting winners with `*BEST`.

---

## 2. Module 3: Sales, Reservation & History

### 2.1 Atomic State Machine & Thread Synchronization (L13, L14)
- **Vehicle Lifecycle**:
  ```
  [AVAILABLE]  ──(reserveCar)──►  [RESERVED]
       │                             │
   (sellCar)                     (sellCar by reserver)
       │                             │
       ▼                             ▼
    [SOLD]  ◄────────────────────────┘
  ```
- **Terminal State**: `SOLD` is irreversible during normal operation; sold cars cannot be re-reserved or deleted.
- **Shared Intrinsic Monitor**: `SalesService` synchronizes critical sections (`sellCar()`, `reserveCar()`, `cancelReservation()`) using `inventoryService.getLock()`. This guarantees that inventory mutations and sales transactions share the identical lock, preventing race conditions (such as two buyers concurrently buying the same supercar) and eliminating deadlock risks from multi-lock acquisition.
- **Atomic Rollback Guarantee**: If writing the sale record to `FileSaleRepository` fails, `sellCar()` automatically rolls back the vehicle's status to its prior state in memory before propagating the error.

### 2.2 Priority Expiration with Min-Heap & Iterator (L6–7)
- **Min-Heap Ordering**: `ReservationManager` stores active holds in a `PriorityQueue<Reservation>`. `Reservation` implements `Comparable<Reservation>` based on `expiresAt` ascending (`this.expiresAt.compareTo(other.expiresAt)`). The reservation expiring earliest is always at the head of the heap.
- **Safe Expiration Purge**: `purgeExpired()` iterates over the queue and uses `Iterator.remove()` (explicit syllabus requirement) to safely remove expired reservations while avoiding `ConcurrentModificationException`.

### 2.3 FIFO Customer Waitlist (`WaitlistService`) (L6–7)
- Demonstrates `java.util.Queue` implemented with `java.util.LinkedList`.
- When a car is `RESERVED`, subsequent interested buyers join a FIFO waitlist (`queue.offer(buyer)`).
- If the active reservation expires or is cancelled, `waitlistService.poll(carId)` retrieves the next customer in line.

### 2.4 Stack-Based Undo History (`UndoManager`) (L6–7)
- Demonstrates `java.util.Deque` implemented with `java.util.ArrayDeque` as a LIFO stack.
- Whenever an inventory mutation occurs (`ADD`, `DELETE`, `UPDATE`), an immutable snapshot `UndoAction` is pushed onto the stack.
- **EX-7 (`undo()`)**: Pops the top action from the stack and invokes the reciprocal operation on `InventoryService`.

### 2.5 LRU Showroom Browsing Cache (`RecentlyViewed`) (L6–7)
- Extends `LinkedHashMap<Integer, Car>` initialized with `accessOrder = true`.
- Overrides `protected boolean removeEldestEntry(Map.Entry<Integer, Car> eldest)` returning `size() > 5`.
- As a customer views cars in the showroom browser, each vehicle is automatically placed at the end of the linked hash map. When capacity exceeds 5, the least recently viewed car is automatically evicted.

---

## 3. Anticipated Viva Questions & Model Answers

### Q1: Why did you use `ListIterator<Car>` instead of a standard `for-each` loop in the showroom browser?
> **Answer**: A standard `for-each` loop or basic `Iterator` can only traverse in the forward direction. `ListIterator<Car>` provides bidirectional navigation via `hasPrevious()` and `previous()`, which allows buyers to freely move back and forth between adjacent supercars in the console showroom without having to re-query or rebuild the list.

### Q2: How does `RecentlyViewed` implement an LRU cache without any external libraries?
> **Answer**: `RecentlyViewed` subclasses `LinkedHashMap` and passes `true` for the `accessOrder` argument in the `super()` constructor. In access-order mode, accessing an existing entry moves it to the tail of the underlying doubly-linked list. By overriding `removeEldestEntry(Map.Entry eldest)` to return `size() > 5`, Java automatically evicts the head of the list (the least recently accessed vehicle) whenever a new car is viewed.

### Q3: Explain why `Reservation` implements `Comparable<Reservation>` and how it works inside `ReservationManager`.
> **Answer**: `ReservationManager` uses a `PriorityQueue<Reservation>` to store holds. A `PriorityQueue` requires elements to define an ordering so it can maintain a min-heap structure. By implementing `Comparable<Reservation>` with `this.expiresAt.compareTo(other.expiresAt)`, the reservation with the earliest expiration timestamp always remains at the top of the heap. This allows the system to check and evict expired holds in $O(1)$ inspect time.

### Q4: Why is it necessary to use `Iterator.remove()` in `ReservationManager.purgeExpired()` instead of removing directly from the collection inside a for-each loop?
> **Answer**: Iterating through a collection with a standard for-each loop while calling `collection.remove()` triggers a `ConcurrentModificationException` because the collection's internal modification count changes while the iterator is active. Calling `Iterator.remove()` updates the iterator's internal cursor state synchronously with the collection, making removal during traversal completely safe.

### Q5: How do `InventoryService` and `SalesService` prevent race conditions when selling cars concurrently?
> **Answer**: `SalesService` obtains its monitor lock directly from `inventoryService.getLock()`. Both services synchronize critical sections—such as checking a car's status and updating it to `SOLD`—on the exact same intrinsic lock object. This ensures strict mutual exclusion across all threads, eliminates check-then-act race conditions (double selling), and avoids deadlocks by acquiring only one single lock.
