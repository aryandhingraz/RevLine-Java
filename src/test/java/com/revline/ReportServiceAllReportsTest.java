package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.repository.FileCarRepository;
import com.revline.repository.FileSaleRepository;
import com.revline.service.InventoryService;
import com.revline.service.ReportService;
import com.revline.util.Pair;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gate 5 Verification Suite: Hand-computed expectation checks for all 15 reports (R1 to R15).
 * Validates correctness of Java 17 Stream pipelines, collectors, and functional mappings.
 */
public final class ReportServiceAllReportsTest {

    private ReportServiceAllReportsTest() {}

    private static ReportService createSeededReportService() throws Exception {
        FileCarRepository carRepo = new FileCarRepository(new File("data/seed/cars.csv"));
        FileSaleRepository saleRepo = new FileSaleRepository(new File("data/seed/sales.csv"));
        InventoryService invService = new InventoryService(carRepo, null);
        return new ReportService(invService, saleRepo);
    }

    // [R1] Available cars filter
    public static void testR1AvailableCars() throws Exception {
        ReportService reportService = createSeededReportService();
        List<Car> availableCars = reportService.getAvailableCars();

        // 18 total seeded cars - 3 SOLD (LaFerrari, SF90, R8) = 15 AVAILABLE
        Assert.assertEquals(15, availableCars.size(), "R1: Count of available cars must be exactly 15");
        for (Car c : availableCars) {
            Assert.assertEquals(Status.AVAILABLE, c.getStatus(), "R1: Every car returned must have AVAILABLE status");
        }
    }

    // [R2] Stock value of AVAILABLE cars
    public static void testR2AvailableStockValue() throws Exception {
        ReportService reportService = createSeededReportService();
        double value = reportService.calculateAvailableStockValue();

        // Hand-computed: 22,737,598 total - 2,104,000 sold = 20,633,598.00
        Assert.assertEquals(20633598.00, value, 0.01, "R2: Stock value of available cars must match $20,633,598.00");
    }

    // [R3] Portfolio value of ALL cars
    public static void testR3TotalPortfolioValue() throws Exception {
        ReportService reportService = createSeededReportService();
        double totalPortfolio = reportService.calculateTotalPortfolioValue();

        // Hand-computed total of all 18 cars in seed catalog = 22,737,598.00
        Assert.assertEquals(22737598.00, totalPortfolio, 0.01, "R3: Total portfolio value must match $22,737,598.00");
    }

    // [R4] Cars per fuel type (groupingBy + counting into EnumMap)
    public static void testR4CarsByFuelType() throws Exception {
        ReportService reportService = createSeededReportService();
        Map<FuelType, Long> counts = reportService.countCarsByFuelType();

        Assert.assertEquals(13L, counts.get(FuelType.PETROL).longValue(), "R4: Petrol cars count must be 13");
        Assert.assertEquals(4L, counts.get(FuelType.HYBRID).longValue(), "R4: Hybrid cars count must be 4");
        Assert.assertEquals(1L, counts.get(FuelType.ELECTRIC).longValue(), "R4: Electric cars count must be 1");
    }

    // [R5] Top-3 fastest cars
    public static void testR5Top3FastestCars() throws Exception {
        ReportService reportService = createSeededReportService();
        List<Car> top3 = reportService.getTop3FastestCars();

        Assert.assertEquals(3, top3.size(), "R5: Top-3 list size must be 3");
        Assert.assertEquals("Koenigsegg", top3.get(0).getBrand(), "R5: #1 fastest is Jesko (480 km/h)");
        Assert.assertEquals(480, top3.get(0).getTopSpeed(), "R5: #1 top speed is 480");

        Assert.assertEquals("Koenigsegg", top3.get(1).getBrand(), "R5: #2 fastest is Agera RS (447 km/h)");
        Assert.assertEquals(447, top3.get(1).getTopSpeed(), "R5: #2 top speed is 447");

        Assert.assertEquals("Bugatti", top3.get(2).getBrand(), "R5: #3 fastest is Veyron SS (431 km/h)");
        Assert.assertEquals(431, top3.get(2).getTopSpeed(), "R5: #3 top speed is 431");
    }

