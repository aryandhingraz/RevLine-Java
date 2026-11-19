package com.revline.app;

import com.revline.model.Car;
import com.revline.model.Sale;
import com.revline.repository.DatabaseManager;
import com.revline.repository.FileCarRepository;
import com.revline.repository.FileSaleRepository;
import com.revline.repository.JdbcCarRepository;
import com.revline.repository.JdbcSaleRepository;
import com.revline.repository.SchemaInitializer;
import com.revline.util.FileUtil;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.sql.Connection;
import java.util.List;

/**
 * Ensures required storage directories, flat-files, and SQLite relational database exist on startup (L17-L20, L26-L27).
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
     * Verifies presence of runtime CSV database files; copies seed data if missing.
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

    /**
     * Initializes the SQLite relational database schema and auto-seeds initial records.
     *
     * @return active initialized database connection
     * @throws RepositoryException if initialization fails
     */
    public static Connection initDatabase() throws RepositoryException {
        Connection conn = DatabaseManager.getConnection();
        SchemaInitializer.init(conn);
        return conn;
    }

    /**
     * Migrates records from CSV flat-files into the SQLite relational database (L17-20).
     */
    public static int migrateCsvToJdbc(FileCarRepository csvCarRepo,
                                       FileSaleRepository csvSaleRepo,
                                       JdbcCarRepository jdbcCarRepo,
                                       JdbcSaleRepository jdbcSaleRepo) throws RepositoryException {
        List<Car> csvCars = csvCarRepo.findAll();
        jdbcCarRepo.saveAll(csvCars);

        List<Sale> csvSales = csvSaleRepo.findAll();
        jdbcSaleRepo.saveAll(csvSales);

        return csvCars.size() + csvSales.size();
    }
}
