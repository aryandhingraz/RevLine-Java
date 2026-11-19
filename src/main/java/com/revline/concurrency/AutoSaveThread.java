package com.revline.concurrency;

import com.revline.service.InventoryService;
import com.revline.util.AppLogger;
import com.revline.util.exception.RepositoryException;

/**
 * Background daemon thread performing periodic inventory snapshots (L9–L12).
 * Runs at MIN_PRIORITY, sleeps in intervals, and terminates cleanly upon interrupt().
 */
public final class AutoSaveThread extends Thread {

    public static final long DEFAULT_INTERVAL_MS = 30_000L;

    private final InventoryService inventoryService;
    private final long intervalMs;
    private final AppLogger logger;
    private volatile boolean running = true;
    private int saveCount = 0;

    public AutoSaveThread(InventoryService inventoryService, AppLogger logger) {
        this(inventoryService, DEFAULT_INTERVAL_MS, logger);
    }

    public AutoSaveThread(InventoryService inventoryService, long intervalMs, AppLogger logger) {
        super("AutoSaveThread");
        if (inventoryService == null) {
            throw new IllegalArgumentException("InventoryService cannot be null.");
        }
        if (intervalMs <= 0) {
            throw new IllegalArgumentException("Interval must be positive.");
        }
        this.inventoryService = inventoryService;
        this.intervalMs = intervalMs;
        this.logger = logger;

        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
        if (logger != null) {
            logger.log("AUTOSAVE", "START", "AutoSaveThread started with interval " + intervalMs + " ms");
        }

        while (running && !isInterrupted()) {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                // Restore interrupt status and exit loop cleanly (L10)
                Thread.currentThread().interrupt();
                break;
            }

            if (!running || isInterrupted()) {
                break;
            }

            try {
                inventoryService.saveSnapshot();
                saveCount++;
                if (logger != null) {
                    logger.log("AUTOSAVE", "SUCCESS", "AutoSave snapshot #" + saveCount + " persisted successfully");
                }
            } catch (RepositoryException e) {
                if (logger != null) {
                    logger.log("AUTOSAVE", "ERROR", "AutoSave snapshot failed: " + e.getMessage());
                }
            }
        }

        if (logger != null) {
            logger.log("AUTOSAVE", "STOP", "AutoSaveThread shutdown complete");
        }
    }

    /**
     * Gracefully signals the thread to stop and interrupts sleep (L10).
     */
    public void shutdown() {
        this.running = false;
        interrupt();
    }

    public boolean isRunning() {
        return running;
    }

    public int getSaveCount() {
        return saveCount;
    }
}
