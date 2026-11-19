package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.service.ComparisonService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tests for multi-dimensional spec comparisons and metric winners (L2).
 */
public class ComparisonServiceTest {

    private static Car carA() {
        // Price: $338,000, Speed: 330, HP: 830, Accel: 2.9
        return new Car(1, "Ferrari", "296 GTB", 338000.0, 330, FuelType.HYBRID, 830, 2.9, Status.AVAILABLE);
    }

    private static Car carB() {
        // Price: $225,000 (lowest), Speed: 318, HP: 510, Accel: 3.4
        return new Car(2, "Porsche", "911 GT3", 225000.0, 318, FuelType.PETROL, 510, 3.4, Status.AVAILABLE);
    }

    private static Car carC() {
        // Price: $3,000,000, Speed: 420 (highest), HP: 1500 (highest), Accel: 2.4 (quickest)
        return new Car(3, "Bugatti", "Chiron", 3000000.0, 420, FuelType.PETROL, 1500, 2.4, Status.AVAILABLE);
    }

    public static void testCompareTwoCarsGridDimensions() {
        ComparisonService service = new ComparisonService();
        List<Car> cars = new ArrayList<>();
        cars.add(carA());
        cars.add(carB());

        ComparisonService.ComparisonResult result = service.compare(cars);
        String[][] grid = result.getGrid();

        Assert.assertEquals(ComparisonService.METRIC_LABELS.length, grid.length, "Row count equals metric label count (8)");
        Assert.assertEquals(2, grid[0].length, "Column count equals number of compared cars (2)");
    }

    public static void testCompareThreeCarsGridDimensions() {
        ComparisonService service = new ComparisonService();
        List<Car> cars = new ArrayList<>();
        cars.add(carA());
        cars.add(carB());
        cars.add(carC());

        ComparisonService.ComparisonResult result = service.compare(cars);
        String[][] grid = result.getGrid();

        Assert.assertEquals(8, grid.length, "Row count is 8");
        Assert.assertEquals(3, grid[0].length, "Column count is 3");
    }

    public static void testCompareDeterminesCorrectWinners() {
        ComparisonService service = new ComparisonService();
        List<Car> cars = new ArrayList<>();
        cars.add(carA()); // idx 0
        cars.add(carB()); // idx 1: lowest price ($225k)
        cars.add(carC()); // idx 2: highest speed (420), highest HP (1500), lowest accel (2.4)

        ComparisonService.ComparisonResult result = service.compare(cars);
        int[] winners = result.getWinnerColumnPerRow();

        // Row 2 is Price: Car B (idx 1) has lowest price ($225k)
        Assert.assertEquals(1, winners[2], "Car B should win lowest price");

        // Row 3 is Top Speed: Car C (idx 2) has highest speed (420 km/h)
        Assert.assertEquals(2, winners[3], "Car C should win highest top speed");

        // Row 4 is Horsepower: Car C (idx 2) has highest HP (1500 HP)
        Assert.assertEquals(2, winners[4], "Car C should win highest horsepower");

        // Row 5 is Acceleration: Car C (idx 2) has quickest 0-100 (2.4s)
        Assert.assertEquals(2, winners[5], "Car C should win quickest acceleration");
    }

    public static void testCompareRejectsInvalidCounts() {
        ComparisonService service = new ComparisonService();

        boolean caughtOne = false;
        try {
            service.compare(Collections.singletonList(carA()));
        } catch (IllegalArgumentException e) {
            caughtOne = true;
        }
        Assert.assertTrue(caughtOne, "Comparing 1 car should throw IllegalArgumentException");

        boolean caughtFive = false;
        try {
            List<Car> five = new ArrayList<>();
            for (int i = 0; i < 5; i++) five.add(carA());
            service.compare(five);
        } catch (IllegalArgumentException e) {
            caughtFive = true;
        }
        Assert.assertTrue(caughtFive, "Comparing 5 cars should throw IllegalArgumentException");
    }

    public static void testFormatTableRenders() {
        ComparisonService service = new ComparisonService();
        List<Car> cars = new ArrayList<>();
        cars.add(carA());
        cars.add(carB());

        ComparisonService.ComparisonResult result = service.compare(cars);
        String table = result.formatTable();

        Assert.assertNotNull(table, "Table should not be null");
        Assert.assertTrue(table.contains("SPECIFICATION"), "Table has header");
        Assert.assertTrue(table.contains("*BEST"), "Table highlights winning metrics with *BEST");
    }
}
