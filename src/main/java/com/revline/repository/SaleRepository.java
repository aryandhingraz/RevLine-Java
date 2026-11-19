package com.revline.repository;

import com.revline.model.Sale;
import com.revline.util.exception.RepositoryException;

import java.util.List;

/**
 * Specific repository contract for Sale entities.
 */
public interface SaleRepository extends Repository<Sale, Integer> {

    /**
     * Finds all sale records associated with a specific car ID.
     *
     * @param carId target car ID
     * @return list of matching sales
     * @throws RepositoryException on persistence failure
     */
    List<Sale> findByCarId(int carId) throws RepositoryException;
}
