package com.revline.service;

import com.revline.util.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Manages FIFO customer waitlists for reserved or high-demand vehicles (L6, L7 Queue).
 */
public class WaitlistService {

    // Demonstrates Map of Queues using LinkedList implementation (L6-7)
    private final Map<Integer, Queue<String>> waitlists = new HashMap<>();

    public WaitlistService() {}

    /**
     * Adds a buyer to the FIFO waitlist for a specific car.
     *
     * @param carId target car ID
     * @param buyerName name of the interested buyer
     * @return 1-based position in the queue
     */
    public synchronized int join(int carId, String buyerName) {
        Validator.requireNonBlank(buyerName, "Buyer Name");
        String trimmed = buyerName.trim();

        Queue<String> queue = waitlists.computeIfAbsent(carId, k -> new LinkedList<>());

        int pos = 1;
        for (String b : queue) {
            if (b.equalsIgnoreCase(trimmed)) {
                return pos; // Already in queue
            }
            pos++;
        }

        queue.offer(trimmed);
        return queue.size();
    }

    /**
     * Retrieves and removes the next buyer in line for a car.
     *
     * @param carId target car ID
     * @return next buyer name, or null if waitlist is empty
     */
    public synchronized String poll(int carId) {
        Queue<String> queue = waitlists.get(carId);
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        String next = queue.poll();
        if (queue.isEmpty()) {
            waitlists.remove(carId);
        }
        return next;
    }

    /**
     * Inspects the next buyer without removing them.
     */
    public synchronized String peek(int carId) {
        Queue<String> queue = waitlists.get(carId);
        return (queue != null) ? queue.peek() : null;
    }

    /**
     * Returns the 1-based queue position of a buyer, or -1 if not in line.
     */
    public synchronized int getPosition(int carId, String buyerName) {
        if (buyerName == null) return -1;
        Queue<String> queue = waitlists.get(carId);
        if (queue == null) return -1;

        int pos = 1;
        for (String b : queue) {
            if (b.equalsIgnoreCase(buyerName.trim())) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    /**
     * Returns the number of customers currently waiting for a car.
     */
    public synchronized int getQueueSize(int carId) {
        Queue<String> queue = waitlists.get(carId);
        return (queue != null) ? queue.size() : 0;
    }

    /**
     * Returns an unmodifiable snapshot of all buyers currently waiting in order.
     */
    public synchronized List<String> getWaitlist(int carId) {
        Queue<String> queue = waitlists.get(carId);
        if (queue == null || queue.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(queue));
    }

    /**
     * Removes a specific buyer from the waitlist if they choose to withdraw.
     */
    public synchronized boolean remove(int carId, String buyerName) {
        if (buyerName == null) return false;
        Queue<String> queue = waitlists.get(carId);
        if (queue == null) return false;

        boolean removed = queue.removeIf(b -> b.equalsIgnoreCase(buyerName.trim()));
        if (queue.isEmpty()) {
            waitlists.remove(carId);
        }
        return removed;
    }

    /**
     * Clears all waitlist entries for a specific car.
     */
    public synchronized void clear(int carId) {
        waitlists.remove(carId);
    }
}
