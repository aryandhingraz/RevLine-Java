package com.revline.service;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Batch DSL command runner powered by StreamTokenizer (L25).
 */
final class ScriptRunner {

    private ScriptRunner() {}

    static DataToolsService.ScriptImportResult run(File scriptFile, InventoryService inventoryService,
                                                   SalesService salesService) throws IOException {
        if (scriptFile == null || !scriptFile.exists()) {
            throw new IllegalArgumentException("Script file does not exist: " + scriptFile);
        }

        int executed = 0;
        int failed = 0;
        List<String> log = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
            StreamTokenizer st = new StreamTokenizer(br);
            st.resetSyntax();
            st.whitespaceChars(0, ' ');
            st.wordChars('a', 'z');
            st.wordChars('A', 'Z');
            st.wordChars('0', '9');
            st.wordChars('_', '_');
            st.wordChars('-', '-');
            st.wordChars('.', '.');
            st.quoteChar('"');
            st.commentChar('#');
            st.eolIsSignificant(true);

            int token;
            while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
                if (token == StreamTokenizer.TT_EOL) {
                    continue;
                }

                if (token == StreamTokenizer.TT_WORD) {
                    String command = st.sval;
                    if (command.equalsIgnoreCase("ADD")) {
                        try {
                            st.nextToken(); String brand = st.sval;
                            st.nextToken(); String model = st.sval;
                            st.nextToken(); double price = Double.parseDouble(st.sval);
                            st.nextToken(); int speed = Integer.parseInt(st.sval);
                            st.nextToken(); FuelType fuel = FuelType.fromString(st.sval);
                            st.nextToken(); int hp = Integer.parseInt(st.sval);
                            st.nextToken(); double accel = Double.parseDouble(st.sval);

                            Car car = new Car(1, brand, model, price, speed, fuel, hp, accel, Status.AVAILABLE);
                            if (inventoryService != null) {
                                inventoryService.addCar(car);
                            }
                            executed++;
                            log.add("Executed ADD: " + brand + " " + model);
                        } catch (Exception e) {
                            failed++;
                            log.add("Failed ADD on line " + st.lineno() + ": " + e.getMessage());
                            skipToEol(st);
                        }
                    } else if (command.equalsIgnoreCase("SELL")) {
                        try {
                            st.nextToken(); int carId = Integer.parseInt(st.sval);
                            st.nextToken(); String buyer = st.sval;
                            if (salesService != null) {
                                salesService.sellCar(carId, buyer, "ScriptAgent");
                            }
                            executed++;
                            log.add("Executed SELL: Car #" + carId + " to " + buyer);
                        } catch (Exception e) {
                            failed++;
                            log.add("Failed SELL on line " + st.lineno() + ": " + e.getMessage());
                            skipToEol(st);
                        }
                    } else if (command.equalsIgnoreCase("RESERVE")) {
                        try {
                            st.nextToken(); int carId = Integer.parseInt(st.sval);
                            st.nextToken(); String buyer = st.sval;
                            st.nextToken(); int minutes = Integer.parseInt(st.sval);
                            if (salesService != null) {
                                salesService.reserveCar(carId, buyer, minutes);
                            }
                            executed++;
                            log.add("Executed RESERVE: Car #" + carId + " for " + buyer + " (" + minutes + " min)");
                        } catch (Exception e) {
                            failed++;
                            log.add("Failed RESERVE on line " + st.lineno() + ": " + e.getMessage());
                            skipToEol(st);
                        }
                    } else {
                        failed++;
                        log.add("Unknown command '" + command + "' on line " + st.lineno());
                        skipToEol(st);
                    }
                }
            }
        }

        return new DataToolsService.ScriptImportResult(executed, failed, log);
    }

    private static void skipToEol(StreamTokenizer st) throws IOException {
        while (st.nextToken() != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF) {
            // consume remaining tokens on line
        }
    }
}
