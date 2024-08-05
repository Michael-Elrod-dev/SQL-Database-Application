-- Michael Elrod

USE `pizzeria`;

-- Insert into 'baseprice' table
INSERT INTO `baseprice` (BasePriceCrustType, BasePriceSize, BasePricePrice, BasePriceCost) VALUES
('Thin', 'Small', 3, 0.5),
('Original', 'Small', 3, 0.75),
('Pan', 'Small', 3.5, 1),
('Gluten-Free', 'Small', 4, 2),
('Thin', 'Medium', 5, 1),
('Original', 'Medium', 5, 1.5),
('Pan', 'Medium', 6, 2.25),
('Gluten-Free', 'Medium', 6.25, 3),
('Thin', 'Large', 8, 1.25),
('Original', 'Large', 8, 2),
('Pan', 'Large', 9, 3),
('Gluten-Free', 'Large', 9.5, 4),
('Thin', 'XLarge', 10, 2),
('Original', 'XLarge', 10, 3),
('Pan', 'XLarge', 11.5, 4.5),
('Gluten-Free', 'XLarge', 12.5, 6);

-- Insert into 'topping' table
INSERT INTO `topping` (ToppingName, ToppingPriceToCustomer, ToppingPriceToBusiness, ToppingAmountUsedPerSize, ToppingCurINVT, ToppingMinimumInventoryLevel) VALUES
('Pepperoni', 1.25, 0.2, '2,2.75,3.5,4.5', 100, 50),
('Sausage', 1.25, 0.15, '2.5,3,3.5,4.25', 100, 50),
('Ham', 1.5, 0.15, '2,2.5,3.25,4', 78, 25),
('Chicken', 1.75, 0.25, '1.5,2,2.25,3', 56, 25),
('Green Pepper', 0.5, 0.02, '1,1.5,2,2.5', 79, 25),
('Onion', 0.5, 0.02, '1,1.5,2,2.75', 85, 25),
('Roma Tomato', 0.75, 0.03, '2,3,3.5,4.5', 86, 10),
('Mushrooms', 0.75, 0.1, '1.5,2,2.5,3', 52, 50),
('Black Olives', 0.6, 0.1, '0.75,1,1.5,2', 39, 25),
('Pineapple', 1, 0.25, '1,1.25,1.75,2', 15, 0),
('Jalapenos', 0.5, 0.05, '0.5,0.75,1.25,1.75', 64, 0),
('Banana Peppers', 0.5, 0.05, '0.6,1,1.3,1.75', 36, 0),
('Regular Cheese', 0.5, 0.12, '2,3.5,5,7', 250, 50),
('Four Cheese Blend', 1, 0.15, '2,3.5,5,7', 150, 25),
('Feta Cheese', 1.5, 0.18, '1.75,3,4,5.5', 75, 0),
('Goat Cheese', 1.5, 0.2, '1.6,2.75,4,5.5', 54, 0),
('Bacon', 1.5, 0.25, '1,1.5,2,3', 89, 0);

-- Insert into 'discount' table
INSERT INTO `discount` (DiscountName, DiscountIsPercent, DiscountAmount) VALUES
('Employee', 'Percentage', 15),
('Lunch Special Medium', 'Dollar', 1),
('Lunch Special Large', 'Dollar', 2),
('Specialty Pizza', 'Dollar', 1.5),
('Happy Hour', 'Percentage', 10),
('Gameday Special', 'Percentage', 20);

-- Insert into 'customer' table
INSERT INTO `customer` (CustomerFName, CustomerLName, CustomerPhone, CustomerAddressCity, CustomerAddressState, CustomerAddressStreet, CustomerAddressZip) VALUES
('Andrew', 'Wilkes-Krier', '8642545861', 'Anderson', 'SC', 'Unknown Street', '29621'),
('Matt', 'Engers', '8644749953', 'Anderson', 'SC', 'Unknown Street', '29621'),
('Frank', 'Turner', '8642328944', 'Anderson', 'SC', '6745 Wessex St', '29621'),
('Milo', 'Auckerman', '8648785679', 'Anderson', 'SC', '8879 Suburban Home', '29621');

