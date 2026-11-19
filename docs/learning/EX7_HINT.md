# Student Exercise Hint: EX-7 (Undo Manager via Deque)

## Method to Implement
Location: `src/main/java/com/revline/service/UndoManager.java`

```java
public synchronized String undo(InventoryService inventoryService) throws RevLineException
```

---

## Goal
Implement the **Undo** capability in RevLine using a **LIFO Stack** (`java.util.Deque`).
When a showroom manager accidentally adds a car, deletes a car, or updates a car's details, they should be able to press **Undo** to restore the previous state immediately.

### Syllabus Topics
- **Lecture 6–7**: Collection Framework interfaces — `Deque`, `ArrayDeque`, Stack operations (`push()`, `pop()`, `isEmpty()`), LIFO ordering.
- **Lecture 23**: Custom exception propagation (`RevLineException`).

---

## How It Works

Each mutating action (`ADD`, `DELETE`, `UPDATE`) in `InventoryService` creates an `UndoAction`:
- **`ADD`**: snapshot contains the newly added car. Reversing means removing it from inventory.
- **`DELETE`**: snapshot contains the car as it existed before deletion. Reversing means restoring it to inventory.
- **`UPDATE`**: snapshot contains the previous state of the car before changes. Reversing means restoring the snapshot back into inventory.

---

## Step-by-Step Implementation Guide

```java
public synchronized String undo(InventoryService inventoryService) throws RevLineException {
    // 1. Guard check: is there anything to undo?
    if (!canUndo() || stack.isEmpty()) {
        throw new RevLineException("No actions available to undo.");
    }
    if (inventoryService == null) {
        throw new RevLineException("InventoryService cannot be null.");
    }

    // 2. Pop the most recent action (LIFO)
    UndoAction action = stack.pop();
    Car snapshot = action.getSnapshot();

    // 3. Reverse the mutation based on action type
    switch (action.getType()) {
        case ADD:
            // Reversing an ADD means deleting the car that was added
            if (snapshot != null) {
                inventoryService.removeCarInternal(snapshot.getId());
            }
            break;

        case DELETE:
            // Reversing a DELETE means restoring the car that was deleted
            if (snapshot != null) {
                inventoryService.restoreCar(snapshot);
            }
            break;

        case UPDATE:
            // Reversing an UPDATE means restoring the pre-update snapshot
            if (snapshot != null) {
                inventoryService.restoreCar(snapshot);
            }
            break;

        default:
            throw new RevLineException("Unknown action type: " + action.getType());
    }

    // 4. Return confirmation message
    return "Undone: " + action.getDescription();
}
```

---

## Verification
Run `scripts/test.bat` (or `scripts/test.sh`) to see `UndoManagerTest` verify your implementation against additions, deletions, and updates.
