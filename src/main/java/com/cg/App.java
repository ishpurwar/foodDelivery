package com.cg;

import com.cg.services.FoodService;
import com.cg.services.FoodServiceImpl;
import com.cg.ui.MenuHandler;

/**
 * Hello world!
 *
 */
public class App 
{
     public static void main(String[] args) {
        // Initialize the system with some sample data
        initializeSampleData();
        
        // Start the application
        MenuHandler menuHandler = new MenuHandler();
        menuHandler.start();
    }
    
    private static void initializeSampleData() {
        FoodService foodService = new FoodServiceImpl();
        
        // Add sample food items
        foodService.addNewFoodItem("Burger", 5.99, 50);
        foodService.addNewFoodItem("Pizza", 12.99, 30);
        foodService.addNewFoodItem("Pasta", 8.99, 40);
        foodService.addNewFoodItem("Salad", 6.99, 25);
        foodService.addNewFoodItem("Sandwich", 4.99, 35);
        foodService.addNewFoodItem("French Fries", 3.99, 60);
        foodService.addNewFoodItem("Soda", 1.99, 100);
        foodService.addNewFoodItem("Coffee", 2.99, 80);
        
        System.out.println("Sample data initialized successfully!");
        System.out.println("Default Manager - ID: MGR001, Password: admin123");
    }
}
