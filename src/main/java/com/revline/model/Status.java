package com.revline.model;

/**
 * Lifecycle status of a vehicle in the showroom.
 * Follows a finite state machine:
 * AVAILABLE -> RESERVED or SOLD
 * RESERVED  -> AVAILABLE or SOLD
 * SOLD      -> terminal state (no further transitions)
 */
public enum Status {
    AVAILABLE,
    RESERVED,
    SOLD;

    /**
     * Checks whether transitioning from this status to next status is permitted.
     *
     * @param next target status
     * @return true if permitted, false otherwise
     */
    public boolean canTransitionTo(Status next) {
        if (next == null || this == next) {
            return false;
        }
        if (this == AVAILABLE) {
            return next == RESERVED || next == SOLD;
        }
        if (this == RESERVED) {
            return next == AVAILABLE || next == SOLD;
        }
        // SOLD is terminal
        return false;
    }

    /**
     * Parses a string to Status case-insensitively.
     */
    public static Status fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Status text cannot be blank.");
        }
        String clean = text.trim().toUpperCase();
        for (Status s : values()) {
            if (s.name().equals(clean)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: '" + text + "'. Allowed: AVAILABLE, RESERVED, SOLD.");
    }
}
