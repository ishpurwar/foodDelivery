package com.cg.services;

import com.cg.exception.UserNotFoundException;

public interface ManagerService {
    void addNewFoodItem(String name, double price, int quantity);
    void restockFoodItem(String itemName, int quantity);
    void removeDeliveryPerson(String deliveryPersonId) throws UserNotFoundException;
    boolean validateManagerRole(String managerId) throws UserNotFoundException;
}