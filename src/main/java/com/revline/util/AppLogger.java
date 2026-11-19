package com.revline.util;

import com.revline.model.AuditEntry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe audit logger using FileWriter and StringBuffer (L2, L25).
 * Writes events to logs/audit.log and maintains an in-memory buffer of recent events.
 */
public class AppLogger {

    private static final String DEFAULT_LOG_PATH = "logs/audit.log";
    private static final int MAX_BUFFER_LINES = 20;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static AppLogger instance;

    private final File logFile;
    // StringBuffer demonstrates thread-safe mutable string buffer from Lecture 2
    private final StringBuffer recentBuffer;
    private final List<String> recentLines;

    public AppLogger() {
        this(new File(DEFAULT_LOG_PATH));
    }

    public AppLogger(File logFile) {
        this.logFile = logFile;
        this.recentBuffer = new StringBuffer();
        this.recentLines = new ArrayList<>();
        ensureLogDir();
    }

    public static synchronized AppLogger getInstance() {
        if (instance == null) {
            instance = new AppLogger();
        }
        return instance;
    }

    /**
     * Records a structured action to the log file and the recent activity buffer.
     * Synchronized to guarantee sequential, ungarbled multi-thread writing (L13).
     */
    public synchronized void log(String actor, String action, String details) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String line = String.format("%s | %-8s | %-16s | %s", timestamp, actor, action, details);

        // 1. Append to disk log
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("[WARN] Failed to write to audit log: " + e.getMessage());
        }

        // 2. Maintain recent memory buffer
        recentLines.add(line);
        if (recentLines.size() > MAX_BUFFER_LINES) {
            recentLines.remove(0);
        }

        // Rebuild recentBuffer
        recentBuffer.setLength(0);
        for (String l : recentLines) {
            recentBuffer.append(l).append(System.lineSeparator());
        }
    }

    public void log(AuditEntry entry) {
        if (entry != null) {
            log(entry.getActor(), entry.getAction(), entry.getDetails());
        }
    }

    /**
     * Returns a snapshot of the last 20 log events.
     */
    public synchronized String getRecentActivity() {
        return recentBuffer.toString();
    }

    /**
     * Returns a copy of recent log entries as a list of strings.
     */
    public synchronized List<String> getRecentLines() {
        return new ArrayList<>(recentLines);
    }

    public File getLogFile() {
        return logFile;
    }

    private void ensureLogDir() {
        File parent = logFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
