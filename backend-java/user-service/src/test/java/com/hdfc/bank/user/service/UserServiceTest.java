package com.hdfc.bank.user.service;

import com.hdfc.bank.user.dto.UserResponse;
import com.hdfc.bank.user.dto.UserUpdateRequest;
import com.hdfc.bank.user.model.User;
import com.hdfc.bank.user.repository.UserRepository;
import com.hdfc.bank.user.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponse userResponse;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUserId("USER001");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhoneNumber("9876543210");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRole(User.UserRole.CUSTOMER);
        user.setCreatedAt(LocalDateTime.now());

        userResponse = new UserResponse();
        userResponse.setUserId("USER001");
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setEmail("john.doe@example.com");
        userResponse.setPhoneNumber("9876543210");
        userResponse.setStatus(User.UserStatus.ACTIVE);

        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("John Updated");
        updateRequest.setLastName("Doe Updated");
        updateRequest.setPhoneNumber("9876543211");
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findByUserId("USER001")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserById("USER001");

        // Then
        assertNotNull(result);
        assertEquals("USER001", result.getUserId());
        assertEquals("John", result.getFirstName());
        verify(userRepository).findByUserId("USER001");
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserById_NotFound() {
        // Given
        when(userRepository.findByUserId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.getUserById("INVALID"));
        verify(userRepository).findByUserId("INVALID");
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void getAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(user);
        List<UserResponse> userResponses = Arrays.asList(userResponse);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER001", result.get(0).getUserId());
        verify(userRepository).findAll();
        verify(userMapper).toResponse(user);
    }

    @Test
    void updateUser_Success() {
        // Given
        when(userRepository.findByUserId("USER001")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When
        UserResponse result = userService.updateUser("USER001", updateRequest);

        // Then
        assertNotNull(result);
        verify(userRepository).findByUserId("USER001");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Given
        when(userRepository.findByUserId("USER001")).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // When
        userService.deleteUser("USER001");

        // Then
        verify(userRepository).findByUserId("USER001");
        verify(userRepository).delete(user);
    }

    @Test
    void getUsersByStatus_Success() {
        // Given
        List<User> users = Arrays.asList(user);
        when(userRepository.findByStatus(User.UserStatus.ACTIVE)).thenReturn(users);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        List<UserResponse> result = userService.getUsersByStatus(User.UserStatus.ACTIVE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findByStatus(User.UserStatus.ACTIVE);
        verify(userMapper).toResponse(user);
    }

    @Test
    void isEmailExists_True() {
        // Given
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When
        boolean result = userService.isEmailExists("john.doe@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("john.doe@example.com");
    }

    @Test
    void isEmailExists_False() {
        // Given
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // When
        boolean result = userService.isEmailExists("new@example.com");

        // Then
        assertFalse(result);
        verify(userRepository).existsByEmail("new@example.com");
    }

    @Test
    void isPhoneNumberExists_True() {
        // Given
        when(userRepository.existsByPhoneNumber("9876543210")).thenReturn(true);

        // When
        boolean result = userService.isPhoneNumberExists("9876543210");

        // Then
        assertTrue(result);
        verify(userRepository).existsByPhoneNumber("9876543210");
    }
}