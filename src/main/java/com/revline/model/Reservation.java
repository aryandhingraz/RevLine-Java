package com.revline.model;

import com.revline.util.exception.InvalidCarException;

import java.time.LocalDateTime;

/**
 * Temporary vehicle reservation held by a buyer.
 * Ordered naturally by expiration time (earliest first) to feed a PriorityQueue.
 */
public class Reservation implements Comparable<Reservation> {

    private final int carId;
    private final String buyerName;
    private final LocalDateTime expiresAt;

    public Reservation(int carId, String buyerName, LocalDateTime expiresAt) {
        if (carId <= 0) {
            throw new InvalidCarException("Car ID must be positive.");
        }
        if (buyerName == null || buyerName.trim().isEmpty()) {
            throw new InvalidCarException("Buyer name cannot be blank.");
        }
        if (expiresAt == null) {
            throw new InvalidCarException("Expiration time cannot be null.");
        }

        this.carId = carId;
        this.buyerName = buyerName.trim();
        this.expiresAt = expiresAt;
    }

    public int getCarId() {
        return carId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @Override
    public int compareTo(Reservation other) {
        return this.expiresAt.compareTo(other.expiresAt);
    }

    @Override
    public String toString() {
        return String.format("Reservation[Car #%d for '%s', expires at %s]", carId, buyerName, expiresAt);
    }
}
