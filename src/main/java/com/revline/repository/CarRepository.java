package com.revline.repository;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.util.exception.RepositoryException;

import java.util.List;

/**
 * Specific repository contract for Car entities.
 */
public interface CarRepository extends Repository<Car, Integer> {

    /**
     * Finds cars matching a specific status (e.g. AVAILABLE).
     *
     * @param status target status
     * @return list of matching cars
     * @throws RepositoryException on persistence failure
     */
    List<Car> findByStatus(Status status) throws RepositoryException;

    /**
     * Finds cars within a price band [minPrice, maxPrice] inclusive.
     *
     * @param minPrice lower price boundary
     * @param maxPrice upper price boundary
     * @return list of matching cars
     * @throws RepositoryException on persistence failure
     */
    List<Car> findByPriceRange(double minPrice, double maxPrice) throws RepositoryException;

    /**
     * Generates or calculates the next unique integer ID for a new car.
     *
     * @return next unique ID
     * @throws RepositoryException on failure
     */
    int nextId() throws RepositoryException;
}
