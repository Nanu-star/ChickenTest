-- -----------------------------------------------------
-- Schema ChickenTestWeb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `ChickenTestWeb` DEFAULT CHARACTER SET utf8mb4 ;
USE `ChickenTestWeb` ;

-- -----------------------------------------------------
-- Table `ChickenTestWeb`.`usuarios`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ChickenTestWeb`.`usuarios` (
  `username` VARCHAR(20) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `saldo` DECIMAL(8,2) NOT NULL,
  PRIMARY KEY (`username`));


-- -----------------------------------------------------
-- Table `ChickenTestWeb`.`movimientos`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ChickenTestWeb`.`movimientos` (
  `idmovimiento` INT NOT NULL AUTO_INCREMENT,
  `articulo` VARCHAR(20) NULL,
  `fecha` DATE NULL,
  `tipo` VARCHAR(10) NOT NULL,
  `monto` DECIMAL(8,2) NOT NULL,
  `username` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`idmovimiento`));


-- -----------------------------------------------------
-- Table `ChickenTestWeb`.`articulos`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ChickenTestWeb`.`articulos` (
  `idarticulo` INT NOT NULL AUTO_INCREMENT,
  `unidades` INT NOT NULL,
  `stock` INT NOT NULL,
  `nombre` VARCHAR(45) NOT NULL,
  `precio` DECIMAL(8,2) NOT NULL,
  `categoria` VARCHAR(45) NOT NULL,
  `produccion` VARCHAR(45) NULL,
  `creacion` DATE NULL,
  PRIMARY KEY (`idarticulo`));
  -- -----------------------------------------------------
-- Configuraciones para que funcione
-- -----------------------------------------------------
  SET GLOBAL time_zone = '-3:00';
  ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
-- -----------------------------------------------------
-- Información de ejemplo
-- -----------------------------------------------------
insert into usuarios(username, password, saldo) values ('admin', 'admin', 50000.00);
insert into articulos (idarticulo, nombre, categoria, unidades, stock, precio, produccion, creacion) values 
(1, 'Lote ChickenTest', 'Huevos', 20, 10, 1200.00, 'ChickenTest', '2021-03-03');
insert into articulos (idarticulo, nombre, categoria, unidades, stock, precio, produccion, creacion) values
 (2, 'Lote GallinasFelices', 'Huevos', 20, 5, 1500.00, 'Gallinas felices', '2021-03-03');
insert into articulos (idarticulo, nombre, categoria, unidades, stock, precio, produccion, creacion) values
 (3, 'Gallina', 'Gallinas', 1, 5, 3000.00, 'ChickenTest', '2021-03-03');
insert into articulos (idarticulo, nombre, categoria, unidades, stock, precio, produccion, creacion) values
 (4, 'Gallina', 'Gallinas', 1, 5, 2500.00, 'Gallinas felices', '2021-03-03');
  
  