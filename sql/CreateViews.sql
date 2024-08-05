-- Michael Elrod

USE `pizzeria`;

-- Drop the 'ToppingPopularity' view
DROP VIEW IF EXISTS `ToppingPopularity`;

-- Drop the 'ProfitByPizza' view
DROP VIEW IF EXISTS `ProfitByPizza`;

-- Drop the 'ProfitByOrderType' view
DROP VIEW IF EXISTS `ProfitByOrderType`;

-- View for Topping Popularity
CREATE VIEW `ToppingPopularity` AS
SELECT 
    t.ToppingName AS Topping,
    SUM(pt.PizzaToppingQuantity + IF(pt.PizzaToppingIsDouble, pt.PizzaToppingQuantity, 0)) AS ToppingCount
FROM 
    `topping` t
JOIN 
    `pizzatopping` pt ON t.ToppingID = pt.PizzaToppingToppingID
GROUP BY 
    t.ToppingName
ORDER BY 
    ToppingCount DESC, Topping;

-- View for Profit by Pizza
CREATE VIEW `ProfitByPizza` AS
SELECT 
    bp.BasePriceSize AS Size,
    bp.BasePriceCrustType AS Crust,
    SUM(p.PizzaPrice - p.PizzaCost) AS Profit,
    MIN(DATE_FORMAT(o.OrderTimestamp, '%c/%Y')) AS OrderMonth
FROM 
    `pizza` p
JOIN 
    `baseprice` bp ON p.PizzaBasePriceID = bp.BasePriceID
JOIN 
    `order` o ON p.PizzaOrderID = o.OrderID
GROUP BY 
    Size, Crust
ORDER BY 
    Profit ASC;

-- View for Profit by Order Type
CREATE VIEW `ProfitByOrderType` AS
SELECT 
    customerType,
    MIN(OrderMonth) AS OrderMonth,
    SUM(TotalOrderPrice) AS TotalOrderPrice,
    SUM(TotalOrderCost) AS TotalOrderCost,
    SUM(Profit) AS Profit
FROM (
    SELECT 
        o.OrderType AS customerType,
        DATE_FORMAT(o.OrderTimestamp, '%c/%Y') AS OrderMonth,
        o.OrderTotalPriceToCustomer AS TotalOrderPrice,
        o.OrderTotalCostToBusiness AS TotalOrderCost,
        (o.OrderTotalPriceToCustomer - o.OrderTotalCostToBusiness) AS Profit
    FROM 
        `order` o
) AS DetailedOrders
GROUP BY customerType;
SELECT * FROM `ToppingPopularity`;
SELECT * FROM `ProfitByPizza`;
SELECT 
    customerType,
    OrderMonth,
    TotalOrderPrice,
    TotalOrderCost,
    Profit
FROM (
    SELECT 
        customerType,
        OrderMonth,
        TotalOrderPrice,
        TotalOrderCost,
        Profit
    FROM 
        `ProfitByOrderType`
    UNION ALL
    SELECT 
        'Grand Total' AS customerType,
        NULL AS OrderMonth,
        SUM(TotalOrderPrice),
        SUM(TotalOrderCost),
        SUM(Profit)
    FROM 
        `ProfitByOrderType`
    ) AS CombinedResults
ORDER BY 
    CASE WHEN customerType = 'Grand Total' THEN 1 ELSE 0 END, 
    Profit ASC;
