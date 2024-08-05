package cpsc4620;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/*
 * This file is where most of your code changes will occur You will write the code to retrieve
 * information from the database, or save information to the database
 * 
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 * 
 * This class also has static string variables for pickup, delivery and dine-in. If your database
 * stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will
 * ensure that the comparison is checking for the right string in other places in the program. You
 * will also need to use these strings if you store this as boolean fields or an integer.
 * 
 * 
 */

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
	private static Connection conn;

	// Change these variables to however you record dine-in, pick-up and delivery, and sizes and crusts
	public final static String pickup = "pickup";
	public final static String delivery = "delivery";
	public final static String dine_in = "dinein";

	public final static String size_s = "Small";
	public final static String size_m = "Medium";
	public final static String size_l = "Large";
	public final static String size_xl = "XLarge";

	public final static String crust_thin = "Thin";
	public final static String crust_orig = "Original";
	public final static String crust_pan = "Pan";
	public final static String crust_gf = "Gluten-Free";


	public static boolean connect_to_db() throws SQLException, IOException {
		try {
			conn = DBConnector.make_connection();
			return true;
		} catch (SQLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	}

	public static int addOrder(Order o) throws SQLException, IOException {
		connect_to_db();
		try {
			String query = "INSERT INTO `order` (OrderType, OrderTimestamp, OrderTotalCostToBusiness, OrderTotalPriceToCustomer, OrderState) VALUES (?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, o.getOrderType());
			stmt.setString(2, o.getDate());
			stmt.setDouble(3, o.getBusPrice());
			stmt.setDouble(4, o.getCustPrice());
			stmt.setString(5, o.getIsComplete() == 1 ? "Completed" : "Processing");

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected == 0) {
				throw new SQLException("Creating order failed, no rows affected.");
			}

			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int orderID = generatedKeys.getInt(1);
					o.setOrderID(orderID);
					return orderID;
				} else {
					throw new SQLException("Creating order failed, no ID obtained.");
				}
			}
		} finally {
			conn.close();
		}
	}


	public static int addPizza(Pizza p) throws SQLException, IOException {
		connect_to_db();

		String query = "INSERT INTO pizza (PizzaState, PizzaTimestamp, PizzaBasePriceID, PizzaCost, PizzaPrice, PizzaOrderID) " +
				"VALUES (?, ?, (SELECT BasePriceID FROM baseprice WHERE BasePriceSize = ? AND BasePriceCrustType = ?), ?, ?, ?)";

		PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		stmt.setString(1, p.getPizzaState());
		stmt.setString(2, p.getPizzaDate());
		stmt.setString(3, p.getSize());
		stmt.setString(4, p.getCrustType());
		stmt.setDouble(5, p.getBusPrice());
		stmt.setDouble(6, p.getCustPrice());
		stmt.setInt(7, p.getOrderID());

		int rowsAffected = stmt.executeUpdate();

		if (rowsAffected == 0) {
			throw new SQLException("Creating pizza failed, no rows affected.");
		}

		try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
			if (generatedKeys.next()) {
				int pizzaID = generatedKeys.getInt(1);
				p.setPizzaID(pizzaID);

				for (Discount discount : p.getDiscounts()) {
					String discountQuery = "INSERT INTO pizzadiscount (PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES (?, ?)";
					PreparedStatement discountStmt = conn.prepareStatement(discountQuery);
					discountStmt.setInt(1, pizzaID);
					discountStmt.setInt(2, discount.getDiscountID());
					discountStmt.executeUpdate();
					discountStmt.close();
				}

				for (Topping topping : p.getToppings()) {
					String toppingQuery = "INSERT INTO pizzatopping (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingQuantity, PizzaToppingIsDouble) VALUES (?, ?, ?, ?)";
					PreparedStatement toppingStmt = conn.prepareStatement(toppingQuery);
					toppingStmt.setInt(1, pizzaID);
					toppingStmt.setInt(2, topping.getTopID());
					toppingStmt.setDouble(3, p.getIsDoubleArray()[p.getToppings().indexOf(topping)] ? 2 * topping.getPerAMT() : topping.getPerAMT());
					toppingStmt.setBoolean(4, p.getIsDoubleArray()[p.getToppings().indexOf(topping)]);
					toppingStmt.executeUpdate();
					toppingStmt.close();
				}

				return pizzaID;
			} else {
				throw new SQLException("Creating pizza failed, no ID obtained.");
			}
		}

		finally {
			conn.close();
		}
	}


	public static void useTopping(Pizza p, Topping t, boolean isDoubled) throws SQLException, IOException {
		connect_to_db();

		String query = "INSERT INTO pizzatopping (PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingIsDouble, PizzaToppingQuantity) VALUES (?, ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setInt(1, p.getPizzaID());
		stmt.setInt(2, t.getTopID());
		stmt.setBoolean(3, isDoubled);
		double toppingQuantity = isDoubled ? t.getPerAMT() * 2 : t.getPerAMT();
		stmt.setDouble(4, toppingQuantity);
		stmt.executeUpdate();

		String updateQuery = "UPDATE topping SET ToppingCurINVT = ToppingCurINVT - ? WHERE ToppingID = ?";
		PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
		double amountUsed = isDoubled ? t.getPerAMT() * 2 : t.getPerAMT();
		updateStmt.setDouble(1, amountUsed);
		updateStmt.setInt(2, t.getTopID());
		updateStmt.executeUpdate();

		stmt.close();
		updateStmt.close();
		conn.close();
	}

	public static void usePizzaDiscount(Pizza p, Discount d) throws SQLException, IOException {
		connect_to_db();

		String query = "INSERT INTO pizzadiscount (PizzaDiscountPizzaID, PizzaDiscountDiscountID) VALUES (?, ?)";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setInt(1, p.getPizzaID());
		stmt.setInt(2, d.getDiscountID());
		stmt.executeUpdate();

		stmt.close();
		conn.close();
	}

	public static void useOrderDiscount(Order o, Discount d) throws SQLException, IOException {
		connect_to_db();

		String query = "INSERT INTO orderdiscount (OrderDiscountOrderID, OrderDiscountDiscountID) VALUES (?, ?)";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setInt(1, o.getOrderID());
		stmt.setInt(2, d.getDiscountID());
		stmt.executeUpdate();

		stmt.close();
		conn.close();
	}

	public static void addCustomer(Customer c) throws SQLException, IOException {
		connect_to_db();

		String query = "INSERT INTO customer (CustomerFName, CustomerLName, CustomerPhone) VALUES (?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		stmt.setString(1, c.getFName());
		stmt.setString(2, c.getLName());
		stmt.setString(3, c.getPhone());

		int rowsAffected = stmt.executeUpdate();

		if (rowsAffected == 1) {
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				int CustID = rs.getInt(1);
				c.setCustID(CustID);
			}
			rs.close();
		}

		stmt.close();
		conn.close();
	}

	public static void completeOrder(Order o) throws SQLException, IOException {
		connect_to_db();

		String query = "UPDATE `order` SET OrderState = 'Completed' WHERE OrderID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setInt(1, o.getOrderID());
		int rowsAffected = stmt.executeUpdate();

		if (rowsAffected != 1) {
			System.out.println("Failed to mark order " + o.getOrderID() + " as complete.");
		}

		stmt.close();
		conn.close();
	}

	public static ArrayList<Order> getOrders(boolean openOnly) throws SQLException, IOException {
		connect_to_db();
		ArrayList<Order> orders = new ArrayList<>();

		String query = "SELECT o.*, d.DeliveryCustID, di.DineInTableNumber, p.PickupCustID " +
				"FROM `order` o " +
				"LEFT JOIN `delivery` d ON o.OrderID = d.DeliveryOrderID " +
				"LEFT JOIN `dinein` di ON o.OrderID = di.DineInOrderID " +
				"LEFT JOIN `pickup` p ON o.OrderID = p.PickupOrderID " +
				"ORDER BY o.OrderTimestamp DESC";
		PreparedStatement stmt = conn.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			int orderId = rs.getInt("OrderID");
			String orderType = rs.getString("OrderType");
			String orderTimestamp = rs.getString("OrderTimestamp");
			double totalCost = rs.getDouble("OrderTotalCostToBusiness");
			double totalPrice = rs.getDouble("OrderTotalPriceToCustomer");
			String orderState = rs.getString("OrderState");

			Order order;
			if (orderType.equalsIgnoreCase(delivery)) {
				int custID = rs.getInt("DeliveryCustID");
				String address = getCustomerAddress(custID);
				order = new DeliveryOrder(orderId, custID, orderTimestamp, totalPrice, totalCost, orderState.equals("Completed") ? 1 : 0, address);
			} else if (orderType.equalsIgnoreCase(pickup)) {
				int custID = rs.getInt("PickupCustID");
				order = new PickupOrder(orderId, custID, orderTimestamp, totalPrice, totalCost, 0, orderState.equals("Completed") ? 1 : 0);
			} else {
				int tableNumber = rs.getInt("DineInTableNumber");
				order = new DineinOrder(orderId, 0, orderTimestamp, totalPrice, totalCost, orderState.equals("Completed") ? 1 : 0, tableNumber);
			}

			if (!openOnly || orderState.equals("Processing")) {
				orders.add(order);
			}
		}

		rs.close();
		stmt.close();
		conn.close();

		return orders;
	}

	public static ArrayList<Order> getOrdersByDate(String date) throws SQLException, IOException {
		connect_to_db();
		ArrayList<Order> orders = new ArrayList<>();

		String query = "SELECT o.*, d.DeliveryCustID, di.DineInTableNumber, p.PickupCustID " +
				"FROM `order` o " +
				"LEFT JOIN `delivery` d ON o.OrderID = d.DeliveryOrderID " +
				"LEFT JOIN `dinein` di ON o.OrderID = di.DineInOrderID " +
				"LEFT JOIN `pickup` p ON o.OrderID = p.PickupOrderID " +
				"WHERE DATE(o.OrderTimestamp) = ? " +
				"ORDER BY o.OrderTimestamp DESC";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, date);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			int orderId = rs.getInt("OrderID");
			String orderType = rs.getString("OrderType");
			String orderTimestamp = rs.getString("OrderTimestamp");
			double totalCost = rs.getDouble("OrderTotalCostToBusiness");
			double totalPrice = rs.getDouble("OrderTotalPriceToCustomer");
			String orderState = rs.getString("OrderState");

			Order order;
			if (orderType.equalsIgnoreCase(delivery)) {
				int custID = rs.getInt("DeliveryCustID");
				String address = getCustomerAddress(custID);
				order = new DeliveryOrder(orderId, custID, orderTimestamp, totalPrice, totalCost, orderState.equals("Completed") ? 1 : 0, address);
			} else if (orderType.equalsIgnoreCase(pickup)) {
				int custID = rs.getInt("PickupCustID");
				order = new PickupOrder(orderId, custID, orderTimestamp, totalPrice, totalCost, 0, orderState.equals("Completed") ? 1 : 0);
			} else {
				int tableNumber = rs.getInt("DineInTableNumber");
				order = new DineinOrder(orderId, 0, orderTimestamp, totalPrice, totalCost, orderState.equals("Completed") ? 1 : 0, tableNumber);
			}

			orders.add(order);
		}

		rs.close();
		stmt.close();
		conn.close();

		return orders;
	}

	private static String getCustomerAddress(int CustID) throws SQLException {
		String query = "SELECT CustomerAddressStreet, CustomerAddressCity, CustomerAddressState, CustomerAddressZip " +
				"FROM customer WHERE CustomerCustID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setInt(1, CustID);
		ResultSet rs = stmt.executeQuery();

		String address = "";
		if (rs.next()) {
			String street = rs.getString("CustomerAddressStreet");
			String city = rs.getString("CustomerAddressCity");
			String state = rs.getString("CustomerAddressState");
			String zip = rs.getString("CustomerAddressZip");
			address = street + ", " + city + ", " + state + " " + zip;
		}

		rs.close();
		stmt.close();

		return address;
	}

	public static Order getLastOrder() {
		Order order = null;
		try {
			if (!connect_to_db()) {
				System.out.println("Failed to connect to database.");
				return null;
			}
			String query = "SELECT * FROM `order` ORDER BY OrderID DESC LIMIT 1";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			if (rs.next()) {
				int orderID = rs.getInt("OrderID");
				String orderType = rs.getString("OrderType");
				String date = rs.getString("OrderTimestamp");
				double custPrice = rs.getDouble("OrderTotalPriceToCustomer");
				double busPrice = rs.getDouble("OrderTotalCostToBusiness");
				int isComplete = rs.getInt("IsComplete");

				order = new Order(orderID, -1, orderType, date, custPrice, busPrice, isComplete);
			} else {
				System.out.println("No orders found.");
			}

			rs.close();
			stmt.close();
		} catch (SQLException | IOException e) {
			System.out.println("Error retrieving last order: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) conn.close();
			} catch (SQLException e) {
				System.out.println("Error closing connection: " + e.getMessage());
			}
		}
		return order;
	}


	public static Customer getLastCustomer() throws SQLException, IOException {
		connect_to_db();

		String query = "SELECT * FROM customer ORDER BY CustomerCustID DESC LIMIT 1";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		Customer lastCustomer = null;
		if (rs.next()) {
			int custID = rs.getInt("CustomerCustID");
			String fName = rs.getString("CustomerFName");
			String lName = rs.getString("CustomerLName");
			String phone = rs.getString("CustomerPhone");
			lastCustomer = new Customer(custID, fName, lName, phone);
		}

		rs.close();
		stmt.close();
		conn.close();

		return lastCustomer;
	}

	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
		connect_to_db();

		ArrayList<Discount> discounts = new ArrayList<>();

		String query = "SELECT * FROM discount";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			int discountID = rs.getInt("DiscountID");
			String discountName = rs.getString("DiscountName");
			double amount = rs.getDouble("DiscountAmount");
			String isPercentStr = rs.getString("DiscountIsPercent");
			boolean isPercent = isPercentStr.equalsIgnoreCase("Percentage");

			Discount discount = new Discount(discountID, discountName, amount, isPercent);
			discounts.add(discount);
		}

		rs.close();
		stmt.close();
		conn.close();

		return discounts;
	}

	public static Discount findDiscountByName(String name) {
		try {
			connect_to_db();

			String query = "SELECT * FROM discount WHERE DiscountName = ?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, name);

			ResultSet rs = stmt.executeQuery();
			Discount discount = null;
			if (rs.next()) {
				int discountID = rs.getInt("DiscountID");
				String discountName = rs.getString("DiscountName");
				double amount = rs.getDouble("DiscountAmount");
				String isPercentStr = rs.getString("DiscountIsPercent");
				boolean isPercent = isPercentStr.equalsIgnoreCase("Percentage");

				discount = new Discount(discountID, discountName, amount, isPercent);
			}

			rs.close();
			stmt.close();
			conn.close();

			return discount;
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Discount getDiscountByID(int id) throws SQLException, IOException {
		connect_to_db();
		String query = "SELECT * FROM discount WHERE DiscountID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		Discount discount = null;
		if (rs.next()) {
			int discountID = rs.getInt("DiscountID");
			String discountName = rs.getString("DiscountName");
			double amount = rs.getDouble("DiscountAmount");
			String isPercentStr = rs.getString("DiscountIsPercent");
			boolean isPercent = isPercentStr.equalsIgnoreCase("Percentage");
			discount = new Discount(discountID, discountName, amount, isPercent);
		}
		rs.close();
		stmt.close();
		conn.close();
		return discount;
	}



	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException {
		connect_to_db();
		ArrayList<Customer> customers = new ArrayList<>();

		String query = "SELECT CustomerCustID, CustomerFName AS FirstName, " +
				"CustomerLName AS LastName, CustomerPhone AS Phone " +
				"FROM customer " +
				"ORDER BY LastName, FirstName, Phone";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			int CustID = rs.getInt("CustomerCustID");
			String firstName = rs.getString("FirstName");
			String lastName = rs.getString("LastName");
			String phoneNumber = rs.getString("Phone");

			Customer customer = new Customer(CustID, firstName, lastName, phoneNumber);
			customers.add(customer);
		}

		rs.close();
		stmt.close();
		conn.close();

		return customers;
	}


	public static Customer findCustomerByPhone(String phoneNumber) {
		try {
			connect_to_db();

			String query = "SELECT * FROM customer WHERE CustomerPhone = ?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, phoneNumber);

			ResultSet rs = stmt.executeQuery();
			Customer customer = null;
			if (rs.next()) {
				int custID = rs.getInt("CustomerCustID");
				String fName = rs.getString("CustomerFName");
				String lName = rs.getString("CustomerLName");
				String phone = rs.getString("CustomerPhone");

				customer = new Customer(custID, fName, lName, phone);
			}

			rs.close();
			stmt.close();
			conn.close();

			return customer;
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<Topping> getToppingList() throws SQLException, IOException {
		connect_to_db();
		ArrayList<Topping> toppings = new ArrayList<>();

		String query = "SELECT ToppingID, ToppingName, ToppingAmountUsedPerSize, ToppingPriceToCustomer, " +
				"ToppingPriceToBusiness, ToppingMinimumInventoryLevel, ToppingCurINVT " +
				"FROM topping " +
				"ORDER BY ToppingName";

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		while (rs.next()) {
			int TopID = rs.getInt("ToppingID");
			String toppingName = rs.getString("ToppingName");
			String amountUsedPerSize = rs.getString("ToppingAmountUsedPerSize");
			double custPrice = rs.getDouble("ToppingPriceToCustomer");
			double busPrice = rs.getDouble("ToppingPriceToBusiness");
			int minInventory = rs.getInt("ToppingMinimumInventoryLevel");
			int curInventory = rs.getInt("ToppingCurINVT");

			String[] amounts = amountUsedPerSize.split(",");
			double perAMT = Double.parseDouble(amounts[0]);
			double medAMT = Double.parseDouble(amounts[1]);
			double lgAMT = Double.parseDouble(amounts[2]);
			double xlAMT = Double.parseDouble(amounts[3]);

			Topping topping = new Topping(TopID, toppingName, perAMT, medAMT, lgAMT, xlAMT,
					custPrice, busPrice, minInventory, curInventory);
			toppings.add(topping);
		}

		rs.close();
		stmt.close();
		conn.close();

		return toppings;
	}


	public static Topping findToppingByName(String name) {
		try {
			connect_to_db();
			String query = "SELECT * FROM topping WHERE ToppingName = ?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, name);

			ResultSet rs = stmt.executeQuery();
			Topping topping = null;
			if (rs.next()) {
				int topID = rs.getInt("ToppingID");
				String topName = rs.getString("ToppingName");
				double custPrice = rs.getDouble("ToppingPriceToCustomer");
				double busPrice = rs.getDouble("ToppingPriceToBusiness");
				int minINVT = rs.getInt("ToppingMinimumInventoryLevel");
				int curINVT = rs.getInt("ToppingCurINVT");

				String amounts = rs.getString("ToppingAmountUsedPerSize");
				String[] sizeAmounts = amounts.split(",");
				double perAMT = Double.parseDouble(sizeAmounts[0].trim());
				double medAMT = Double.parseDouble(sizeAmounts[1].trim());
				double lgAMT = Double.parseDouble(sizeAmounts[2].trim());
				double xlAMT = Double.parseDouble(sizeAmounts[3].trim());

				topping = new Topping(topID, topName, perAMT, medAMT, lgAMT, xlAMT, custPrice, busPrice, minINVT, curINVT);
			}

			rs.close();
			stmt.close();
			conn.close();

			return topping;
		} catch (SQLException | IOException e) {
			System.out.println("Error finding topping by name: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}


	public static void addToInventory(Topping t, double quantity) throws SQLException, IOException {
		connect_to_db();

		String query = "UPDATE topping SET ToppingCurINVT = ToppingCurINVT + ? WHERE ToppingID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setDouble(1, quantity);
		stmt.setInt(2, t.getTopID());
		int rowsAffected = stmt.executeUpdate();

		if (rowsAffected != 1) {
			System.out.println("Failed to update inventory for topping " + t.getTopName());
		}

		stmt.close();
		conn.close();
	}

	public static double getBaseCustPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();

		String query = "SELECT BasePricePrice FROM baseprice WHERE BasePriceSize = ? AND BasePriceCrustType = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, size);
		stmt.setString(2, crust);

		ResultSet rs = stmt.executeQuery();
		double price = 0.0;
		if (rs.next()) {
			price = rs.getDouble("BasePricePrice");
		}

		rs.close();
		stmt.close();
		conn.close();

		return price;
	}

	public static double getBaseBusPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();

		String query = "SELECT BasePriceCost FROM baseprice WHERE BasePriceSize = ? AND BasePriceCrustType = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, size);
		stmt.setString(2, crust);

		ResultSet rs = stmt.executeQuery();
		double price = 0.0;
		if (rs.next()) {
			price = rs.getDouble("BasePriceCost");
		}

		rs.close();
		stmt.close();
		conn.close();

		return price;
	}

	public static void printInventory() throws SQLException, IOException {
		connect_to_db();

		try {
			String query = "SELECT ToppingID, ToppingName, ToppingCurINVT FROM topping ORDER BY ToppingName";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			boolean found = false;
			while (rs.next()) {
				if (!found) {
					System.out.println("Current Inventory Levels:");
					System.out.printf("%-5s %-20s %-10s\n", "ID", "Topping", "Inventory");
					found = true;
				}
				int TopID = rs.getInt("ToppingID");
				String toppingName = rs.getString("ToppingName");
				int inventory = rs.getInt("ToppingCurINVT");

				System.out.printf("%-5d %-20s %-10d\n", TopID, toppingName, inventory);
			}

			if (!found) {
				System.out.println("No toppings found in the inventory.");
			}

			rs.close();
			stmt.close();
		} finally {
			conn.close();
		}
	}

	public static void printToppingPopReport() throws SQLException, IOException {
		connect_to_db();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM ToppingPopularity");
		System.out.println("Topping Popularity Report:");
		while (rs.next()) {
			System.out.println(rs.getString("Topping") + ": " + rs.getInt("ToppingCount"));
		}
	}

	public static void printProfitByPizzaReport() throws SQLException, IOException {
		connect_to_db();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM ProfitByPizza");
		System.out.println("Profit by Pizza Report:");
		while (rs.next()) {
			System.out.println("Size: " + rs.getString("Size") + ", Crust: " + rs.getString("Crust") + ", Profit: " + rs.getDouble("Profit"));
		}
	}

	public static void printProfitByOrderType() throws SQLException, IOException {
		connect_to_db();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM ProfitByOrderType");
		System.out.println("Profit by Order Type Report:");
		while (rs.next()) {
			System.out.println("Customer Type: " + rs.getString("customerType") + ", Total Profit: " + rs.getDouble("Profit"));
		}
	}

	public static String getCustomerName(int CustID) throws SQLException, IOException {
		connect_to_db();

		String query = "SELECT CustomerFName, CustomerLName FROM customer WHERE CustomerCustID = ?";
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setInt(1, CustID);
		ResultSet rs = stmt.executeQuery();

		String customerName = "";
		if (rs.next()) {
			customerName = rs.getString("CustomerFName") + " " + rs.getString("CustomerLName");
		} else {
			customerName = "Unknown Customer";
		}

		rs.close();
		stmt.close();
		conn.close();

		return customerName;
	}

	/*
	 * The next 3 private methods help get the individual components of a SQL datetime object. 
	 * You're welcome to keep them or remove them.
	 */
	private static int getYear(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(0,4));
	}
	private static int getMonth(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(5, 7));
	}
	private static int getDay(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(8, 10));
	}

	public static boolean checkDate(int year, int month, int day, String dateOfOrder)
	{
		if(getYear(dateOfOrder) > year)
			return true;
		else if(getYear(dateOfOrder) < year)
			return false;
		else
		{
			if(getMonth(dateOfOrder) > month)
				return true;
			else if(getMonth(dateOfOrder) < month)
				return false;
			else
			{
				if(getDay(dateOfOrder) >= day)
					return true;
				else
					return false;
			}
		}
	}


}