    // [R6] EX-4 Top-N by horsepower
    public static void testR6TopNByHorsepower() throws Exception {
        ReportService reportService = createSeededReportService();
        List<String> top3Hp = reportService.topNByHorsepower(3);

        Assert.assertEquals(3, top3Hp.size(), "R6: Top 3 by HP size must be 3");
        Assert.assertTrue(top3Hp.get(0).contains("Jesko"), "R6: 1600 HP Jesko must be #1");
        Assert.assertTrue(top3Hp.get(1).contains("Chiron"), "R6: 1500 HP Chiron must be #2");
        Assert.assertTrue(top3Hp.get(2).contains("Agera RS"), "R6: 1341 HP Agera RS must be #3");
    }

    // [R7] EX-5 Average price by brand
    public static void testR7AveragePriceByBrand() throws Exception {
        ReportService reportService = createSeededReportService();
        Map<String, Double> brandAvgs = reportService.averagePriceByBrand();

        // Bugatti has Chiron (3.0M) and Veyron SS (2.5M) -> avg 2.75M
        Assert.assertEquals(2750000.00, brandAvgs.get("Bugatti"), 0.01, "R7: Bugatti average price must be $2,750,000.00");
        // Tesla has Model S Plaid (135,990) -> avg 135,990.00
        Assert.assertEquals(135990.00, brandAvgs.get("Tesla"), 0.01, "R7: Tesla average price must be $135,990.00");
        Assert.assertEquals(11, brandAvgs.size(), "R7: All 11 distinct brands must have an average entry");
    }

    // [R8] Distinct brands sorted
    public static void testR8DistinctBrandsSorted() throws Exception {
        ReportService reportService = createSeededReportService();
        List<String> brands = reportService.getDistinctBrandsSorted();

        Assert.assertEquals(11, brands.size(), "R8: Distinct brand count must be 11");
        Assert.assertEquals("Aston Martin", brands.get(0), "R8: First brand alphabetically is Aston Martin");
        Assert.assertEquals("Tesla", brands.get(10), "R8: Last brand alphabetically is Tesla");
    }

    // [R9] Pagination of inventory
    public static void testR9Pagination() throws Exception {
        ReportService reportService = createSeededReportService();

        // Page 1 (size 5) -> IDs 1 to 5
        List<Car> page1 = reportService.getCarsPaged(1, 5);
        Assert.assertEquals(5, page1.size(), "R9: Page 1 should contain 5 cars");
        Assert.assertEquals(1, page1.get(0).getId(), "R9: First car of page 1 should be ID 1");
        Assert.assertEquals(5, page1.get(4).getId(), "R9: Last car of page 1 should be ID 5");

        // Page 4 (size 5) -> IDs 16, 17, 18 (remaining 3)
        List<Car> page4 = reportService.getCarsPaged(4, 5);
        Assert.assertEquals(3, page4.size(), "R9: Page 4 should contain remaining 3 cars");
        Assert.assertEquals(16, page4.get(0).getId(), "R9: First car of page 4 should be ID 16");
        Assert.assertEquals(18, page4.get(2).getId(), "R9: Last car of page 4 should be ID 18");

        // Invalid page
        Assert.assertTrue(reportService.getCarsPaged(0, 5).isEmpty(), "R9: Page <= 0 returns empty list");
    }

    // [R10] Total sales revenue
    public static void testR10TotalRevenue() throws Exception {
        ReportService reportService = createSeededReportService();
        double revenue = reportService.calculateTotalRevenue();

        // Seeded sales: 1,400,000 + 507,000 + 197,000 = 2,104,000.00
        Assert.assertEquals(2104000.00, revenue, 0.01, "R10: Total sales revenue must be $2,104,000.00");
    }

    // [R11] Revenue by buyer
    public static void testR11RevenueByBuyer() throws Exception {
        ReportService reportService = createSeededReportService();
        Map<String, Double> revByBuyer = reportService.getRevenueByBuyer();

        Assert.assertEquals(1, revByBuyer.size(), "R11: Exactly 1 seeded buyer");
        Assert.assertEquals(2104000.00, revByBuyer.get("Legacy C Import"), 0.01,
                "R11: Legacy C Import revenue must be $2,104,000.00");
    }

