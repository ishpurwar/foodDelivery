package com.cg.utils;

import java.util.Scanner;
import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\d{10}$");
    
    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty() && id.length() >= 3;
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    public static boolean isValidPrice(double price) {
        return price > 0;
    }
    
    public static boolean isValidQuantity(int quantity) {
        return quantity > 0;
    }
    
    public static String getValidInput(Scanner scanner, String prompt, String errorMessage) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(errorMessage);
            }
        } while (input.isEmpty());
        return input;
    }
}