package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Reservation;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.repository.FileCarRepository;
import com.revline.repository.FileSaleRepository;
import com.revline.repository.SaleRepository;
import com.revline.service.InventoryService;
import com.revline.service.ReservationManager;
import com.revline.service.SalesService;
import com.revline.service.UndoManager;
import com.revline.service.WaitlistService;
import com.revline.util.FileUtil;
import com.revline.util.exception.CarNotAvailableException;
import com.revline.util.exception.RevLineException;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Tests for showroom sales, reservations, receipts, and state transitions (L6-7, L13-14).
 */
public class SalesServiceTest {

    private static class TestFixture {
        final File carsFile;
        final File salesFile;
        final InventoryService inventoryService;
        final SalesService salesService;
        final ReservationManager reservationManager;
        final WaitlistService waitlistService;

        TestFixture() throws RevLineException {
            carsFile = new File("build/test_sales_cars.csv");
            salesFile = new File("build/test_sales_sales.csv");
            if (carsFile.exists()) carsFile.delete();
            if (salesFile.exists()) salesFile.delete();

            FileUtil.safeWrite(carsFile, Collections.singletonList(FileCarRepository.CSV_HEADER));
            FileUtil.safeWrite(salesFile, Collections.singletonList(FileSaleRepository.CSV_HEADER));

            CarRepository carRepo = new FileCarRepository(carsFile);
            SaleRepository saleRepo = new FileSaleRepository(salesFile);

            inventoryService = new InventoryService(carRepo, new UndoManager(), null);
            reservationManager = new ReservationManager();
            waitlistService = new WaitlistService();
            salesService = new SalesService(inventoryService, saleRepo, reservationManager, waitlistService, null);
        }

        void cleanup() {
            if (carsFile.exists()) carsFile.delete();
            if (salesFile.exists()) salesFile.delete();
        }
    }

    public static void testSellCarSuccessMovesStatusToSold() throws RevLineException {
        TestFixture fix = new TestFixture();
        try {
            Car car = fix.inventoryService.addCar(
                    new Car(0, "Ferrari", "Daytona SP3", 2250000.0, 340, FuelType.PETROL, 840, 2.85, Status.AVAILABLE));
            int carId = car.getId();

            Sale sale = fix.salesService.sellCar(carId, "Charles Leclerc", "RevLine Showroom");
            Assert.assertNotNull(sale, "Sale record generated");
            Assert.assertEquals(carId, sale.getCarId(), "Car ID matches in sale");
            Assert.assertEquals("Charles Leclerc", sale.getBuyerName(), "Buyer matches in sale");

            Car afterSale = fix.inventoryService.findById(carId);
            Assert.assertEquals(Status.SOLD, afterSale.getStatus(), "Car status transitioned to SOLD");
            Assert.assertEquals(1, fix.salesService.getSalesHistory().size(), "Sales history holds 1 record");
        } finally {
            fix.cleanup();
        }
    }

    public static void testSellCarFailsIfAlreadySold() throws RevLineException {
        TestFixture fix = new TestFixture();
        try {
            Car car = fix.inventoryService.addCar(
                    new Car(0, "Porsche", "918 Spyder", 845000.0, 345, FuelType.HYBRID, 887, 2.6, Status.AVAILABLE));
            int carId = car.getId();

            fix.salesService.sellCar(carId, "Buyer One", "Seller");

            boolean caught = false;
            try {
                fix.salesService.sellCar(carId, "Buyer Two", "Seller");
            } catch (CarNotAvailableException e) {
                caught = true;
            }
            Assert.assertTrue(caught, "Selling already sold car must throw CarNotAvailableException");
        } finally {
            fix.cleanup();
        }
    }

    public static void testSellCarFailsIfReservedByAnother() throws RevLineException {
        TestFixture fix = new TestFixture();
        try {
            Car car = fix.inventoryService.addCar(
                    new Car(0, "Lamborghini", "Revuelto", 608000.0, 350, FuelType.HYBRID, 1015, 2.5, Status.AVAILABLE));
            int carId = car.getId();

            fix.salesService.reserveCar(carId, "Original Reserver", 60);

            boolean caught = false;
            try {
                fix.salesService.sellCar(carId, "Intruder Buyer", "Seller");
            } catch (CarNotAvailableException e) {
                caught = true;
            }
            Assert.assertTrue(caught, "Selling to someone other than reserving buyer must throw CarNotAvailableException");
        } finally {
            fix.cleanup();
        }
    }

