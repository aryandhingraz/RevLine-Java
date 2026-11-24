package com.revline.model;

import com.revline.util.exception.DataFormatException;
import com.revline.util.exception.InvalidCarException;

import java.util.Locale;

/**
 * Concrete Car entity representing a showroom vehicle.
 * Implements Comparable for natural ID sorting (L8).
 */
public class Car extends Vehicle implements Comparable<Car> {

    private int topSpeed;
    private int horsepower;
    private double acceleration;
    private FuelType fuelType;
    private Status status;

    /**
     * Default constructor with sensible showroom defaults.
     */
    public Car(int id, String brand, String model, double price) {
        this(id, brand, model, price, 200, FuelType.PETROL, 300, 4.5, Status.AVAILABLE);
    }

    /**
     * Full constructor with complete validation.
     */
    public Car(int id, String brand, String model, double price,
               int topSpeed, FuelType fuelType, int horsepower,
               double acceleration, Status status) {
        super(id, brand, model, price);

        if (topSpeed < 50 || topSpeed > 600) {
            throw new InvalidCarException("Top speed must be between 50 and 600 km/h.");
        }
        if (horsepower < 50 || horsepower > 3000) {
            throw new InvalidCarException("Horsepower must be between 50 and 3000 HP.");
        }
        if (acceleration < 1.0 || acceleration > 20.0) {
            throw new InvalidCarException("Acceleration (0-100) must be between 1.0 and 20.0 seconds.");
        }
        if (fuelType == null) {
            throw new InvalidCarException("Fuel type cannot be null.");
        }
        if (status == null) {
            throw new InvalidCarException("Status cannot be null.");
        }

        this.topSpeed = topSpeed;
        this.fuelType = fuelType;
        this.horsepower = horsepower;
        this.acceleration = acceleration;
        this.status = status;
    }

    public int getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(int topSpeed) {
        if (topSpeed < 50 || topSpeed > 600) {
            throw new InvalidCarException("Top speed must be between 50 and 600 km/h.");
        }
        this.topSpeed = topSpeed;
    }

    public int getHorsepower() {
        return horsepower;
    }

    public void setHorsepower(int horsepower) {
        if (horsepower < 50 || horsepower > 3000) {
            throw new InvalidCarException("Horsepower must be between 50 and 3000 HP.");
        }
        this.horsepower = horsepower;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        if (acceleration < 1.0 || acceleration > 20.0) {
            throw new InvalidCarException("Acceleration must be between 1.0 and 20.0 seconds.");
        }
        this.acceleration = acceleration;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        if (fuelType == null) {
            throw new InvalidCarException("Fuel type cannot be null.");
        }
        this.fuelType = fuelType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new InvalidCarException("Status cannot be null.");
        }
        if (this.status == Status.SOLD && status != Status.SOLD) {
            throw new IllegalStateException("Cannot change status of a SOLD car. Terminal state is immutable.");
        }
        this.status = status;
    }

    /**
     * Creates an independent deep copy of this car.
     */
    public Car copy() {
        return new Car(this.id, this.brand, this.model, this.price,
                       this.topSpeed, this.fuelType, this.horsepower,
                       this.acceleration, this.status);
    }

    @Override
    public int compareTo(Car other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String describe() {
        return String.format("#%d: %s %s | $%,.2f | %d km/h | %d HP | %.1fs 0-100 | %s | %s",
                id, brand, model, price, topSpeed, horsepower, acceleration, fuelType.label(), status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Car{id=").append(id)
          .append(", brand='").append(brand).append('\'')
          .append(", model='").append(model).append('\'')
          .append(", price=").append(price)
          .append(", topSpeed=").append(topSpeed)
          .append(", fuelType=").append(fuelType)
          .append(", horsepower=").append(horsepower)
          .append(", acceleration=").append(acceleration)
          .append(", status=").append(status)
          .append('}');
        return sb.toString();
    }

    /**
     * Serializes this Car to a single standard CSV row.
     */
    public String toCsv() {
        return String.format(Locale.US, "%d,%s,%s,%.2f,%d,%s,%d,%.2f,%s",
                id, brand, model, price, topSpeed, fuelType.name(), horsepower, acceleration, status.name());
    }

    /**
     * Parses a CSV row into a Car object.
     *
     * @param line comma-delimited text row
     * @return parsed Car
     * @throws DataFormatException if line does not contain 9 valid fields
     */
    public static Car fromCsv(String line, int lineNumber) throws DataFormatException {
        if (line == null || line.trim().isEmpty()) {
            throw new DataFormatException("Empty CSV line", lineNumber);
        }
        String[] parts = line.split(",", -1);
        if (parts.length != 9) {
            throw new DataFormatException("Expected 9 fields, but found " + parts.length, lineNumber);
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String brand = parts[1].trim();
            String model = parts[2].trim();
            double price = Double.parseDouble(parts[3].trim());
            int topSpeed = Integer.parseInt(parts[4].trim());
            FuelType fuelType = FuelType.fromString(parts[5].trim());
            int horsepower = Integer.parseInt(parts[6].trim());
            double acceleration = Double.parseDouble(parts[7].trim());
            Status status = Status.valueOf(parts[8].trim().toUpperCase());

            return new Car(id, brand, model, price, topSpeed, fuelType, horsepower, acceleration, status);
        } catch (NumberFormatException e) {
            throw new DataFormatException("Numeric parse error: " + e.getMessage(), lineNumber, e);
        } catch (IllegalArgumentException | InvalidCarException e) {
            throw new DataFormatException("Validation error: " + e.getMessage(), lineNumber, e);
        }
    }
}
