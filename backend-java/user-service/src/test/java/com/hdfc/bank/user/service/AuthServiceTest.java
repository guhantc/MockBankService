package com.hdfc.bank.user.service;

import com.hdfc.bank.user.dto.LoginRequest;
import com.hdfc.bank.user.dto.LoginResponse;
import com.hdfc.bank.user.dto.RegisterRequest;
import com.hdfc.bank.user.dto.UserResponse;
import com.hdfc.bank.user.model.User;
import com.hdfc.bank.user.repository.UserRepository;
import com.hdfc.bank.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPhoneNumber("9876543210");
        registerRequest.setPassword("password123");
        registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setUserId("USER001");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhoneNumber("9876543210");
        user.setPassword("encodedPassword");
        user.setStatus(User.UserStatus.ACTIVE);
    }

    @Test
    void testRegisterSuccess() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        verify(userRepository).save(any(User.class)); // For updating last login
    }

    @Test
    void testLoginInvalidCredentials() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLoginUserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testValidateTokenSuccess() {
        // Given
        when(jwtUtil.validateToken(anyString())).thenReturn(true);

        // When
        boolean isValid = authService.validateToken("valid-token");

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenFailure() {
        // Given
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        // When
        boolean isValid = authService.validateToken("invalid-token");

        // Then
        assertFalse(isValid);
    }
}