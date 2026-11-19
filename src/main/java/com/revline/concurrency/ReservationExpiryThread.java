package com.revline.concurrency;

import com.revline.service.SalesService;
import com.revline.util.AppLogger;

import java.util.List;

/**
 * Background daemon worker continuously sweeping for expired reservation holds (L9–L12).
 * Runs on a periodic timer and restores expired vehicles to AVAILABLE.
 */
public class ReservationExpiryThread implements Runnable {

    public static final long DEFAULT_INTERVAL_MS = 5_000L;

    private final SalesService salesService;
    private final long intervalMs;
    private final AppLogger logger;
    private volatile boolean running = true;
    private int sweepCount = 0;

    public ReservationExpiryThread(SalesService salesService, AppLogger logger) {
        this(salesService, DEFAULT_INTERVAL_MS, logger);
    }

    public ReservationExpiryThread(SalesService salesService, long intervalMs, AppLogger logger) {
        if (salesService == null) {
            throw new IllegalArgumentException("SalesService cannot be null.");
        }
        if (intervalMs <= 0) {
            throw new IllegalArgumentException("Interval must be positive.");
        }
        this.salesService = salesService;
        this.intervalMs = intervalMs;
        this.logger = logger;
    }

    @Override
    public void run() {
        if (logger != null) {
            logger.log("RESERVATION_EXPIRY", "START", "ReservationExpiryThread started with interval " + intervalMs + " ms");
        }

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (!running || Thread.currentThread().isInterrupted()) {
                break;
            }

            sweepCount++;
            List<Integer> expiredIds = salesService.purgeExpiredReservations();
            if (!expiredIds.isEmpty() && logger != null) {
                logger.log("RESERVATION_EXPIRY", "PURGED", "Sweep #" + sweepCount + " purged expired reservations on cars: " + expiredIds);
            }
        }

        if (logger != null) {
            logger.log("RESERVATION_EXPIRY", "STOP", "ReservationExpiryThread shutdown complete");
        }
    }

    public void shutdown() {
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public int getSweepCount() {
        return sweepCount;
    }
}
