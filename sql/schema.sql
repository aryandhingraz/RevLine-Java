-- ============================================================
-- RevLine Car Management System — Relational Schema
-- Target: SQLite database used by the Java JDBC implementation
-- ============================================================

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

CREATE TABLE IF NOT EXISTS audit_log (
  log_id      INTEGER PRIMARY KEY,
  actor_name  VARCHAR(60)   NOT NULL,
  action      VARCHAR(100)  NOT NULL,
  created_at  TIMESTAMP     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cars_brand  ON cars(brand);
CREATE INDEX IF NOT EXISTS idx_cars_status ON cars(status);
CREATE INDEX IF NOT EXISTS idx_sales_car   ON sales(car_id);
