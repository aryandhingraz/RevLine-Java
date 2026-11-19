package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.util.exception.CarNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Vehicle search and discovery service (L2, L5).
 */
public class SearchService {

    private final InventoryService inventoryService;

    public SearchService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Looks up a vehicle by ID.
     */
    public Car findById(int id) throws CarNotFoundException {
        return inventoryService.findById(id);
    }

    /**
     * Filters cars by their status (e.g. AVAILABLE).
     */
    public List<Car> findByStatus(Status status) {
        List<Car> matches = new ArrayList<>();
        for (Car c : inventoryService.findAll()) {
            if (c.getStatus() == status) {
                matches.add(c);
            }
        }
        return matches;
    }

    // =========================================================================
    // STUDENT EXERCISE EX-3 ZONE
    // =========================================================================

    /**
     * EX-3: Case-insensitive multi-field search across brand, model, and fuel type.
     * Splits query on whitespace into tokens; every token must match within
     * (brand + " " + model + " " + fuelType).
     *
     * @param query search text (e.g. "ferrari hybrid" or "porsche")
     * @return list of matching vehicles (empty if no matches)
     * @see docs/learning/EX3_HINT.md
     */
    public List<Car> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return inventoryService.findAll();
        }

        String[] tokens = query.trim().toLowerCase().split("\\s+");
        List<Car> matches = new ArrayList<>();
        for (Car car : inventoryService.findAll()) {
            String searchableText = (car.getBrand() + " " + car.getModel() + " "
                    + car.getFuelType().name()).toLowerCase();
            boolean allMatch = true;
            for (String token : tokens) {
                if (!searchableText.contains(token)) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                matches.add(car);
            }
        }
        return matches;
    }
}
