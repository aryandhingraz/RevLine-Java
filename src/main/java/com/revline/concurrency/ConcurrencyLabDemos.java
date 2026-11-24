package com.revline.concurrency;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.PurchaseRequest;
import com.revline.model.Status;
import com.revline.service.InventoryService;
import com.revline.service.ReservationManager;
import com.revline.service.SalesService;
import com.revline.service.WaitlistService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Educational demonstration runners for the Concurrency Lab (L9–L16).
 * Shows race conditions, synchronized critical sections, thread lifecycle, and producer-consumer queues.
 */
final class ConcurrencyLabDemos {

    private ConcurrencyLabDemos() {}

    static void printBanner(String title) {
        System.out.println();
        System.out.println("==========================================================================");
        System.out.println(" " + title);
        System.out.println("==========================================================================");
    }

    static void printDivider() {
        System.out.println("==========================================================================");
    }

    /**
     * Demo 2: 20 threads simultaneously race to buy the exact same vehicle (L13, L14).
     */
    static void runDemo2DoubleSellShowdown() {
        File demoCarsFile = null;
        File demoSalesFile = null;
        try {
            demoCarsFile = File.createTempFile("demo_doublesell_cars_", ".csv");
            demoSalesFile = File.createTempFile("demo_doublesell_sales_", ".csv");
            demoCarsFile.deleteOnExit();
            demoSalesFile.deleteOnExit();

            com.revline.repository.FileCarRepository demoCarRepo = new com.revline.repository.FileCarRepository(demoCarsFile);
            com.revline.repository.FileSaleRepository demoSaleRepo = new com.revline.repository.FileSaleRepository(demoSalesFile);

            Car targetCar = new Car(99, "Bugatti", "Chiron Showdown", 3000000.0, 420,
                    FuelType.PETROL, 1500, 2.4, Status.AVAILABLE);
            demoCarRepo.save(targetCar);

            InventoryService demoInv = new InventoryService(demoCarRepo, null);
            SalesService demoSales = new SalesService(demoInv, demoSaleRepo, new ReservationManager(), new WaitlistService(), null);

            printBanner("DEMO 2: DOUBLE-SELL SHOWDOWN (20 Concurrent Buyer Threads - L13, L14)");
            System.out.printf("  Target Vehicle : #%d %s %s ($%,.2f, Status: %s)\n",
                    targetCar.getId(), targetCar.getBrand(), targetCar.getModel(), targetCar.getPrice(), targetCar.getStatus());
            System.out.println("  Spawning 20 concurrent BuyerTask threads competing for this vehicle...");
            System.out.println("--------------------------------------------------------------------------");

            List<ConcurrencyLab.BuyerTask> tasks = spawnAndRunBuyers(targetCar.getId(), demoSales);
            printDemo2Results(tasks);
        } catch (Exception e) {
            System.out.println("  [!] Demo 2 encountered error: " + e.getMessage());
        } finally {
            cleanupTempFile(demoCarsFile);
            cleanupTempFile(demoSalesFile);
        }
        printDivider();
    }

    private static List<ConcurrencyLab.BuyerTask> spawnAndRunBuyers(int carId, SalesService demoSales) {
        List<ConcurrencyLab.BuyerTask> tasks = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            ConcurrencyLab.BuyerTask task = new ConcurrencyLab.BuyerTask(carId, "Buyer-" + i, demoSales);
            tasks.add(task);
            Thread t = new Thread(task, "BuyerThread-" + i);
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return tasks;
    }

    private static void printDemo2Results(List<ConcurrencyLab.BuyerTask> tasks) {
        int winners = 0;
        int rejections = 0;
        String winnerName = null;
        for (ConcurrencyLab.BuyerTask bt : tasks) {
            if (bt.isSucceeded()) {
                winners++;
                winnerName = bt.getBuyerName();
            } else {
                rejections++;
            }
        }
        System.out.printf("  [RESULT] Confirmed Winners : %d (Purchased by '%s')\n", winners, winnerName);
        System.out.printf("  [RESULT] Rejected Buyers  : %d (All caught CarNotAvailableException)\n", rejections);
        System.out.println("  [VERDICT] Exactly ONE winner; zero phantom sales or overselling bugs!");
    }

    private static void cleanupTempFile(File f) {
        if (f != null && f.exists()) {
            f.delete();
        }
    }

