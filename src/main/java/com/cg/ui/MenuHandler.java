package com.cg.ui;

import com.cg.dto.User;
import com.cg.dto.FoodItem;
import com.cg.dto.Order;
import com.cg.services.*;
import com.cg.exception.*;
import com.cg.utils.InputValidator;
import java.util.*;

public class MenuHandler {
    private final Scanner scanner;
    private final UserService userService;
    private final FoodService foodService;
    private final OrderService orderService;
    private final ManagerService managerService;
    private User currentUser;
    
    public MenuHandler() {
        this.scanner = new Scanner(System.in);
        this.userService = new UserServiceImpl();
        this.foodService = new FoodServiceImpl();
        this.orderService = new OrderServiceImpl();
        this.managerService = new ManagerServiceImpl();
    }
    
    public void start() {
        System.out.println("=== Welcome to Online Food Delivery System ===");
        
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }
    
    private void showLoginMenu() {
        System.out.println("\n=== Login/Registration Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Register Customer");
        System.out.println("3. Register Delivery Person");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                registerCustomer();
                break;
            case 3:
                registerDeliveryPerson();
                break;
            case 4:
                System.out.println("Thank you for using Food Delivery System!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    private void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        
        // Show delivery status for delivery personnel at the top
        if ("DELIVERY".equalsIgnoreCase(currentUser.getRole())) {
            showDeliveryStatus();
        }
        
        if ("CUSTOMER".equalsIgnoreCase(currentUser.getRole())) {
            showCustomerMenu();
        } else if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            showManagerMenu();
        } else if ("DELIVERY".equalsIgnoreCase(currentUser.getRole())) {
            showDeliveryPersonMenu();
        }
        
        System.out.println("0. Logout");
        System.out.print("Choose an option: ");
        
        int choice = getIntInput();
        handleMenuChoice(choice);
    }
    
