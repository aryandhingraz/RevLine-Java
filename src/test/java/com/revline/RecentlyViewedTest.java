package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.service.RecentlyViewed;

import java.util.List;

/**
 * Tests for LRU caching and access-order evictions in RecentlyViewed (L6-7).
 */
public class RecentlyViewedTest {

    private static Car makeCar(int id, String model) {
        return new Car(id, "TestBrand", model, 200000.0, 300, FuelType.PETROL, 500, 3.5, Status.AVAILABLE);
    }

    public static void testLruCapacityEviction() {
        RecentlyViewed cache = new RecentlyViewed(5);

        for (int i = 1; i <= 6; i++) {
            cache.recordView(makeCar(i, "Model" + i));
        }

        Assert.assertEquals(5, cache.size(), "Cache size should not exceed capacity of 5");
        // Car #1 should have been evicted as the eldest unaccessed entry
        Assert.assertFalse(cache.containsKey(1), "Car #1 should have been evicted");
        Assert.assertTrue(cache.containsKey(2), "Car #2 should still be in cache");
        Assert.assertTrue(cache.containsKey(6), "Car #6 should be in cache");
    }

    public static void testAccessOrderRefresh() {
        RecentlyViewed cache = new RecentlyViewed(3);

        cache.recordView(makeCar(1, "Model1"));
        cache.recordView(makeCar(2, "Model2"));
        cache.recordView(makeCar(3, "Model3"));

        // Access Car #1 again to refresh its recency
        cache.recordView(makeCar(1, "Model1"));

        // Now add Car #4: Since Car #1 was refreshed, Car #2 is now the eldest and should be evicted!
        cache.recordView(makeCar(4, "Model4"));

        Assert.assertEquals(3, cache.size(), "Cache size is 3");
        Assert.assertFalse(cache.containsKey(2), "Car #2 should be evicted (eldest)");
        Assert.assertTrue(cache.containsKey(1), "Car #1 should still be present because it was refreshed");
        Assert.assertTrue(cache.containsKey(3), "Car #3 should be present");
        Assert.assertTrue(cache.containsKey(4), "Car #4 should be present");
    }

    public static void testRecentlyViewedListOrder() {
        RecentlyViewed cache = new RecentlyViewed(5);

        cache.recordView(makeCar(1, "One"));
        cache.recordView(makeCar(2, "Two"));
        cache.recordView(makeCar(3, "Three"));

        List<Car> list = cache.getRecentlyViewedList();
        Assert.assertEquals(3, list.size(), "3 cars in list");
        // Reverse chronological: newest first (Three, Two, One)
        Assert.assertEquals(3, list.get(0).getId(), "Most recently viewed (Car #3) should be first");
        Assert.assertEquals(2, list.get(1).getId(), "Car #2 in middle");
        Assert.assertEquals(1, list.get(2).getId(), "Oldest viewed (Car #1) should be last");
    }
}
