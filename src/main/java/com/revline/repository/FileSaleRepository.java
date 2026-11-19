package com.revline.repository;

import com.revline.model.Sale;
import com.revline.util.FileUtil;
import com.revline.util.exception.DataFormatException;
import com.revline.util.exception.RepositoryException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CSV file-backed implementation of SaleRepository with atomic crash-safe persistence (L26, L27).
 */
public class FileSaleRepository implements SaleRepository {

    public static final String CSV_HEADER = "saleId,carId,buyerName,sellerName,saleDate,price";

    private final File file;
    private final List<Sale> sales = new ArrayList<>();

    public FileSaleRepository(File file) throws RepositoryException {
        this.file = file;
        load();
    }

    /**
     * Loads existing sales from CSV storage.
     */
    public final synchronized void load() throws RepositoryException {
        sales.clear();
        if (!file.exists() || file.length() == 0) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                // Skip header line if present
                if (lineNumber == 1 && trimmed.toLowerCase().startsWith("saleid")) {
                    continue;
                }
                try {
                    Sale sale = Sale.fromCsv(trimmed, lineNumber);
                    sales.add(sale);
                } catch (DataFormatException e) {
                    throw new RepositoryException("Failed to parse sale on line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            if (e instanceof RepositoryException) throw (RepositoryException) e;
            throw new RepositoryException("I/O error while reading sales file: " + file.getPath(), e);
        }
    }

    @Override
    public synchronized List<Sale> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(sales));
    }

    @Override
    public synchronized Sale findById(Integer id) {
        if (id == null) return null;
        for (Sale s : sales) {
            if (s.getSaleId() == id) {
                return s;
            }
        }
        return null;
    }

    @Override
    public synchronized List<Sale> findByCarId(int carId) {
        List<Sale> result = new ArrayList<>();
        for (Sale s : sales) {
            if (s.getCarId() == carId) {
                result.add(s);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public synchronized void save(Sale sale) throws RepositoryException {
        if (sale == null) return;
        boolean updated = false;
        for (int i = 0; i < sales.size(); i++) {
            if (sales.get(i).getSaleId() == sale.getSaleId()) {
                sales.set(i, sale);
                updated = true;
                break;
            }
        }
        if (!updated) {
            sales.add(sale);
        }
        persistAll();
    }

    @Override
    public synchronized boolean deleteById(Integer id) throws RepositoryException {
        if (id == null) return false;
        Sale target = null;
        for (Sale s : sales) {
            if (s.getSaleId() == id) {
                target = s;
                break;
            }
        }
        if (target != null) {
            sales.remove(target);
            persistAll();
            return true;
        }
        return false;
    }

    @Override
    public synchronized void saveAll(List<Sale> all) throws RepositoryException {
        sales.clear();
        if (all != null) {
            sales.addAll(all);
        }
        persistAll();
    }

    @Override
    public synchronized int count() {
        return sales.size();
    }

    /**
     * Calculates the next unique sale identifier.
     */
    public synchronized int nextId() {
        int max = 0;
        for (Sale s : sales) {
            if (s.getSaleId() > max) {
                max = s.getSaleId();
            }
        }
        return max + 1;
    }

    private void persistAll() throws RepositoryException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        for (Sale s : sales) {
            lines.add(s.toCsv());
        }
        FileUtil.safeWrite(file, lines);
    }
}