    public static void testSellCarSucceedsIfReservedBySameBuyer() throws RevLineException {
        TestFixture fix = new TestFixture();
        try {
            Car car = fix.inventoryService.addCar(
                    new Car(0, "McLaren", "P1", 1150000.0, 350, FuelType.HYBRID, 916, 2.8, Status.AVAILABLE));
            int carId = car.getId();

            fix.salesService.reserveCar(carId, "Legit Buyer", 60);
            Sale sale = fix.salesService.sellCar(carId, "Legit Buyer", "Seller");

            Assert.assertNotNull(sale, "Sale succeeds for legit reserving buyer");
            Assert.assertEquals(Status.SOLD, fix.inventoryService.findById(carId).getStatus(), "Car is now SOLD");
            Assert.assertFalse(fix.reservationManager.isReserved(carId), "Active reservation cleared upon sale");
        } finally {
            fix.cleanup();
        }
    }

    public static void testReserveAndCancelIntegration() throws RevLineException {
        TestFixture fix = new TestFixture();
        try {
            Car car = fix.inventoryService.addCar(
                    new Car(0, "Koenigsegg", "Jesko", 3400000.0, 480, FuelType.PETROL, 1600, 2.5, Status.AVAILABLE));
            int carId = car.getId();

            fix.salesService.reserveCar(carId, "Christian", 30);
            Assert.assertEquals(Status.RESERVED, fix.inventoryService.findById(carId).getStatus(), "Car is RESERVED");

            fix.salesService.cancelReservation(carId, "Christian");
            Assert.assertEquals(Status.AVAILABLE, fix.inventoryService.findById(carId).getStatus(), "Car reverted to AVAILABLE");
        } finally {
            fix.cleanup();
        }
    }

    public static void testGenerateReceiptFormat() throws RevLineException {
        TestFixture fix = new TestFixture();
        try {
            Car car = new Car(1, "Bugatti", "Chiron", 3000000.0, 420, FuelType.PETROL, 1500, 2.4, Status.SOLD);
            Sale sale = new Sale(101, 1, "Bruce Wayne", "Alfred", java.time.LocalDate.of(2026, 5, 20), 3000000.0);

            String receipt = fix.salesService.generateReceipt(sale, car);
            Assert.assertNotNull(receipt, "Receipt generated");
            Assert.assertTrue(receipt.contains("OFFICIAL SALES RECEIPT"), "Receipt contains header");
            Assert.assertTrue(receipt.contains("Bruce Wayne"), "Receipt contains buyer name");
            Assert.assertTrue(receipt.contains("Bugatti Chiron"), "Receipt contains vehicle make");
            Assert.assertTrue(receipt.contains("$3,000,000.00"), "Receipt contains formatted price");
        } finally {
            fix.cleanup();
        }
    }

    public static void testPurgeExpiredReservationsRevertsStatus() throws RevLineException {
        TestFixture fix = new TestFixture();
        try {
            Car car = fix.inventoryService.addCar(
                    new Car(0, "Pagani", "Utopia", 2500000.0, 354, FuelType.PETROL, 864, 2.9, Status.AVAILABLE));
            int carId = car.getId();

            // Set car to RESERVED in inventory
            fix.inventoryService.updateCarStatus(carId, Status.RESERVED);

            // Add expired reservation directly to reservationManager
            fix.reservationManager.addReservationDirectly(
                    new Reservation(carId, "Late Guy", LocalDateTime.now().minusMinutes(5)));

            Assert.assertEquals(Status.RESERVED, fix.inventoryService.findById(carId).getStatus(), "Car is RESERVED before purge");

            fix.salesService.purgeExpiredReservations();

            Assert.assertEquals(Status.AVAILABLE, fix.inventoryService.findById(carId).getStatus(), "Car reverted to AVAILABLE after purge");
        } finally {
            fix.cleanup();
        }
    }
}
