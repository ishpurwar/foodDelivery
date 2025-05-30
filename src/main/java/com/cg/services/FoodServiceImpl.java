package com.cg.services;

import com.cg.dao.FoodDao;
import com.cg.dao.FoodDaoImpl;
import com.cg.dto.FoodItem;
import java.util.Map;

public class FoodServiceImpl implements FoodService {
    private final FoodDao foodDao;
    
    public FoodServiceImpl() {
        this.foodDao = new FoodDaoImpl();
    }
    
    @Override
    public void addNewFoodItem(String name, double price, int quantity) {
        FoodItem item = new FoodItem(name, price);
        foodDao.addFoodItem(item, quantity);
    }
    
    @Override
    public void restockItem(String itemName, int quantity) {
        FoodItem existingItem = foodDao.getFoodItemByName(itemName);
        if (existingItem != null) {
            foodDao.restockItem(itemName, quantity);
        } else {
            System.out.println("Item not found. Please add it as a new item first.");
        }
    }
    
    @Override
    public Map<FoodItem, Integer> getMenu() {
        return foodDao.getAllFoodItems();
    }
    
    @Override
    public FoodItem getFoodItemByName(String name) {
        return foodDao.getFoodItemByName(name);
    }
    
    @Override
    public boolean isItemAvailable(String itemName, int requestedQuantity) {
        FoodItem item = getFoodItemByName(itemName);
        if (item == null) return false;
        
        Map<FoodItem, Integer> inventory = foodDao.getAllFoodItems();
        return inventory.getOrDefault(item, 0) >= requestedQuantity;
    }
}
