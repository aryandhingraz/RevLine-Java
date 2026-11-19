package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.repository.FileCarRepository;
import com.revline.service.InventoryService;
import com.revline.service.ReportService;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Tests for stream-based inventory reports and exercises EX-4 and EX-5. */
public final class ReportServiceTest {

    private ReportServiceTest() {}

    private static ReportService createService() throws Exception {
        File file = new File("build/test_reports_cars.csv");
        if (file.exists()) {
            file.delete();
        }
        FileCarRepository repository = new FileCarRepository(file);
        repository.saveAll(Arrays.asList(
                new Car(1, "Apex", "One", 100000.0, 300, FuelType.PETROL, 500, 3.5, Status.AVAILABLE),
                new Car(2, "Apex", "Two", 200000.0, 320, FuelType.HYBRID, 800, 2.8, Status.RESERVED),
                new Car(3, "Boreal", "Three", 300000.0, 340, FuelType.ELECTRIC, 1000, 2.2, Status.AVAILABLE),
                new Car(4, "Crest", "Four", 50000.0, 250, FuelType.PETROL, 1200, 2.0, Status.SOLD)));
        return new ReportService(new InventoryService(repository, null));
    }

    public static void testExercise4TopNByHorsepower() throws Exception {
        ReportService service = createService();
        List<String> topTwo = service.topNByHorsepower(2);

        Assert.assertEquals(2, topTwo.size(), "Top N should return no more than N descriptions");
        Assert.assertTrue(topTwo.get(0).contains("Boreal Three"), "Highest horsepower active car ranks first");
        Assert.assertTrue(topTwo.get(1).contains("Apex Two"), "Second highest horsepower active car ranks next");
        Assert.assertTrue(service.topNByHorsepower(0).isEmpty(), "Non-positive N returns an empty result");
    }

    public static void testExercise5AveragePriceByBrand() throws Exception {
        Map<String, Double> averages = createService().averagePriceByBrand();

        Assert.assertEquals(150000.0, averages.get("Apex"), 0.01, "Apex average combines both Apex cars");
        Assert.assertEquals(300000.0, averages.get("Boreal"), 0.01, "Single-car brand average is its price");
        Assert.assertEquals(3, averages.size(), "Each exact brand appears once in the result");
    }
}