-- Order 1
INSERT INTO `order` (OrderType, OrderTimestamp, OrderTotalCostToBusiness, OrderTotalPriceToCustomer, OrderState) VALUES
('DineIn', '2024-03-05 12:03:00', 3.68, 20.75, 'Completed');
SET @OrderID = LAST_INSERT_ID();
INSERT INTO `pizza` (PizzaState, PizzaTimestamp, PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-03-05 12:03:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Thin' AND BasePriceSize = 'Large'), 3.68, 20.75, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, TRUE);
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, FALSE);
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Sausage'), 1, FALSE);
INSERT INTO `pizzadiscount` (PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES
(@PizzaID, (SELECT DiscountID FROM discount WHERE DiscountName = 'Lunch Special Large'));
INSERT INTO `dinein` (DineInOrderID, DineInTableNumber) VALUES
(@OrderID, 21);

-- Order 2
INSERT INTO `order` (OrderType, OrderTimestamp, OrderTotalCostToBusiness, OrderTotalPriceToCustomer, OrderState) VALUES
('DineIn', '2024-04-03 12:05:00', 4.63, 19.78, 'Completed');
SET @OrderID = LAST_INSERT_ID();
INSERT INTO `pizza` (PizzaState, PizzaTimestamp, PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-03 12:05:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Pan' AND BasePriceSize = 'Medium'), 3.23, 12.85, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Feta Cheese'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Black Olives'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Roma Tomato'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Mushrooms'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Banana Peppers'), 1, FALSE);
INSERT INTO `pizza` (PizzaState, PizzaTimestamp, PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-03 12:05:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'Small'), 1.40, 6.93, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Chicken'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Banana Peppers'), 1, FALSE);
INSERT INTO `pizzadiscount` (PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES
(@PizzaID, (SELECT DiscountID FROM discount WHERE DiscountName = 'Lunch Special Medium')),
(@PizzaID, (SELECT DiscountID FROM discount WHERE DiscountName = 'Specialty Pizza'));
INSERT INTO `dinein` (DineInOrderID, DineInTableNumber) VALUES
(@OrderID, 4);

-- Order 3
INSERT INTO `order` (OrderType, OrderTimestamp, OrderTotalCostToBusiness, OrderTotalPriceToCustomer, OrderState) VALUES
('Pickup', '2024-03-03 21:30:00', 19.80, 89.28, 'Processing');
SET @OrderID = LAST_INSERT_ID();
INSERT INTO `pizza` (PizzaState, PizzaTimestamp, PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-03-03 21:30:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'Large'), 3.30, 14.88, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, FALSE);
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, FALSE);
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-03-03 21:30:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'Large'), 3.30, 14.88, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, FALSE);
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, FALSE);
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-03-03 21:30:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'Large'), 3.30, 14.88, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, FALSE);
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, FALSE);
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-03-03 21:30:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'Large'), 3.30, 14.88, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, FALSE);
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, FALSE);
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-03-03 21:30:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'Large'), 3.30, 14.88, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, FALSE);
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, FALSE);
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-03-03 21:30:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'Large'), 3.30, 14.88, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, FALSE);
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, FALSE);
INSERT INTO `pickup` (PickupOrderID, PickupCustID) VALUES
((SELECT OrderID FROM `order` ORDER BY OrderID DESC LIMIT 1), (SELECT CustomerCustID FROM customer WHERE CustomerPhone = '8642545861'));

