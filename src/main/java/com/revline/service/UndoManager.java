package com.revline.service;

import com.revline.model.Car;
import com.revline.util.exception.RevLineException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Manages reversible inventory operations using a stack data structure (L6, L7 Deque).
 * Supports undoing ADD, DELETE, and UPDATE operations.
 */
public class UndoManager {

    /**
     * Reversible inventory mutation types.
     */
    public enum ActionType {
        ADD,
        DELETE,
        UPDATE
    }

    /**
     * Immutable snapshot record representing a reversible mutation.
     */
    public static class UndoAction {
        private final ActionType type;
        private final Car snapshot;
        private final String description;

        public UndoAction(ActionType type, Car snapshot, String description) {
            this.type = type;
            this.snapshot = (snapshot != null) ? snapshot.copy() : null;
            this.description = (description != null) ? description : "";
        }

        public ActionType getType() {
            return type;
        }

        public Car getSnapshot() {
            return (snapshot != null) ? snapshot.copy() : null;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s", type, description);
        }
    }

    // ArrayDeque implements L6-7 Deque as an efficient LIFO stack
    private final Deque<UndoAction> stack = new ArrayDeque<>();
    private final int maxCapacity;

    public UndoManager() {
        this(50);
    }

    public UndoManager(int maxCapacity) {
        this.maxCapacity = (maxCapacity > 0) ? maxCapacity : 50;
    }

    /**
     * Pushes a new reversible action onto the undo stack.
     *
     * @param action the mutation record to push
     */
    public synchronized void push(UndoAction action) {
        if (action == null) return;
        if (stack.size() >= maxCapacity) {
            stack.removeLast(); // Discard oldest history entry when capacity reached
        }
        stack.push(action);
    }

    /**
     * Checks if any action is available to undo.
     */
    public synchronized boolean canUndo() {
        return !stack.isEmpty();
    }

    /**
     * Inspects the most recent action on the stack without removing it.
     */
    public synchronized UndoAction peek() {
        return stack.peek();
    }

    /**
     * Returns the current number of reversible operations.
     */
    public synchronized int size() {
        return stack.size();
    }

    /**
     * Clears all recorded undo history.
     */
    public synchronized void clear() {
        stack.clear();
    }

    /**
     * Returns a snapshot list of current actions on the stack from newest to oldest.
     */
    public synchronized List<UndoAction> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(stack));
    }

    // =========================================================================
    // STUDENT EXERCISE EX-7 ZONE
    // =========================================================================

    /**
     * EX-7: Pops the most recent action and reverses its effect on inventory (L6-7 Deque).
     *
     * Behavior:
     * 1. If canUndo() is false, throw RevLineException("No actions available to undo.").
     * 2. Pop the top UndoAction from the stack.
     * 3. Apply reverse operation to inventoryService based on action.getType():
     *    - ADD:    reverses by deleting the newly added car (inventoryService.removeCarInternal(carId))
     *    - DELETE: reverses by re-inserting the car snapshot (inventoryService.restoreCar(snapshot))
     *    - UPDATE: reverses by restoring the previous car snapshot (inventoryService.restoreCar(snapshot))
     * 4. Return a human-readable confirmation message, e.g.:
     *    "Undone: Reverted addition of Car #20 (Ferrari Roma)"
     *
     * @param inventoryService target inventory service to apply the reversal on
     * @return description of the action that was undone
     * @throws RevLineException if stack is empty or undo operation fails
     * @see docs/learning/EX7_HINT.md
     */
    public synchronized String undo(InventoryService inventoryService) throws RevLineException {
        if (!canUndo()) {
            throw new RevLineException("No actions available to undo.");
        }
        if (inventoryService == null) {
            throw new RevLineException("InventoryService cannot be null.");
        }

        UndoAction action = stack.pop();
        Car snapshot = action.getSnapshot();
        switch (action.getType()) {
            case ADD:
                if (snapshot != null) {
                    inventoryService.removeCarInternal(snapshot.getId());
                }
                break;
            case DELETE:
            case UPDATE:
                if (snapshot != null) {
                    inventoryService.restoreCar(snapshot);
                }
                break;
            default:
                throw new RevLineException("Unknown action type: " + action.getType());
        }
        return "Undone: " + action.getDescription();
    }
}
