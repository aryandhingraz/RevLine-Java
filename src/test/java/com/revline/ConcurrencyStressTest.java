package com.revline;

import com.revline.concurrency.AutoSaveThread;
import com.revline.concurrency.ConcurrencyLab;
import com.revline.concurrency.PurchaseRequestQueue;
import com.revline.concurrency.ReservationExpiryThread;
import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.PurchaseRequest;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.repository.FileCarRepository;
import com.revline.repository.FileSaleRepository;
import com.revline.repository.SaleRepository;
import com.revline.service.InventoryService;
import com.revline.service.ReservationManager;
import com.revline.service.SalesService;
import com.revline.service.WaitlistService;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concurrency Test & Stress Suite for Phase 6 (L9–L16, GATE 6).
 * Verifies monitor synchronization, thread lifecycle, producer-consumer queue,
 * and executes 20 consecutive rounds of concurrent double-sell stress tests.
 */
public final class ConcurrencyStressTest {

    private ConcurrencyStressTest() {}

    /**
     * Verifies SafeCounter atomicity under concurrent execution (L13, L14).
     */
    public static void testSafeCounterIncrementIntegrity() throws InterruptedException {
        ConcurrencyLab.SafeCounter counter = new ConcurrencyLab.SafeCounter();
        int threads = 4;
        int incrementsPerThread = 5_000;
        int expected = threads * incrementsPerThread;

        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
            workers.add(t);
            t.start();
        }

        for (Thread t : workers) {
            t.join(2000);
        }

