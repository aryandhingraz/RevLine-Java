package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Status;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Stream-based showroom reports for inventory analytics (L21-L23).
 */
public class ReportService {

    private final InventoryService inventoryService;

    public ReportService(InventoryService inventoryService) {
        if (inventoryService == null) {
            throw new IllegalArgumentException("InventoryService cannot be null.");
        }
        this.inventoryService = inventoryService;
    }

    /**
     * Returns descriptions of up to {@code n} non-sold cars, highest horsepower first.
     */
    public List<String> topNByHorsepower(int n) {
        if (n <= 0) {
            return Collections.emptyList();
        }
        return inventoryService.findAll().stream()
                .filter(car -> car.getStatus() != Status.SOLD)
                .sorted((first, second) -> Integer.compare(second.getHorsepower(), first.getHorsepower()))
                .limit(n)
                .map(Car::describe)
                .collect(Collectors.toList());
    }

    /**
     * Returns the average price of inventory cars grouped by exact brand name.
     */
    public Map<String, Double> averagePriceByBrand() {
        return inventoryService.findAll().stream()
                .collect(Collectors.groupingBy(Car::getBrand,
                        Collectors.averagingDouble(Car::getPrice)));
    }
}
