package com.cg.services;


import com.cg.dao.UserDao;
import com.cg.dto.User;
import com.cg.exception.UserNotFoundException;
import com.cg.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;
    
    private UserServiceImpl userService;
    
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        // Use reflection to inject mock
        try {
            java.lang.reflect.Field userDaoField = UserServiceImpl.class.getDeclaredField("userDao");
            userDaoField.setAccessible(true);
            userDaoField.set(userService, userDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRegisterCustomer() {
        // Arrange
        String id = "CUST001";
        String name = "John Doe";
        String email = "john@email.com";
        String phone = "1234567890";
        String password = "password123";
        
        // Act
        userService.registerCustomer(id, name, email, phone, password);
        
        // Assert
        verify(userDao).registerUser(argThat(user -> 
            user.getId().equals(id) &&
            user.getName().equals(name) &&
            user.getEmail().equals(email) &&
            user.getPhone().equals(phone) &&
            user.getPassword().equals(password) &&
            user.getRole().equals("CUSTOMER")
        ));
    }

    @Test
    void testRegisterDeliveryPerson() {
        // Arrange
        String id = "DEL001";
        String name = "Jane Smith";
        String email = "jane@email.com";
        String phone = "0987654321";
        String password = "password456";
        
        // Act
        userService.registerDeliveryPerson(id, name, email, phone, password);
        
        // Assert
        verify(userDao).registerUser(argThat(user -> 
            user.getId().equals(id) &&
            user.getName().equals(name) &&
            user.getEmail().equals(email) &&
            user.getPhone().equals(phone) &&
            user.getPassword().equals(password) &&
            user.getRole().equals("DELIVERY")
        ));
    }

    @Test
    void testAuthenticateUser_Success() throws Exception {
        // Arrange
        String id = "CUST001";
        String password = "password123";
        User expectedUser = new User(id, "John Doe", "john@email.com", "1234567890", password, "CUSTOMER");
        
        when(userDao.authenticateUser(id, password)).thenReturn(expectedUser);
        
        // Act
        User result = userService.authenticateUser(id, password);
        
        // Assert
        assertEquals(expectedUser, result);
        verify(userDao).authenticateUser(id, password);
    }

    @Test
    void testAuthenticateUser_UserNotFound() throws Exception {
        // Arrange
        String id = "INVALID";
        String password = "password123";
        
        when(userDao.authenticateUser(id, password))
            .thenThrow(new UserNotFoundException("User not found"));
        
        // Act & Assert
        assertThrows(UserNotFoundException.class, 
            () -> userService.authenticateUser(id, password));
        
        verify(userDao).authenticateUser(id, password);
    }

    @Test
    void testAuthenticateUser_InvalidCredentials() throws Exception {
        // Arrange
        String id = "CUST001";
        String password = "wrongpassword";
        
        when(userDao.authenticateUser(id, password))
            .thenThrow(new InvalidCredentialsException("Invalid credentials"));
        
        // Act & Assert
        assertThrows(InvalidCredentialsException.class, 
            () -> userService.authenticateUser(id, password));
        
        verify(userDao).authenticateUser(id, password);
    }

    @Test
    void testGetUserById_Success() throws Exception {
        // Arrange
        String id = "CUST001";
        User expectedUser = new User(id, "John Doe", "john@email.com", "1234567890", "password", "CUSTOMER");
        
        when(userDao.getUserById(id)).thenReturn(expectedUser);
        
        // Act
        User result = userService.getUserById(id);
        
        // Assert
        assertEquals(expectedUser, result);
        verify(userDao).getUserById(id);
    }

    @Test
    void testGetUserById_UserNotFound() throws Exception {
        // Arrange
        String id = "INVALID";
        
        when(userDao.getUserById(id)).thenThrow(new UserNotFoundException("User not found"));
        
        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));
        verify(userDao).getUserById(id);
    }

    @Test
    void testGetAllDeliveryPersons() {
        // Arrange
        List<User> expectedDeliveryPersons = Arrays.asList(
            new User("DEL001", "Jane Smith", "jane@email.com", "0987654321", "password", "DELIVERY"),
            new User("DEL002", "Bob Johnson", "bob@email.com", "1122334455", "password", "DELIVERY")
        );
        
        when(userDao.getAllUsersByRole("DELIVERY")).thenReturn(expectedDeliveryPersons);
        
        // Act
        List<User> result = userService.getAllDeliveryPersons();
        
        // Assert
        assertEquals(expectedDeliveryPersons, result);
        verify(userDao).getAllUsersByRole("DELIVERY");
    }

    @Test
    void testGetAvailableDeliveryPersons() {
        // Arrange
        User availableDelivery = new User("DEL001", "Jane Smith", "jane@email.com", "0987654321", "password", "DELIVERY");
        availableDelivery.setAvailable(true);
        
        User unavailableDelivery = new User("DEL002", "Bob Johnson", "bob@email.com", "1122334455", "password", "DELIVERY");
        unavailableDelivery.setAvailable(false);
        
        List<User> allDeliveryPersons = Arrays.asList(availableDelivery, unavailableDelivery);
        
        when(userDao.getAllUsersByRole("DELIVERY")).thenReturn(allDeliveryPersons);
        
        // Act
        List<User> result = userService.getAvailableDeliveryPersons();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(availableDelivery, result.get(0));
        assertTrue(result.get(0).isAvailable());
        verify(userDao).getAllUsersByRole("DELIVERY");
    }

    @Test
    void testGetAvailableDeliveryPersons_EmptyList() {
        // Arrange
        User unavailableDelivery1 = new User("DEL001", "Jane Smith", "jane@email.com", "0987654321", "password", "DELIVERY");
        unavailableDelivery1.setAvailable(false);
        
        User unavailableDelivery2 = new User("DEL002", "Bob Johnson", "bob@email.com", "1122334455", "password", "DELIVERY");
        unavailableDelivery2.setAvailable(false);
        
        List<User> allDeliveryPersons = Arrays.asList(unavailableDelivery1, unavailableDelivery2);
        
        when(userDao.getAllUsersByRole("DELIVERY")).thenReturn(allDeliveryPersons);
        
        // Act
        List<User> result = userService.getAvailableDeliveryPersons();
        
        // Assert
        assertTrue(result.isEmpty());
        verify(userDao).getAllUsersByRole("DELIVERY");
    }

    @Test
    void testUpdateUserAvailability_Success() throws Exception {
        // Arrange
        String userId = "DEL001";
        boolean available = true;
        User user = new User(userId, "Jane Smith", "jane@email.com", "0987654321", "password", "DELIVERY");
        
        when(userDao.getUserById(userId)).thenReturn(user);
        
        // Act
        userService.updateUserAvailability(userId, available);
        
        // Assert
        assertTrue(user.isAvailable());
        verify(userDao).getUserById(userId);
        verify(userDao).updateUser(user);
    }

    @Test
    void testUpdateUserAvailability_UserNotFound() throws Exception {
        // Arrange
        String userId = "INVALID";
        boolean available = true;
        
        when(userDao.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));
        
        // Act & Assert
        assertThrows(UserNotFoundException.class, 
            () -> userService.updateUserAvailability(userId, available));
        
        verify(userDao).getUserById(userId);
        verify(userDao, never()).updateUser(any());
    }

    @Test
    void testRemoveDeliveryPerson_Success() throws Exception {
        // Arrange
        String id = "DEL001";
        User deliveryPerson = new User(id, "Jane Smith", "jane@email.com", "0987654321", "password", "DELIVERY");
        
        when(userDao.getUserById(id)).thenReturn(deliveryPerson);
        
        // Act
        userService.removeDeliveryPerson(id);
        
        // Assert
        verify(userDao).getUserById(id);
        verify(userDao).removeUser(id);
    }

    @Test
    void testRemoveDeliveryPerson_UserNotFound() throws Exception {
        // Arrange
        String id = "INVALID";
        
        when(userDao.getUserById(id)).thenThrow(new UserNotFoundException("User not found"));
        
        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.removeDeliveryPerson(id));
        verify(userDao).getUserById(id);
        verify(userDao, never()).removeUser(any());
    }

    @Test
    void testRemoveDeliveryPerson_NotDeliveryPerson() throws Exception {
        // Arrange
        String id = "CUST001";
        User customer = new User(id, "John Doe", "john@email.com", "1234567890", "password", "CUSTOMER");
        
        when(userDao.getUserById(id)).thenReturn(customer);
        
        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, 
            () -> userService.removeDeliveryPerson(id));
        
        assertEquals("User is not a delivery person", exception.getMessage());
        verify(userDao).getUserById(id);
        verify(userDao, never()).removeUser(any());
    }
}