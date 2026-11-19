package com.revline.concurrency;

import com.revline.model.PurchaseRequest;
import com.revline.model.Sale;
import com.revline.service.SalesService;
import com.revline.util.AppLogger;
import com.revline.util.exception.RevLineException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Consumer worker in the producer-consumer pipeline (L15, L16).
 * Dequeues requests via PurchaseRequestQueue.take() and executes sales transactions atomically.
 */
public class SalesProcessor implements Runnable {

    private final PurchaseRequestQueue queue;
    private final SalesService salesService;
    private final AppLogger logger;
    private final List<Sale> processedSales = Collections.synchronizedList(new ArrayList<>());
    private final List<String> processingErrors = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean running = true;

    public SalesProcessor(PurchaseRequestQueue queue, SalesService salesService, AppLogger logger) {
        if (queue == null) throw new IllegalArgumentException("Queue cannot be null.");
        if (salesService == null) throw new IllegalArgumentException("SalesService cannot be null.");
        this.queue = queue;
        this.salesService = salesService;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (running) {
            try {
                PurchaseRequest request = queue.take();
                if (request == null) {
                    break;
                }
                try {
                    Sale sale = salesService.sellCar(request.getCarId(), request.getBuyerName(), request.getSellerName());
                    processedSales.add(sale);
                    if (logger != null) {
                        logger.log("SALES_PROCESSOR", "PROCESSED",
                                String.format("[%s] Successfully executed Sale #%d for Car #%d",
                                        Thread.currentThread().getName(), sale.getSaleId(), request.getCarId()));
                    }
                } catch (RevLineException e) {
                    processingErrors.add("Request #" + request.getRequestId() + " failed: " + e.getMessage());
                    if (logger != null) {
                        logger.log("SALES_PROCESSOR", "FAILED",
                                String.format("[%s] Request #%d rejected: %s",
                                        Thread.currentThread().getName(), request.getRequestId(), e.getMessage()));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (UnsupportedOperationException e) {
                // EX-8 pending implementation by student
                break;
            } catch (Throwable t) {
                processingErrors.add("Unexpected consumer error: " + t.getMessage());
                break;
            }
        }
    }

    public void stop() {
        this.running = false;
    }

    public List<Sale> getProcessedSales() {
        return new ArrayList<>(processedSales);
    }

    public List<String> getProcessingErrors() {
        return new ArrayList<>(processingErrors);
    }
}
