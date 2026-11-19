-- ============================================================
-- Sample SQL Queries for RevLine
-- ============================================================

-- 1. Total showroom stock value (AVAILABLE only)
SELECT SUM(price) AS showroom_stock_value
FROM cars
WHERE status = 'AVAILABLE';

-- 2. Count cars by fuel type
SELECT fuel_type, COUNT(*) AS count
FROM cars
GROUP BY fuel_type;

-- 3. Top 3 fastest cars
SELECT brand, model, top_speed, horsepower
FROM cars
ORDER BY top_speed DESC
LIMIT 3;

-- 4. Average price by brand
SELECT brand, AVG(price) AS average_price
FROM cars
GROUP BY brand
ORDER BY average_price DESC;

-- 5. Sales history joined with car details
SELECT s.sale_id, c.brand, c.model, s.buyer_name, s.seller_name, s.sale_date, s.price
FROM sales s
INNER JOIN cars c ON s.car_id = c.car_id
ORDER BY s.sale_date DESC;

-- 6. Total realized revenue
SELECT SUM(price) AS total_revenue
FROM sales;
