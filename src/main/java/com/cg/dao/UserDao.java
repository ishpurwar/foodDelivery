package com.cg.dao;

import com.cg.dto.User;
import com.cg.exception.UserNotFoundException;
import com.cg.exception.InvalidCredentialsException;
import java.util.List;

public interface UserDao {
    void registerUser(User user);
    User authenticateUser(String id, String password) throws UserNotFoundException, InvalidCredentialsException;
    User getUserById(String id) throws UserNotFoundException;
    List<User> getAllUsersByRole(String role);
    void updateUser(User user) throws UserNotFoundException;
    void removeUser(String id) throws UserNotFoundException;
}