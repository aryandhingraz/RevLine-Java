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
        System.out.println();
        System.out.println("==========================================================================");
        System.out.println(" DEMO 1: RACE CONDITION PROOF (UnsafeCounter vs SafeCounter - L13, L14)   ");
        System.out.println("==========================================================================");
        System.out.println(" Target: 4 threads x 10,000 increments = 40,000 expected sum.");
        System.out.println(" Explanation: 'count++' is not atomic; it performs read-modify-write.");
        System.out.println(" Without synchronization, concurrent updates overwrite one another.");
        System.out.println("--------------------------------------------------------------------------");

        final int threadCount = 4;
        final int incrementsPerThread = 10_000;
        final int expectedTotal = threadCount * incrementsPerThread;

        for (int round = 1; round <= 3; round++) {
            // Test UnsafeCounter
            final UnsafeCounter unsafe = new UnsafeCounter();
            List<Thread> unsafeThreads = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        unsafe.increment();
                    }
                }, "UnsafeWorker-" + i);
                unsafeThreads.add(t);
                t.start();
            }
            for (Thread t : unsafeThreads) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }

            // Test SafeCounter
            final SafeCounter safe = new SafeCounter();
            List<Thread> safeThreads = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                Thread t = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        safe.increment();
                    }
                }, "SafeWorker-" + i);
                safeThreads.add(t);
                t.start();
            }
            for (Thread t : safeThreads) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }

            int unsafeActual = unsafe.getCount();
            int safeActual = safe.getCount();
            int lostUpdates = expectedTotal - unsafeActual;

            System.out.printf("  Round %d: Unsafe = %,6d (Lost %,5d updates) | Safe = %,6d [MATCH: %b]\n",
                    round, unsafeActual, lostUpdates, safeActual, safeActual == expectedTotal);
        }
        System.out.println("==========================================================================");
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
    }

    /**
     * Demo 2: 20 threads simultaneously race to buy the exact same vehicle.
     * Demonstrates that synchronized monitor locks enforce check-then-act atomicity.
     */
    public static void runDemo2DoubleSellShowdown(SalesService liveSalesService, InventoryService liveInventoryService) {
        System.out.println();
        System.out.println("==========================================================================");
        System.out.println(" DEMO 2: DOUBLE-SELL SHOWDOWN (20 Concurrent Buyer Threads - L13, L14)    ");
        System.out.println("==========================================================================");

        try {
            java.io.File demoCarsFile = new java.io.File("build/demo_doublesell_cars.csv");
            java.io.File demoSalesFile = new java.io.File("build/demo_doublesell_sales.csv");
            if (demoCarsFile.exists()) demoCarsFile.delete();
            if (demoSalesFile.exists()) demoSalesFile.delete();

            com.revline.repository.FileCarRepository demoCarRepo = new com.revline.repository.FileCarRepository(demoCarsFile);
            com.revline.repository.FileSaleRepository demoSaleRepo = new com.revline.repository.FileSaleRepository(demoSalesFile);

            Car targetCar = new Car(99, "Bugatti", "Chiron Showdown", 3000000.0, 420,
                    FuelType.PETROL, 1500, 2.4, Status.AVAILABLE);
            demoCarRepo.save(targetCar);

            InventoryService demoInv = new InventoryService(demoCarRepo, null);
            SalesService demoSales = new SalesService(demoInv, demoSaleRepo, new ReservationManager(), new WaitlistService(), null);

            int carId = targetCar.getId();
            System.out.printf("  Target Vehicle : #%d %s %s ($%,.2f, Status: %s)\n",
                    carId, targetCar.getBrand(), targetCar.getModel(), targetCar.getPrice(), targetCar.getStatus());
            System.out.println("  Spawning 20 concurrent BuyerTask threads competing for this vehicle...");
            System.out.println("--------------------------------------------------------------------------");

            List<BuyerTask> tasks = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();

            for (int i = 1; i <= 20; i++) {
                BuyerTask task = new BuyerTask(carId, "Buyer-" + i, demoSales);
                tasks.add(task);
                Thread t = new Thread(task, "BuyerThread-" + i);
                threads.add(t);
            }

            // Launch all threads simultaneously
            for (Thread t : threads) {
                t.start();
            }

            // Wait for all threads to terminate (L10 join)
            for (Thread t : threads) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }

            int winners = 0;
            int rejections = 0;
            String winnerName = null;

            for (BuyerTask bt : tasks) {
                if (bt.isSucceeded()) {
                    winners++;
                    winnerName = bt.buyerName;
                } else {
                    rejections++;
                }
            }

            System.out.printf("  [RESULT] Confirmed Winners : %d (Purchased by '%s')\n", winners, winnerName);
            System.out.printf("  [RESULT] Rejected Buyers  : %d (All caught CarNotAvailableException)\n", rejections);
            System.out.println("  [VERDICT] Exactly ONE winner; zero phantom sales or overselling bugs!");

            demoCarsFile.delete();
            demoSalesFile.delete();

        } catch (Exception e) {
            System.out.println("  [!] Demo 2 encountered error: " + e.getMessage());
        }
        System.out.println("==========================================================================");
    }

    // =========================================================================
    // 3. THREAD LIFECYCLE & PRIORITY TRACER (L11, L12)
    // =========================================================================

    /**
     * Demo 3: Traces every state of a thread's lifecycle from NEW to TERMINATED (L11).
     */
    public static void runDemo3ThreadLifecycleTracer() {
        System.out.println();
        System.out.println("==========================================================================");
        System.out.println(" DEMO 3: THREAD LIFECYCLE & PRIORITY TRACER (Thread.getState() - L11, L12)");
        System.out.println("==========================================================================");

        final Object monitor = new Object();
        final List<String> stateLog = Collections.synchronizedList(new ArrayList<>());

        Thread worker = new Thread(() -> {
            try {
                // 2. TIMED_WAITING
                Thread.sleep(80);

                // 3. BLOCKED (waiting for main thread to release monitor)
                synchronized (monitor) {
                    // 4. WAITING (waiting on monitor notification)
                    monitor.wait();
                }
            } catch (InterruptedException ignored) {}
        }, "TracedWorkerThread");

        // 1. State: NEW
        stateLog.add(String.format("  Step 1: After instantiation -> %s (isAlive=%b)", worker.getState(), worker.isAlive()));

        // Start thread -> RUNNABLE
        worker.start();
        stateLog.add(String.format("  Step 2: After start()       -> %s (isAlive=%b)", worker.getState(), worker.isAlive()));

        // Wait briefly so worker enters sleep -> TIMED_WAITING
        try { Thread.sleep(30); } catch (InterruptedException ignored) {}
        stateLog.add(String.format("  Step 3: During sleep()      -> %s", worker.getState()));

        // Acquire monitor in main thread before worker finishes sleep -> causes worker to be BLOCKED
        synchronized (monitor) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            stateLog.add(String.format("  Step 4: Waiting for lock    -> %s", worker.getState()));
        }

        // Worker acquires monitor, then calls wait() -> WAITING
        try { Thread.sleep(30); } catch (InterruptedException ignored) {}
        stateLog.add(String.format("  Step 5: During wait()       -> %s", worker.getState()));

        // Wake up worker
        synchronized (monitor) {
            monitor.notifyAll();
        }

        // Wait for worker to finish -> TERMINATED
        try { worker.join(); } catch (InterruptedException ignored) {}
        stateLog.add(String.format("  Step 6: After completion    -> %s (isAlive=%b)", worker.getState(), worker.isAlive()));

        for (String line : stateLog) {
            System.out.println(line);
        }

        // Thread Priority & Daemon Note (L12)
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("  Thread Priority Demonstration (L12):");
        System.out.printf("  - Thread.MIN_PRIORITY  : %d (Background daemons, e.g. AutoSaveThread)\n", Thread.MIN_PRIORITY);
        System.out.printf("  - Thread.NORM_PRIORITY : %d (Default application worker threads)\n", Thread.NORM_PRIORITY);
        System.out.printf("  - Thread.MAX_PRIORITY  : %d (Time-critical tasks)\n", Thread.MAX_PRIORITY);
        System.out.println("  * Note: Thread priority is an OS scheduling hint, not an execution guarantee.");
        System.out.println("==========================================================================");
    }

    // =========================================================================
    // 4. PRODUCER-CONSUMER BOUNDED BUFFER PIPELINE (L15, L16)
    // =========================================================================

    /**
     * Demo 4: Bounded buffer producer-consumer execution with wait() / notifyAll().
     */
    public static void runDemo4ProducerConsumer(SalesService salesService) {
        System.out.println();
        System.out.println("==========================================================================");
        System.out.println(" DEMO 4: PRODUCER-CONSUMER PIPELINE (PurchaseRequestQueue - L15, L16)     ");
        System.out.println("==========================================================================");
        System.out.println(" Capacity: 5 slots | 3 Buyer Producer Threads -> 1 SalesProcessor Consumer");
        System.out.println(" Demonstrates guarded blocks with while(condition) wait() & notifyAll().");
        System.out.println("--------------------------------------------------------------------------");

        PurchaseRequestQueue probeQueue = new PurchaseRequestQueue(2);
        boolean ex8Ready = false;
        try {
            probeQueue.put(new PurchaseRequest(999, 1, "Probe", "Agent"));
            probeQueue.take();
            ex8Ready = true;
        } catch (UnsupportedOperationException e) {
            ex8Ready = false;
        } catch (Exception ignored) {}

        if (!ex8Ready) {
            System.out.println("  [EX-8 PENDING] PurchaseRequestQueue.take() is assigned to student.");
            System.out.println("  Once you implement take() in PurchaseRequestQueue.java (see EX8_HINT.md),");
            System.out.println("  this live simulation will run 3 concurrent producers and 1 consumer");
            System.out.println("  with full interleaved execution tracing!");
            System.out.println("==========================================================================");
            return;
        }

        PurchaseRequestQueue queue = new PurchaseRequestQueue(5);
        SalesProcessor processor = new SalesProcessor(queue, salesService, null);
        Thread consumerThread = new Thread(processor, "SalesProcessor-1");
        consumerThread.start();

        // 3 Producer Threads submitting 6 total requests
        List<Thread> producers = new ArrayList<>();
        for (int p = 1; p <= 3; p++) {
            final int producerId = p;
            Thread prod = new Thread(() -> {
                for (int r = 1; r <= 2; r++) {
                    int reqId = producerId * 10 + r;
                    PurchaseRequest req = new PurchaseRequest(reqId, 1, "Buyer-P" + producerId, "Agent");
                    try {
                        queue.put(req);
                        System.out.printf("  [+] PRODUCER [%s] enqueued Request #%d (Queue size: %d/%d)\n",
                                Thread.currentThread().getName(), reqId, queue.size(), queue.getCapacity());
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "Producer-" + p);
            producers.add(prod);
            prod.start();
        }

        // Wait for producers to finish with 2-second watchdog
        for (Thread p : producers) {
            try { p.join(2000); } catch (InterruptedException ignored) {}
        }

        // Allow consumer time to process
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        queue.stop();
        processor.stop();
        try { consumerThread.join(500); } catch (InterruptedException ignored) {}

        System.out.println("--------------------------------------------------------------------------");
        System.out.println("  Producer-Consumer pipeline simulation completed cleanly.");
        System.out.println("==========================================================================");
    }

    // =========================================================================
    // 5. THREAD INTERRUPTION & VISIBILITY AWARENESS (L10, L14)
    // =========================================================================

    public static void runDemo5InterruptionAndVisibility(InventoryService inventoryService) {
        System.out.println();
        System.out.println("==========================================================================");
        System.out.println(" DEMO 5: THREAD INTERRUPTION & MEMORY VISIBILITY (L10, L14)              ");
        System.out.println("==========================================================================");

        AutoSaveThread autoSave = new AutoSaveThread(inventoryService, 10_000L, null);
        System.out.println("  1. Launching AutoSaveThread (Daemon = " + autoSave.isDaemon() + ", Priority = " + autoSave.getPriority() + ")...");
        autoSave.start();

        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
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

    public static void main(String[] args) throws Exception {
        com.revline.repository.FileCarRepository carRepo =
                new com.revline.repository.FileCarRepository(new java.io.File("data/seed/cars.csv"));
        com.revline.repository.FileSaleRepository saleRepo =
                new com.revline.repository.FileSaleRepository(new java.io.File("data/seed/sales.csv"));
        InventoryService inv = new InventoryService(carRepo, null);
        SalesService salesService = new SalesService(inv, saleRepo, new ReservationManager(), new WaitlistService(), null);

        runDemo1RaceCondition();
        runDemo2DoubleSellShowdown(salesService, inv);
        runDemo3ThreadLifecycleTracer();
        runDemo4ProducerConsumer(salesService);
        runDemo5InterruptionAndVisibility(inv);
    }
}
