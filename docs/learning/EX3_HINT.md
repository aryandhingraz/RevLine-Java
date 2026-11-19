# Student Exercise Hint: EX-3 (Multi-Field Search Service)

## Method to Implement
Location: `src/main/java/com/revline/service/SearchService.java`

```java
public List<Car> search(String query)
```

---

## Goal
Perform a fast, case-insensitive multi-field search across all cars in inventory.
A user should be able to type `"ferrari hybrid"` or `"porsche"` or `"911"` and receive all matching vehicles.

### Syllabus Topics
- **Lecture 2**: String methods (`toLowerCase()`, `contains()`, `split("\\s+")`).
- **Lecture 5**: Iteration over `ArrayList<Car>` (`inventoryService.findAll()`).

---

## Hint / Algorithm

1. **Input Guarding**:
   - If `query == null` or `query.trim().isEmpty()`, return `inventoryService.findAll()` (or an empty list if preferred; `findAll()` allows browsing all cars on empty search).
2. **Tokenization**:
   - Convert the entire query to lower case: `String lowerQuery = query.trim().toLowerCase();`
   - Split into individual words on whitespace:
     ```java
     String[] tokens = lowerQuery.split("\\s+");
     ```
3. **Filtering**:
   - Create a results list: `List<Car> matches = new ArrayList<>();`
   - Iterate through each car from `inventoryService.findAll()`:
     - Build a searchable text representation of the car in lower case:
       ```java
       String haystack = (car.getBrand() + " " + car.getModel() + " " + car.getFuelType().name()).toLowerCase();
       ```
     - Check whether **every** token in `tokens` is contained in `haystack`:
       ```java
       boolean allMatch = true;
       for (String token : tokens) {
           if (!haystack.contains(token)) {
               allMatch = false;
               break;
           }
       }
       if (allMatch) {
           matches.add(car);
       }
       ```
4. **Return**:
   - Return `matches`.

---

## Verification
Run `scripts/test.bat` (or `scripts/test.sh`) to see `SearchServiceTest` execute your implementation.
