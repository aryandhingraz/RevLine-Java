# Module 4: JDBC Persistence & Relational Transactions (L17–L20)

> **Module 4**: Relational Persistence & SQL Security  
> **Target Milestone**: Phase 4 (`v0.4-jdbc`)  
> **Target Syllabus**: 25CSE0202 Programming in Java (Lectures 17, 18, 19, 20)

---

## 1. Architectural Overview

RevLine supports a **pluggable dual-persistence architecture**:
1. **Flat-File Mode (`FileCarRepository`, `FileSaleRepository`)**: High-speed, human-readable CSV storage with atomic crash-safe writes (`FileUtil.safeWrite`).
2. **Relational Mode (`JdbcCarRepository`, `JdbcSaleRepository`)**: ACID-compliant relational persistence powered by **SQLite JDBC** (`sqlite-jdbc-3.54.1.0.jar`) with schema DDL, foreign key enforcement, and transactional integrity.

Because `InventoryService` and `SalesService` depend exclusively on the generic `CarRepository` and `SaleRepository` interfaces rather than concrete storage classes, switching between CSV and SQLite is accomplished via constructor dependency injection with zero modifications to business logic.

---

## 2. Relational Schema & Relational Integrity (L17–L18)

The relational schema is defined in `sql/schema.sql` and initialized programmatically by `SchemaInitializer`:

```sql
CREATE TABLE IF NOT EXISTS cars (
  car_id        INTEGER PRIMARY KEY,
  brand         VARCHAR(50)   NOT NULL,
  model         VARCHAR(50)   NOT NULL,
  price         DECIMAL(12,2) NOT NULL CHECK (price > 0),
  top_speed     INTEGER       NOT NULL CHECK (top_speed BETWEEN 50 AND 600),
  fuel_type     VARCHAR(20)   NOT NULL CHECK (fuel_type IN ('PETROL','HYBRID','ELECTRIC')),
  horsepower    INTEGER       NOT NULL CHECK (horsepower BETWEEN 50 AND 3000),
  acceleration  DECIMAL(4,2)  NOT NULL CHECK (acceleration BETWEEN 1 AND 20),
  status        VARCHAR(10)   NOT NULL CHECK (status IN ('AVAILABLE','RESERVED','SOLD')),
  UNIQUE (brand, model)
);

CREATE TABLE IF NOT EXISTS sales (
  sale_id      INTEGER PRIMARY KEY,
  car_id       INTEGER       NOT NULL,
  seller_name  VARCHAR(60)   NOT NULL,
  buyer_name   VARCHAR(60)   NOT NULL,
  sale_date    DATE          NOT NULL,
  price        DECIMAL(12,2) NOT NULL CHECK (price > 0),
  FOREIGN KEY (car_id) REFERENCES cars(car_id)
);
```

### Relational Constraints Enforced:
- **Primary Key (`PRIMARY KEY`)**: Guarantees uniqueness for `car_id` and `sale_id`.
- **Foreign Key (`FOREIGN KEY (car_id) REFERENCES cars(car_id)`)**: Prohibits orphaned sales records. Enabled explicitly on every SQLite connection via:
  ```java
  statement.execute("PRAGMA foreign_keys = ON;");
  ```
- **Check Constraints (`CHECK`)**: Enforces domain boundaries on price, horsepower, speed, and enums directly at the database engine level.
- **Candidate Key (`UNIQUE(brand, model)`)**: Database-level duplicate protection matching `InventoryService.duplicateGuard`.

---

## 3. Secure SQL: PreparedStatements vs Statements (L19)

### 3.1 Parameter Binding vs String Concatenation
In RevLine, **all DML statements (SELECT, INSERT, UPDATE, DELETE) exclusively use `PreparedStatement`**. String concatenation is strictly prohibited in SQL generation.

```java
// SECURE (RevLine Implementation)
String sql = "SELECT * FROM cars WHERE brand = ? AND status = ?";
try (PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, brand);
    ps.setString(2, status.name());
    try (ResultSet rs = ps.executeQuery()) {
        return mapRows(rs);
    }
}
```

### 3.2 SQL Injection Proof (`SqlInjectionTest`)
When an attacker inputs a payload such as `' OR '1'='1`:
- **Unsafe String Concatenation**:
  `SELECT * FROM cars WHERE brand = '' OR '1'='1'`  
  The database parser treats `'1'='1'` as a tautology, dumping all records in the table.
