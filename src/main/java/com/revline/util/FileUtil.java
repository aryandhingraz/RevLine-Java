package com.revline.util;

import com.revline.util.exception.RepositoryException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * File utilities for crash-safe writes and stream copying (L25, L26).
 */
public final class FileUtil {

    private FileUtil() {}

    /**
     * Crash-safe atomic file write using temp file and rename (ADR-005, Objective O5).
     *
     * @param target destination file
     * @param lines  text lines to write
     * @throws RepositoryException if saving fails
     */
    public static void safeWrite(File target, List<String> lines) throws RepositoryException {
        if (target == null) {
            throw new RepositoryException("Target file cannot be null.");
        }

        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        File tmpFile = new File(target.getAbsolutePath() + ".tmp");
        File bakFile = new File(target.getAbsolutePath() + ".bak");

        // 1. Write full content to .tmp file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            throw new RepositoryException("Failed writing temporary save file: " + e.getMessage(), e);
        }

        // 2. Backup existing file if present
        if (target.exists()) {
            if (bakFile.exists()) {
                bakFile.delete();
            }
            if (!target.renameTo(bakFile)) {
                tmpFile.delete();
                throw new RepositoryException("Failed creating backup of target file before overwrite.");
            }
        }

        // 3. Atomically rename .tmp to target
        if (!tmpFile.renameTo(target)) {
            // Restore from backup if rename failed
            if (bakFile.exists()) {
                bakFile.renameTo(target);
            }
            throw new RepositoryException("Failed to atomically promote temp file to target: " + target.getName());
        }

        // 4. Success: delete temporary backup
        if (bakFile.exists()) {
            bakFile.delete();
        }
    }

    /**
     * Reads all non-empty lines from a text file using BufferedReader (L25, L26).
     */
    public static List<String> readLines(File file) throws RepositoryException {
        List<String> lines = new ArrayList<>();
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RepositoryException("Error reading file: " + file.getName(), e);
        }
        return lines;
    }

    /**
     * Copies binary files using byte streams and a 4KB buffer (L25).
     */
    public static void copyBytes(File source, File destination) throws RepositoryException {
        if (!source.exists()) {
            throw new RepositoryException("Source file does not exist: " + source.getPath());
        }

        File destParent = destination.getParentFile();
        if (destParent != null && !destParent.exists()) {
            destParent.mkdirs();
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(destination);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        } catch (IOException e) {
            throw new RepositoryException("Byte copy failed: " + e.getMessage(), e);
        } finally {
            // Explicit finally block resource closing (L3, L25)
            if (fis != null) {
                try { fis.close(); } catch (IOException ignored) {}
            }
            if (fos != null) {
                try { fos.close(); } catch (IOException ignored) {}
            }
        }
    }
}
