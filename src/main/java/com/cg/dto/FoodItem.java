package com.cg.dto;

import java.util.Objects;

public class FoodItem {
    private String name;
    private double price;
    
    public FoodItem() {}
    
    public FoodItem(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return Objects.equals(name, foodItem.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public String toString() {
        return String.format("%s - $%.2f", name, price);
    }
}