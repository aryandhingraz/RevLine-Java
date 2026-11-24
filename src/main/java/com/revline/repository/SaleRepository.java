package com.revline.repository;

import com.revline.model.Car;
import com.revline.model.Sale;
import com.revline.util.exception.RepositoryException;

import java.util.List;

/**
 * Specific repository contract for Sale entities (L1, L24).
 * Declares storage operations for recording transactions and querying sales history.
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

    /**
     * Generates or calculates the next unique integer ID for a new sale transaction.
     *
     * @return next unique ID
     * @throws RepositoryException on failure
     */
    int nextId() throws RepositoryException;

    /**
     * Persists a sale transaction safely to underlying storage (L19, L26).
     * In JDBC mode, executes an atomic relational transaction (updates car status to SOLD and saves sale).
     * In CSV flat-file mode, saves the sale record to the sales ledger file.
     *
     * @param sale completed sale transaction entity
     * @param soldCar car entity associated with the transaction
     * @throws RepositoryException on storage or relational transaction failure
     */
    void recordSale(Sale sale, Car soldCar) throws RepositoryException;
}
