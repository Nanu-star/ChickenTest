-- Insert initial categories
INSERT INTO categories (id, name, display_name) VALUES
(1, 'EGG', 'Eggs'),
(2, 'CHICKEN', 'Chickens');
-- Insert movement types
INSERT INTO movement_types (name, description) VALUES
('PURCHASE', 'COMPRA'),
('SALE', 'VENTA'),
('PRODUCTION', 'PRODUCCION');

-- Insert admin user (username: nadia, password: 123, role: ADMIN)
INSERT INTO users (id, username, password, balance) VALUES
(1, 'nadia', '{bcrypt}$2a$10$B7e0l8qkB2lJ6h1pQz2tLea6RzqKc6T9dVwI9l7u6wR3xX9Uu1Qp2O', 1000.00);
-- Insert initial articles for user nadia
INSERT INTO articles (id, name, units, price, age, last_aged_date, production, display_price, category_id, user_id) VALUES
(1, 'Starter Egg', 10, 2.50, 0, NULL, 'Free-range', '$2.50', 1, 1),
(2, 'Starter Chicken', 5, 15.00, 0, NULL, 'Organic', '$15.00', 2, 1);

