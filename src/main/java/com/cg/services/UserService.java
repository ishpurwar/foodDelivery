package com.cg.services;

import com.cg.dto.User;
import com.cg.exception.UserNotFoundException;
import com.cg.exception.InvalidCredentialsException;
import java.util.List;

public interface UserService {
    void registerCustomer(String id, String name, String email, String phone, String password);
    void registerDeliveryPerson(String id, String name, String email, String phone, String password);
    User authenticateUser(String id, String password) throws UserNotFoundException, InvalidCredentialsException;
    User getUserById(String id) throws UserNotFoundException;
    List<User> getAllDeliveryPersons();
    List<User> getAvailableDeliveryPersons();
    void updateUserAvailability(String userId, boolean available) throws UserNotFoundException;
    void removeDeliveryPerson(String id) throws UserNotFoundException;
}
