package com.cg.services;

import com.cg.dto.User;
import com.cg.exception.UserNotFoundException;
import com.cg.utils.RoleCheck;

@RoleCheck(role = "MANAGER")
public class ManagerServiceImpl implements ManagerService {
    private final FoodService foodService;
    private final UserService userService;
    
    public ManagerServiceImpl() {
        this.foodService = new FoodServiceImpl();
        this.userService = new UserServiceImpl();
    }
    
    @Override
    public void addNewFoodItem(String name, double price, int quantity) {
        foodService.addNewFoodItem(name, price, quantity);
    }
    
    @Override
    public void restockFoodItem(String itemName, int quantity) {
        foodService.restockItem(itemName, quantity);
    }
    
    @Override
    public void removeDeliveryPerson(String deliveryPersonId) throws UserNotFoundException {
        userService.removeDeliveryPerson(deliveryPersonId);
    }
    
    @Override
    public boolean validateManagerRole(String managerId) throws UserNotFoundException {
        User manager = userService.getUserById(managerId);
        
        // Use reflection to validate manager role
        try {
            Class<?> clazz = this.getClass();
            if (clazz.isAnnotationPresent(RoleCheck.class)) {
                RoleCheck roleCheck = clazz.getAnnotation(RoleCheck.class);
                String requiredRole = roleCheck.role();
                return manager.getRole().equalsIgnoreCase(requiredRole);
            }
        } catch (Exception e) {
            System.out.println("Error in role validation: " + e.getMessage());
        }
        
        return false;
    }
}