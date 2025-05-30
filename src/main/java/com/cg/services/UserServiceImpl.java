package com.cg.services;

import com.cg.dao.UserDao;
import com.cg.dao.UserDaoImpl;
import com.cg.dto.User;
import com.cg.exception.UserNotFoundException;
import com.cg.exception.InvalidCredentialsException;
import java.util.List;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    
    public UserServiceImpl() {
        this.userDao = new UserDaoImpl();
    }
    
    @Override
    public void registerCustomer(String id, String name, String email, String phone, String password) {
        User customer = new User(id, name, email, phone, password, "CUSTOMER");
        userDao.registerUser(customer);
    }
    
    @Override
    public void registerDeliveryPerson(String id, String name, String email, String phone, String password) {
        User deliveryPerson = new User(id, name, email, phone, password, "DELIVERY");
        userDao.registerUser(deliveryPerson);
    }
    
    @Override
    public User authenticateUser(String id, String password) 
            throws UserNotFoundException, InvalidCredentialsException {
        return userDao.authenticateUser(id, password);
    }
    
    @Override
    public User getUserById(String id) throws UserNotFoundException {
        return userDao.getUserById(id);
    }
    
    @Override
    public List<User> getAllDeliveryPersons() {
        return userDao.getAllUsersByRole("DELIVERY");
    }
    
    @Override
    public List<User> getAvailableDeliveryPersons() {
        return getAllDeliveryPersons().stream()
                .filter(User::isAvailable)
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateUserAvailability(String userId, boolean available) throws UserNotFoundException {
        User user = userDao.getUserById(userId);
        user.setAvailable(available);
        userDao.updateUser(user);
    }
    
    @Override
    public void removeDeliveryPerson(String id) throws UserNotFoundException {
        User user = userDao.getUserById(id);
        if (!"DELIVERY".equalsIgnoreCase(user.getRole())) {
            throw new UserNotFoundException("User is not a delivery person");
        }
        userDao.removeUser(id);
    }
}