    private void showDeliveryStatus() {
        System.out.println("\n=== Your Delivery Status ===");
        List<Order> assignedOrders = orderService.getOrdersByDeliveryPersonId(currentUser.getId());
        
        if (assignedOrders.isEmpty()) {
            System.out.println("No deliveries assigned to you currently.");
        } else {
            System.out.println("Assigned Deliveries:");
            for (Order order : assignedOrders) {
                if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) {
                    System.out.println("- Order ID: " + order.getOrderId() + 
                                     " | Customer ID: " + order.getCustomerId() + 
                                     " | Status: " + order.getStatus() + 
                                     " | Amount: $" + String.format("%.2f", order.getTotalAmount()));
                }
            }
        }
        System.out.println("Availability: " + (currentUser.isAvailable() ? "AVAILABLE" : "BUSY"));
        System.out.println("=============================");
    }
    
    private void showCustomerMenu() {
        System.out.println("1. View Menu");
        System.out.println("2. Place Order");
        System.out.println("3. View My Orders");
    }
    
    private void showManagerMenu() {
        System.out.println("1. View Menu");
        System.out.println("2. Add New Food Item");
        System.out.println("3. Restock Food Item");
        System.out.println("4. View All Delivery Personnel");
        System.out.println("5. Remove Delivery Person");
    }
    
    private void showDeliveryPersonMenu() {
        System.out.println("1. View My Orders");
        System.out.println("2. Complete Order");
        System.out.println("3. Toggle Availability");
    }
    
    private void handleMenuChoice(int choice) {
        if (choice == 0) {
            logout();
            return;
        }
        
        try {
            if ("CUSTOMER".equalsIgnoreCase(currentUser.getRole())) {
                handleCustomerChoice(choice);
            } else if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
                handleManagerChoice(choice);
            } else if ("DELIVERY".equalsIgnoreCase(currentUser.getRole())) {
                handleDeliveryChoice(choice);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void handleCustomerChoice(int choice) throws InvalidOrderException {
        switch (choice) {
            case 1:
                displayMenu();
                break;
            case 2:
                placeOrder();
                break;
            case 3:
                viewMyOrders();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
    
    private void handleManagerChoice(int choice) throws UserNotFoundException {
        switch (choice) {
            case 1:
                displayMenu();
                break;
            case 2:
                addNewFoodItem();
                break;
            case 3:
                restockFoodItem();
                break;
            case 4:
                viewAllDeliveryPersonnel();
                break;
            case 5:
                removeDeliveryPerson();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
    
    private void handleDeliveryChoice(int choice) throws InvalidOrderException, UserNotFoundException {
        switch (choice) {
            case 1:
                viewMyDeliveries();
                break;
            case 2:
                completeOrder();
                break;
            case 3:
                toggleAvailability();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
    
    private void login() {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        
        try {
            currentUser = userService.authenticateUser(id, password);
            System.out.println("Login successful! Welcome " + currentUser.getName());
        } catch (UserNotFoundException | InvalidCredentialsException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }
    
    private void logout() {
        currentUser = null;
        System.out.println("Logged out successfully!");
    }
    
    private void registerCustomer() {
        System.out.println("\n=== Customer Registration ===");
        
        String id = InputValidator.getValidInput(scanner, "Enter Customer ID (min 3 chars): ", 
                                               "ID cannot be empty and must be at least 3 characters long.");
        
        if (!InputValidator.isValidId(id)) {
            System.out.println("Invalid ID format.");
            return;
        }
        
        // Check if user ID already exists
        try {
            userService.getUserById(id);
            System.out.println("Error: User ID already exists. Please choose a different ID.");
            return;
        } catch (UserNotFoundException e) {
            // ID doesn't exist, which is good - we can proceed
        }
        
        String name = InputValidator.getValidInput(scanner, "Enter Name: ", "Name cannot be empty.");
        
        String email;
        do {
            email = InputValidator.getValidInput(scanner, "Enter Email: ", "Email cannot be empty.");
            if (!InputValidator.isValidEmail(email)) {
                System.out.println("Invalid email format. Please try again.");
            }
        } while (!InputValidator.isValidEmail(email));
        
        String phone;
        do {
            phone = InputValidator.getValidInput(scanner, "Enter Phone (10 digits): ", "Phone cannot be empty.");
            if (!InputValidator.isValidPhone(phone)) {
                System.out.println("Invalid phone format. Please enter 10 digits.");
            }
        } while (!InputValidator.isValidPhone(phone));
        
        String password;
        do {
            password = InputValidator.getValidInput(scanner, "Enter Password (min 6 chars): ", "Password cannot be empty.");
            if (!InputValidator.isValidPassword(password)) {
                System.out.println("Password must be at least 6 characters long.");
            }
        } while (!InputValidator.isValidPassword(password));
        
        userService.registerCustomer(id, name, email, phone, password);
        System.out.println("Customer registered successfully!");
    }
    
    private void registerDeliveryPerson() {
        System.out.println("\n=== Delivery Person Registration ===");
        
        String id = InputValidator.getValidInput(scanner, "Enter Delivery Person ID (min 3 chars): ", 
                                               "ID cannot be empty and must be at least 3 characters long.");
        
        if (!InputValidator.isValidId(id)) {
            System.out.println("Invalid ID format.");
            return;
        }
        
        // Check if user ID already exists
        try {
            userService.getUserById(id);
            System.out.println("Error: User ID already exists. Please choose a different ID.");
            return;
        } catch (UserNotFoundException e) {
            // ID doesn't exist, which is good - we can proceed
        }
        
        String name = InputValidator.getValidInput(scanner, "Enter Name: ", "Name cannot be empty.");
        
        String email;
        do {
            email = InputValidator.getValidInput(scanner, "Enter Email: ", "Email cannot be empty.");
            if (!InputValidator.isValidEmail(email)) {
                System.out.println("Invalid email format. Please try again.");
            }
        } while (!InputValidator.isValidEmail(email));
        
        String phone;
        do {
            phone = InputValidator.getValidInput(scanner, "Enter Phone (10 digits): ", "Phone cannot be empty.");
            if (!InputValidator.isValidPhone(phone)) {
                System.out.println("Invalid phone format. Please enter 10 digits.");
            }
        } while (!InputValidator.isValidPhone(phone));
        
        String password;
        do {
            password = InputValidator.getValidInput(scanner, "Enter Password (min 6 chars): ", "Password cannot be empty.");
            if (!InputValidator.isValidPassword(password)) {
                System.out.println("Password must be at least 6 characters long.");
            }
        } while (!InputValidator.isValidPassword(password));
        
        userService.registerDeliveryPerson(id, name, email, phone, password);
        System.out.println("Delivery Person registered successfully!");
    }
    
    private void displayMenu() {
        System.out.println("\n=== Food Menu ===");
        Map<FoodItem, Integer> menu = foodService.getMenu();
        
        if (menu.isEmpty()) {
            System.out.println("No items available in the menu.");
            return;
        }
        
        System.out.printf("%-20s %-10s %-10s%n", "Item Name", "Price", "Quantity");
        System.out.println("----------------------------------------");
        
        for (Map.Entry<FoodItem, Integer> entry : menu.entrySet()) {
            FoodItem item = entry.getKey();
            int quantity = entry.getValue();
            System.out.printf("%-20s $%-9.2f %-10d%n", item.getName(), item.getPrice(), quantity);
        }
    }
    
    private void placeOrder() throws InvalidOrderException {
        // Check if delivery personnel are available before taking order
        List<User> availableDeliveryPersons = userService.getAvailableDeliveryPersons();
        if (availableDeliveryPersons.isEmpty()) {
            System.out.println("Sorry! No delivery personnel are available right now. Orders cannot be placed at this time.");
            System.out.println("Please try again later when delivery staff becomes available.");
            return;
        }
        
        displayMenu();
        
        Map<String, Integer> orderItems = new HashMap<>();
        
        System.out.println("\nEnter items to order (type 'done' to finish):");
        
        while (true) {
            System.out.print("Item name (or 'done'): ");
            String itemName = scanner.nextLine().trim();
            
            if ("done".equalsIgnoreCase(itemName)) {
                break;
            }
            
            if (foodService.getFoodItemByName(itemName) == null) {
                System.out.println("Item not found. Please try again.");
                continue;
            }
            
            int quantity;
            do {
                System.out.print("Quantity: ");
                quantity = getIntInput();
                if (!InputValidator.isValidQuantity(quantity)) {
                    System.out.println("Quantity must be greater than 0.");
                }
            } while (!InputValidator.isValidQuantity(quantity));
            
            orderItems.put(itemName, orderItems.getOrDefault(itemName, 0) + quantity);
        }
        
        if (orderItems.isEmpty()) {
            System.out.println("No items selected for order.");
            return;
        }
        
        Order order = orderService.placeOrder(currentUser.getId(), orderItems);
        System.out.println("\nOrder placed successfully!");
        System.out.println("Order ID: " + order.getOrderId());
        System.out.println("Total Amount: $" + String.format("%.2f", order.getTotalAmount()));
        System.out.println("Delivery Person ID: " + order.getDeliveryPersonId());
    }
    
    private void viewMyOrders() {
        List<Order> orders = orderService.getOrdersByCustomerId(currentUser.getId());
        
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        
        System.out.println("\n=== Your Orders ===");
        for (Order order : orders) {
            System.out.println(orderService.getOrderDetails(order.getOrderId()));
            System.out.println("------------------------");
        }
    }
    
    private void addNewFoodItem() {
        System.out.println("\n=== Add New Food Item ===");
        
        String name = InputValidator.getValidInput(scanner, "Enter item name: ", "Name cannot be empty.");
        
        double price;
        do {
            System.out.print("Enter price: $");
            price = getDoubleInput();
            if (!InputValidator.isValidPrice(price)) {
                System.out.println("Price must be greater than 0.");
            }
        } while (!InputValidator.isValidPrice(price));
        
        int quantity;
        do {
            System.out.print("Enter initial quantity: ");
            quantity = getIntInput();
            if (!InputValidator.isValidQuantity(quantity)) {
                System.out.println("Quantity must be greater than 0.");
            }
        } while (!InputValidator.isValidQuantity(quantity));
        
        managerService.addNewFoodItem(name, price, quantity);
        System.out.println("Food item added successfully!");
    }
    
    private void restockFoodItem() {
        displayMenu();
        
        String itemName = InputValidator.getValidInput(scanner, "Enter item name to restock: ", 
                                                     "Item name cannot be empty.");
        
        int quantity;
        do {
            System.out.print("Enter quantity to add: ");
            quantity = getIntInput();
            if (!InputValidator.isValidQuantity(quantity)) {
                System.out.println("Quantity must be greater than 0.");
            }
        } while (!InputValidator.isValidQuantity(quantity));
        
        managerService.restockFoodItem(itemName, quantity);
        System.out.println("Item restocked successfully!");
    }
    
    private void viewAllDeliveryPersonnel() {
        List<User> deliveryPersons = userService.getAllDeliveryPersons();
        
        if (deliveryPersons.isEmpty()) {
            System.out.println("No delivery personnel registered.");
            return;
        }
        
        System.out.println("\n=== All Delivery Personnel ===");
        System.out.printf("%-10s %-20s %-15s %-12s%n", "ID", "Name", "Phone", "Available");
        System.out.println("-------------------------------------------------------");
        
        for (User person : deliveryPersons) {
            System.out.printf("%-10s %-20s %-15s %-12s%n", 
                            person.getId(), person.getName(), person.getPhone(), 
                            person.isAvailable() ? "Yes" : "No");
        }
    }
    
    private void removeDeliveryPerson() throws UserNotFoundException {
        viewAllDeliveryPersonnel();
        
        String id = InputValidator.getValidInput(scanner, "Enter Delivery Person ID to remove: ", 
                                               "ID cannot be empty.");
        
        managerService.removeDeliveryPerson(id);
        System.out.println("Delivery person removed successfully!");
    }
    
    private void viewMyDeliveries() {
        System.out.println("\n=== My Assigned Deliveries ===");
        List<Order> assignedOrders = orderService.getOrdersByDeliveryPersonId(currentUser.getId());
        
        if (assignedOrders.isEmpty()) {
            System.out.println("No deliveries assigned to you.");
            return;
        }
        
        for (Order order : assignedOrders) {
            System.out.println(orderService.getOrderDetails(order.getOrderId()));
            System.out.println("------------------------");
        }
    }
    
    private void completeOrder() throws InvalidOrderException {
        System.out.print("Enter Order ID to complete: ");
        String orderId = scanner.nextLine().trim();
        
        orderService.completeOrder(orderId);
        System.out.println("Order completed successfully!");
    }
    
    private void toggleAvailability() throws UserNotFoundException {
        boolean newStatus = !currentUser.isAvailable();
        userService.updateUserAvailability(currentUser.getId(), newStatus);
        currentUser.setAvailable(newStatus);
        
        System.out.println("Availability updated. You are now " + 
                          (newStatus ? "AVAILABLE" : "UNAVAILABLE") + " for deliveries.");
    }
    
    private int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    private double getDoubleInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}