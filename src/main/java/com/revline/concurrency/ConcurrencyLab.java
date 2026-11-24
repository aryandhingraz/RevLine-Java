package com.revline.concurrency;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.PurchaseRequest;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.repository.SaleRepository;
import com.revline.service.InventoryService;
import com.revline.service.ReservationManager;
import com.revline.service.SalesService;
import com.revline.service.WaitlistService;
import com.revline.util.AppLogger;
import com.revline.util.exception.CarNotAvailableException;
import com.revline.util.exception.RepositoryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interactive Concurrency Lab demonstrating core Java multithreading concepts (L9–L16).
 * Features 5 deterministic educational demonstrations:
 * 1. Race condition proof: UnsafeCounter vs SafeCounter (L13, L14).
 * 2. Double-sell showdown: Unsynchronized vs Synchronized SalesService (L13, L14).
 * 3. Thread lifecycle & state tracer: NEW -> RUNNABLE -> TIMED_WAITING -> BLOCKED -> WAITING -> TERMINATED (L11, L12).
 * 4. Producer-consumer bounded buffer pipeline (L15, L16).
 * 5. Thread interruption, graceful shutdown, and volatile visibility awareness (L9, L10, L14).
 */
public final class ConcurrencyLab {

    private ConcurrencyLab() {}

    // =========================================================================
    // 1. COUNTER ABSTRACTIONS (L13, L14)
    // =========================================================================

    public static class UnsafeCounter {
        private int count = 0;

        // Non-atomic read-modify-write without synchronization: prone to data race
        public void increment() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    public static class SafeCounter {
        private int count = 0;

        // Synchronized method guarantees atomicity and memory visibility (L13, L14)
        public synchronized void increment() {
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    /**
     * Demo 1: Proves race conditions by running 4 threads incrementing counters 10,000 times each.
     */
    public static void runDemo1RaceCondition() {
        printDemo1Header();

        final int threadCount = 4;
        final int incrementsPerThread = 10_000;
        final int expectedTotal = threadCount * incrementsPerThread;

        for (int round = 1; round <= 3; round++) {
            int unsafeActual = runUnsafeTrial(threadCount, incrementsPerThread);
            int safeActual = runSafeTrial(threadCount, incrementsPerThread);
            int lostUpdates = expectedTotal - unsafeActual;

            System.out.printf("  Round %d: Unsafe = %,6d (Lost %,5d updates) | Safe = %,6d [MATCH: %b]\n",
                    round, unsafeActual, lostUpdates, safeActual, safeActual == expectedTotal);
        }
        System.out.println("==========================================================================");
    }

    private static void printDemo1Header() {
        System.out.println();
        System.out.println("==========================================================================");
        System.out.println(" DEMO 1: RACE CONDITION PROOF (UnsafeCounter vs SafeCounter - L13, L14)   ");
        System.out.println("==========================================================================");
        System.out.println(" Target: 4 threads x 10,000 increments = 40,000 expected sum.");
        System.out.println(" Explanation: 'count++' is not atomic; it performs read-modify-write.");
        System.out.println(" Without synchronization, concurrent updates overwrite one another.");
        System.out.println("--------------------------------------------------------------------------");
    }

    private static int runUnsafeTrial(int threadCount, int incrementsPerThread) {
        final UnsafeCounter unsafe = new UnsafeCounter();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    unsafe.increment();
                }
            }, "UnsafeWorker-" + i);
            threads.add(t);
            t.start();
        }
        joinAllThreads(threads);
        return unsafe.getCount();
    }

    private static int runSafeTrial(int threadCount, int incrementsPerThread) {
        final SafeCounter safe = new SafeCounter();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    safe.increment();
                }
            }, "SafeWorker-" + i);
            threads.add(t);
            t.start();
        }
        joinAllThreads(threads);
        return safe.getCount();
    }

    private static void joinAllThreads(List<Thread> threads) {
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status (L9)
                break;
            }
        }
    }

    // =========================================================================
    // 2. DOUBLE-SELL SHOWDOWN (L13, L14)
    // =========================================================================

    public static class BuyerTask implements Runnable {
        private final int carId;
        private final String buyerName;
        private final SalesService salesService;
        private boolean succeeded = false;
        private String failureMessage = null;

        public BuyerTask(int carId, String buyerName, SalesService salesService) {
            this.carId = carId;
            this.buyerName = buyerName;
            this.salesService = salesService;
        }

        @Override
        public void run() {
            try {
                salesService.sellCar(carId, buyerName, "Showroom Agent");
                succeeded = true;
            } catch (Exception e) {
                failureMessage = e.getMessage();
            }
        }

        public boolean isSucceeded() { return succeeded; }
        public String getFailureMessage() { return failureMessage; }
        public String getBuyerName() { return buyerName; }
    }

    /**
     * Demo 2: 20 threads simultaneously race to buy the exact same vehicle.
     */
    public static void runDemo2DoubleSellShowdown(SalesService liveSalesService, InventoryService liveInventoryService) {
        ConcurrencyLabDemos.runDemo2DoubleSellShowdown();
    }

    /**
     * Demo 3: Traces every state of a thread's lifecycle from NEW to TERMINATED (L11).
     */
    public static void runDemo3ThreadLifecycleTracer() {
        ConcurrencyLabDemos.runDemo3ThreadLifecycleTracer();
    }

    /**
     * Demo 4: Bounded buffer producer-consumer execution with wait() / notifyAll().
     */
    public static void runDemo4ProducerConsumer(SalesService salesService) {
        ConcurrencyLabDemos.runDemo4ProducerConsumer(salesService);
    }

    /**
     * Demo 5: Thread interruption, graceful shutdown, and memory visibility (L10, L14).
     */
    public static void runDemo5InterruptionAndVisibility(InventoryService inventoryService) {
        System.out.println();
        System.out.println("==========================================================================");
        System.out.println(" DEMO 5: THREAD INTERRUPTION & MEMORY VISIBILITY (L10, L14)              ");
        System.out.println("==========================================================================");

        AutoSaveThread autoSave = new AutoSaveThread(inventoryService, 10_000L, null);
        System.out.println("  1. Launching AutoSaveThread (Daemon = " + autoSave.isDaemon()
                + ", Priority = " + autoSave.getPriority() + ")...");
        autoSave.start();

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("  2. AutoSaveThread state while sleeping: " + autoSave.getState());

        System.out.println("  3. Signaling interrupt() and shutdown()...");
        long startTime = System.currentTimeMillis();
        autoSave.shutdown();

        try {
            autoSave.join(2000);
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.printf("  4. AutoSaveThread joined cleanly in %d ms (State: %s, isAlive: %b)\n",
                    elapsed, autoSave.getState(), autoSave.isAlive());
        } catch (InterruptedException e) {
            System.out.println("  [!] Join was interrupted.");
        }

        System.out.println("--------------------------------------------------------------------------");
        System.out.println("  Memory Visibility Awareness (L14 volatile keyword):");
        System.out.println("  - Without 'volatile', a thread's cached read of a boolean flag may never see");
        System.out.println("    writes made by another thread due to CPU register caching.");
        System.out.println("  - Marking 'volatile boolean running' guarantees immediate visibility across threads.");
        System.out.println("==========================================================================");
    }
}
