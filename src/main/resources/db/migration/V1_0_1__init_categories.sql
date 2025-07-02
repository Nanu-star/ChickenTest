-- Insert initial categories
INSERT INTO categories (id, name, display_name) VALUES
(1, 'EGG', 'Eggs'),
(2, 'CHICKEN', 'Chickens');
-- Insert movement types
INSERT INTO movement_types (name, description) VALUES
('PURCHASE', 'purchase'),
('SALE', 'sale'),
('SYSTEM', 'system');
INSERT INTO farm_config (config_key, "value", description, type)
VALUES
('incubation_days', '3', 'Días para que un huevo se convierta en gallina', 'INT'),
('max_eggs', '2000', 'Cantidad máxima de huevos en stock', 'INT'),
('max_chickens', '1500', 'Cantidad máxima de gallinas en stock', 'INT'),
('egg_production_per_chicken', '1', 'Huevos producidos por gallina por ciclo', 'INT'),
('egg_production_interval_days', '1', 'Cada cuántos días las gallinas ponen huevos', 'INT'),
('chicken_lifetime_days', '365', 'Días de vida útil promedio de una gallina', 'INT'),
('egg_base_price', '20', 'Precio base de venta del huevo', 'DOUBLE'),
('chicken_base_price', '200', 'Precio base de venta de la gallina', 'DOUBLE');
