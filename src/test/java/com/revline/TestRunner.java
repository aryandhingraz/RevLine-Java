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
                System.out.printf("  [PASS] %-52s\n", tc.name);
            } catch (Throwable t) {
                coreFailures.add(tc.name + " -> " + t.getMessage());
                System.out.printf("  [FAIL] %-52s\n", tc.name);
            }
        }

        // 2. Run Student Exercise Checks
        System.out.println("\n--- ASSIGNED EXERCISE CHECKS ---");
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
        System.out.printf(" Exercises  : %d/%d Completed\n", completedExercises, exerciseChecks.size());
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

        // Car Comparators (6 tests)
        coreTests.add(new TestCase("CarComparatorsTest.testByPriceAsc",
                CarComparatorsTest::testByPriceAsc));
        coreTests.add(new TestCase("CarComparatorsTest.testByPriceDesc",
                CarComparatorsTest::testByPriceDesc));
        coreTests.add(new TestCase("CarComparatorsTest.testBySpeedDesc",
                CarComparatorsTest::testBySpeedDesc));
        coreTests.add(new TestCase("CarComparatorsTest.testByHpDesc",
                CarComparatorsTest::testByHpDesc));
        coreTests.add(new TestCase("CarComparatorsTest.testByAccelAsc",
                CarComparatorsTest::testByAccelAsc));
        coreTests.add(new TestCase("CarComparatorsTest.testByBrandModel",
                CarComparatorsTest::testByBrandModel));

        // Filter Builder (7 tests)
        coreTests.add(new TestCase("FilterBuilderTest.testBrandFilterCaseInsensitive",
                FilterBuilderTest::testBrandFilterCaseInsensitive));
        coreTests.add(new TestCase("FilterBuilderTest.testFuelTypeFilter",
                FilterBuilderTest::testFuelTypeFilter));
        coreTests.add(new TestCase("FilterBuilderTest.testPriceRangeFilter",
                FilterBuilderTest::testPriceRangeFilter));
        coreTests.add(new TestCase("FilterBuilderTest.testPerformanceFilter",
                FilterBuilderTest::testPerformanceFilter));
        coreTests.add(new TestCase("FilterBuilderTest.testAvailableOnlyFilter",
                FilterBuilderTest::testAvailableOnlyFilter));
        coreTests.add(new TestCase("FilterBuilderTest.testComposedFiltersWithAnd",
                FilterBuilderTest::testComposedFiltersWithAnd));
        coreTests.add(new TestCase("FilterBuilderTest.testEmptyFilterMatchesAll",
                FilterBuilderTest::testEmptyFilterMatchesAll));

        // Comparison Service (5 tests)
        coreTests.add(new TestCase("ComparisonServiceTest.testCompareTwoCarsGridDimensions",
                ComparisonServiceTest::testCompareTwoCarsGridDimensions));
        coreTests.add(new TestCase("ComparisonServiceTest.testCompareThreeCarsGridDimensions",
                ComparisonServiceTest::testCompareThreeCarsGridDimensions));
        coreTests.add(new TestCase("ComparisonServiceTest.testCompareDeterminesCorrectWinners",
                ComparisonServiceTest::testCompareDeterminesCorrectWinners));
        coreTests.add(new TestCase("ComparisonServiceTest.testCompareRejectsInvalidCounts",
                ComparisonServiceTest::testCompareRejectsInvalidCounts));
        coreTests.add(new TestCase("ComparisonServiceTest.testFormatTableRenders",
                ComparisonServiceTest::testFormatTableRenders));

        // Recently Viewed (3 tests)
        coreTests.add(new TestCase("RecentlyViewedTest.testLruCapacityEviction",
                RecentlyViewedTest::testLruCapacityEviction));
        coreTests.add(new TestCase("RecentlyViewedTest.testAccessOrderRefresh",
                RecentlyViewedTest::testAccessOrderRefresh));
        coreTests.add(new TestCase("RecentlyViewedTest.testRecentlyViewedListOrder",
                RecentlyViewedTest::testRecentlyViewedListOrder));

        // Undo Manager (3 tests)
        coreTests.add(new TestCase("UndoManagerTest.testPushAndPeek",
                UndoManagerTest::testPushAndPeek));
        coreTests.add(new TestCase("UndoManagerTest.testCapacityEviction",
                UndoManagerTest::testCapacityEviction));
        coreTests.add(new TestCase("UndoManagerTest.testCanUndoAndClear",
                UndoManagerTest::testCanUndoAndClear));

        // File Sale Repository (4 tests)
        coreTests.add(new TestCase("FileSaleRepositoryTest.testSaveAndFindAll",
                FileSaleRepositoryTest::testSaveAndFindAll));
        coreTests.add(new TestCase("FileSaleRepositoryTest.testFindByIdAndCarId",
                FileSaleRepositoryTest::testFindByIdAndCarId));
        coreTests.add(new TestCase("FileSaleRepositoryTest.testNextIdGeneration",
                FileSaleRepositoryTest::testNextIdGeneration));
        coreTests.add(new TestCase("FileSaleRepositoryTest.testCrashSafeWriteIntegrity",
                FileSaleRepositoryTest::testCrashSafeWriteIntegrity));

        // Reservation Manager (4 tests)
        coreTests.add(new TestCase("ReservationManagerTest.testReserveCar",
                ReservationManagerTest::testReserveCar));
        coreTests.add(new TestCase("ReservationManagerTest.testCannotDoubleReserve",
                ReservationManagerTest::testCannotDoubleReserve));
        coreTests.add(new TestCase("ReservationManagerTest.testCancelReservation",
                ReservationManagerTest::testCancelReservation));
        coreTests.add(new TestCase("ReservationManagerTest.testPurgeExpiredUsesIteratorRemove",
                ReservationManagerTest::testPurgeExpiredUsesIteratorRemove));

        // Waitlist Service (3 tests)
        coreTests.add(new TestCase("WaitlistServiceTest.testJoinWaitlistPosition",
                WaitlistServiceTest::testJoinWaitlistPosition));
        coreTests.add(new TestCase("WaitlistServiceTest.testPollOrderFifo",
                WaitlistServiceTest::testPollOrderFifo));
        coreTests.add(new TestCase("WaitlistServiceTest.testRemoveAndPosition",
                WaitlistServiceTest::testRemoveAndPosition));

        // Sales Service (7 tests)
        coreTests.add(new TestCase("SalesServiceTest.testSellCarSuccessMovesStatusToSold",
                SalesServiceTest::testSellCarSuccessMovesStatusToSold));
        coreTests.add(new TestCase("SalesServiceTest.testSellCarFailsIfAlreadySold",
                SalesServiceTest::testSellCarFailsIfAlreadySold));
        coreTests.add(new TestCase("SalesServiceTest.testSellCarFailsIfReservedByAnother",
                SalesServiceTest::testSellCarFailsIfReservedByAnother));
        coreTests.add(new TestCase("SalesServiceTest.testSellCarSucceedsIfReservedBySameBuyer",
                SalesServiceTest::testSellCarSucceedsIfReservedBySameBuyer));
        coreTests.add(new TestCase("SalesServiceTest.testReserveAndCancelIntegration",
                SalesServiceTest::testReserveAndCancelIntegration));
        coreTests.add(new TestCase("SalesServiceTest.testGenerateReceiptFormat",
                SalesServiceTest::testGenerateReceiptFormat));
        coreTests.add(new TestCase("SalesServiceTest.testPurgeExpiredReservationsRevertsStatus",
                SalesServiceTest::testPurgeExpiredReservationsRevertsStatus));

        // Database Manager & Schema Initialization (2 tests)
        coreTests.add(new TestCase("DatabaseManagerTest.testConnectionAndPragmaForeignKeys",
                DatabaseManagerTest::testConnectionAndPragmaForeignKeys));
        coreTests.add(new TestCase("DatabaseManagerTest.testSchemaInitializerCreatesTablesAndSeeds",
                DatabaseManagerTest::testSchemaInitializerCreatesTablesAndSeeds));

        // SQL Injection Security Verification (1 test)
        coreTests.add(new TestCase("SqlInjectionTest.testPreparedStatementImmunityToSqlInjection",
                SqlInjectionTest::testPreparedStatementImmunityToSqlInjection));

        // JDBC Sale Repository & Relational Transactions (3 tests)
        coreTests.add(new TestCase("JdbcSaleRepositoryTest.testSaveAndFindAllJdbc",
                JdbcSaleRepositoryTest::testSaveAndFindAllJdbc));
        coreTests.add(new TestCase("JdbcSaleRepositoryTest.testFindByIdAndFindByCarIdJdbc",
                JdbcSaleRepositoryTest::testFindByIdAndFindByCarIdJdbc));
        coreTests.add(new TestCase("JdbcSaleRepositoryTest.testRecordSaleAtomicCommitAndRollback",
                JdbcSaleRepositoryTest::testRecordSaleAtomicCommitAndRollback));

        // Dual-Mode Repository Contract Suite (GATE 4) (2 tests)
        coreTests.add(new TestCase("RepositoryContractTest.testCarRepositoryContractInBothModes",
                RepositoryContractTest::testCarRepositoryContractInBothModes));
        coreTests.add(new TestCase("RepositoryContractTest.testSaleRepositoryContractInBothModes",
                RepositoryContractTest::testSaleRepositoryContractInBothModes));

        // Advanced Stream Analytics & Reports R1-R15 (GATE 5) (15 tests)
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR1AvailableCars",
                ReportServiceAllReportsTest::testR1AvailableCars));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR2AvailableStockValue",
                ReportServiceAllReportsTest::testR2AvailableStockValue));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR3TotalPortfolioValue",
                ReportServiceAllReportsTest::testR3TotalPortfolioValue));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR4CarsByFuelType",
                ReportServiceAllReportsTest::testR4CarsByFuelType));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR5Top3FastestCars",
                ReportServiceAllReportsTest::testR5Top3FastestCars));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR6TopNByHorsepower",
                ReportServiceAllReportsTest::testR6TopNByHorsepower));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR7AveragePriceByBrand",
                ReportServiceAllReportsTest::testR7AveragePriceByBrand));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR8DistinctBrandsSorted",
                ReportServiceAllReportsTest::testR8DistinctBrandsSorted));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR9Pagination",
                ReportServiceAllReportsTest::testR9Pagination));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR10TotalRevenue",
                ReportServiceAllReportsTest::testR10TotalRevenue));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR11RevenueByBuyer",
                ReportServiceAllReportsTest::testR11RevenueByBuyer));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR12FastestPerFuelType",
                ReportServiceAllReportsTest::testR12FastestPerFuelType));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR13PriceBandMatrix",
                ReportServiceAllReportsTest::testR13PriceBandMatrix));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR14FormattedNamesList",
                ReportServiceAllReportsTest::testR14FormattedNamesList));
        coreTests.add(new TestCase("ReportServiceAllReportsTest.testR15LazinessDemo",
                ReportServiceAllReportsTest::testR15LazinessDemo));

        // Generics & Functional Programming / Lambdas (6 tests)
        coreTests.add(new TestCase("GenericsAndLambdaTest.testPairContainer",
                GenericsAndLambdaTest::testPairContainer));
        coreTests.add(new TestCase("GenericsAndLambdaTest.testGenericUtilsMaxOf",
                GenericsAndLambdaTest::testGenericUtilsMaxOf));
        coreTests.add(new TestCase("GenericsAndLambdaTest.testGenericUtilsWildcardAndMap",
                GenericsAndLambdaTest::testGenericUtilsWildcardAndMap));
        coreTests.add(new TestCase("GenericsAndLambdaTest.testGenericUtilsPriceLadderTreeMap",
                GenericsAndLambdaTest::testGenericUtilsPriceLadderTreeMap));
        coreTests.add(new TestCase("GenericsAndLambdaTest.testLambdaShowcaseStandardInterfaces",
                GenericsAndLambdaTest::testLambdaShowcaseStandardInterfaces));
        coreTests.add(new TestCase("GenericsAndLambdaTest.testLambdaShowcaseFourMethodReferenceKinds",
                GenericsAndLambdaTest::testLambdaShowcaseFourMethodReferenceKinds));

        // Multithreading & Concurrency Stress Suite (GATE 6) (4 tests)
        coreTests.add(new TestCase("ConcurrencyStressTest.testSafeCounterIncrementIntegrity",
                ConcurrencyStressTest::testSafeCounterIncrementIntegrity));
        coreTests.add(new TestCase("ConcurrencyStressTest.testAutoSaveThreadInterruption",
                ConcurrencyStressTest::testAutoSaveThreadInterruption));
        coreTests.add(new TestCase("ConcurrencyStressTest.testReservationExpiryThreadLifecycle",
                ConcurrencyStressTest::testReservationExpiryThreadLifecycle));
        coreTests.add(new TestCase("ConcurrencyStressTest.testDoubleSellStressTwentyRoundsNoDeadlock",
                ConcurrencyStressTest::testDoubleSellStressTwentyRoundsNoDeadlock));
    }

    private static void registerExerciseChecks() {
        exerciseChecks.add(new ExerciseCheck("EX-1 (Part A)", "Validator.isValidFuelType(String)",
                ValidatorTest::testExercise1_isValidFuelType));
        exerciseChecks.add(new ExerciseCheck("EX-1 (Part B)", "Validator.parsePositiveDouble(String, String)",
                ValidatorTest::testExercise1_parsePositiveDouble));
        exerciseChecks.add(new ExerciseCheck("EX-2", "CarComparators.byValueForMoney()",
                CarComparatorsTest::testExercise2_byValueForMoney));
        exerciseChecks.add(new ExerciseCheck("EX-3", "SearchService.search(String query)",
                SearchServiceTest::testExercise3_searchMultiField));
        exerciseChecks.add(new ExerciseCheck("EX-4", "ReportService.topNByHorsepower(int)",
                ReportServiceTest::testExercise4TopNByHorsepower));
        exerciseChecks.add(new ExerciseCheck("EX-5", "ReportService.averagePriceByBrand()",
                ReportServiceTest::testExercise5AveragePriceByBrand));
        exerciseChecks.add(new ExerciseCheck("EX-6", "JdbcCarRepository.findByPriceRange(double, double)",
                JdbcCarRepositoryTest::testExercise6FindByPriceRange));
        exerciseChecks.add(new ExerciseCheck("EX-7", "UndoManager.undo(InventoryService)",
                UndoManagerTest::testExercise7_undoOperation));
        exerciseChecks.add(new ExerciseCheck("EX-8", "PurchaseRequestQueue.take()",
                ConcurrencyStressTest::testExercise8PurchaseRequestQueueTake));
    }
}
