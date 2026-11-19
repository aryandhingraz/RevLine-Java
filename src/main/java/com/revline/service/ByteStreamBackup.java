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
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + sourceFile);
        }
        File parent = backupFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
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
        if (backupFile == null || !backupFile.exists()) {
            throw new IllegalArgumentException("Backup file does not exist: " + backupFile);
        }
        backupFile(backupFile, targetFile, null);
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
