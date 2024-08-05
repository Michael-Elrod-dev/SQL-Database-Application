# SQL-Database-Application

This project implements a comprehensive pizza ordering system with a Java backend and a MySQL database. It allows for managing orders, customers, pizzas, toppings, and discounts in a pizzeria setting.

## Project Structure

The project consists of two main parts:

1. Java Backend
2. SQL Database

### Java Backend

The Java backend is composed of several classes that model the business logic:

- `Customer`: Represents a customer with basic information.
- `Order`: Represents an order, which can be of type dine-in, pickup, or delivery.
- `Pizza`: Represents a pizza with its size, crust type, toppings, and pricing.
- `Topping`: Represents a pizza topping with inventory management.
- `Discount`: Represents various discounts that can be applied to orders or pizzas.
- `DBNinja`: A utility class for database operations.
- `Menu`: The main class that handles user interaction and program flow.

### SQL Database

The database schema includes tables for:

- Customers
- Orders
- Pizzas
- Toppings
- Discounts
- Base prices

It also includes junction tables for many-to-many relationships and tables for specific order types (dine-in, pickup, delivery).

## Key Features

1. Order Management: Create, view, and complete orders.
2. Customer Management: Add new customers and view customer information.
3. Pizza Customization: Build pizzas with various sizes, crusts, and toppings.
4. Inventory Tracking: Monitor and update topping inventory levels.
5. Discount Application: Apply discounts to orders or individual pizzas.
6. Reporting: Generate reports on topping popularity, profits by pizza, and profits by order type.

## Setup Instructions

1. Database Setup:
   - Run `CreateTables.sql` to set up the database schema.
   - Run `PopulateData.sql` to populate the database with initial data.

2. Java Setup:
   - Ensure you have Java Development Kit (JDK) installed.
   - Set up the project in your preferred Java IDE.
   - Update the database connection details in `DBConnector.java`.

3. Running the Application:
   - Compile and run the `Menu` class to start the application.

## Usage

The application presents a menu-driven interface where users can:

1. Enter new orders
2. View and manage customers
3. View orders
4. Mark orders as complete
5. Manage inventory
6. Generate reports

## Reporting

The system offers three main reports:

1. Topping Popularity
2. Profit by Pizza
3. Profit by Order Type

These reports can be generated using the reporting option in the main menu.
