package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.service.CarComparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Tests for vehicle sorting comparators and student exercise EX-2.
 */
public class CarComparatorsTest {

    private static List<Car> createSampleCars() {
        List<Car> list = new ArrayList<>();
        // id, brand, model, price, speed, fuel, hp, accel, status
        list.add(new Car(1, "Bugatti", "Chiron", 3000000.0, 420, FuelType.PETROL, 1500, 2.4, Status.AVAILABLE));
        list.add(new Car(2, "Ferrari", "SF90", 625000.0, 340, FuelType.HYBRID, 1000, 2.5, Status.AVAILABLE));
        list.add(new Car(3, "Porsche", "911 GT3", 225000.0, 318, FuelType.PETROL, 510, 3.4, Status.AVAILABLE));
        list.add(new Car(4, "Ferrari", "296 GTB", 338000.0, 330, FuelType.HYBRID, 830, 2.9, Status.AVAILABLE));
        list.add(new Car(5, "Rimac", "Nevera", 2200000.0, 412, FuelType.ELECTRIC, 1914, 1.81, Status.AVAILABLE));
        return list;
    }

    public static void testByPriceAsc() {
        List<Car> cars = createSampleCars();
        cars.sort(CarComparators.BY_PRICE_ASC);

        Assert.assertEquals(225000.0, cars.get(0).getPrice(), "Lowest price should be first (Porsche)");
        Assert.assertEquals(3000000.0, cars.get(cars.size() - 1).getPrice(), "Highest price should be last (Bugatti)");
    }

    public static void testByPriceDesc() {
        List<Car> cars = createSampleCars();
        cars.sort(CarComparators.BY_PRICE_DESC);

        Assert.assertEquals(3000000.0, cars.get(0).getPrice(), "Highest price should be first");
        Assert.assertEquals(225000.0, cars.get(cars.size() - 1).getPrice(), "Lowest price should be last");
    }

    public static void testBySpeedDesc() {
        List<Car> cars = createSampleCars();
        cars.sort(CarComparators.BY_SPEED_DESC);

        Assert.assertEquals(420, cars.get(0).getTopSpeed(), "Fastest speed (420) should be first");
        Assert.assertEquals(318, cars.get(cars.size() - 1).getTopSpeed(), "Slowest speed (318) should be last");
    }

    public static void testByHpDesc() {
        List<Car> cars = createSampleCars();
        cars.sort(CarComparators.BY_HP_DESC);

        Assert.assertEquals(1914, cars.get(0).getHorsepower(), "Highest HP (Rimac 1914) should be first");
        Assert.assertEquals(510, cars.get(cars.size() - 1).getHorsepower(), "Lowest HP (Porsche 510) should be last");
    }

    public static void testByAccelAsc() {
        List<Car> cars = createSampleCars();
        cars.sort(CarComparators.BY_ACCEL_ASC);

        Assert.assertEquals(1.81, cars.get(0).getAcceleration(), "Quickest 0-100 (Rimac 1.81) should be first");
        Assert.assertEquals(3.4, cars.get(cars.size() - 1).getAcceleration(), "Slowest 0-100 should be last");
    }

    public static void testByBrandModel() {
        List<Car> cars = createSampleCars();
        cars.sort(CarComparators.BY_BRAND_MODEL);

        Assert.assertEquals("Bugatti", cars.get(0).getBrand(), "Bugatti should be first alphabetically");
        Assert.assertEquals("Ferrari", cars.get(1).getBrand(), "Ferrari should be next");
        Assert.assertEquals("296 GTB", cars.get(1).getModel(), "Ferrari 296 GTB before SF90");
        Assert.assertEquals("Ferrari", cars.get(2).getBrand(), "Ferrari SF90");
        Assert.assertEquals("SF90", cars.get(2).getModel(), "Ferrari SF90");
        Assert.assertEquals("Rimac", cars.get(cars.size() - 1).getBrand(), "Rimac should be last alphabetically");
    }

    // =========================================================================
    // EXERCISE EX-2 CHECK METHOD
    // =========================================================================
    public static void testExercise2_byValueForMoney() {
        Comparator<Car> comp = CarComparators.byValueForMoney();
        Assert.assertNotNull(comp, "Comparator should not be null.");

        // Car A: 830 HP, $338,000 -> score = (830 / 338000) * 100000 = ~245.56
        Car carA = new Car(10, "Ferrari", "296 GTB", 338000.0, 330, FuelType.HYBRID, 830, 2.9, Status.AVAILABLE);
        // Car B: 510 HP, $225,000 -> score = (510 / 225000) * 100000 = ~226.67
        Car carB = new Car(11, "Porsche", "911 GT3", 225000.0, 318, FuelType.PETROL, 510, 3.4, Status.AVAILABLE);
        // Car C: 1500 HP, $3,000,000 -> score = (1500 / 3000000) * 100000 = 50.0
        Car carC = new Car(12, "Bugatti", "Chiron", 3000000.0, 420, FuelType.PETROL, 1500, 2.4, Status.AVAILABLE);

        List<Car> list = new ArrayList<>();
        list.add(carC);
        list.add(carB);
        list.add(carA);

        list.sort(comp);

        Assert.assertEquals(10, list.get(0).getId(), "Ferrari 296 GTB has best value-for-money score (~245.56)");
        Assert.assertEquals(11, list.get(1).getId(), "Porsche 911 GT3 has 2nd best score (~226.67)");
        Assert.assertEquals(12, list.get(2).getId(), "Bugatti Chiron has lowest score (50.0)");

        // Test tie-break by acceleration ascending:
        // Car D: 500 HP, $250,000 -> score = 200.0, accel = 3.5
        Car carD = new Car(13, "BrandX", "Model1", 250000.0, 300, FuelType.PETROL, 500, 3.5, Status.AVAILABLE);
        // Car E: 500 HP, $250,000 -> score = 200.0, accel = 3.1 (quicker wins)
        Car carE = new Car(14, "BrandY", "Model2", 250000.0, 300, FuelType.PETROL, 500, 3.1, Status.AVAILABLE);

        List<Car> tieList = new ArrayList<>();
        tieList.add(carD);
        tieList.add(carE);
        tieList.sort(comp);

        Assert.assertEquals(14, tieList.get(0).getId(), "On equal score, quicker acceleration (3.1s vs 3.5s) wins tie-break");
    }
}
