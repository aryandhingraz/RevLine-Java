package com.revline.model;

import java.time.LocalDateTime;

/**
 * Single recorded audit event for security and accountability.
 */
public class AuditEntry {

    private final LocalDateTime time;
    private final String actor;
    private final String action;
    private final String details;

    public AuditEntry(LocalDateTime time, String actor, String action, String details) {
        this.time = (time != null) ? time : LocalDateTime.now();
        this.actor = (actor != null) ? actor : "SYSTEM";
        this.action = (action != null) ? action : "UNKNOWN";
        this.details = (details != null) ? details : "";
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public String toLogLine() {
        return String.format("%s | %s | %s | %s", time, actor, action, details);
    }

    @Override
    public String toString() {
        return toLogLine();
    }
}
