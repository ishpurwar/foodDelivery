package com.cg.dao;

import com.cg.dto.User;
import com.cg.exception.UserNotFoundException;
import com.cg.exception.InvalidCredentialsException;
import com.cg.utils.CollectionUtil;
import java.util.List;
import java.util.stream.Collectors;

public class UserDaoImpl implements UserDao {
    
    @Override
    public void registerUser(User user) {
        CollectionUtil.addUser(user);
    }
    
    @Override
    public User authenticateUser(String id, String password) 
            throws UserNotFoundException, InvalidCredentialsException {
        User user = CollectionUtil.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        if (!user.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Invalid password for user " + id);
        }
        return user;
    }
    
    @Override
    public User getUserById(String id) throws UserNotFoundException {
        User user = CollectionUtil.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        return user;
    }
    
    @Override
    public List<User> getAllUsersByRole(String role) {
        return CollectionUtil.getUsers().values().stream()
                .filter(user -> user.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateUser(User user) throws UserNotFoundException {
        if (CollectionUtil.getUserById(user.getId()) == null) {
            throw new UserNotFoundException("User with ID " + user.getId() + " not found");
        }
        CollectionUtil.addUser(user);
    }
    
    @Override
    public void removeUser(String id) throws UserNotFoundException {
        User user = getUserById(id);
        CollectionUtil.getUsers().remove(id);
        
        switch (user.getRole().toLowerCase()) {
            case "customer":
                CollectionUtil.getCustomers().remove(id);
                break;
            case "delivery":
                CollectionUtil.getDeliveryPersons().remove(id);
                break;
            case "manager":
                CollectionUtil.getManagers().remove(id);
                break;
        }
    }
}