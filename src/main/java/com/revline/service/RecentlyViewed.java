package com.revline.service;

import com.revline.model.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Least Recently Used (LRU) showroom browsing cache.
 * Demonstrates LinkedHashMap with access-order eviction (L6, L7 Collections).
 */
public class RecentlyViewed extends LinkedHashMap<Integer, Car> {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_CAPACITY = 5;

    private final int maxCapacity;

    public RecentlyViewed() {
        this(DEFAULT_CAPACITY);
    }

    public RecentlyViewed(int maxCapacity) {
        // initialCapacity = 16, loadFactor = 0.75f, accessOrder = true (LRU mode)
        super(16, 0.75f, true);
        this.maxCapacity = (maxCapacity > 0) ? maxCapacity : DEFAULT_CAPACITY;
    }

    /**
     * Evicts the least recently accessed vehicle whenever cache size exceeds maxCapacity.
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Car> eldest) {
        return size() > maxCapacity;
    }

    /**
     * Records a car viewing action. Accessing or inserting refreshes its LRU recency.
     *
     * @param car the viewed car
     */
    public synchronized void recordView(Car car) {
        if (car == null) return;
        // Re-putting the entry triggers access-order refresh and eviction check
        put(car.getId(), car.copy());
    }

    /**
     * Returns the recently viewed cars in reverse chronological order (newest viewed first).
     */
    public synchronized List<Car> getRecentlyViewedList() {
        List<Car> list = new ArrayList<>(values());
        Collections.reverse(list); // Newest first
        return Collections.unmodifiableList(list);
    }
}
