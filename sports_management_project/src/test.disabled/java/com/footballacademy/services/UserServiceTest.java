package com.footballacademy.services;

import com.footballacademy.model.User;
import com.footballacademy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.
class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;
    private User testUser;
    @BeforeEach void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setNom("Test User");
        testUser.setEmail("test@example.com");
        testUser.setMdp("password123");
        testUser.setMainRole(User.UserRole.PLAYER);
    }
    @Test void testCreateUser_Success() {
        //
        Arrange when(userRepository.existsByEmail(anyString())) .thenReturn(false);
        when(userRepository.save(any(User.
        class))) .thenReturn(testUser);
        when(passwordEncoder.encode(anyString())) .thenReturn("encodedPassword");
        // Act
        User result = userService.createUser(testUser);
        //
        Assert assertNotNull(result);
        assertEquals("Test User", result.getNom());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)) .save(any(User.
        class));
    }
    @Test void testCreateUser_EmailAlreadyExists() {
        //
        Arrange when(userRepository.existsByEmail(anyString())) .thenReturn(true);
        // Act &
        Assert assertThrows(RuntimeException.class,() -> userService.createUser(testUser));
        verify(userRepository, never()) .save(any(User.
        class));
    }
    @Test void testGetUserById_Success() {
        //
        Arrange when(userRepository.findById(1L)) .thenReturn(Optional.of(testUser));
        // Act
        User result = userService.getUserById(1L);
        //
        Assert assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)) .findById(1L);
    }
    @Test void testGetUserById_NotFound() {
        //
        Arrange when(userRepository.findById(1L)) .thenReturn(Optional.empty());
        // Act &
        Assert assertThrows(RuntimeException.class,() -> userService.getUserById(1L));
    }
    @Test void testUpdateUser_Success() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setNom("Updated User");
        updatedUser.setEmail("updated@example.com");
        when(userRepository.findById(1L)) .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.
        class))) .thenReturn(testUser);
        // Act
        User result = userService.updateUser(1L, updatedUser);
        //
        Assert assertNotNull(result);
        verify(userRepository, times(1)) .save(any(User.class));
    }
    @Test void testDeleteUser_Success() {
        //
        Arrange when(userRepository.existsById(1L)) .thenReturn(true);
        doNothing() .when(userRepository) .deleteById(1L);
        // Act
        userService.deleteUser(1L);
        //
        Assert verify(userRepository, times(1)) .deleteById(1L);
    }
    @Test void testDeleteUser_NotFound() {
        //
        Arrange when(userRepository.existsById(1L)) .thenReturn(false);
        // Act &
        Assert assertThrows(RuntimeException.class,() -> userService.deleteUser(1L));
        verify(userRepository, never()) .deleteById(1L);
    }
}
