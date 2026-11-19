-- ============================================================
-- Seed data for RevLine (18 initial supercars, 3 initial sales)
-- ============================================================

INSERT INTO cars (car_id, brand, model, price, top_speed, fuel_type, horsepower, acceleration, status) VALUES
(1, 'Bugatti', 'Chiron', 3000000.00, 420, 'PETROL', 1500, 2.40, 'AVAILABLE'),
(2, 'Bugatti', 'Veyron SS', 2500000.00, 431, 'PETROL', 1200, 2.50, 'AVAILABLE'),
(3, 'Ferrari', 'LaFerrari', 1400000.00, 350, 'HYBRID', 949, 2.40, 'SOLD'),
(4, 'Ferrari', 'SF90 Stradale', 507000.00, 340, 'HYBRID', 986, 2.50, 'SOLD'),
(5, 'Lamborghini', 'Aventador SVJ', 517770.00, 350, 'PETROL', 759, 2.80, 'AVAILABLE'),
(6, 'Lamborghini', 'Huracan STO', 327838.00, 310, 'PETROL', 631, 3.00, 'AVAILABLE'),
(7, 'Porsche', '911 Turbo S', 207000.00, 330, 'PETROL', 640, 2.60, 'AVAILABLE'),
(8, 'Porsche', 'Carrera GT', 448000.00, 330, 'PETROL', 605, 3.50, 'AVAILABLE'),
(9, 'McLaren', 'P1', 1150000.00, 350, 'HYBRID', 903, 2.80, 'AVAILABLE'),
(10, 'McLaren', '720S', 305000.00, 341, 'PETROL', 710, 2.90, 'AVAILABLE'),
(11, 'Aston Martin', 'Valkyrie', 3000000.00, 402, 'HYBRID', 1140, 2.50, 'AVAILABLE'),
(12, 'Tesla', 'Model S Plaid', 135990.00, 322, 'ELECTRIC', 1020, 1.99, 'AVAILABLE'),
(13, 'Koenigsegg', 'Jesko', 3000000.00, 480, 'PETROL', 1600, 2.50, 'AVAILABLE'),
(14, 'Koenigsegg', 'Agera RS', 2500000.00, 447, 'PETROL', 1341, 2.80, 'AVAILABLE'),
(15, 'Pagani', 'Huayra', 1400000.00, 383, 'PETROL', 720, 2.80, 'AVAILABLE'),
(16, 'Pagani', 'Zonda Cinque', 2000000.00, 350, 'PETROL', 669, 3.40, 'AVAILABLE'),
(17, 'BMW', 'M5 CS', 142000.00, 305, 'PETROL', 627, 2.90, 'AVAILABLE'),
(18, 'Audi', 'R8 V10 Plus', 197000.00, 330, 'PETROL', 602, 3.20, 'SOLD');

INSERT INTO sales (sale_id, car_id, seller_name, buyer_name, sale_date, price) VALUES
(1, 3, 'RevLine Showroom', 'Legacy C Import', '2026-01-01', 1400000.00),
(2, 4, 'RevLine Showroom', 'Legacy C Import', '2026-01-01', 507000.00),
(3, 18, 'RevLine Showroom', 'Legacy C Import', '2026-01-01', 197000.00);
