package com.revline.service;

import com.revline.model.Reservation;
import com.revline.util.Validator;
import com.revline.util.exception.CarNotAvailableException;
import com.revline.util.exception.InvalidCarException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Manages temporary vehicle holds with automated priority expiration.
 * Demonstrates min-heap PriorityQueue and Iterator.remove() (L6, L7 Collections).
 */
public class ReservationManager {

    // Min-heap ordered by expiration time (earliest expiration polled first)
    private final PriorityQueue<Reservation> queue = new PriorityQueue<>();
    // O(1) direct lookup by car ID
    private final Map<Integer, Reservation> byCarId = new HashMap<>();

    public ReservationManager() {}

    /**
     * Creates a time-bounded reservation on a vehicle.
     *
     * @param carId car identifier
     * @param buyerName name of reserving customer
     * @param durationMinutes reservation lifespan in minutes
     * @return newly established Reservation record
     * @throws CarNotAvailableException if vehicle is already under active reservation
     */
    public synchronized Reservation reserve(int carId, String buyerName, int durationMinutes)
            throws CarNotAvailableException {
        if (carId <= 0) {
            throw new InvalidCarException("Car ID must be positive.");
        }
        Validator.requireNonBlank(buyerName, "Buyer Name");
        if (durationMinutes <= 0) {
            throw new InvalidCarException("Reservation duration must be positive minutes.");
        }

        // Clean any expired reservations first
        purgeExpired();

        Reservation existing = byCarId.get(carId);
        if (existing != null && !existing.isExpired()) {
            throw new CarNotAvailableException(carId,
                    "Vehicle is already reserved by " + existing.getBuyerName() + " until " + existing.getExpiresAt());
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(durationMinutes);
        Reservation reservation = new Reservation(carId, buyerName.trim(), expiresAt);

        queue.offer(reservation);
        byCarId.put(carId, reservation);

        return reservation;
    }

    /**
     * Finds active reservation for a car, or null if none or expired.
     */
    public synchronized Reservation getReservation(int carId) {
        Reservation res = byCarId.get(carId);
        if (res == null) return null;
        if (res.isExpired()) {
            // Clean up expired entry
            byCarId.remove(carId);
            queue.remove(res);
            return null;
        }
        return res;
    }

    /**
     * Checks if a car is currently reserved by anyone.
     */
    public synchronized boolean isReserved(int carId) {
        return getReservation(carId) != null;
    }

    /**
     * Cancels an active reservation, verifying buyer identity.
     */
    public synchronized Reservation cancel(int carId, String buyerName) throws CarNotAvailableException {
        Reservation res = getReservation(carId);
        if (res == null) {
            throw new CarNotAvailableException(carId, "No active reservation found for car #" + carId);
        }
        if (buyerName != null && !res.getBuyerName().equalsIgnoreCase(buyerName.trim())) {
            throw new CarNotAvailableException(carId,
                    "Reservation was placed by '" + res.getBuyerName() + "', not '" + buyerName + "'.");
        }

        byCarId.remove(carId);
        queue.remove(res);
        return res;
    }

    /**
     * Directly removes reservation without verification (e.g. upon sale completion).
     */
    public synchronized Reservation remove(int carId) {
        Reservation res = byCarId.remove(carId);
        if (res != null) {
            queue.remove(res);
        }
        return res;
    }

    /**
     * Direct registration method primarily used for testing or persistence hydration.
     */
    public synchronized void addReservationDirectly(Reservation reservation) {
        if (reservation != null) {
            queue.offer(reservation);
            byCarId.put(reservation.getCarId(), reservation);
        }
    }

    /**
     * Purges all expired holds using an explicit Iterator.remove() (L6-7).
     *
     * @return list of car IDs that have been released from expired reservations
     */
    public synchronized List<Integer> purgeExpired() {
        List<Integer> expiredCarIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Syllabus requirement: explicit use of Iterator and Iterator.remove()
        Iterator<Reservation> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Reservation res = iterator.next();
            if (now.isAfter(res.getExpiresAt())) {
                expiredCarIds.add(res.getCarId());
                byCarId.remove(res.getCarId());
                iterator.remove();
            }
        }
        return expiredCarIds;
    }

    /**
     * Returns an unmodifiable snapshot of all current active reservations.
     */
    public synchronized List<Reservation> getAllActive() {
        purgeExpired();
        return Collections.unmodifiableList(new ArrayList<>(byCarId.values()));
    }

    public synchronized int count() {
        purgeExpired();
        return byCarId.size();
    }

    public synchronized void clear() {
        queue.clear();
        byCarId.clear();
    }
}
