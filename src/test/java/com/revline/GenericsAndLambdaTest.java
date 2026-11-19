package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.service.LambdaShowcase;
import com.revline.util.GenericUtils;
import com.revline.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Verification Suite for Generics (L24) and Functional Programming / Lambdas (L21, L22).
 */
public final class GenericsAndLambdaTest {

    private GenericsAndLambdaTest() {}

    public static void testPairContainer() {
        Pair<String, Integer> p1 = Pair.of("Horsepower", 800);
        Assert.assertEquals("Horsepower", p1.getFirst(), "Pair first element accessor");
        Assert.assertEquals(800, p1.getSecond().intValue(), "Pair second element accessor");

        Pair<String, Integer> p2 = Pair.of("Horsepower", 800);
        Assert.assertEquals(p1, p2, "Pair equality check");
        Assert.assertEquals(p1.hashCode(), p2.hashCode(), "Pair hashCode equality");
        Assert.assertEquals("(Horsepower, 800)", p1.toString(), "Pair toString format");
    }

    public static void testGenericUtilsMaxOf() {
        // Natural ordering with Integers
        List<Integer> numbers = Arrays.asList(45, 12, 89, 34, 76);
        Assert.assertEquals(89, GenericUtils.maxOf(numbers).intValue(), "GenericUtils.maxOf finds max integer");

        // Natural ordering with Strings
        List<String> strings = Arrays.asList("Porsche", "Aston Martin", "Tesla", "Bugatti");
        Assert.assertEquals("Tesla", GenericUtils.maxOf(strings), "GenericUtils.maxOf finds lexicographical max string");

        // Natural ordering with Car (Car implements Comparable<Car> ordered by ID)
        Car c1 = new Car(10, "BrandA", "ModelA", 100000, 300, FuelType.PETROL, 500, 3.5, Status.AVAILABLE);
        Car c2 = new Car(25, "BrandB", "ModelB", 150000, 320, FuelType.PETROL, 600, 3.2, Status.AVAILABLE);
        Car c3 = new Car(5, "BrandC", "ModelC", 200000, 340, FuelType.PETROL, 700, 2.9, Status.AVAILABLE);

        Car maxCar = GenericUtils.maxOf(Arrays.asList(c1, c2, c3));
        Assert.assertEquals(25, maxCar.getId(), "GenericUtils.maxOf selects car with highest ID under natural order");

        // Edge case: null or empty
        Assert.assertNull(GenericUtils.maxOf(Collections.<Integer>emptyList()), "Empty list returns null");
    }

    public static void testGenericUtilsWildcardAndMap() {
        Car c1 = new Car(1, "Apex", "One", 100000, 300, FuelType.PETROL, 500, 3.5, Status.AVAILABLE);
        Car c2 = new Car(2, "Apex", "Two", 150000, 320, FuelType.HYBRID, 600, 3.2, Status.AVAILABLE);
        List<Car> cars = Arrays.asList(c1, c2);

        // Test wildcard printAll<? extends Vehicle> (capture stdout to verify no crash)
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            GenericUtils.printAll(cars);
        } finally {
            System.setOut(originalOut);
        }

        // Test toMap
        List<Pair<String, Integer>> pairs = Arrays.asList(Pair.of("A", 1), Pair.of("B", 2));
        Map<String, Integer> map = GenericUtils.toMap(pairs);
        Assert.assertEquals(2, map.size(), "toMap converts list of pairs to map");
        Assert.assertEquals(1, map.get("A").intValue(), "toMap value lookup");
    }

    public static void testGenericUtilsPriceLadderTreeMap() {
        Car c1 = new Car(1, "Tesla", "Model 3", 50000, 250, FuelType.ELECTRIC, 450, 3.8, Status.AVAILABLE);
        Car c2 = new Car(2, "BMW", "M3", 80000, 290, FuelType.PETROL, 503, 3.8, Status.AVAILABLE);
        Car c3 = new Car(3, "Audi", "RS5", 80000, 280, FuelType.PETROL, 444, 3.7, Status.AVAILABLE);
        Car c4 = new Car(4, "Porsche", "911", 120000, 310, FuelType.PETROL, 473, 3.4, Status.AVAILABLE);

        TreeMap<Double, List<Car>> ladder = GenericUtils.buildPriceLadder(Arrays.asList(c1, c2, c3, c4));
        Assert.assertEquals(3, ladder.size(), "Price ladder contains 3 distinct price tiers");
        Assert.assertEquals(50000.0, ladder.firstKey(), 0.01, "Lowest tier is 50,000");
        Assert.assertEquals(120000.0, ladder.lastKey(), 0.01, "Highest tier is 120,000");
        Assert.assertEquals(2, ladder.get(80000.0).size(), "80,000 tier contains both BMW and Audi");
    }

    public static void testLambdaShowcaseStandardInterfaces() {
        Car car = new Car(1, "Ferrari", "F8", 280000, 340, FuelType.PETROL, 710, 2.9, Status.AVAILABLE);

        // Predicate
        boolean isFast = LambdaShowcase.testPredicate(car, c -> c.getTopSpeed() > 300);
        Assert.assertTrue(isFast, "Predicate evaluation matches top speed condition");

        // Function
        String title = LambdaShowcase.applyFunction(car, c -> c.getBrand() + " " + c.getModel());
        Assert.assertEquals("Ferrari F8", title, "Function mapping extracts vehicle title");

        // Consumer
        final boolean[] consumed = new boolean[1];
        LambdaShowcase.executeConsumer(car, c -> consumed[0] = true);
        Assert.assertTrue(consumed[0], "Consumer action executed");

        // Supplier
        String supplied = LambdaShowcase.provideValue(() -> "RevLine Showroom");
        Assert.assertEquals("RevLine Showroom", supplied, "Supplier provides string value");

        // Custom @FunctionalInterface
        int hpDoubled = LambdaShowcase.executeTransformer(car, c -> c.getHorsepower() * 2);
        Assert.assertEquals(1420, hpDoubled, "Custom VehicleTransformer transforms car horsepower");
    }

    public static void testLambdaShowcaseFourMethodReferenceKinds() {
        Car car = new Car(1, "Ferrari", "F8", 280000, 340, FuelType.PETROL, 710, 2.9, Status.AVAILABLE);

        // 1. Static method reference: Integer::compare
        int cmp = LambdaShowcase.staticMethodRef(10, 20);
        Assert.assertTrue(cmp < 0, "Static method ref Integer::compare works");

        // 2. Bound instance method reference: out::print
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        LambdaShowcase.boundInstanceMethodRef(ps, "HelloRevLine");
        Assert.assertEquals("HelloRevLine", baos.toString(), "Bound instance method ref works");

        // 3. Unbound instance method reference: Car::getPrice
        double price = LambdaShowcase.unboundInstanceMethodRef(car);
        Assert.assertEquals(280000.0, price, 0.01, "Unbound instance method ref Car::getPrice works");

        // 4. Constructor reference: ArrayList::new
        List<String> list = LambdaShowcase.constructorMethodRef();
        Assert.assertTrue(list.isEmpty(), "Constructor method ref ArrayList::new creates empty list");
    }
}
