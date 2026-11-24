package com.revline.service;

import com.revline.util.AppLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Byte-stream binary backup, restoration, and identity verification (L25).
 */
final class ByteStreamBackup {

    private ByteStreamBackup() {}

    static void backupFile(File sourceFile, File backupFile, AppLogger logger) throws IOException {
        if (sourceFile == null || !sourceFile.isFile()) {
            throw new IllegalArgumentException("Source file does not exist: " + sourceFile);
        }
        if (backupFile == null) {
            throw new IllegalArgumentException("Backup destination cannot be null.");
        }
        if (sourceFile.getCanonicalFile().equals(backupFile.getCanonicalFile())) {
            throw new IllegalArgumentException("Source and backup destination must be different files.");
        }
        File parent = backupFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create backup directory: " + parent.getPath());
        }

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(backupFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        }

        if (logger != null) {
            logger.log("DATA_TOOLS", "BACKUP", "Created byte backup: " + backupFile.getName());
        }
    }

    static void restoreFile(File backupFile, File targetFile, AppLogger logger) throws IOException {
        if (backupFile == null || !backupFile.isFile()) {
            throw new IllegalArgumentException("Backup file does not exist: " + backupFile);
        }
        if (targetFile == null) {
            throw new IllegalArgumentException("Restore target cannot be null.");
        }
        if (backupFile.getCanonicalFile().equals(targetFile.getCanonicalFile())) {
            throw new IllegalArgumentException("Backup and restore target must be different files.");
        }

        File parent = targetFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create restore directory: " + parent.getPath());
        }

        File temporary = new File(targetFile.getAbsolutePath() + ".restore.tmp");
        File previous = new File(targetFile.getAbsolutePath() + ".restore.bak");
        if (temporary.exists() && !temporary.delete()) {
            throw new IOException("Could not remove an older temporary restore file.");
        }
        if (previous.exists() && !previous.delete()) {
            throw new IOException("Could not remove an older restore backup.");
        }

        // Prepare the replacement before touching the active target.
        backupFile(backupFile, temporary, null);

        boolean hadOriginal = targetFile.exists();
        if (hadOriginal && !targetFile.renameTo(previous)) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            if (!targetFile.renameTo(previous)) {
                temporary.delete();
                throw new IOException("Could not preserve the current target before restore: " + targetFile.getName());
            }
        }

        if (!temporary.renameTo(targetFile)) {
            if (hadOriginal && !previous.renameTo(targetFile)) {
                throw new IOException("Restore failed and the previous target could not be recovered.");
            }
            throw new IOException("Could not place the restored file at " + targetFile.getPath());
        }

        if (previous.exists()) {
            previous.delete();
        }
        if (logger != null) {
            logger.log("DATA_TOOLS", "RESTORE", "Restored file from: " + backupFile.getName());
        }
    }

    static boolean areFilesByteIdentical(File file1, File file2) throws IOException {
        if (file1 == null || file2 == null || !file1.exists() || !file2.exists()) {
            return false;
        }
        if (file1.length() != file2.length()) {
            return false;
        }

        try (FileInputStream fis1 = new FileInputStream(file1);
             FileInputStream fis2 = new FileInputStream(file2)) {
            int b1, b2;
            while ((b1 = fis1.read()) != -1) {
                b2 = fis2.read();
                if (b1 != b2) return false;
            }
            return fis2.read() == -1;
        }
    }
}
