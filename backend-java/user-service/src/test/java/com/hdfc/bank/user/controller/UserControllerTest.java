package com.hdfc.bank.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdfc.bank.user.dto.UserResponse;
import com.hdfc.bank.user.dto.UserUpdateRequest;
import com.hdfc.bank.user.model.User;
import com.hdfc.bank.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
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
    @WithMockUser
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById("USER001")).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/users/USER001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("USER001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).getUserById("USER001");
    }

    @Test
    @WithMockUser
    void getUserById_NotFound() throws Exception {
        // Given
        when(userService.getUserById("INVALID")).thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/users/INVALID"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById("INVALID");
    }

    @Test
    @WithMockUser
    void getAllUsers_Success() throws Exception {
        // Given
        List<UserResponse> users = Arrays.asList(userResponse);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value("USER001"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser
    void updateUser_Success() throws Exception {
        // Given
        when(userService.updateUser(eq("USER001"), any(UserUpdateRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(put("/users/USER001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("USER001"));

        verify(userService).updateUser(eq("USER001"), any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser
    void deleteUser_Success() throws Exception {
        // Given
        doNothing().when(userService).deleteUser("USER001");

        // When & Then
        mockMvc.perform(delete("/users/USER001"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("USER001");
    }

    @Test
    @WithMockUser
    void updateUser_InvalidData() throws Exception {
        // Given
        updateRequest.setFirstName(""); // Invalid empty name

        // When & Then
        mockMvc.perform(put("/users/USER001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(anyString(), any());
    }

    @Test
    @WithMockUser
    void getUsersByStatus_Success() throws Exception {
        // Given
        List<UserResponse> activeUsers = Arrays.asList(userResponse);
        when(userService.getUsersByStatus(User.UserStatus.ACTIVE)).thenReturn(activeUsers);

        // When & Then
        mockMvc.perform(get("/users/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(userService).getUsersByStatus(User.UserStatus.ACTIVE);
    }
}