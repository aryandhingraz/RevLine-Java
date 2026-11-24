package com.revline.util;

/**
 * Centralized constant definitions for application file system paths and folders (L26, L27).
 * Eliminates repeated hard-coded path strings and guarantees directory consistency.
 */
public final class AppPaths {

    public static final String DATA_DIR = "data";
    public static final String SEED_DIR = DATA_DIR + "/seed";
    public static final String SAMPLE_DIR = DATA_DIR + "/sample";
    public static final String LEGACY_DIR = DATA_DIR + "/legacy";
    public static final String BACKUP_DIR = DATA_DIR + "/backups";
    public static final String EXPORT_DIR = DATA_DIR + "/exports";
    public static final String REPORT_DIR = DATA_DIR + "/reports";

    public static final String SEED_CARS = SEED_DIR + "/cars.csv";
    public static final String SEED_SALES = SEED_DIR + "/sales.csv";
    public static final String RUNTIME_CARS = DATA_DIR + "/cars.csv";
    public static final String RUNTIME_SALES = DATA_DIR + "/sales.csv";
    public static final String RUNTIME_DB = DATA_DIR + "/revline.db";
    public static final String LEGACY_CARS = LEGACY_DIR + "/cars.txt";

    public static final String LOGS_DIR = "logs";
    public static final String AUDIT_LOG = LOGS_DIR + "/audit.log";
    public static final String ERROR_LOG = LOGS_DIR + "/error.log";


    private AppPaths() {
        // Prevent instantiation of utility constants class
    }
}