- **PreparedStatement (Secure)**:  
  The database engine precompiles the SQL query structure. The payload is sent separately in a protocol parameter packet and evaluated strictly as a literal string value (`brand = "' OR '1'='1"`), returning zero matching rows.

---

## 4. ACID Relational Transactions (L17–L20)

### 4.1 The Sale Transaction Flow
A vehicle sale requires two distinct database mutations:
1. Updating `cars` status to `SOLD`.
2. Inserting the transaction record into `sales`.

If step 1 succeeds but step 2 fails (e.g. disk full, constraint violation), the database would be left in an inconsistent state: a car marked `SOLD` with no financial transaction ledger record.

### 4.2 Two-Phase Transaction in `JdbcSaleRepository.recordSaleAtomic`:
```java
boolean previousAutoCommit = connection.getAutoCommit();
try {
    connection.setAutoCommit(false); // Begin transaction block

    // Step 1: Update vehicle state
    try (PreparedStatement ps = connection.prepareStatement("UPDATE cars SET status = 'SOLD' WHERE car_id = ?")) {
        ps.setInt(1, car.getId());
        ps.executeUpdate();
    }

    // Step 2: Insert sales ledger entry
    try (PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO sales (sale_id, car_id, buyer_name, seller_name, sale_date, price) VALUES (?, ?, ?, ?, ?, ?)")) {
        ps.setInt(1, sale.getSaleId());
        ps.setInt(2, sale.getCarId());
        ps.setString(3, sale.getBuyerName());
        ps.setString(4, sale.getSellerName());
        ps.setString(5, sale.getSaleDate().toString());
        ps.setDouble(6, sale.getPrice());
        ps.executeUpdate();
    }

    connection.commit(); // Atomically commit both operations
} catch (SQLException e) {
    connection.rollback(); // Rollback on any failure
    throw new RepositoryException("Transaction failed. Rollback executed.", e);
} finally {
    connection.setAutoCommit(previousAutoCommit); // Restore auto-commit state
}
```

---

## 5. Anticipated Viva Questions & Model Answers

### Q1: What is the difference between `Statement` and `PreparedStatement` in JDBC?
> **Answer**: `Statement` sends raw SQL text to the database engine where it must be parsed and compiled on every execution, making it slower for repeated queries and vulnerable to SQL injection if input is concatenated. `PreparedStatement` precompiles the query structure in advance and accepts parameters via typed setter methods (`setInt`, `setString`). This provides precompilation performance gains and guarantees immunity to SQL injection because parameters are treated purely as literal values, never executable SQL commands.

### Q2: Why is `PRAGMA foreign_keys = ON;` required when connecting to SQLite?
> **Answer**: By default, SQLite disables foreign key constraint enforcement for backwards compatibility with legacy database formats. To maintain relational integrity and ensure child records in `sales` cannot reference nonexistent `car_id` values, `DatabaseManager` explicitly executes `PRAGMA foreign_keys = ON;` immediately after establishing each connection.

### Q3: How do you manage database transactions in JDBC without an external framework like Spring?
> **Answer**: In JDBC, transactions are managed manually via `Connection.setAutoCommit(false)`, which marks the boundary of a single atomic transaction. Multiple SQL operations (such as updating car status and inserting a sale record) are executed sequentially. If all succeed, `connection.commit()` commits the changes permanently. If any operation throws a `SQLException`, `connection.rollback()` reverts all changes made within the block. Finally, the original auto-commit state is restored in a `finally` block.

### Q4: Explain the difference between `executeQuery()` and `executeUpdate()`.
> **Answer**: `executeQuery()` is used exclusively for queries that retrieve data (`SELECT`). It returns a `ResultSet` cursor pointing before the first row. `executeUpdate()` is used for Data Manipulation Language (DML) statements (`INSERT`, `UPDATE`, `DELETE`) or DDL statements. It returns an `int` representing the number of rows affected by the statement (or 0 for DDL statements), rather than a result set.

### Q5: How does RevLine ensure the same test suite passes in both CSV and JDBC modes?
> **Answer**: RevLine defines generic storage contracts via `CarRepository` and `SaleRepository` interfaces. In `RepositoryContractTest`, a single parametric test harness executes the identical set of assertions (CRUD operations, next ID generation, price range lookups, status filtering) against both `FileCarRepository` and `JdbcCarRepository`. This proves behavioral parity between the file-backed and relational storage engines.