        Assert.assertEquals(expected, counter.getCount(),
                "SafeCounter must equal exactly " + expected + " under concurrent thread access");
    }

    /**
     * Verifies AutoSaveThread daemon status, priority, and clean interruption (L10, L12).
     */
    public static void testAutoSaveThreadInterruption() throws Exception {
        File file = new File("build/test_autosave_cars.csv");
        if (file.exists()) file.delete();
        FileCarRepository repo = new FileCarRepository(file);
        InventoryService inv = new InventoryService(repo, null);

        AutoSaveThread thread = new AutoSaveThread(inv, 10_000L, null);
        Assert.assertTrue(thread.isDaemon(), "AutoSaveThread must be a daemon thread");
        Assert.assertEquals(Thread.MIN_PRIORITY, thread.getPriority(), "AutoSaveThread must have MIN_PRIORITY");

        thread.start();
        Assert.assertTrue(thread.isAlive(), "AutoSaveThread must be alive after start()");

        long start = System.currentTimeMillis();
        thread.shutdown();
        thread.join(2000);
        long elapsed = System.currentTimeMillis() - start;

        Assert.assertTrue(!thread.isAlive(), "AutoSaveThread must terminate after shutdown()");
        Assert.assertTrue(elapsed < 1500, "AutoSaveThread must exit sleep and join within 1.5 seconds");
    }

    /**
     * Verifies ReservationExpiryThread periodic sweep and shutdown (L9, L10).
     */
    public static void testReservationExpiryThreadLifecycle() throws Exception {
        File carsFile = new File("build/test_expiry_cars.csv");
        File salesFile = new File("build/test_expiry_sales.csv");
        if (carsFile.exists()) carsFile.delete();
        if (salesFile.exists()) salesFile.delete();

        FileCarRepository carRepo = new FileCarRepository(carsFile);
        FileSaleRepository saleRepo = new FileSaleRepository(salesFile);
        InventoryService inv = new InventoryService(carRepo, null);
        SalesService salesService = new SalesService(inv, saleRepo, new ReservationManager(), new WaitlistService(), null);

        ReservationExpiryThread worker = new ReservationExpiryThread(salesService, 100L, null);
        Thread t = new Thread(worker, "TestExpiryWorker");
        t.setDaemon(true);
        t.start();

        Thread.sleep(250);
        Assert.assertTrue(worker.getSweepCount() >= 1, "ReservationExpiryThread should perform periodic sweeps");

        worker.shutdown();
        t.interrupt();
        t.join(1000);
        Assert.assertTrue(!t.isAlive(), "ReservationExpiryThread must terminate cleanly upon interrupt");
    }

    /**
     * GATE 6 STRESS TEST: 20 consecutive stress rounds of concurrent double-sell.
     * Proves check-then-act atomicity in SalesService.sellCar() under aggressive contention.
     * Guaranteed: Exactly 1 winner, N-1 rejections, 0 phantom sales, 0 deadlocks across all 20 rounds.
     */
    public static void testDoubleSellStressTwentyRoundsNoDeadlock() throws Exception {
        final int rounds = 20;
        final int buyersPerRound = 15;

        for (int round = 1; round <= rounds; round++) {
            File carsFile = new File("build/test_stress_cars_" + round + ".csv");
            File salesFile = new File("build/test_stress_sales_" + round + ".csv");
            if (carsFile.exists()) carsFile.delete();
            if (salesFile.exists()) salesFile.delete();

            FileCarRepository carRepo = new FileCarRepository(carsFile);
            FileSaleRepository saleRepo = new FileSaleRepository(salesFile);

            Car target = new Car(100 + round, "Bugatti", "Chiron-R" + round, 3000000.0, 420,
                    FuelType.PETROL, 1500, 2.4, Status.AVAILABLE);
            carRepo.save(target);

            InventoryService inv = new InventoryService(carRepo, null);
            SalesService salesService = new SalesService(inv, saleRepo, new ReservationManager(), new WaitlistService(), null);

            List<ConcurrencyLab.BuyerTask> tasks = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();

            for (int b = 1; b <= buyersPerRound; b++) {
                ConcurrencyLab.BuyerTask task = new ConcurrencyLab.BuyerTask(target.getId(), "Buyer-" + b, salesService);
                tasks.add(task);
                threads.add(new Thread(task, "StressBuyer-" + round + "-" + b));
            }

            // Launch all buyers simultaneously
            for (Thread t : threads) {
                t.start();
            }

            // Wait for all buyers to complete
            for (Thread t : threads) {
                t.join(3000); // 3-second watchdog per thread
                Assert.assertTrue(!t.isAlive(), "Round " + round + ": Thread did not finish (deadlock detected)");
            }

            int winners = 0;
            int losers = 0;
            for (ConcurrencyLab.BuyerTask bt : tasks) {
                if (bt.isSucceeded()) {
                    winners++;
                } else {
                    losers++;
                }
            }

            Assert.assertEquals(1, winners, "Round " + round + ": Exactly 1 buyer must succeed in purchasing");
            Assert.assertEquals(buyersPerRound - 1, losers,
                    "Round " + round + ": Exactly " + (buyersPerRound - 1) + " buyers must be rejected");

            Car finalCar = inv.findById(target.getId());
            Assert.assertEquals(Status.SOLD, finalCar.getStatus(),
                    "Round " + round + ": Vehicle status must be moved to SOLD");

            // Clean temporary files
            carsFile.delete();
            salesFile.delete();
        }
    }

    /**
     * STUDENT EXERCISE EX-8 TEST: PurchaseRequestQueue.take()
     */
    public static void testExercise8PurchaseRequestQueueTake() throws Exception {
        PurchaseRequestQueue queue = new PurchaseRequestQueue(3);
        PurchaseRequest req1 = new PurchaseRequest(1, 10, "Aryan", "Agent");
        PurchaseRequest req2 = new PurchaseRequest(2, 20, "Kabir", "Agent");

        queue.put(req1);
        queue.put(req2);

        // This will call take(), which throws UnsupportedOperationException until student implements it
        PurchaseRequest polled1 = queue.take();
        Assert.assertEquals(1, polled1.getRequestId(), "EX-8: First polled request must match first enqueued (FIFO)");

        PurchaseRequest polled2 = queue.take();
        Assert.assertEquals(2, polled2.getRequestId(), "EX-8: Second polled request must match second enqueued (FIFO)");

        Assert.assertEquals(0, queue.size(), "EX-8: Queue must be empty after taking both elements");
    }
}
