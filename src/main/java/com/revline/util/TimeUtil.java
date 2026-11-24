package com.revline.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Shared utility for generating standardized date-time timestamps (L28).
 * Uses java.time (LocalDateTime and DateTimeFormatter) instead of legacy java.util.Date.
 */
public final class TimeUtil {

    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private TimeUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Returns a formatted timestamp string suitable for safe filenames (e.g. 20261124_174500).
     */
    public static String fileTimestamp() {
        return LocalDateTime.now().format(FILE_STAMP);
    }
}