-- Order 4
INSERT INTO `order` (OrderType, OrderTimestamp, OrderTotalCostToBusiness, OrderTotalPriceToCustomer, OrderState) VALUES
('Delivery', '2024-04-20 19:11:00', 23.62, 86.19, 'Completed');
SET @OrderID = LAST_INSERT_ID();
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-20 19:11:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'XLarge'), 9.19, 27.94, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Sausage'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Four Cheese Blend'), 1, FALSE);
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-20 19:11:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'XLarge'), 6.25, 31.50, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Ham'), 1, TRUE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pineapple'), 1, TRUE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Four Cheese Blend'), 1, FALSE);
INSERT INTO `pizzadiscount` (PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES
(@OrderID, (SELECT DiscountID FROM discount WHERE DiscountName = 'Specialty Pizza'));
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-20 19:11:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Original' AND BasePriceSize = 'XLarge'), 8.18, 26.75, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Chicken'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Bacon'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Four Cheese Blend'), 1, FALSE);
INSERT INTO `orderdiscount` (OrderDiscountOrderID, OrderDiscountDiscountID) VALUES
(@OrderID, (SELECT DiscountID FROM discount WHERE DiscountName = 'Gameday Special'));
INSERT INTO `delivery` (DeliveryOrderID, DeliveryCustID) VALUES
(@OrderID, (SELECT CustomerCustID FROM customer WHERE CustomerPhone = '8642545861'));

-- Order 5
INSERT INTO `order` (OrderType, OrderTimestamp, OrderTotalCostToBusiness, OrderTotalPriceToCustomer, OrderState) VALUES
('Pickup', '2024-03-02 17:30:00', 7.88, 27.45, 'Processing');
SET @OrderID = LAST_INSERT_ID();
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-20 19:11:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Gluten-Free' AND BasePriceSize = 'XLarge'), 7.88, 27.45, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Green Pepper'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Onion'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Roma Tomato'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Mushrooms'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Black Olives'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Goat Cheese'), 1, FALSE);
INSERT INTO `pizzadiscount` (PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES
(@OrderID, (SELECT DiscountID FROM discount WHERE DiscountName = 'Specialty Pizza'));
INSERT INTO `pickup` (PickupOrderID, PickupCustID) VALUES
((SELECT OrderID FROM `order` ORDER BY OrderID DESC LIMIT 1), (SELECT CustomerCustID FROM customer WHERE CustomerPhone = '8644749953'));

-- Order 6
INSERT INTO `order` (OrderType, OrderTimestamp, OrderTotalCostToBusiness, OrderTotalPriceToCustomer, OrderState) VALUES
('Delivery', '2024-03-02 18:17:00', 3.19, 20.81, 'Processing');
SET @OrderID = LAST_INSERT_ID();
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-20 19:11:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Thin' AND BasePriceSize = 'Large'), 3.19, 20.81, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Chicken'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Green Pepper'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Onion'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Mushrooms'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Four Cheese Blend'), 1, TRUE);
INSERT INTO `delivery` (DeliveryOrderID, DeliveryCustID) VALUES
((SELECT OrderID FROM `order` ORDER BY OrderID DESC LIMIT 1), (SELECT CustomerCustID FROM customer WHERE CustomerPhone = '8642328944'));

-- Order 7
INSERT INTO `order` (OrderType, OrderTimestamp, OrderTotalCostToBusiness, OrderTotalPriceToCustomer, OrderState) VALUES
('Delivery', '2024-04-13 20:32:00', 5.25, 32.25, 'Completed');
SET @OrderID = LAST_INSERT_ID();
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-20 19:11:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Thin' AND BasePriceSize = 'Large'), 2.00, 13.00, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Four Cheese Blend'), 1, TRUE);
INSERT INTO `pizza` (PizzaState, PizzaTimestamp,PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) VALUES
('Completed', '2024-04-20 19:11:00', (SELECT BasePriceID FROM baseprice WHERE BasePriceCrustType = 'Thin' AND BasePriceSize = 'Large'), 3.25, 19.25, @OrderID);
SET @PizzaID = LAST_INSERT_ID();
INSERT INTO `pizzatopping` (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Regular Cheese'), 1, FALSE),
(@PizzaID, (SELECT ToppingID FROM topping WHERE ToppingName = 'Pepperoni'), 1, TRUE);
INSERT INTO `orderdiscount` (OrderDiscountOrderID, OrderDiscountDiscountID) VALUES
(@OrderID, (SELECT DiscountID FROM discount WHERE DiscountName = 'Employee'));
INSERT INTO `delivery` (DeliveryOrderID, DeliveryCustID) VALUES
((SELECT OrderID FROM `order` ORDER BY OrderID DESC LIMIT 1), (SELECT CustomerCustID FROM customer WHERE CustomerPhone = '8648785679'));
