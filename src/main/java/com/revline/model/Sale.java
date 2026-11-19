package com.revline.model;

import com.revline.util.exception.DataFormatException;
import com.revline.util.exception.InvalidCarException;

import java.time.LocalDate;

/**
 * Immutable transaction record representing a completed car sale.
 */
public class Sale {

    private final int saleId;
    private final int carId;
    private final String buyerName;
    private final String sellerName;
    private final LocalDate saleDate;
    private final double price;

    public Sale(int saleId, int carId, String buyerName, String sellerName, LocalDate saleDate, double price) {
        if (saleId <= 0) {
            throw new InvalidCarException("Sale ID must be positive.");
        }
        if (carId <= 0) {
            throw new InvalidCarException("Car ID must be positive.");
        }
        if (buyerName == null || buyerName.trim().isEmpty()) {
            throw new InvalidCarException("Buyer name cannot be blank.");
        }
        if (sellerName == null || sellerName.trim().isEmpty()) {
            throw new InvalidCarException("Seller name cannot be blank.");
        }
        if (saleDate == null) {
            throw new InvalidCarException("Sale date cannot be null.");
        }
        if (price <= 0.0) {
            throw new InvalidCarException("Sale price must be positive.");
        }

        this.saleId = saleId;
        this.carId = carId;
        this.buyerName = buyerName.trim();
        this.sellerName = sellerName.trim();
        this.saleDate = saleDate;
        this.price = price;
    }

    public int getSaleId() {
        return saleId;
    }

    public int getCarId() {
        return carId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public double getPrice() {
        return price;
    }

    public String toCsv() {
        return String.format("%d,%d,%s,%s,%s,%.2f",
                saleId, carId, buyerName, sellerName, saleDate.toString(), price);
    }

    public static Sale fromCsv(String line, int lineNumber) throws DataFormatException {
        if (line == null || line.trim().isEmpty()) {
            throw new DataFormatException("Empty CSV line for Sale", lineNumber);
        }
        String[] parts = line.split(",", -1);
        if (parts.length != 6) {
            throw new DataFormatException("Expected 6 fields for Sale, found " + parts.length, lineNumber);
        }

        try {
            int saleId = Integer.parseInt(parts[0].trim());
            int carId = Integer.parseInt(parts[1].trim());
            String buyerName = parts[2].trim();
            String sellerName = parts[3].trim();
            LocalDate saleDate = LocalDate.parse(parts[4].trim());
            double price = Double.parseDouble(parts[5].trim());

            return new Sale(saleId, carId, buyerName, sellerName, saleDate, price);
        } catch (Exception e) {
            throw new DataFormatException("Error parsing Sale row: " + e.getMessage(), lineNumber, e);
        }
    }

    @Override
    public String toString() {
        return String.format("Sale #%d [Car #%d]: %s bought from %s on %s for $%,.2f",
                saleId, carId, buyerName, sellerName, saleDate, price);
    }
}
