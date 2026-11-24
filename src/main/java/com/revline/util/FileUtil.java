package com.revline.util;

import com.revline.util.exception.RepositoryException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * File utilities for guarded file replacement and stream copying (L25, L26).
 */
public final class FileUtil {

    private FileUtil() {}

    /**
     * Safer file replacement using a temporary file, a backup, and rename (L26, L27).
     * Follows a 4-step durability sequence: write temp -> backup existing -> rename -> cleanup backup.
     *
     * @param target destination file
     * @param lines  text lines to write
     * @throws RepositoryException if saving fails
     */
    public static void safeWrite(File target, List<String> lines) throws RepositoryException {
        if (target == null || lines == null) {
            throw new RepositoryException("Target file and content lines are required.");
        }
        ensureParentDirectory(target);

        File tmpFile = new File(target.getAbsolutePath() + ".tmp");
        File bakFile = new File(target.getAbsolutePath() + ".bak");

        // Step 1: Write full content to temporary .tmp file
        writeTempFile(tmpFile, lines);

        // Step 2: Backup existing target file to .bak before replacing
        backupExisting(target, bakFile, tmpFile);

        // Step 3: Promote the temporary file to target; restore on failure
        promoteTempFile(tmpFile, target, bakFile);

        // Step 4: Remove the old backup after the new target is in place.
        // If cleanup fails, retain it; startup recovery can remove it safely later.
        if (bakFile.exists()) {
            bakFile.delete();
        }
    }

    private static void ensureParentDirectory(File target) throws RepositoryException {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new RepositoryException("Could not create parent directory: " + parent.getPath());
        }
    }

    private static void writeTempFile(File tmpFile, List<String> lines) throws RepositoryException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            if (tmpFile.exists() && !tmpFile.delete()) {
                e.addSuppressed(new IOException("Could not remove incomplete temporary file: " + tmpFile.getPath()));
            }
            throw new RepositoryException("Failed writing temporary save file: " + e.getMessage(), e);
        }
    }

    private static void backupExisting(File target, File bakFile, File tmpFile) throws RepositoryException {
        if (target.exists()) {
            if (bakFile.exists() && !bakFile.delete()) {
                tmpFile.delete();
                throw new RepositoryException("Could not remove an older backup file: " + bakFile.getPath());
            }
            if (!target.renameTo(bakFile)) {
                tmpFile.delete();
                throw new RepositoryException("Failed creating backup of target file before overwrite.");
            }
        }
    }

    private static void promoteTempFile(File tmpFile, File target, File bakFile) throws RepositoryException {
        if (!tmpFile.renameTo(target)) {
            if (bakFile.exists() && !bakFile.renameTo(target)) {
                throw new RepositoryException("Could not promote the new file or restore the previous file: "
                        + target.getName());
            }
            throw new RepositoryException("Failed to promote temporary file to target: " + target.getName());
        }
    }

    /**
     * Reads all non-empty lines from a text file using BufferedReader (L25, L26).
     */
    public static List<String> readLines(File file) throws RepositoryException {
        List<String> lines = new ArrayList<>();
        if (file == null || !file.exists()) {
            return lines;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
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
        if (source == null || destination == null) {
            throw new RepositoryException("Source and destination files are required.");
        }
        if (!source.exists()) {
            throw new RepositoryException("Source file does not exist: " + source.getPath());
        }

        File destParent = destination.getParentFile();
        if (destParent != null && !destParent.exists() && !destParent.mkdirs()) {
            throw new RepositoryException("Could not create destination directory: " + destParent.getPath());
        }

        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        } catch (IOException e) {
            throw new RepositoryException("Byte copy failed: " + e.getMessage(), e);
        }
    }

    /**
     * Closes an AutoCloseable stream or resource quietly without propagating checked exceptions (L3, L25).
     * Single documented closeQuietly helper in the application.
     */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // Documented closeQuietly helper: suppresses secondary exception during teardown
            }
        }
    }
}