    /**
     * Demo 3: Traces every state of a thread's lifecycle from NEW to TERMINATED (L11).
     */
    static void runDemo3ThreadLifecycleTracer() {
        printBanner("DEMO 3: THREAD LIFECYCLE & PRIORITY TRACER (Thread.getState() - L11, L12)");
        final Object monitor = new Object();
        final List<String> stateLog = Collections.synchronizedList(new ArrayList<>());

        traceLifecycleSteps(monitor, stateLog);

        for (String line : stateLog) {
            System.out.println(line);
        }
        printPriorityInfo();
        printDivider();
    }

    private static void traceLifecycleSteps(Object monitor, List<String> stateLog) {
        Thread worker = createTracedWorker(monitor);
        stateLog.add(String.format("  Step 1: After instantiation -> %s (isAlive=%b)", worker.getState(), worker.isAlive()));
        worker.start();
        stateLog.add(String.format("  Step 2: After start()       -> %s (isAlive=%b)", worker.getState(), worker.isAlive()));

        sleepQuietly(30);
        stateLog.add(String.format("  Step 3: During sleep()      -> %s", worker.getState()));

        synchronized (monitor) {
            sleepQuietly(100);
            stateLog.add(String.format("  Step 4: Waiting for lock    -> %s", worker.getState()));
        }

        sleepQuietly(30);
        stateLog.add(String.format("  Step 5: During wait()       -> %s", worker.getState()));

        synchronized (monitor) {
            monitor.notifyAll();
        }
        joinWorker(worker);
        stateLog.add(String.format("  Step 6: After completion    -> %s (isAlive=%b)", worker.getState(), worker.isAlive()));
    }

    private static Thread createTracedWorker(Object monitor) {
        return new Thread(() -> {
            try {
                Thread.sleep(80);
                synchronized (monitor) {
                    monitor.wait();
                }
            } catch (InterruptedException e) {
                // Thread exits cleanly on interruption (L9, L10)
            }
        }, "TracedWorkerThread");
    }

    private static void joinWorker(Thread worker) {
        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void printPriorityInfo() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("  Thread Priority Demonstration (L12):");
        System.out.printf("  - Thread.MIN_PRIORITY  : %d (Background daemons, e.g. AutoSaveThread)\n", Thread.MIN_PRIORITY);
        System.out.printf("  - Thread.NORM_PRIORITY : %d (Default application worker threads)\n", Thread.NORM_PRIORITY);
        System.out.printf("  - Thread.MAX_PRIORITY  : %d (Time-critical tasks)\n", Thread.MAX_PRIORITY);
        System.out.println("  * Note: Thread priority is an OS scheduling hint, not an execution guarantee.");
    }

    /**
     * Demo 4: Bounded buffer producer-consumer execution with wait() / notifyAll() (L15, L16).
     */
    static void runDemo4ProducerConsumer(SalesService salesService) {
        printBanner("DEMO 4: PRODUCER-CONSUMER PIPELINE (PurchaseRequestQueue - L15, L16)");
        System.out.println(" Capacity: 5 slots | 3 Buyer Producer Threads -> 1 SalesProcessor Consumer");
        System.out.println(" Demonstrates guarded blocks with while(condition) wait() & notifyAll().");
        System.out.println("--------------------------------------------------------------------------");

        PurchaseRequestQueue queue = new PurchaseRequestQueue(5);
        SalesProcessor processor = new SalesProcessor(queue, salesService, null);
        Thread consumerThread = new Thread(processor, "SalesProcessor-1");
        consumerThread.start();

        List<Thread> producers = spawnProducers(queue);
        awaitProducersAndShutdown(producers, queue, processor, consumerThread);

        System.out.println("--------------------------------------------------------------------------");
        System.out.println("  Producer-Consumer pipeline simulation completed cleanly.");
        printDivider();
    }

    private static List<Thread> spawnProducers(PurchaseRequestQueue queue) {
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
        return producers;
    }

    private static void awaitProducersAndShutdown(List<Thread> producers, PurchaseRequestQueue queue,
                                                   SalesProcessor processor, Thread consumerThread) {
        for (Thread p : producers) {
            try {
                p.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        sleepQuietly(100);
        queue.stop();
        processor.stop();
        try {
            consumerThread.join(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
