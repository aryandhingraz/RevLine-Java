package com.revline.util;

import com.revline.model.Car;
import com.revline.model.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility library demonstrating generic methods, bounded type parameters,
 * and wildcard bounds (L24 Generics).
 */
public final class GenericUtils {

    private GenericUtils() {}

    /**
     * Finds the maximum element in a list using natural ordering (L24 Bounded Type Parameter).
     *
     * @param list non-empty list of comparable elements
     * @param <T>  type implementing Comparable
     * @return maximum element, or null if list is empty or null
     */
    public static <T extends Comparable<T>> T maxOf(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        T max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            T candidate = list.get(i);
            if (candidate != null && (max == null || candidate.compareTo(max) > 0)) {
                max = candidate;
            }
        }
        return max;
    }

    /**
     * Displays descriptions for vehicles using an upper-bounded wildcard (L24 Wildcards).
     * Demonstrates read-only access to sub-types of Vehicle by writing to the provided PrintStream.
     *
     * @param vehicles list of objects extending Vehicle
     * @param out target output destination
     */
    public static void printAll(List<? extends Vehicle> vehicles, java.io.PrintStream out) {
        if (vehicles == null || out == null) return;
        for (Vehicle v : vehicles) {
            if (v != null) {
                out.println(v.describe());
            }
        }
    }

    /**
     * Converts a list of Pairs into a standard Map.
     */
    public static <K, V> Map<K, V> toMap(List<Pair<K, V>> pairs) {
        if (pairs == null) return Collections.emptyMap();
        Map<K, V> map = new HashMap<>();
        for (Pair<K, V> p : pairs) {
            if (p != null) {
                map.put(p.getFirst(), p.getSecond());
            }
        }
        return map;
    }

    /**
     * Constructs a price ladder using a TreeMap to sort cars ascending by price tier (L5, L24).
     *
     * @param cars list of cars
     * @return TreeMap mapping price to list of cars at that price point
     */
    public static TreeMap<Double, List<Car>> buildPriceLadder(List<Car> cars) {
        TreeMap<Double, List<Car>> ladder = new TreeMap<>();
        if (cars == null) return ladder;

        for (Car c : cars) {
            if (c != null) {
                ladder.computeIfAbsent(c.getPrice(), k -> new ArrayList<>()).add(c.copy());
            }
        }
        return ladder;
    }

    /**
     * Preserves insertion order while eliminating duplicates using LinkedHashSet (L5, L24).
     *
     * @param list input list
     * @param <T>  element type
     * @return distinct list preserving first-seen insertion order
     */
    public static <T> List<T> uniquePreservingOrder(List<T> list) {
        if (list == null) return Collections.emptyList();
        return new ArrayList<>(new java.util.LinkedHashSet<>(list));
    }
}
