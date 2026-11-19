package com.revline.app;

import com.revline.util.FileUtil;
import com.revline.util.exception.RepositoryException;

import java.io.File;

/**
 * Ensures required storage directories and seed data exist on startup.
 */
public final class StorageBootstrap {

    public static final String RUNTIME_CARS_PATH = "data/cars.csv";
    public static final String SEED_CARS_PATH = "data/seed/cars.csv";

    public static final String RUNTIME_SALES_PATH = "data/sales.csv";
    public static final String SEED_SALES_PATH = "data/seed/sales.csv";

    private StorageBootstrap() {}

    public static class BootInfo {
        public final boolean wasCarsSeeded;
        public final boolean wasSalesSeeded;
        public final File carsFile;
        public final File salesFile;

        public BootInfo(boolean wasCarsSeeded, boolean wasSalesSeeded, File carsFile, File salesFile) {
            this.wasCarsSeeded = wasCarsSeeded;
            this.wasSalesSeeded = wasSalesSeeded;
            this.carsFile = carsFile;
            this.salesFile = salesFile;
        }
    }

    /**
     * Verifies presence of runtime database files; copies seed data if missing.
     */
    public static BootInfo init() throws RepositoryException {
        File carsFile = new File(RUNTIME_CARS_PATH);
        File seedCarsFile = new File(SEED_CARS_PATH);

        boolean carsSeeded = false;
        if (!carsFile.exists() || carsFile.length() == 0) {
            if (seedCarsFile.exists()) {
                FileUtil.copyBytes(seedCarsFile, carsFile);
                carsSeeded = true;
            } else {
                throw new RepositoryException("Neither runtime cars.csv nor seed/cars.csv could be found!");
            }
        }

        File salesFile = new File(RUNTIME_SALES_PATH);
        File seedSalesFile = new File(SEED_SALES_PATH);

        boolean salesSeeded = false;
        if (!salesFile.exists() || salesFile.length() == 0) {
            if (seedSalesFile.exists()) {
                FileUtil.copyBytes(seedSalesFile, salesFile);
                salesSeeded = true;
            } else {
                // If seed sales not found, initialize with header
                FileUtil.safeWrite(salesFile, java.util.Collections.singletonList("saleId,carId,buyerName,sellerName,saleDate,price"));
                salesSeeded = true;
            }
        }

        return new BootInfo(carsSeeded, salesSeeded, carsFile, salesFile);
    }
}
