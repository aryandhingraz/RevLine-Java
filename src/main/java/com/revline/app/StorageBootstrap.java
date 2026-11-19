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

    private StorageBootstrap() {}

    public static class BootInfo {
        public final boolean wasSeeded;
        public final File dataFile;

        public BootInfo(boolean wasSeeded, File dataFile) {
            this.wasSeeded = wasSeeded;
            this.dataFile = dataFile;
        }
    }

    /**
     * Verifies presence of runtime database file; copies seed data if missing.
     */
    public static BootInfo init() throws RepositoryException {
        File runtimeFile = new File(RUNTIME_CARS_PATH);
        File seedFile = new File(SEED_CARS_PATH);

        boolean seeded = false;
        if (!runtimeFile.exists() || runtimeFile.length() == 0) {
            if (seedFile.exists()) {
                FileUtil.copyBytes(seedFile, runtimeFile);
                seeded = true;
            } else {
                throw new RepositoryException("Neither runtime cars.csv nor seed/cars.csv could be found!");
            }
        }

        return new BootInfo(seeded, runtimeFile);
    }
}
