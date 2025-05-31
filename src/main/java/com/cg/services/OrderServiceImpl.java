package com.cg.services;

import com.cg.dao.OrderDao;
import com.cg.dao.OrderDaoImpl;
import com.cg.dao.FoodDao;
import com.cg.dao.FoodDaoImpl;
import com.cg.dto.Order;
import com.cg.dto.FoodItem;
import com.cg.dto.User;
import com.cg.exception.InvalidOrderException;
import com.cg.exception.UserNotFoundException;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private final FoodDao foodDao;
    private final UserService userService;
    
    public OrderServiceImpl() {
        this.orderDao = new OrderDaoImpl();
        this.foodDao = new FoodDaoImpl();
        this.userService = new UserServiceImpl();
    }
    
    @Override
    public Order placeOrder(String customerId, Map<String, Integer> requestedItems) 
            throws InvalidOrderException {
        
        // Validate customer exists
        try {
            userService.getUserById(customerId);
        } catch (UserNotFoundException e) {
            throw new InvalidOrderException("Customer not found: " + customerId);
        }
        
        // Check inventory and calculate total
        double totalAmount = 0;
        for (Map.Entry<String, Integer> entry : requestedItems.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            
            FoodItem item = foodDao.getFoodItemByName(itemName);
            if (item == null) {
                throw new InvalidOrderException("Item not found: " + itemName);
            }
            
            Map<FoodItem, Integer> inventory = foodDao.getAllFoodItems();
            int availableQuantity = inventory.getOrDefault(item, 0);
            
            if (availableQuantity < quantity) {
                throw new InvalidOrderException("Insufficient stock for " + itemName + 
                                              ". Available: " + availableQuantity + ", Requested: " + quantity);
            }
            
            totalAmount += item.getPrice() * quantity;
        }
        
        // Find available delivery person
        List<User> availableDeliveryPersons = userService.getAvailableDeliveryPersons();
        if (availableDeliveryPersons.isEmpty()) {
            throw new InvalidOrderException("No delivery personnel available");
        }
        
        User assignedDeliveryPerson = availableDeliveryPersons.get(0);
        
        // Update inventory
        for (Map.Entry<String, Integer> entry : requestedItems.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            FoodItem item = foodDao.getFoodItemByName(itemName);
            foodDao.updateInventory(item, quantity);
        }
        
        // Mark delivery person as unavailable
        try {
            userService.updateUserAvailability(assignedDeliveryPerson.getId(), false);
        } catch (UserNotFoundException e) {
            throw new InvalidOrderException("Error assigning delivery person");
        }
        
        // Create and save order
        String orderId = "ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderId, customerId, assignedDeliveryPerson.getId(), 
                               requestedItems, totalAmount);
        orderDao.saveOrder(order);
        
        return order;
    }
    
    @Override
    public void completeOrder(String orderId) throws InvalidOrderException {
        Order order = orderDao.getOrderById(orderId);
        if (order == null) {
            throw new InvalidOrderException("Order not found: " + orderId);
        }
        
        order.setStatus("COMPLETED");
        orderDao.updateOrder(order);
        
        // Mark delivery person as available again
        try {
            userService.updateUserAvailability(order.getDeliveryPersonId(), true);
        } catch (UserNotFoundException e) {
            System.out.println("Warning: Could not update delivery person availability");
        }
    }
    
    @Override
    public Order getOrderById(String orderId) {
        return orderDao.getOrderById(orderId);
    }
    
    @Override
    public List<Order> getOrdersByCustomerId(String customerId) {
        return orderDao.getOrdersByCustomerId(customerId);
    }
    
    @Override
    public List<Order> getOrdersByDeliveryPersonId(String deliveryPersonId) {
        return orderDao.getOrdersByDeliveryPersonId(deliveryPersonId);
    }
    
    @Override
    public String getOrderDetails(String orderId) {
        Order order = orderDao.getOrderById(orderId);
        if (order == null) {
            return "Order not found";
        }
        
        StringBuilder details = new StringBuilder();
        details.append("Order ID: ").append(order.getOrderId()).append("\n");
        details.append("Customer ID: ").append(order.getCustomerId()).append("\n");
        details.append("Delivery Person ID: ").append(order.getDeliveryPersonId()).append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n");
        details.append("Total Amount: $").append(String.format("%.2f", order.getTotalAmount())).append("\n");
        details.append("Order Time: ").append(order.getOrderTime()).append("\n");
        details.append("Items Ordered:\n");
        
        for (Map.Entry<String, Integer> entry : order.getItemsOrdered().entrySet()) {
            details.append("  - ").append(entry.getKey()).append(" x ").append(entry.getValue()).append("\n");
        }
        
        return details.toString();
    }
}