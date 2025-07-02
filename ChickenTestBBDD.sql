-- -----------------------------------------------------
-- Schema ChickenTestWeb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `ChickenTestWeb` DEFAULT CHARACTER SET utf8mb4 ;
USE `ChickenTestWeb` ;

-- -----------------------------------------------------
-- Table `ChickenTestWeb`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ChickenTestWeb`.`users`;
CREATE TABLE IF NOT EXISTS `ChickenTestWeb`.`users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`));

-- -----------------------------------------------------
-- Table `ChickenTestWeb`.`farms`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ChickenTestWeb`.`farms`;
CREATE TABLE IF NOT EXISTS `ChickenTestWeb`.`farms` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `owner_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_farms_owner`
    FOREIGN KEY (`owner_id`)
    REFERENCES `ChickenTestWeb`.`users` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);

-- -----------------------------------------------------
-- Table `ChickenTestWeb`.`articles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ChickenTestWeb`.`articles`;
CREATE TABLE IF NOT EXISTS `ChickenTestWeb`.`articles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `stock` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `farm_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_articles_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `ChickenTestWeb`.`users` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_articles_farm`
    FOREIGN KEY (`farm_id`)
    REFERENCES `ChickenTestWeb`.`farms` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);

-- -----------------------------------------------------
-- Table `ChickenTestWeb`.`movements`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ChickenTestWeb`.`movements`;
CREATE TABLE IF NOT EXISTS `ChickenTestWeb`.`movements` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL, -- Price at the moment of transaction
  `date` DATETIME NOT NULL,
  `type` VARCHAR(50) NOT NULL, -- BUY or SELL
  `user_id` BIGINT NOT NULL,
  `farm_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_movements_article`
    FOREIGN KEY (`article_id`)
    REFERENCES `ChickenTestWeb`.`articles` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_movements_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `ChickenTestWeb`.`users` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_movements_farm`
    FOREIGN KEY (`farm_id`)
    REFERENCES `ChickenTestWeb`.`farms` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);

-- -----------------------------------------------------
-- Configuraciones
-- -----------------------------------------------------
SET GLOBAL time_zone = '-3:00';
-- Ensure 'root'@'localhost' user exists and has appropriate privileges.
-- The password 'root' is insecure and should be changed in a production environment.
-- Example: CREATE USER IF NOT EXISTS 'chickentestuser'@'localhost' IDENTIFIED BY 'your_secure_password';
-- GRANT ALL PRIVILEGES ON ChickenTestWeb.* TO 'chickentestuser'@'localhost';
-- FLUSH PRIVILEGES;

-- Note: The original "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';" is generally not recommended
-- as it might interfere with existing MySQL setups or security policies.
-- Managing users and grants should be done carefully.

-- -----------------------------------------------------
-- No sample data inserted here. Manage data via API.
-- -----------------------------------------------------
