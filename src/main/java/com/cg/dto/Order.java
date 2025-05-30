package com.cg.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class Order {
    private String orderId;
    private String customerId;
    private String deliveryPersonId;
    private Map<String, Integer> itemsOrdered;
    private String status;
    private double totalAmount;
    private LocalDateTime orderTime;
    
    public Order() {}
    
    public Order(String orderId, String customerId, String deliveryPersonId, 
                 Map<String, Integer> itemsOrdered, double totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.deliveryPersonId = deliveryPersonId;
        this.itemsOrdered = itemsOrdered;
        this.totalAmount = totalAmount;
        this.status = "PLACED";
        this.orderTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getDeliveryPersonId() { return deliveryPersonId; }
    public void setDeliveryPersonId(String deliveryPersonId) { this.deliveryPersonId = deliveryPersonId; }
    
    public Map<String, Integer> getItemsOrdered() { return itemsOrdered; }
    public void setItemsOrdered(Map<String, Integer> itemsOrdered) { this.itemsOrdered = itemsOrdered; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
    
    @Override
    public String toString() {
        return String.format("Order{orderId='%s', customerId='%s', status='%s', totalAmount=%.2f}", 
                           orderId, customerId, status, totalAmount);
    }
}