    // [R12] Fastest 0-100 per fuel type
    public static void testR12FastestPerFuelType() throws Exception {
        ReportService reportService = createSeededReportService();
        Map<FuelType, Optional<Car>> fastest = reportService.getFastestPerFuelType();

        Assert.assertTrue(fastest.get(FuelType.PETROL).isPresent(), "R12: Petrol fastest must be present");
        Assert.assertEquals("Chiron", fastest.get(FuelType.PETROL).get().getModel(),
                "R12: Fastest Petrol is Chiron (2.40s)");

        Assert.assertTrue(fastest.get(FuelType.HYBRID).isPresent(), "R12: Hybrid fastest must be present");
        Assert.assertEquals("LaFerrari", fastest.get(FuelType.HYBRID).get().getModel(),
                "R12: Fastest Hybrid is LaFerrari (2.40s)");

        Assert.assertTrue(fastest.get(FuelType.ELECTRIC).isPresent(), "R12: Electric fastest must be present");
        Assert.assertEquals("Model S Plaid", fastest.get(FuelType.ELECTRIC).get().getModel(),
                "R12: Fastest Electric is Model S Plaid (1.99s)");
    }

    // [R13] Price band matrix (2-D array int[fuel][band])
    public static void testR13PriceBandMatrix() throws Exception {
        ReportService reportService = createSeededReportService();
        int[][] matrix = reportService.getPriceBandMatrix();

        // PETROL (idx 0): Band 0: 6, Band 1: 1, Band 2: 1, Band 3: 5 -> Total 13
        Assert.assertEquals(6, matrix[0][0], "R13: Petrol in < $500k band");
        Assert.assertEquals(1, matrix[0][1], "R13: Petrol in $500k-$1M band");
        Assert.assertEquals(1, matrix[0][2], "R13: Petrol in $1M-$2M band");
        Assert.assertEquals(5, matrix[0][3], "R13: Petrol in >= $2M band");

        // HYBRID (idx 1): Band 0: 0, Band 1: 1, Band 2: 2, Band 3: 1 -> Total 4
        Assert.assertEquals(0, matrix[1][0], "R13: Hybrid in < $500k band");
        Assert.assertEquals(1, matrix[1][1], "R13: Hybrid in $500k-$1M band");
        Assert.assertEquals(2, matrix[1][2], "R13: Hybrid in $1M-$2M band");
        Assert.assertEquals(1, matrix[1][3], "R13: Hybrid in >= $2M band");

        // ELECTRIC (idx 2): Band 0: 1, Band 1: 0, Band 2: 0, Band 3: 0 -> Total 1
        Assert.assertEquals(1, matrix[2][0], "R13: Electric in < $500k band");
        Assert.assertEquals(0, matrix[2][1], "R13: Electric in $500k-$1M band");
        Assert.assertEquals(0, matrix[2][2], "R13: Electric in $1M-$2M band");
        Assert.assertEquals(0, matrix[2][3], "R13: Electric in >= $2M band");

        // Matrix formatting test
        String formatted = reportService.formatPriceBandMatrix();
        Assert.assertTrue(formatted.contains("Petrol"), "Formatted matrix should contain Petrol");
        Assert.assertTrue(formatted.contains("Hybrid"), "Formatted matrix should contain Hybrid");
    }

    // [R14] Formatted vehicle names list
    public static void testR14FormattedNamesList() throws Exception {
        ReportService reportService = createSeededReportService();
        String names = reportService.getFormattedVehicleNames();

        Assert.assertTrue(names.contains("Bugatti Chiron"), "R14: Should contain Bugatti Chiron");
        Assert.assertTrue(names.contains("Tesla Model S Plaid"), "R14: Should contain Tesla Model S Plaid");
        Assert.assertTrue(names.contains(", "), "R14: Names should be delimited by comma space");
    }

    // [R15] Stream intermediate laziness demo
    public static void testR15LazinessDemo() throws Exception {
        ReportService reportService = createSeededReportService();

        Pair<Integer, Integer> result = reportService.demonstrateLaziness(4);
        Assert.assertEquals(0, result.getFirst().intValue(),
                "R15: Unconsumed stream pipeline must execute 0 peeks (proving laziness)");
        Assert.assertEquals(4, result.getSecond().intValue(),
                "R15: Consumed stream pipeline with limit(4) must execute exactly 4 peeks (short-circuiting)");
    }
}
