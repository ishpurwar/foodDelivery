package com.cg.services;

import com.cg.dto.Order;
import com.cg.exception.InvalidOrderException;
import java.util.Map;
import java.util.List;

public interface OrderService {
    Order placeOrder(String customerId, Map<String, Integer> requestedItems) 
            throws InvalidOrderException;
    void completeOrder(String orderId) throws InvalidOrderException;
    Order getOrderById(String orderId);
    List<Order> getOrdersByCustomerId(String customerId);
    String getOrderDetails(String orderId);
}
