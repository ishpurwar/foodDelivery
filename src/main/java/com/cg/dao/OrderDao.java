package com.cg.dao;

import com.cg.dto.Order;
import java.util.List;

public interface OrderDao {
    void saveOrder(Order order);
    Order getOrderById(String orderId);
    List<Order> getOrdersByCustomerId(String customerId);
    List<Order> getOrdersByDeliveryPersonId(String deliveryPersonId);
    void updateOrder(Order order);
     List<Order> getAllOrders();
}
