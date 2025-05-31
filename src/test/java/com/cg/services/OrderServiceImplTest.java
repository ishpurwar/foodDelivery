package com.cg.services;

import com.cg.dao.OrderDao;
import com.cg.dao.FoodDao;
import com.cg.dto.Order;
import com.cg.dto.FoodItem;
import com.cg.dto.User;
import com.cg.exception.InvalidOrderException;
import com.cg.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;
    
    @Mock
    private FoodDao foodDao;
    
    @Mock
    private UserService userService;
    
    private OrderServiceImpl orderService;
    
    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl();
        // Use reflection to inject mocks
        try {
            java.lang.reflect.Field orderDaoField = OrderServiceImpl.class.getDeclaredField("orderDao");
            orderDaoField.setAccessible(true);
            orderDaoField.set(orderService, orderDao);
            
            java.lang.reflect.Field foodDaoField = OrderServiceImpl.class.getDeclaredField("foodDao");
            foodDaoField.setAccessible(true);
            foodDaoField.set(orderService, foodDao);
            
            java.lang.reflect.Field userServiceField = OrderServiceImpl.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(orderService, userService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testPlaceOrder_Success() throws Exception {
        // Arrange
        String customerId = "CUST001";
        Map<String, Integer> requestedItems = new HashMap<>();
        requestedItems.put("Pizza", 2);
        requestedItems.put("Burger", 1);
        
        User customer = new User(customerId, "John Doe", "john@email.com", "1234567890", "password", "CUSTOMER");
        FoodItem pizza = new FoodItem("Pizza", 15.99);
        FoodItem burger = new FoodItem("Burger", 8.99);
        
        Map<FoodItem, Integer> inventory = new HashMap<>();
        inventory.put(pizza, 5);
        inventory.put(burger, 3);
        
        List<User> availableDeliveryPersons = Arrays.asList(
            new User("DEL001", "Delivery Person", "del@email.com", "0987654321", "password", "DELIVERY")
        );
        
        when(userService.getUserById(customerId)).thenReturn(customer);
        when(foodDao.getFoodItemByName("Pizza")).thenReturn(pizza);
        when(foodDao.getFoodItemByName("Burger")).thenReturn(burger);
        when(foodDao.getAllFoodItems()).thenReturn(inventory);
        when(userService.getAvailableDeliveryPersons()).thenReturn(availableDeliveryPersons);
        when(foodDao.updateInventory(any(FoodItem.class), anyInt())).thenReturn(true);
        
        // Act
        Order result = orderService.placeOrder(customerId, requestedItems);
        
        // Assert
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals("DEL001", result.getDeliveryPersonId());
        assertEquals(40.97, result.getTotalAmount(), 0.01); // 2*15.99 + 1*8.99
        assertTrue(result.getOrderId().startsWith("ORD"));
        
        verify(userService).getUserById(customerId);
        verify(foodDao, times(2)).getFoodItemByName(anyString());
        verify(foodDao, times(2)).updateInventory(any(FoodItem.class), anyInt());
        verify(userService).updateUserAvailability("DEL001", false);
        verify(orderDao).saveOrder(any(Order.class));
    }

    @Test
    void testPlaceOrder_CustomerNotFound() throws Exception {
        // Arrange
        String customerId = "INVALID";
        Map<String, Integer> requestedItems = new HashMap<>();
        requestedItems.put("Pizza", 1);
        
        when(userService.getUserById(customerId)).thenThrow(new UserNotFoundException("User not found"));
        
        // Act & Assert
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, 
            () -> orderService.placeOrder(customerId, requestedItems));
        
        assertEquals("Customer not found: INVALID", exception.getMessage());
        verify(userService).getUserById(customerId);
    }

    @Test
    void testPlaceOrder_ItemNotFound() throws Exception {
        // Arrange
        String customerId = "CUST001";
        Map<String, Integer> requestedItems = new HashMap<>();
        requestedItems.put("NonExistentItem", 1);
        
        User customer = new User(customerId, "John Doe", "john@email.com", "1234567890", "password", "CUSTOMER");
        
        when(userService.getUserById(customerId)).thenReturn(customer);
        when(foodDao.getFoodItemByName("NonExistentItem")).thenReturn(null);
        
        // Act & Assert
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, 
            () -> orderService.placeOrder(customerId, requestedItems));
        
        assertEquals("Item not found: NonExistentItem", exception.getMessage());
    }

    @Test
    void testPlaceOrder_InsufficientStock() throws Exception {
        // Arrange
        String customerId = "CUST001";
        Map<String, Integer> requestedItems = new HashMap<>();
        requestedItems.put("Pizza", 10);
        
        User customer = new User(customerId, "John Doe", "john@email.com", "1234567890", "password", "CUSTOMER");
        FoodItem pizza = new FoodItem("Pizza", 15.99);
        
        Map<FoodItem, Integer> inventory = new HashMap<>();
        inventory.put(pizza, 2); // Only 2 available, but 10 requested
        
        when(userService.getUserById(customerId)).thenReturn(customer);
        when(foodDao.getFoodItemByName("Pizza")).thenReturn(pizza);
        when(foodDao.getAllFoodItems()).thenReturn(inventory);
        
        // Act & Assert
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, 
            () -> orderService.placeOrder(customerId, requestedItems));
        
        assertTrue(exception.getMessage().contains("Insufficient stock for Pizza"));
        assertTrue(exception.getMessage().contains("Available: 2, Requested: 10"));
    }

    @Test
    void testPlaceOrder_NoDeliveryPersonAvailable() throws Exception {
        // Arrange
        String customerId = "CUST001";
        Map<String, Integer> requestedItems = new HashMap<>();
        requestedItems.put("Pizza", 1);
        
        User customer = new User(customerId, "John Doe", "john@email.com", "1234567890", "password", "CUSTOMER");
        FoodItem pizza = new FoodItem("Pizza", 15.99);
        
        Map<FoodItem, Integer> inventory = new HashMap<>();
        inventory.put(pizza, 5);
        
        when(userService.getUserById(customerId)).thenReturn(customer);
        when(foodDao.getFoodItemByName("Pizza")).thenReturn(pizza);
        when(foodDao.getAllFoodItems()).thenReturn(inventory);
        when(userService.getAvailableDeliveryPersons()).thenReturn(Collections.emptyList());
        
        // Act & Assert
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, 
            () -> orderService.placeOrder(customerId, requestedItems));
        
        assertEquals("No delivery personnel available", exception.getMessage());
    }

    @Test
    void testCompleteOrder_Success() throws Exception {
        // Arrange
        String orderId = "ORD12345678";
        Order order = new Order(orderId, "CUST001", "DEL001", new HashMap<>(), 25.99);
        
        when(orderDao.getOrderById(orderId)).thenReturn(order);
        
        // Act
        orderService.completeOrder(orderId);
        
        // Assert
        assertEquals("COMPLETED", order.getStatus());
        verify(orderDao).getOrderById(orderId);
        verify(orderDao).updateOrder(order);
        verify(userService).updateUserAvailability("DEL001", true);
    }

    @Test
    void testCompleteOrder_OrderNotFound() {
        // Arrange
        String orderId = "INVALID";
        when(orderDao.getOrderById(orderId)).thenReturn(null);
        
        // Act & Assert
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, 
            () -> orderService.completeOrder(orderId));
        
        assertEquals("Order not found: INVALID", exception.getMessage());
        verify(orderDao).getOrderById(orderId);
    }

    @Test
    void testCompleteOrder_DeliveryPersonUpdateFails() throws Exception {
        // Arrange
        String orderId = "ORD12345678";
        Order order = new Order(orderId, "CUST001", "DEL001", new HashMap<>(), 25.99);
        
        when(orderDao.getOrderById(orderId)).thenReturn(order);
        doThrow(new UserNotFoundException("Delivery person not found"))
            .when(userService).updateUserAvailability("DEL001", true);
        
        // Act
        assertDoesNotThrow(() -> orderService.completeOrder(orderId));
        
        // Assert
        assertEquals("COMPLETED", order.getStatus());
        verify(orderDao).updateOrder(order);
    }

    @Test
    void testGetOrderById() {
        // Arrange
        String orderId = "ORD12345678";
        Order expectedOrder = new Order(orderId, "CUST001", "DEL001", new HashMap<>(), 25.99);
        when(orderDao.getOrderById(orderId)).thenReturn(expectedOrder);
        
        // Act
        Order result = orderService.getOrderById(orderId);
        
        // Assert
        assertEquals(expectedOrder, result);
        verify(orderDao).getOrderById(orderId);
    }

    @Test
    void testGetOrdersByCustomerId() {
        // Arrange
        String customerId = "CUST001";
        List<Order> expectedOrders = Arrays.asList(
            new Order("ORD1", customerId, "DEL001", new HashMap<>(), 25.99),
            new Order("ORD2", customerId, "DEL002", new HashMap<>(), 35.99)
        );
        when(orderDao.getOrdersByCustomerId(customerId)).thenReturn(expectedOrders);
        
        // Act
        List<Order> result = orderService.getOrdersByCustomerId(customerId);
        
        // Assert
        assertEquals(expectedOrders, result);
        verify(orderDao).getOrdersByCustomerId(customerId);
    }

    @Test
    void testGetOrderDetails_Success() {
        // Arrange
        String orderId = "ORD12345678";
        Map<String, Integer> items = new HashMap<>();
        items.put("Pizza", 2);
        items.put("Burger", 1);
        
        Order order = new Order(orderId, "CUST001", "DEL001", items, 40.97);
        when(orderDao.getOrderById(orderId)).thenReturn(order);
        
        // Act
        String result = orderService.getOrderDetails(orderId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Order ID: " + orderId));
        assertTrue(result.contains("Customer ID: CUST001"));
        assertTrue(result.contains("Delivery Person ID: DEL001"));
        assertTrue(result.contains("Total Amount: $40.97"));
        assertTrue(result.contains("Pizza x 2"));
        assertTrue(result.contains("Burger x 1"));
        verify(orderDao).getOrderById(orderId);
    }

    @Test
    void testGetOrderDetails_OrderNotFound() {
        // Arrange
        String orderId = "INVALID";
        when(orderDao.getOrderById(orderId)).thenReturn(null);
        
        // Act
        String result = orderService.getOrderDetails(orderId);
        
        // Assert
        assertEquals("Order not found", result);
        verify(orderDao).getOrderById(orderId);
    }
}