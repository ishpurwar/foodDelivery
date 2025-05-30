package com.cg.dao;

import com.cg.dto.FoodItem;
import com.cg.utils.CollectionUtil;
import java.util.Map;

public class FoodDaoImpl implements FoodDao {
    
    @Override
    public void addFoodItem(FoodItem item, int quantity) {
        CollectionUtil.addFoodItem(item, quantity);
    }
    
    @Override
    public Map<FoodItem, Integer> getAllFoodItems() {
        return CollectionUtil.getInventory();
    }
    
    @Override
    public FoodItem getFoodItemByName(String name) {
        return CollectionUtil.findFoodItemByName(name);
    }
    
    @Override
    public boolean updateInventory(FoodItem item, int quantity) {
        return CollectionUtil.updateInventory(item, quantity);
    }
    
    @Override
    public void restockItem(String itemName, int quantity) {
        FoodItem item = getFoodItemByName(itemName);
        if (item != null) {
            CollectionUtil.addFoodItem(item, quantity);
        }
    }
}