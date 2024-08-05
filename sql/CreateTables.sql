-- Create the Pizzeria schema
CREATE SCHEMA `pizzeria`;

-- Switch to the Pizzeria schema
USE `pizzeria`;

-- Create 'customer' table
CREATE TABLE `customer` (
  `CustomerCustID` INT AUTO_INCREMENT PRIMARY KEY,
  `CustomerFName` VARCHAR(255) NOT NULL,
  `CustomerLName` VARCHAR(255) NOT NULL,
  `CustomerPhone` VARCHAR(20),
  `CustomerAddressCity` VARCHAR(100),
  `CustomerAddressState` VARCHAR(100),
  `CustomerAddressStreet` VARCHAR(100),
  `CustomerAddressZip` VARCHAR(10)
);

-- Create 'baseprice' table
CREATE TABLE `baseprice` (
  `BasePriceID` INT AUTO_INCREMENT PRIMARY KEY,
  `BasePriceCrustType` ENUM('Thin', 'Original', 'Pan', 'Gluten-Free') NOT NULL,
  `BasePriceSize` ENUM('Small', 'Medium', 'Large', 'XLarge') NOT NULL,
  `BasePricePrice` DECIMAL(10, 2) NOT NULL,
  `BasePriceCost` DECIMAL(10, 2) NOT NULL
);

-- Create 'order' table
CREATE TABLE `order` (
  `OrderID` INT AUTO_INCREMENT PRIMARY KEY,
  `OrderType` ENUM('DineIn', 'Pickup', 'Delivery') NOT NULL,
  `OrderTimestamp` TIMESTAMP NOT NULL,
  `OrderTotalCostToBusiness` DECIMAL(10, 2) NOT NULL,
  `OrderTotalPriceToCustomer` DECIMAL(10, 2) NOT NULL,
  `OrderState` ENUM('Completed', 'Processing') NOT NULL
);

-- Create 'topping' table
CREATE TABLE `topping` (
  `ToppingID` INT AUTO_INCREMENT PRIMARY KEY,
  `ToppingName` VARCHAR(255) NOT NULL,
  `ToppingPriceToCustomer` DECIMAL(10, 2) NOT NULL,
  `ToppingPriceToBusiness` DECIMAL(10, 2) NOT NULL,
  `ToppingAmountUsedPerSize` VARCHAR(255) NOT NULL,
  `ToppingCurINVT` INT NOT NULL,
  `ToppingMinimumInventoryLevel` INT NOT NULL
);

-- Create 'discount' table
CREATE TABLE `discount` (
  `DiscountID` INT AUTO_INCREMENT PRIMARY KEY,
  `DiscountName` VARCHAR(255) NOT NULL,
  `DiscountIsPercent` ENUM('Dollar', 'Percentage') NOT NULL,
  `DiscountAmount` DECIMAL(10, 2) NOT NULL
);

-- Create 'pizza' table
CREATE TABLE `pizza` (
  `PizzaID` INT AUTO_INCREMENT PRIMARY KEY,
  `PizzaState` ENUM('Completed', 'Processing') NOT NULL,
  `PizzaTimestamp` TIMESTAMP NOT NULL,
  `PizzaBasePriceID` INT,
  `PizzaCost` DECIMAL(10, 2) NOT NULL,
  `PizzaPrice` DECIMAL(10, 2) NOT NULL,
  `PizzaOrderID` INT,
  FOREIGN KEY (`PizzaBasePriceID`) REFERENCES `baseprice`(`BasePriceID`),
  FOREIGN KEY (`PizzaOrderID`) REFERENCES `order`(`OrderID`)
);

-- Create 'pizzatopping' table (junction table for many-to-many relationship between pizza and topping)
CREATE TABLE `pizzatopping` (
  `PizzaToppingPizzaID` INT,
  `PizzaToppingToppingID` INT,
  `PizzaToppingQuantity` INT NOT NULL,
  `PizzaToppingIsDouble` BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`PizzaToppingPizzaID`, `PizzaToppingToppingID`),
  FOREIGN KEY (`PizzaToppingPizzaID`) REFERENCES `pizza`(`PizzaID`),
  FOREIGN KEY (`PizzaToppingToppingID`) REFERENCES `topping`(`ToppingID`)
);

-- Create 'orderdiscount' table (junction table for many-to-many relationship between order and discount)
CREATE TABLE `orderdiscount` (
  `OrderDiscountOrderID` INT,
  `OrderDiscountDiscountID` INT,
  PRIMARY KEY (`OrderDiscountOrderID`, `OrderDiscountDiscountID`),
  FOREIGN KEY (`OrderDiscountOrderID`) REFERENCES `order`(`OrderID`),
  FOREIGN KEY (`OrderDiscountDiscountID`) REFERENCES `discount`(`DiscountID`)
);

-- Create 'pizzadiscount' table (junction table for many-to-many relationship between pizza and discount)
CREATE TABLE `pizzadiscount` (
  `PizzaDiscountPizzaID` INT,
  `PizzaDiscountDiscountID` INT,
  PRIMARY KEY (`PizzaDiscountPizzaID`, `PizzaDiscountDiscountID`),
  FOREIGN KEY (`PizzaDiscountPizzaID`) REFERENCES `pizza`(`PizzaID`),
  FOREIGN KEY (`PizzaDiscountDiscountID`) REFERENCES `discount`(`DiscountID`)
);

-- Create 'dinein' table
CREATE TABLE `dinein` (
  `DineInOrderID` INT PRIMARY KEY,
  `DineInTableNumber` INT NOT NULL,
  FOREIGN KEY (`DineInOrderID`) REFERENCES `order`(`OrderID`)
);

-- Create 'pickup' table
CREATE TABLE `pickup` (
  `PickupOrderID` INT PRIMARY KEY,
  `PickupCustID` INT NOT NULL,
  FOREIGN KEY (`PickupOrderID`) REFERENCES `order`(`OrderID`),
  FOREIGN KEY (`PickupCustID`) REFERENCES `customer`(`CustomerCustID`)
);

-- Create 'delivery' table
CREATE TABLE `delivery` (
  `DeliveryOrderID` INT PRIMARY KEY,
  `DeliveryCustID` INT NOT NULL,
  FOREIGN KEY (`DeliveryOrderID`) REFERENCES `order`(`OrderID`),
  FOREIGN KEY (`DeliveryCustID`) REFERENCES `customer`(`CustomerCustID`)
);
