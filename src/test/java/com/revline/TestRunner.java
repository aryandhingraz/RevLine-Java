package com.revline;

import java.util.ArrayList;
import java.util.List;

/**
 * In-house console test runner for RevLine (no JUnit required).
 * Discovers tests via explicit method calls, reports results, and exits with non-zero on failure.
 * Tracks student exercises (EX-1 to EX-8) in a distinct section.
 */
public class TestRunner {

    @FunctionalInterface
    public interface TestCaseRunnable {
        void run() throws Throwable;
    }

    private static class TestCase {
        final String name;
        final TestCaseRunnable runnable;

        TestCase(String name, TestCaseRunnable runnable) {
            this.name = name;
            this.runnable = runnable;
        }
    }

    private static class ExerciseCheck {
        final String id;
        final String description;
        final TestCaseRunnable runnable;

        ExerciseCheck(String id, String description, TestCaseRunnable runnable) {
            this.id = id;
            this.description = description;
            this.runnable = runnable;
        }
    }

    private static final List<TestCase> coreTests = new ArrayList<>();
    private static final List<ExerciseCheck> exerciseChecks = new ArrayList<>();
    private static final List<String> coreFailures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" RevLine In-House Automated Test Suite");
        System.out.println("=================================================");

        registerCoreTests();
        registerExerciseChecks();

        // 1. Run Core Framework & Module Tests
        int passed = 0;
        long startTime = System.currentTimeMillis();

        System.out.println("--- CORE MODULE CHECKS ---");
        for (TestCase tc : coreTests) {
            try {
                tc.runnable.run();
                passed++;
                System.out.printf("  [PASS] %-48s\n", tc.name);
            } catch (Throwable t) {
                coreFailures.add(tc.name + " -> " + t.getMessage());
                System.out.printf("  [FAIL] %-48s\n", tc.name);
            }
        }

        // 2. Run Student Exercise Checks
        System.out.println("\n--- STUDENT EXERCISES (Owned by Aryan) ---");
        int completedExercises = 0;
        for (ExerciseCheck ec : exerciseChecks) {
            try {
                ec.runnable.run();
                completedExercises++;
                System.out.printf("  [COMPLETE] %s: %s\n", ec.id, ec.description);
            } catch (UnsupportedOperationException e) {
                System.out.printf("  [PENDING]  %s: %s\n", ec.id, ec.description);
            } catch (Throwable t) {
                System.out.printf("  [FAILING]  %s: %s (Error: %s)\n", ec.id, ec.description, t.getMessage());
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("=================================================");
        System.out.printf(" Core Suite : %d/%d Passed (%d ms)\n", passed, coreTests.size(), elapsed);
        System.out.printf(" Exercises  : %d/8 Completed (Active: EX-1, EX-3)\n", completedExercises);
        System.out.println("=================================================");

        if (!coreFailures.isEmpty()) {
            System.out.println("\nCORE TEST FAILURES:");
            for (String f : coreFailures) {
                System.out.println(" - " + f);
            }
            System.exit(1);
        } else {
            System.out.println("ALL CORE CHECKS PASSED WITH ZERO ERRORS.\n");
            System.exit(0);
        }
    }

    private static void registerCoreTests() {
        // Seed Consistency (3 tests)
        coreTests.add(new TestCase("SeedConsistencyTest.testSeedMatchesLegacyC",
                SeedConsistencyTest::testSeedMatchesLegacyC));
        coreTests.add(new TestCase("SeedConsistencyTest.testLegacySoldCarsPreserved",
                SeedConsistencyTest::testLegacySoldCarsPreserved));
        coreTests.add(new TestCase("SeedConsistencyTest.testSeedSalesConsistency",
                SeedConsistencyTest::testSeedSalesConsistency));

        // Car Domain Model (10 tests)
        coreTests.add(new TestCase("CarTest.testValidCarCreationAndGetters",
                CarTest::testValidCarCreationAndGetters));
        coreTests.add(new TestCase("CarTest.testConstructorValidationInvalidSpeed",
                CarTest::testConstructorValidationInvalidSpeed));
        coreTests.add(new TestCase("CarTest.testConstructorValidationInvalidHorsepower",
                CarTest::testConstructorValidationInvalidHorsepower));
        coreTests.add(new TestCase("CarTest.testConstructorValidationInvalidAcceleration",
                CarTest::testConstructorValidationInvalidAcceleration));
        coreTests.add(new TestCase("CarTest.testConstructorValidationNegativePrice",
                CarTest::testConstructorValidationNegativePrice));
        coreTests.add(new TestCase("CarTest.testConstructorValidationBlankBrandOrModel",
                CarTest::testConstructorValidationBlankBrandOrModel));
        coreTests.add(new TestCase("CarTest.testCsvSerializationAndDeserializationRoundTrip",
                CarTest::testCsvSerializationAndDeserializationRoundTrip));
        coreTests.add(new TestCase("CarTest.testCarDeepCopy",
                CarTest::testCarDeepCopy));
        coreTests.add(new TestCase("CarTest.testComparableNaturalOrderById",
                CarTest::testComparableNaturalOrderById));
        coreTests.add(new TestCase("CarTest.testStatusTransitions",
                CarTest::testStatusTransitions));

        // Validator Rules (7 tests)
        coreTests.add(new TestCase("ValidatorTest.testRequireNonBlank",
                ValidatorTest::testRequireNonBlank));
        coreTests.add(new TestCase("ValidatorTest.testValidateStringNoDelimiter",
                ValidatorTest::testValidateStringNoDelimiter));
        coreTests.add(new TestCase("ValidatorTest.testParsePositiveIntValid",
                ValidatorTest::testParsePositiveIntValid));
        coreTests.add(new TestCase("ValidatorTest.testParsePositiveIntInvalidThrows",
                ValidatorTest::testParsePositiveIntInvalidThrows));
        coreTests.add(new TestCase("ValidatorTest.testValidateTopSpeedBoundary",
                ValidatorTest::testValidateTopSpeedBoundary));
        coreTests.add(new TestCase("ValidatorTest.testValidateHorsepowerBoundary",
                ValidatorTest::testValidateHorsepowerBoundary));
        coreTests.add(new TestCase("ValidatorTest.testValidateAccelerationBoundary",
                ValidatorTest::testValidateAccelerationBoundary));

        // File Repository (6 tests)
        coreTests.add(new TestCase("FileCarRepositoryTest.testFindAllReturnsSeededCount",
                FileCarRepositoryTest::testFindAllReturnsSeededCount));
        coreTests.add(new TestCase("FileCarRepositoryTest.testFindByIdExistingAndNonExisting",
                FileCarRepositoryTest::testFindByIdExistingAndNonExisting));
        coreTests.add(new TestCase("FileCarRepositoryTest.testFindByStatus",
                FileCarRepositoryTest::testFindByStatus));
        coreTests.add(new TestCase("FileCarRepositoryTest.testFindByPriceRange",
                FileCarRepositoryTest::testFindByPriceRange));
        coreTests.add(new TestCase("FileCarRepositoryTest.testNextId",
                FileCarRepositoryTest::testNextId));
        coreTests.add(new TestCase("FileCarRepositoryTest.testCrashSafeWriteIntegrity",
                FileCarRepositoryTest::testCrashSafeWriteIntegrity));

        // Inventory Service (6 tests)
        coreTests.add(new TestCase("InventoryServiceTest.testFindAllDefensiveCopyImmutability",
                InventoryServiceTest::testFindAllDefensiveCopyImmutability));
        coreTests.add(new TestCase("InventoryServiceTest.testFindByIdExisting",
                InventoryServiceTest::testFindByIdExisting));
        coreTests.add(new TestCase("InventoryServiceTest.testFindByIdThrowsWhenNotFound",
                InventoryServiceTest::testFindByIdThrowsWhenNotFound));
        coreTests.add(new TestCase("InventoryServiceTest.testAddCarRejectsDuplicate",
                InventoryServiceTest::testAddCarRejectsDuplicate));
        coreTests.add(new TestCase("InventoryServiceTest.testCalculateTotalStockValueOnlyAvailable",
                InventoryServiceTest::testCalculateTotalStockValueOnlyAvailable));
        coreTests.add(new TestCase("InventoryServiceTest.testGetBrandsSortedTreeSet",
                InventoryServiceTest::testGetBrandsSortedTreeSet));

        // Search Service (2 tests)
        coreTests.add(new TestCase("SearchServiceTest.testFindByIdDelegation",
                SearchServiceTest::testFindByIdDelegation));
        coreTests.add(new TestCase("SearchServiceTest.testFindByStatus",
                SearchServiceTest::testFindByStatus));
    }

    private static void registerExerciseChecks() {
        exerciseChecks.add(new ExerciseCheck("EX-1 (Part A)", "Validator.isValidFuelType(String)",
                ValidatorTest::testExercise1_isValidFuelType));
        exerciseChecks.add(new ExerciseCheck("EX-1 (Part B)", "Validator.parsePositiveDouble(String, String)",
                ValidatorTest::testExercise1_parsePositiveDouble));
        exerciseChecks.add(new ExerciseCheck("EX-3", "SearchService.search(String query)",
                SearchServiceTest::testExercise3_searchMultiField));
    }
}
