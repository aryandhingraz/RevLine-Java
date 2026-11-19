package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.util.exception.DataFormatException;
import com.revline.util.exception.InvalidCarException;

/**
 * Unit tests for domain Car entity and its lifecycle validations (L1, L3).
 */
public class CarTest {

    public static void testValidCarCreationAndGetters() {
        Car c = new Car(101, "Rimac", "Nevera", 2400000.0, 412, FuelType.ELECTRIC, 1914, 1.85, Status.AVAILABLE);
        Assert.assertEquals(101, c.getId(), "ID must match");
        Assert.assertEquals("Rimac", c.getBrand(), "Brand must match");
        Assert.assertEquals("Nevera", c.getModel(), "Model must match");
        Assert.assertEquals(2400000.0, c.getPrice(), 0.01, "Price must match");
        Assert.assertEquals(412, c.getTopSpeed(), "Speed must match");
        Assert.assertEquals(FuelType.ELECTRIC, c.getFuelType(), "Fuel must match");
        Assert.assertEquals(1914, c.getHorsepower(), "HP must match");
        Assert.assertEquals(1.85, c.getAcceleration(), 0.01, "Accel must match");
        Assert.assertEquals(Status.AVAILABLE, c.getStatus(), "Status must match");
    }

    public static void testConstructorValidationInvalidSpeed() {
        Assert.assertThrows(InvalidCarException.class, () ->
            new Car(1, "Test", "Car", 10000, 40, FuelType.PETROL, 300, 3.0, Status.AVAILABLE),
            "Speed < 50 km/h must throw InvalidCarException");

        Assert.assertThrows(InvalidCarException.class, () ->
            new Car(1, "Test", "Car", 10000, 650, FuelType.PETROL, 300, 3.0, Status.AVAILABLE),
            "Speed > 600 km/h must throw InvalidCarException");
    }

    public static void testConstructorValidationInvalidHorsepower() {
        Assert.assertThrows(InvalidCarException.class, () ->
            new Car(1, "Test", "Car", 10000, 200, FuelType.PETROL, 20, 3.0, Status.AVAILABLE),
            "Horsepower < 50 must throw InvalidCarException");
    }

    public static void testConstructorValidationInvalidAcceleration() {
        Assert.assertThrows(InvalidCarException.class, () ->
            new Car(1, "Test", "Car", 10000, 200, FuelType.PETROL, 300, 0.5, Status.AVAILABLE),
            "Acceleration < 1.0s must throw InvalidCarException");
    }

    public static void testConstructorValidationNegativePrice() {
        Assert.assertThrows(InvalidCarException.class, () ->
            new Car(1, "Test", "Car", -500.0, 200, FuelType.PETROL, 300, 3.0, Status.AVAILABLE),
            "Negative price must throw InvalidCarException");
    }

    public static void testConstructorValidationBlankBrandOrModel() {
        Assert.assertThrows(InvalidCarException.class, () ->
            new Car(1, "   ", "Model", 10000, 200, FuelType.PETROL, 300, 3.0, Status.AVAILABLE),
            "Blank brand must throw InvalidCarException");

        Assert.assertThrows(InvalidCarException.class, () ->
            new Car(1, "Brand", "", 10000, 200, FuelType.PETROL, 300, 3.0, Status.AVAILABLE),
            "Empty model must throw InvalidCarException");
    }

    public static void testCsvSerializationAndDeserializationRoundTrip() throws DataFormatException {
        Car original = new Car(55, "Koenigsegg", "Jesko", 3000000.0, 480, FuelType.PETROL, 1600, 2.5, Status.AVAILABLE);
        String csvLine = original.toCsv();
        Car parsed = Car.fromCsv(csvLine, 1);

        Assert.assertEquals(original.getId(), parsed.getId(), "Round trip ID mismatch");
        Assert.assertEquals(original.getBrand(), parsed.getBrand(), "Round trip Brand mismatch");
        Assert.assertEquals(original.getModel(), parsed.getModel(), "Round trip Model mismatch");
        Assert.assertEquals(original.getPrice(), parsed.getPrice(), 0.01, "Round trip Price mismatch");
        Assert.assertEquals(original.getFuelType(), parsed.getFuelType(), "Round trip Fuel mismatch");
        Assert.assertEquals(original.getStatus(), parsed.getStatus(), "Round trip Status mismatch");
    }

    public static void testCarDeepCopy() {
        Car c1 = new Car(1, "Ferrari", "F8", 280000.0, 340, FuelType.PETROL, 710, 2.9, Status.AVAILABLE);
        Car c2 = c1.copy();

        Assert.assertEquals(c1.getId(), c2.getId(), "Copied ID matches");
        c2.setStatus(Status.SOLD);
        Assert.assertEquals(Status.AVAILABLE, c1.getStatus(), "Original status must not be modified when copy mutates");
    }

    public static void testComparableNaturalOrderById() {
        Car c1 = new Car(5, "CarA", "M1", 10000, 200, FuelType.PETROL, 300, 3.0, Status.AVAILABLE);
        Car c2 = new Car(10, "CarB", "M2", 10000, 200, FuelType.PETROL, 300, 3.0, Status.AVAILABLE);

        Assert.assertTrue(c1.compareTo(c2) < 0, "Car 5 should precede Car 10 in natural ordering");
        Assert.assertTrue(c2.compareTo(c1) > 0, "Car 10 should succeed Car 5 in natural ordering");
        Assert.assertEquals(0, c1.compareTo(c1), "Identical IDs should compare as equal (0)");
    }

    public static void testStatusTransitions() {
        Assert.assertTrue(Status.AVAILABLE.canTransitionTo(Status.RESERVED), "AVAILABLE -> RESERVED is legal");
        Assert.assertTrue(Status.AVAILABLE.canTransitionTo(Status.SOLD), "AVAILABLE -> SOLD is legal");
        Assert.assertTrue(Status.RESERVED.canTransitionTo(Status.AVAILABLE), "RESERVED -> AVAILABLE is legal");
        Assert.assertTrue(Status.RESERVED.canTransitionTo(Status.SOLD), "RESERVED -> SOLD is legal");
        Assert.assertFalse(Status.SOLD.canTransitionTo(Status.AVAILABLE), "SOLD -> AVAILABLE is strictly forbidden");
        Assert.assertFalse(Status.SOLD.canTransitionTo(Status.RESERVED), "SOLD -> RESERVED is strictly forbidden");
    }
}
