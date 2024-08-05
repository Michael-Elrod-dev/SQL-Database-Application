-- Michael Elrod

USE `pizzeria`;

-- Drop subtype tables
DROP TABLE IF EXISTS `delivery`;
DROP TABLE IF EXISTS `pickup`;
DROP TABLE IF EXISTS `dine_in`;

-- Drop junction tables
DROP TABLE IF EXISTS `pizza_discount`;
DROP TABLE IF EXISTS `order_discount`;
DROP TABLE IF EXISTS `pizza_topping`;

-- Drop main tables
DROP TABLE IF EXISTS `pizza`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `customer`;
DROP TABLE IF EXISTS `discount`;
DROP TABLE IF EXISTS `topping`;
DROP TABLE IF EXISTS `base_price`;

-- Drop schema
DROP SCHEMA IF EXISTS `pizzeria`;