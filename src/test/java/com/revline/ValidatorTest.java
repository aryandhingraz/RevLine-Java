package com.revline;

import com.revline.util.Validator;
import com.revline.util.exception.InvalidCarException;

/**
 * Unit tests for input parsing and bounds validation rules (L2, L3, L4).
 */
public class ValidatorTest {

    public static void testRequireNonBlank() {
        Assert.assertEquals("Ferrari", Validator.requireNonBlank("  Ferrari  ", "Brand"), "Should trim and return string");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.requireNonBlank("   ", "Brand"), "Blank string should throw InvalidCarException");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.requireNonBlank(null, "Brand"), "Null string should throw InvalidCarException");
    }

    public static void testValidateStringNoDelimiter() {
        Validator.validateStringNoDelimiter("Porsche 911", "Model"); // should pass
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.validateStringNoDelimiter("Ferrari,SF90", "Model"), "Comma delimiter in name should be rejected");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.validateStringNoDelimiter("Test\nModel", "Model"), "Newline in name should be rejected");
    }

    public static void testParsePositiveIntValid() {
        int parsed = Validator.parsePositiveInt("42", "Count");
        Assert.assertEquals(42, parsed, "Should parse integer 42 correctly");
    }

    public static void testParsePositiveIntInvalidThrows() {
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.parsePositiveInt("-10", "Count"), "Negative number should throw InvalidCarException");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.parsePositiveInt("0", "Count"), "Zero should throw InvalidCarException");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.parsePositiveInt("xyz", "Count"), "Non-numeric string should throw InvalidCarException");
    }

    public static void testValidateTopSpeedBoundary() {
        Validator.validateTopSpeed(50);
        Validator.validateTopSpeed(600);
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.validateTopSpeed(49), "Top speed 49 km/h must fail");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.validateTopSpeed(601), "Top speed 601 km/h must fail");
    }

    public static void testValidateHorsepowerBoundary() {
        Validator.validateHorsepower(50);
        Validator.validateHorsepower(3000);
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.validateHorsepower(49), "Horsepower 49 HP must fail");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.validateHorsepower(3001), "Horsepower 3001 HP must fail");
    }

    public static void testValidateAccelerationBoundary() {
        Validator.validateAcceleration(1.0);
        Validator.validateAcceleration(20.0);
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.validateAcceleration(0.9), "Acceleration 0.9s must fail");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.validateAcceleration(20.1), "Acceleration 20.1s must fail");
    }

    // =========================================================================
    // STUDENT EXERCISE TESTS (EX-1)
    // =========================================================================

    public static void testExercise1_isValidFuelType() {
        Assert.assertTrue(Validator.isValidFuelType("PETROL"), "PETROL should be valid");
        Assert.assertTrue(Validator.isValidFuelType("Hybrid"), "Hybrid should be valid");
        Assert.assertTrue(Validator.isValidFuelType("electric"), "electric should be valid");
        Assert.assertFalse(Validator.isValidFuelType("DIESEL"), "DIESEL should be invalid");
        Assert.assertFalse(Validator.isValidFuelType(""), "Empty string should be invalid");
        Assert.assertFalse(Validator.isValidFuelType(null), "Null should be invalid");
    }

    public static void testExercise1_parsePositiveDouble() {
        Assert.assertEquals(150000.0, Validator.parsePositiveDouble(" 150000.0 ", "Price"), 0.01,
                "Should parse trimmed positive double");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.parsePositiveDouble("0.0", "Price"), "0.0 must throw InvalidCarException");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.parsePositiveDouble("-50.0", "Price"), "Negative double must throw InvalidCarException");
        Assert.assertThrows(InvalidCarException.class, () ->
            Validator.parsePositiveDouble("abc", "Price"), "Malformed text must throw InvalidCarException");
    }
}
