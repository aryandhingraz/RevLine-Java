# Module 1 Explained: Inventory Management & Architecture

> **Guide for Student Viva Defense (CA-3 & CA-4)**  
> Packages: `com.revline.model`, `com.revline.repository`, `com.revline.service`, `com.revline.ui`

---

## 1. What This Module Does (Plain English)
Module 1 is the core showroom inventory engine. It takes the original C console showroom (`main.c`) and re-engineers it into an object-oriented, layered Java application. It:
1. Loads the 18 seeded supercars from disk (`data/cars.csv`) into memory.
2. Maintains fast, index-based $O(1)$ lookup while keeping the original list order.
3. Rejects duplicate cars (`brand + model`) case-insensitively.
4. Prevents crashes from malformed inputs using typed parsing and custom exceptions.
5. Displays cars in an auto-aligning box-drawing console table with active stock value totals.

---

## 2. Why These Collections? (Viva Favorite!)

### Why both `ArrayList<Car>` and `HashMap<Integer, Car>`?
- **`ArrayList<Car>`**: Provides ordered, indexed storage. It lets us iterate from start to finish to print the showroom table and feed Stream pipelines in a predictable order.
- **`HashMap<Integer, Car>`**: Provides $O(1)$ average time complexity for finding a vehicle by its ID (`findById(id)`). Without the map, finding a car would require an $O(N)$ linear loop over the entire list every single time.
- **`HashSet<String>` (`duplicateGuard`)**: Stores keys formatted as `brand.toLowerCase() + "|" + model.toLowerCase()`. It allows us to check in $O(1)$ whether a car with the same brand and model already exists before adding it.
- **`TreeSet<String>`**: In `getBrandsSorted()`, a `TreeSet` automatically removes duplicate brand names and sorts them in alphabetical order (A–Z) using its underlying Red-Black tree.

---

## 3. Crash-Safe File Persistence (Why not just overwrite?)
In the original C code (`saveCarsToFile`), the file `cars.txt` was opened directly with `"w"`. If the computer lost power or crashed during the write loop, the entire database file was wiped out or corrupted.

In Java (`FileUtil.safeWrite`):
1. We write all data to a temporary file (`cars.csv.tmp`).
2. We make a backup copy of the old file (`cars.csv.bak`).
3. We atomically rename `cars.csv.tmp` to `cars.csv`.
4. If anything fails, the `.bak` file is restored. The file on disk is never left half-written.

---

## 4. Likely Viva Questions & Model Answers

### Q1: What is the difference between `Vehicle` and `Describable`?
> **Answer**: `Vehicle` is an **abstract class** representing the core entity identity (holding protected state: `id`, `brand`, `model`, `price` and constructors). A vehicle is an `is-a` relationship. `Describable` is an **interface** representing a capability (`can-do` contract: `describe()`) that any class can implement without sharing inheritance.

### Q2: Why is `id` marked `final` in `Vehicle`?
> **Answer**: A vehicle's ID is its primary domain identifier. Once assigned by the system, it should remain immutable so that collection indexing (`HashMap` keys and `HashSet` lookups) remains consistent and cannot be accidentally corrupted.

### Q3: Why does `InventoryService.findAll()` return an unmodifiable list of copied cars?
> **Answer**: This is a **defensive copy**. If we returned our internal `cars` list directly, an external caller could call `.clear()` or mutate a car's price from outside, bypassing our validation rules and thread synchronization.

### Q4: Why is `InvalidCarException` unchecked (`RuntimeException`), while `RepositoryException` is checked (`Exception`)?
> **Answer**: `InvalidCarException` represents programming or user input constraint violations (like a negative price or empty brand) that can occur anywhere in domain constructors. `RepositoryException` represents environmental failures (disk full, file read error, database connection failure) that calling services **must** anticipate, catch, and handle gracefully.

### Q5: How does constructor chaining work in `Vehicle`?
> **Answer**: `Vehicle(id, brand, model)` calls `this(id, brand, model, 0.0)` using the `this(...)` syntax, ensuring that all field validations in the primary constructor are executed centrally in one place without duplicating code.

### Q6: What does `-Xlint:all` do and why do we compile with `--release 17`?
> **Answer**: `-Xlint:all` enables every compiler warning (such as serialVersionUID or raw types). `--release 17` configures `javac` so that even when compiling on newer JDKs (like JDK 25), the output is strictly binary-compatible with the Java 17 LTS runtime.

---

## 5. Three "Break It on Purpose" Experiments

Try these experiments in the console to understand how the system defends itself:
1. **Experiment 1 (Input Fuzzing)**: Run `scripts/run.bat`, select `[2] Add New Supercar`, and type `abc` when asked for the price.
   - *Result*: The `InputReader` catches the `NumberFormatException` and re-prompts you safely without crashing.
2. **Experiment 2 (Duplicate Injection)**: Try adding a car with Brand `Bugatti` and Model `Chiron`.
   - *Result*: The `duplicateGuard` detects the existing entry in `HashSet` and throws `InvalidCarException`, printing a clean error message.
3. **Experiment 3 (Bad CSV Row)**: Open `data/cars.csv` in a text editor and add a broken line `99,Broken,Car,NaN,300,Petrol,500,3.0,AVAILABLE`.
   - *Result*: `FileCarRepository` catches the `DataFormatException` on that specific line, skips it, logs it to `getLastLoadReport()`, and loads all valid cars without halting.
