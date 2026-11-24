package com.revline.service;

import com.revline.model.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Side-by-side vehicle comparison service.
 * Builds multi-dimensional comparison matrices (L2 Multi-dimensional Arrays)
 * and determines winning specifications across key performance indicators.
 */
public class ComparisonService {

    public static final String[] METRIC_LABELS = {
            "Vehicle ID",
            "Make & Model",
            "Price ($)",
            "Top Speed (km/h)",
            "Horsepower (HP)",
            "0-100 km/h (sec)",
            "Fuel Type",
            "Availability Status"
    };

    /**
     * Holds the structured 2D comparison matrix and winning indicators.
     */
    public static class ComparisonResult {
        private final List<Car> cars;
        private final String[][] grid;
        private final int[] winnerColumnPerRow;

        public ComparisonResult(List<Car> cars, String[][] grid, int[] winnerColumnPerRow) {
            this.cars = Collections.unmodifiableList(new ArrayList<>(cars));
            this.grid = grid;
            this.winnerColumnPerRow = winnerColumnPerRow;
        }

        public List<Car> getCars() {
            return cars;
        }

        public String[][] getGrid() {
            return grid;
        }

        public int[] getWinnerColumnPerRow() {
            return winnerColumnPerRow;
        }

        public String formatTable() {
            int numCars = cars.size();
            int numRows = METRIC_LABELS.length;

            int col0Width = 22;
            int carColWidth = 26;

            StringBuilder sb = new StringBuilder();
            String divider = buildDivider(col0Width, carColWidth, numCars);

            sb.append(divider).append(System.lineSeparator());
            sb.append(String.format("| %-" + col0Width + "s ", "SPECIFICATION"));
            for (int j = 0; j < numCars; j++) {
                String header = "CAR #" + cars.get(j).getId();
                sb.append(String.format("| %-" + carColWidth + "s ", header));
            }
            sb.append("|").append(System.lineSeparator());
            sb.append(divider).append(System.lineSeparator());

            for (int r = 0; r < numRows; r++) {
                sb.append(String.format("| %-" + col0Width + "s ", METRIC_LABELS[r]));
                int bestCol = winnerColumnPerRow[r];
                for (int c = 0; c < numCars; c++) {
                    String val = grid[r][c];
                    if (c == bestCol && bestCol >= 0) {
                        val = val + " *BEST";
                    }
                    sb.append(String.format("| %-" + carColWidth + "s ", val));
                }
                sb.append("|").append(System.lineSeparator());
            }

            sb.append(divider).append(System.lineSeparator());
            return sb.toString();
        }

        private String buildDivider(int col0Width, int carColWidth, int numCars) {
            StringBuilder sb = new StringBuilder();
            sb.append("+").append("-".repeat(col0Width + 2));
            for (int i = 0; i < numCars; i++) {
                sb.append("+").append("-".repeat(carColWidth + 2));
            }
            sb.append("+");
            return sb.toString();
        }
    }

    public ComparisonService() {}

    /**
     * Compares 2 to 4 cars side-by-side across performance and cost dimensions.
     *
     * @param cars list of cars to compare
     * @return ComparisonResult containing 2D grid matrix and winner indices
     */
    public ComparisonResult compare(List<Car> cars) {
        if (cars == null || cars.size() < 2 || cars.size() > 4) {
            throw new IllegalArgumentException("Comparison requires between 2 and 4 cars.");
        }

        int numCars = cars.size();
        int numRows = METRIC_LABELS.length;

        // Multi-dimensional array representing grid[row][column] (L2)
        String[][] grid = new String[numRows][numCars];
        int[] winnerCol = new int[numRows];
        for (int i = 0; i < numRows; i++) {
            winnerCol[i] = -1; // -1 means no winner for this row
        }

        populateGrid(cars, grid);
        calculateRowWinners(cars, winnerCol);

        return new ComparisonResult(cars, grid, winnerCol);
    }

    private void populateGrid(List<Car> cars, String[][] grid) {
        for (int c = 0; c < cars.size(); c++) {
            Car car = cars.get(c);
            grid[0][c] = "#" + car.getId();
            grid[1][c] = car.getBrand() + " " + car.getModel();
            grid[2][c] = String.format("$%,.2f", car.getPrice());
            grid[3][c] = car.getTopSpeed() + " km/h";
            grid[4][c] = car.getHorsepower() + " HP";
            grid[5][c] = String.format("%.2f s", car.getAcceleration());
            grid[6][c] = car.getFuelType().name();
            grid[7][c] = car.getStatus().name();
        }
    }

    private void calculateRowWinners(List<Car> cars, int[] winnerCol) {
        // Determine winners per metric (L2 algorithm + min/max analysis)
        winnerCol[2] = findMinDouble(cars, Car::getPrice);          // Price: lowest wins
        winnerCol[3] = findMaxInt(cars, Car::getTopSpeed);          // Top Speed: highest wins
        winnerCol[4] = findMaxInt(cars, Car::getHorsepower);        // Horsepower: highest wins
        winnerCol[5] = findMinDouble(cars, Car::getAcceleration);    // Acceleration: lowest wins
    }

    private interface DoubleMetricExtractor {
        double extract(Car c);
    }

    private interface IntMetricExtractor {
        int extract(Car c);
    }

    private int findMinDouble(List<Car> cars, DoubleMetricExtractor extractor) {
        int bestIdx = 0;
        double minVal = extractor.extract(cars.get(0));
        boolean tie = false;

        for (int i = 1; i < cars.size(); i++) {
            double v = extractor.extract(cars.get(i));
            if (v < minVal) {
                minVal = v;
                bestIdx = i;
                tie = false;
            } else if (Double.compare(v, minVal) == 0) {
                tie = true;
            }
        }
        return tie ? -1 : bestIdx;
    }

    private int findMaxInt(List<Car> cars, IntMetricExtractor extractor) {
        int bestIdx = 0;
        int maxVal = extractor.extract(cars.get(0));
        boolean tie = false;

        for (int i = 1; i < cars.size(); i++) {
            int v = extractor.extract(cars.get(i));
            if (v > maxVal) {
                maxVal = v;
                bestIdx = i;
                tie = false;
            } else if (v == maxVal) {
                tie = true;
            }
        }
        return tie ? -1 : bestIdx;
    }
}
