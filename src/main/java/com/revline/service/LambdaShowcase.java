package com.revline.service;

import com.revline.model.Car;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Educational showcase demonstrating Java functional programming capabilities (L21–L22).
 * Covers standard functional interfaces, custom functional interfaces, lambdas,
 * and all four categories of method references.
 */
public final class LambdaShowcase {

    /**
     * Custom functional interface for vehicle transformations (L21).
     */
    @FunctionalInterface
    public interface VehicleTransformer<T, R> {
        R transform(T input);
    }

    private LambdaShowcase() {}

    /**
     * Evaluates a Predicate<Car> against a vehicle (L21).
     */
    public static boolean testPredicate(Car car, Predicate<Car> condition) {
        return condition != null && condition.test(car);
    }

    /**
     * Applies a Function<Car, R> mapping over a vehicle (L21).
     */
    public static <R> R applyFunction(Car car, Function<Car, R> mapper) {
        return (mapper != null) ? mapper.apply(car) : null;
    }

    /**
     * Consumes a vehicle with a Consumer<Car> action (L21).
     */
    public static void executeConsumer(Car car, Consumer<Car> action) {
        if (action != null) {
            action.accept(car);
        }
    }

    /**
     * Supplies an object via a Supplier<T> provider (L21).
     */
    public static <T> T provideValue(Supplier<T> supplier) {
        return (supplier != null) ? supplier.get() : null;
    }

    /**
     * Executes custom VehicleTransformer functional interface.
     */
    public static <T, R> R executeTransformer(T input, VehicleTransformer<T, R> transformer) {
        return (transformer != null) ? transformer.transform(input) : null;
    }

    // =========================================================================
    // Four Categories of Method References (L22)
    // =========================================================================

    /**
     * Category 1: Reference to a Static Method (e.g. Integer::compare).
     */
    public static int staticMethodRef(int a, int b) {
        java.util.Comparator<Integer> comparator = Integer::compare;
        return comparator.compare(a, b);
    }

    /**
     * Category 2: Reference to an Instance Method of a Particular Object (Bound Instance Ref)
     * (e.g. System.out::print).
     */
    public static void boundInstanceMethodRef(PrintStream out, String text) {
        Consumer<String> printer = out::print;
        printer.accept(text);
    }

    /**
     * Category 3: Reference to an Instance Method of an Arbitrary Object of a Particular Type (Unbound Instance Ref)
     * (e.g. Car::getPrice).
     */
    public static double unboundInstanceMethodRef(Car car) {
        Function<Car, Double> priceExtractor = Car::getPrice;
        return priceExtractor.apply(car);
    }

    /**
     * Category 4: Reference to a Constructor (Constructor Ref)
     * (e.g. ArrayList::new).
     */
    public static <T> List<T> constructorMethodRef() {
        Supplier<List<T>> listFactory = ArrayList::new;
        return listFactory.get();
    }
}
