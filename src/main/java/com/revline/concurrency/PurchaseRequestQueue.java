package com.revline.concurrency;

import com.revline.model.PurchaseRequest;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Bounded buffer queue for purchase requests demonstrating guarded blocks,
 * monitor wait/notifyAll, and producer-consumer synchronization (L15, L16).
 *
 * EX-8 is assigned here: the student must implement take().
 */
public class PurchaseRequestQueue {

    private final Queue<PurchaseRequest> queue;
    private final int capacity;
    private volatile boolean active = true;

    public PurchaseRequestQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    /**
     * Producer enqueue: adds request to bounded queue, blocking if capacity reached (L15).
     *
     * @param request purchase request to enqueue
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized void put(PurchaseRequest request) throws InterruptedException {
        if (request == null) {
            throw new IllegalArgumentException("Purchase request cannot be null.");
        }
        while (queue.size() >= capacity && active) {
            wait();
        }
        if (!active) {
            return;
        }
        queue.add(request);
        notifyAll();
    }

    /**
     * STUDENT EXERCISE EX-8: Consumer dequeue with guarded wait (L15, L16).
     *
     * In-syllabus requirements (L15, L16):
     * - Must be synchronized on this monitor.
     * - While queue is empty and active is true, call wait().
     * - If active is false and queue is empty, return null.
     * - Poll the head request from queue.
     * - Call notifyAll() to wake any waiting producers blocked in put().
     * - Return the polled request.
     *
     * @return next PurchaseRequest, or null if stopped
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized PurchaseRequest take() throws InterruptedException {
        // ASSIGNED TO STUDENT AS EX-8. DO NOT IMPLEMENT AUTOMATICALLY.
        throw new UnsupportedOperationException("EX-8: Student to implement guarded wait() and take() in PurchaseRequestQueue.");
    }

    /**
     * Deactivates the queue and wakes all waiting threads.
     */
    public synchronized void stop() {
        this.active = false;
        notifyAll();
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized int size() {
        return queue.size();
    }

    public int getCapacity() {
        return capacity;
    }
}
