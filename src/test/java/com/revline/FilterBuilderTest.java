package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.service.FilterBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for fluent predicate composition using FilterBuilder (L21).
 */
public class FilterBuilderTest {

    private static List<Car> createSampleCars() {
        List<Car> list = new ArrayList<>();
        list.add(new Car(1, "Ferrari", "296 GTB", 338000.0, 330, FuelType.HYBRID, 830, 2.9, Status.AVAILABLE));
        list.add(new Car(2, "Ferrari", "SF90", 625000.0, 340, FuelType.HYBRID, 1000, 2.5, Status.RESERVED));
        list.add(new Car(3, "Porsche", "911 GT3", 225000.0, 318, FuelType.PETROL, 510, 3.4, Status.AVAILABLE));
        list.add(new Car(4, "Rimac", "Nevera", 2200000.0, 412, FuelType.ELECTRIC, 1914, 1.81, Status.SOLD));
        list.add(new Car(5, "McLaren", "750S", 324000.0, 332, FuelType.PETROL, 740, 2.8, Status.AVAILABLE));
        return list;
    }

    public static void testBrandFilterCaseInsensitive() {
        List<Car> cars = createSampleCars();
        List<Car> ferraris = new FilterBuilder().brand("ferrari").filter(cars);

        Assert.assertEquals(2, ferraris.size(), "Should find 2 Ferraris regardless of case");
        Assert.assertEquals("Ferrari", ferraris.get(0).getBrand(), "Brand match");
    }

    public static void testFuelTypeFilter() {
        List<Car> cars = createSampleCars();
        List<Car> hybrids = new FilterBuilder().fuelType(FuelType.HYBRID).filter(cars);

        Assert.assertEquals(2, hybrids.size(), "Should find 2 hybrid vehicles");
        List<Car> evs = new FilterBuilder().fuelType(FuelType.ELECTRIC).filter(cars);
        Assert.assertEquals(1, evs.size(), "Should find 1 electric vehicle");
        Assert.assertEquals("Rimac", evs.get(0).getBrand(), "Rimac is electric");
    }

    public static void testPriceRangeFilter() {
        List<Car> cars = createSampleCars();
        List<Car> midRange = new FilterBuilder().minPrice(300000.0).maxPrice(400000.0).filter(cars);

        Assert.assertEquals(2, midRange.size(), "Ferrari 296 GTB ($338k) and McLaren 750S ($324k) are in [300k, 400k]");
    }

    public static void testPerformanceFilter() {
        List<Car> cars = createSampleCars();
        List<Car> highPerf = new FilterBuilder().minSpeed(335).minHorsepower(800).filter(cars);

        Assert.assertEquals(2, highPerf.size(), "Ferrari SF90 (340km/h, 1000HP) and Rimac Nevera (412km/h, 1914HP)");
    }

    public static void testAvailableOnlyFilter() {
        List<Car> cars = createSampleCars();
        List<Car> available = new FilterBuilder().availableOnly().filter(cars);

        Assert.assertEquals(3, available.size(), "3 cars are currently AVAILABLE");
        for (Car c : available) {
            Assert.assertEquals(Status.AVAILABLE, c.getStatus(), "All filtered cars must be AVAILABLE");
        }
    }

    public static void testComposedFiltersWithAnd() {
        List<Car> cars = createSampleCars();
        // Brand Ferrari + Fuel Hybrid + Available only
        List<Car> filtered = new FilterBuilder()
                .brand("Ferrari")
                .fuelType(FuelType.HYBRID)
                .availableOnly()
                .filter(cars);

        Assert.assertEquals(1, filtered.size(), "Only Ferrari 296 GTB is AVAILABLE (SF90 is RESERVED)");
        Assert.assertEquals("296 GTB", filtered.get(0).getModel(), "Model is 296 GTB");
    }

    public static void testEmptyFilterMatchesAll() {
        List<Car> cars = createSampleCars();
        List<Car> all = new FilterBuilder().filter(cars);

        Assert.assertEquals(cars.size(), all.size(), "Empty FilterBuilder predicate matches all cars");
    }
}
