package com.revline.model;

import com.revline.util.exception.InvalidCarException;

/**
 * Abstract base class representing a general vehicle in the showroom.
 * Demonstrates inheritance, constructor chaining, and abstraction (L1).
 */
public abstract class Vehicle implements Describable {

    protected static int createdCount = 0;

    protected final int id;
    protected String brand;
    protected String model;
    protected double price;

    /**
     * Minimal constructor chaining to full constructor with default price.
     */
    public Vehicle(int id, String brand, String model) {
        this(id, brand, model, 0.0);
    }

    /**
     * Primary constructor with validation.
     */
    public Vehicle(int id, String brand, String model, double price) {
        if (id < 0) {
            throw new InvalidCarException("Vehicle ID cannot be negative.");
        }
        if (brand == null || brand.trim().isEmpty()) {
            throw new InvalidCarException("Brand name cannot be blank.");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new InvalidCarException("Model name cannot be blank.");
        }
        if (price < 0.0) {
            throw new InvalidCarException("Price cannot be negative.");
        }

        this.id = id;
        this.brand = brand.trim();
        this.model = model.trim();
        this.price = price;
        createdCount++;
    }

    public int getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new InvalidCarException("Brand name cannot be blank.");
        }
        this.brand = brand.trim();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new InvalidCarException("Model name cannot be blank.");
        }
        this.model = model.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0.0) {
            throw new InvalidCarException("Price cannot be negative.");
        }
        this.price = price;
    }

    public static int getCreatedCount() {
        return createdCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vehicle vehicle = (Vehicle) obj;
        return this.id == vehicle.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public abstract String describe();
}
