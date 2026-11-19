# Student Exercise Hint: EX-1 (Input Validation & Parsing)

## Methods to Implement
Location: `src/main/java/com/revline/util/Validator.java`

```java
public static boolean isValidFuelType(String text)
public static double parsePositiveDouble(String text, String fieldName)
```

---

## Part A: `isValidFuelType(String text)`

### Goal
Return `true` if `text` matches any of the three supported fuel types ("PETROL", "HYBRID", "ELECTRIC"), case-insensitively. Return `false` if `null`, blank, or unknown.

### Syllabus Topics
- **Lecture 2**: String methods (`trim()`, `equalsIgnoreCase()`).

### Hint / Algorithm
1. Check if `text == null` or `text.trim().isEmpty()` → return `false`.
2. Clean the string with `.trim()`.
3. Check if it matches `"PETROL"`, `"HYBRID"`, or `"ELECTRIC"` using `.equalsIgnoreCase()`. (Alternatively, compare against `FuelType.values()`).

---

## Part B: `parsePositiveDouble(String text, String fieldName)`

### Goal
Parse a user-entered text string (such as price or acceleration) into a strictly positive `double` (`> 0.0`). If the string is invalid or non-positive, throw an `InvalidCarException`.

### Syllabus Topics
- **Lecture 2 & 4**: Wrapper class parsing (`Double.parseDouble`).
- **Lecture 3**: Checked vs. unchecked exception throwing (`throw new InvalidCarException(...)`).

### Hint / Algorithm
1. Ensure `text` is not null or empty using `requireNonBlank(text, fieldName)`.
2. Wrap `Double.parseDouble(text.trim())` inside a `try { ... } catch (NumberFormatException e) { ... }` block.
3. If parsing succeeds, check if `val <= 0.0`. If so, throw `new InvalidCarException(fieldName + " must be strictly positive.");`
4. In the `catch` block, throw `new InvalidCarException(fieldName + " must be a valid number, got: '" + text + "'.", e);`.

---

## Verification
Run `scripts/test.bat` (or `scripts/test.sh`) to see `ValidatorTest` execute your implementation.
