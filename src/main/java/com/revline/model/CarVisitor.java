package com.revline.model;

/**
 * Functional interface for visiting or processing a Car instance (L21).
 */
@FunctionalInterface
public interface CarVisitor {

    /**
     * Applies an operation to the given Car.
     *
     * @param car vehicle to process
     */
    void visit(Car car);
}
