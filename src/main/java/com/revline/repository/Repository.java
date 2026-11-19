package com.revline.repository;

import com.revline.util.exception.RepositoryException;

import java.util.List;

/**
 * Generic CRUD repository interface demonstrating type parameters (L24 Generics).
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
public interface Repository<T, ID> {

    /**
     * Retrieves all entities.
     *
     * @return unmodifiable or new list of all records
     * @throws RepositoryException if underlying persistence fails
     */
    List<T> findAll() throws RepositoryException;

    /**
     * Finds an entity by its identifier.
     *
     * @param id primary identifier
     * @return found entity, or null if absent
     * @throws RepositoryException if query fails
     */
    T findById(ID id) throws RepositoryException;

    /**
     * Inserts or updates an entity in storage.
     *
     * @param entity record to save
     * @throws RepositoryException on persistence failure
     */
    void save(T entity) throws RepositoryException;

    /**
     * Deletes an entity by its identifier.
     *
     * @param id record ID
     * @return true if deleted, false if not found
     * @throws RepositoryException on failure
     */
    boolean deleteById(ID id) throws RepositoryException;

    /**
     * Saves the entire collection of entities to storage.
     *
     * @param all full list of entities
     * @throws RepositoryException on failure
     */
    void saveAll(List<T> all) throws RepositoryException;

    /**
     * Returns total number of records.
     *
     * @return count
     * @throws RepositoryException on failure
     */
    int count() throws RepositoryException;
}
