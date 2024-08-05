package cpsc4620;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;


/*
 * This file is where the front end magic happens.
 * 
 * You will have to write the methods for each of the menu options.
 * 
 * This file should not need to access your DB at all, it should make calls to the DBNinja that will do all the connections.
 * 
 * You can add and remove methods as you see necessary. But you MUST have all of the menu methods (including exit!)
 * 
 * Simply removing menu methods because you don't know how to implement it will result in a major error penalty (akin to your program crashing)
 * 
 * Speaking of crashing. Your program shouldn't do it. Use exceptions, or if statements, or whatever it is you need to do to keep your program from breaking.
 * 
 */

public class Menu {

	public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) throws SQLException, IOException {

		System.out.println("Welcome to Pizzas-R-Us!");
		
		int menu_option = 0;

		// present a menu of options and take their selection
		
		PrintMenu();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String option = reader.readLine();
		menu_option = Integer.parseInt(option);

		while (menu_option != 9) {
			switch (menu_option) {
			case 1:// enter order
				EnterOrder();
				break;
			case 2:// view customers
				ViewCustomers();
				break;
			case 3:// enter customer
				EnterCustomer();
				break;
			case 4:// view order
				// open/closed/date
				ViewOrders();
				break;
			case 5:// mark order as complete
				MarkOrderAsComplete();
				break;
			case 6:// view inventory levels
				ViewInventoryLevels();
				break;
			case 7:// add to inventory
				AddInventory();
				break;
			case 8:// view reports
				PrintReports();
				break;
			}
			PrintMenu();
			option = reader.readLine();
			menu_option = Integer.parseInt(option);
		}

	}

	// allow for a new order to be placed
	public static void EnterOrder() throws SQLException, IOException {
		System.out.println("Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
		int orderTypeChoice = Integer.parseInt(reader.readLine());
		String orderType;
		if (orderTypeChoice == 1) {
			orderType = DBNinja.dine_in;
		} else if (orderTypeChoice == 2) {
			orderType = DBNinja.pickup;
		} else {
			orderType = DBNinja.delivery;
		}

		System.out.println("Is this order for an existing customer? Answer y/n: ");
		String existingCustomer = reader.readLine();
		int customerId = -1;
		String address = "";
		if (existingCustomer.equalsIgnoreCase("y")) {
			ArrayList<Customer> customers = DBNinja.getCustomerList();
			System.out.println("Here's a list of the current customers: ");
			for (Customer c : customers) {
				System.out.println(c.toString());
			}
			System.out.println("Which customer is this order for? Enter ID Number:");
			customerId = Integer.parseInt(reader.readLine());
			if (orderType.equals(DBNinja.delivery)) {
				address = requestDeliveryAddress();
			}
		} else {
			EnterCustomer();
			Customer newCustomer = DBNinja.getLastCustomer();
			customerId = newCustomer.getCustID();
			if (orderType.equals(DBNinja.delivery)) {
				address = requestDeliveryAddress();
			}
		}

		String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		Order order = null;
		if (orderType.equals(DBNinja.dine_in)) {
			System.out.println("What is the table number for this order?");
			int tableNumber = Integer.parseInt(reader.readLine());
			order = new DineinOrder(0, customerId, currentTimestamp, 0.0, 0.0, 0, tableNumber);
		} else if (orderType.equals(DBNinja.pickup)) {
			order = new PickupOrder(0, customerId, currentTimestamp, 0.0, 0.0, 0, 0);
		} else {
			order = new DeliveryOrder(0, customerId, currentTimestamp, 0.0, 0.0, 0, address);
		}

		int orderID = DBNinja.addOrder(order);
		order.setOrderID(orderID);
		buildPizzasForOrder(orderID);
		applyOrderDiscounts(order);

		System.out.println("Finished adding order...Returning to menu...");
	}

	private static String requestDeliveryAddress() throws IOException {
		System.out.println("What is the House/Apt Number for this order? (e.g., 111)");
		String houseNumber = reader.readLine();
		System.out.println("What is the Street for this order? (e.g., Smile Street)");
		String street = reader.readLine();
		System.out.println("What is the City for this order? (e.g., Greenville)");
		String city = reader.readLine();
		System.out.println("What is the State for this order? (e.g., SC)");
		String state = reader.readLine();
		System.out.println("What is the Zip Code for this order? (e.g., 20605)");
		String zipCode = reader.readLine();
		return houseNumber + " " + street + ", " + city + ", " + state + " " + zipCode;
	}

	private static void buildPizzasForOrder(int orderID) throws SQLException, IOException {
		boolean addMorePizzas = true;
		while (addMorePizzas) {
			System.out.println("Let's build a pizza!");
			Pizza pizza = buildPizza(orderID);
			System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
			int continueChoice = Integer.parseInt(reader.readLine());
			if (continueChoice == -1) {
				addMorePizzas = false;
			}
		}
	}

	private static void applyOrderDiscounts(Order order) throws SQLException, IOException {
		System.out.println("Do you want to add discounts to this order? Enter y/n:");
		String addDiscounts = reader.readLine();
		if (addDiscounts.equalsIgnoreCase("y")) {
			boolean addMoreDiscounts = true;
			ArrayList<Discount> discounts = DBNinja.getDiscountList();
			System.out.println("Available Discounts:");
			for (Discount d : discounts) {
				System.out.println(d);
			}
			while (addMoreDiscounts) {
				System.out.println("Which discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts:");
				int discountID = Integer.parseInt(reader.readLine());
				if (discountID == -1) {
					addMoreDiscounts = false;
				} else {
					Discount discount = DBNinja.getDiscountByID(discountID);
					if (discount != null) {
						order.addDiscount(discount);
						DBNinja.useOrderDiscount(order, discount);
					} else {
						System.out.println("Discount not found.");
					}
				}
			}
		}
	}

	public static void ViewCustomers() throws SQLException, IOException {
		ArrayList<Customer> customers = DBNinja.getCustomerList();

		if (customers.isEmpty()) {
			System.out.println("No customers found.");
		} else {
			System.out.println("Customer List:");
			for (Customer customer : customers) {
				System.out.println(customer.toString());
			}
		}

		System.out.println("Returning to menu...");
	}
	

	// Enter a new customer in the database
	public static void EnterCustomer() throws SQLException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("What is this customer's name (first <space> last):");
		String name = reader.readLine();

		System.out.println("What is this customer's phone number (##########) (No dash/space):");
		String phoneNumber = reader.readLine();

		String[] nameParts = name.split(" ");
		String firstName = nameParts[0];
		String lastName = nameParts[1];

		Customer newCustomer = new Customer(0, firstName, lastName, phoneNumber);
		DBNinja.addCustomer(newCustomer);

		System.out.println("New customer added successfully!");
	}

	// View any orders that are not marked as completed
	public static void ViewOrders() throws SQLException, IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Would you like to:");
		System.out.println("(a) display all orders [open or closed]");
		System.out.println("(b) display all open orders");
		System.out.println("(c) display all completed [closed] orders");
		System.out.println("(d) display orders since a specific date");

		String choice = reader.readLine().toLowerCase();
		ArrayList<Order> orders = new ArrayList<>();

		switch (choice) {
			case "a":
				orders = DBNinja.getOrders(false);
				break;
			case "b":
				orders = DBNinja.getOrders(true);
				break;
			case "c":
				orders = DBNinja.getOrders(false);
				orders.removeIf(order -> order.getIsComplete() == 0);
				break;
			case "d":
				System.out.println("What is the date you want to restrict by? (FORMAT: YYYY-MM-DD)");
				String dateStr = reader.readLine();
				orders = DBNinja.getOrdersByDate(dateStr);
				break;
			default:
				System.out.println("I don't understand that input, returning to menu");
				return;
		}

		if (orders.isEmpty()) {
			System.out.println("No orders to display, returning to menu.");
			return;
		}

		for (int i = 0; i < orders.size(); i++) {
			System.out.println((i + 1) + ". " + orders.get(i).toSimplePrint());
		}

		System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
		int orderIndex = Integer.parseInt(reader.readLine());

		if (orderIndex >= 1 && orderIndex <= orders.size()) {
			Order selectedOrder = orders.get(orderIndex - 1);
			System.out.println(selectedOrder.toString());
		} else if (orderIndex != -1) {
			System.out.println("Incorrect entry, returning to menu.");
		}
	}
	
	// When an order is completed, we need to make sure it is marked as complete
	public static void MarkOrderAsComplete() throws SQLException, IOException {
		ArrayList<Order> openOrders = DBNinja.getOrders(true);

		if (openOrders.isEmpty()) {
			System.out.println("There are no open orders currently... returning to menu...");
			return;
		}

		System.out.println("Open orders:");
		for (int i = 0; i < openOrders.size(); i++) {
			Order order = openOrders.get(i);
			System.out.println((i + 1) + ". " + order.toSimplePrint());
		}

		System.out.println("Which order would you like to mark as complete? Enter the OrderID: ");
		int orderID = Integer.parseInt(reader.readLine());

		boolean found = false;
		for (Order order : openOrders) {
			if (order.getOrderID() == orderID) {
				found = true;
				DBNinja.completeOrder(order);
				System.out.println("Order " + orderID + " has been marked as complete.");
				break;
			}
		}

		if (!found) {
			System.out.println("Incorrect entry, not an option");
		}
	}

	public static void ViewInventoryLevels() throws SQLException, IOException {
		DBNinja.printInventory();
	}

	public static void AddInventory() throws SQLException, IOException {
		ArrayList<Topping> toppings = DBNinja.getToppingList();

		System.out.println("Current Inventory:");
		System.out.printf("%-5s %-20s %-10s\n", "ID", "Topping", "Inventory");
		for (Topping topping : toppings) {
			System.out.printf("%-5d %-20s %-10d\n", topping.getTopID(), topping.getTopName(), topping.getCurINVT());
		}

		System.out.print("Which topping do you want to add inventory to? Enter the number: ");
		int toppingID = Integer.parseInt(reader.readLine());

		Topping selectedTopping = null;
		for (Topping topping : toppings) {
			if (topping.getTopID() == toppingID) {
				selectedTopping = topping;
				break;
			}
		}

		if (selectedTopping == null) {
			System.out.println("Incorrect entry, not an option");
			return;
		}

		System.out.print("How many units would you like to add? ");
		double quantityToAdd = Double.parseDouble(reader.readLine());

		DBNinja.addToInventory(selectedTopping, quantityToAdd);
		System.out.println("Inventory updated successfully!");
	}

	public static Pizza buildPizza(int orderID) throws SQLException, IOException {
		System.out.println("Select the size of the pizza:");
		System.out.println("1. " + DBNinja.size_s);
		System.out.println("2. " + DBNinja.size_m);
		System.out.println("3. " + DBNinja.size_l);
		System.out.println("4. " + DBNinja.size_xl);
		System.out.print("Enter your choice: ");
		int sizeChoice = Integer.parseInt(reader.readLine());
		String size = "";
		switch (sizeChoice) {
			case 1:
				size = DBNinja.size_s;
				break;
			case 2:
				size = DBNinja.size_m;
				break;
			case 3:
				size = DBNinja.size_l;
				break;
			case 4:
				size = DBNinja.size_xl;
				break;
			default:
				throw new IllegalArgumentException("Invalid size choice");
		}

		System.out.println("Select the crust for this pizza:");
		System.out.println("1. " + DBNinja.crust_thin);
		System.out.println("2. " + DBNinja.crust_orig);
		System.out.println("3. " + DBNinja.crust_pan);
		System.out.println("4. " + DBNinja.crust_gf);
		System.out.print("Enter your choice: ");
		int crustChoice = Integer.parseInt(reader.readLine());
		String crust = "";
		switch (crustChoice) {
			case 1:
				crust = DBNinja.crust_thin;
				break;
			case 2:
				crust = DBNinja.crust_orig;
				break;
			case 3:
				crust = DBNinja.crust_pan;
				break;
			case 4:
				crust = DBNinja.crust_gf;
				break;
			default:
				throw new IllegalArgumentException("Invalid crust choice");
		}

		String pizzaState = "Processing";
		String pizzaDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		Pizza pizza = new Pizza(0, size, crust, orderID, pizzaState, pizzaDate,
				DBNinja.getBaseCustPrice(size, crust), DBNinja.getBaseBusPrice(size, crust));

		int pizzaID = DBNinja.addPizza(pizza);
		pizza.setPizzaID(pizzaID);

		ArrayList<Topping> availableToppings = DBNinja.getToppingList();
		boolean addMoreToppings = true;
		while (addMoreToppings) {
			System.out.println("Available Toppings:");
			for (Topping topping : availableToppings) {
				System.out.printf("%d: %s\n", topping.getTopID(), topping.getTopName());
			}
			System.out.print("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
			int toppingID = Integer.parseInt(reader.readLine());
			if (toppingID == -1) {
				addMoreToppings = false;
			} else {
				Topping topping = availableToppings.stream()
						.filter(t -> t.getTopID() == toppingID)
						.findFirst()
						.orElse(null);
				if (topping != null) {
					double toppingAmount;
					switch (size) {
						case DBNinja.size_s:
							toppingAmount = topping.getPerAMT();
							break;
						case DBNinja.size_m:
							toppingAmount = topping.getMedAMT();
							break;
						case DBNinja.size_l:
							toppingAmount = topping.getLgAMT();
							break;
						case DBNinja.size_xl:
							toppingAmount = topping.getXLAMT();
							break;
						default:
							throw new IllegalStateException("Invalid pizza size: " + size);
					}

					System.out.print("Do you want to add extra topping? Enter y/n: ");
					String extraTopping = reader.readLine();
					boolean isDoubled = extraTopping.equalsIgnoreCase("y");

					double totalToppingAmount = isDoubled ? 2 * toppingAmount : toppingAmount;

					if (topping.getCurINVT() < totalToppingAmount) {
						System.out.println("We don't have enough of that topping to add it...");
					} else {
						DBNinja.useTopping(pizza, topping, isDoubled);
						pizza.addToppings(topping, isDoubled);
						topping.setCurINVT((int) Math.round(topping.getCurINVT() - totalToppingAmount));
					}
				} else {
					System.out.println("Topping not found. Please try again.");
				}
			}
		}

		System.out.print("Do you want to add discounts to this Pizza? Enter y/n: ");
		String addDiscounts = reader.readLine();
		if (addDiscounts.equalsIgnoreCase("y")) {
			ArrayList<Discount> availableDiscounts = DBNinja.getDiscountList();
			boolean addMoreDiscounts = true;
			while (addMoreDiscounts) {
				System.out.println("Available Discounts:");
				for (Discount discount : availableDiscounts) {
					System.out.printf("%d: %s\n", discount.getDiscountID(), discount.getDiscountName());
				}
				System.out.print("Enter the ID of the discount you want to add or -1 to finish: ");
				int discountID = Integer.parseInt(reader.readLine());
				if (discountID == -1) {
					addMoreDiscounts = false;
				} else {
					Discount discount = availableDiscounts.stream()
							.filter(d -> d.getDiscountID() == discountID)
							.findFirst()
							.orElse(null);
					if (discount != null) {
						DBNinja.usePizzaDiscount(pizza, discount);
						pizza.addDiscounts(discount);
					} else {
						System.out.println("Discount not found. Please try again.");
					}
				}
			}
		}

		return pizza;
	}

	public static void PrintReports() throws SQLException, NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("Which report do you wish to print? Enter:\n(a) ToppingPopularity\n(b) ProfitByPizza\n(c) ProfitByOrderType\n(x) Exit");
			String input = reader.readLine().trim().toLowerCase();

			switch (input) {
				case "a":
					DBNinja.printToppingPopReport();
					return;
				case "b":
					DBNinja.printProfitByPizzaReport();
					return;
				case "c":
					DBNinja.printProfitByOrderType();
					return;
				case "x":
					System.out.println("Exiting...");
					return;
				default:
					System.out.println("I don't understand that input... please try again.");
			}
		}
	}

	//Prompt - NO CODE SHOULD TAKE PLACE BELOW THIS LINE
	// DO NOT EDIT ANYTHING BELOW HERE, THIS IS NEEDED TESTING.
	// IF YOU EDIT SOMETHING BELOW, IT BREAKS THE AUTOGRADER WHICH MEANS YOUR GRADE WILL BE A 0 (zero)!!

	public static void PrintMenu() {
		System.out.println("\n\nPlease enter a menu option:");
		System.out.println("1. Enter a new order");
		System.out.println("2. View Customers ");
		System.out.println("3. Enter a new Customer ");
		System.out.println("4. View orders");
		System.out.println("5. Mark an order as completed");
		System.out.println("6. View Inventory Levels");
		System.out.println("7. Add Inventory");
		System.out.println("8. View Reports");
		System.out.println("9. Exit\n\n");
		System.out.println("Enter your option: ");
	}

	/*
	 * autograder controls....do not modiify!
	 */

	public final static String autograder_seed = "6f1b7ea9aac470402d48f7916ea6a010";

	
	private static void autograder_compilation_check() {

		try {
			Order o = null;
			Pizza p = null;
			Topping t = null;
			Discount d = null;
			Customer c = null;
			ArrayList<Order> alo = null;
			ArrayList<Discount> ald = null;
			ArrayList<Customer> alc = null;
			ArrayList<Topping> alt = null;
			double v = 0.0;
			String s = "";

			DBNinja.addOrder(o);
			DBNinja.addPizza(p);
			DBNinja.useTopping(p, t, false);
			DBNinja.usePizzaDiscount(p, d);
			DBNinja.useOrderDiscount(o, d);
			DBNinja.addCustomer(c);
			DBNinja.completeOrder(o);
			alo = DBNinja.getOrders(false);
			o = DBNinja.getLastOrder();
			alo = DBNinja.getOrdersByDate("01/01/1999");
			ald = DBNinja.getDiscountList();
			d = DBNinja.findDiscountByName("Discount");
			alc = DBNinja.getCustomerList();
			c = DBNinja.findCustomerByPhone("0000000000");
			alt = DBNinja.getToppingList();
			t = DBNinja.findToppingByName("Topping");
			DBNinja.addToInventory(t, 1000.0);
			v = DBNinja.getBaseCustPrice("size", "crust");
			v = DBNinja.getBaseBusPrice("size", "crust");
			DBNinja.printInventory();
			DBNinja.printToppingPopReport();
			DBNinja.printProfitByPizzaReport();
			DBNinja.printProfitByOrderType();
			s = DBNinja.getCustomerName(0);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
}

