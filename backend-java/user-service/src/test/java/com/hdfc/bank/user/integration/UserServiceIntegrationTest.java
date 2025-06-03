package com.hdfc.bank.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdfc.bank.user.dto.LoginRequest;
import com.hdfc.bank.user.dto.RegisterRequest;
import com.hdfc.bank.user.model.User;
import com.hdfc.bank.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User Service
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureWebMvc
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    void completeUserFlow_Success() throws Exception {
        // Step 1: Register a new user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@test.com");
        registerRequest.setPhoneNumber("9876543210");
        registerRequest.setPassword("password123");
        registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"));

        // Step 2: Login with the registered user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@test.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        // Extract token for subsequent requests
        String token = objectMapper.readTree(loginResponse).get("token").asText();

        // Step 3: Get user details
        mockMvc.perform(get("/users/john.doe@test.com")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.email").value("john.doe@test.com"));

        // Step 4: Update user information
        String updateJson = """
            {
                "firstName": "John Updated",
                "lastName": "Doe Updated",
                "phoneNumber": "9876543211"
            }
            """;

        mockMvc.perform(put("/users/john.doe@test.com")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.firstName").value("John Updated"));
    }

    @Test
    void registerUser_DuplicateEmail_Failure() throws Exception {
        // Step 1: Register first user
        RegisterRequest firstUser = new RegisterRequest();
        firstUser.setFirstName("John");
        firstUser.setLastName("Doe");
        firstUser.setEmail("duplicate@test.com");
        firstUser.setPhoneNumber("9876543210");
        firstUser.setPassword("password123");
        firstUser.setDateOfBirth(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isOk());

        // Step 2: Try to register second user with same email
        RegisterRequest secondUser = new RegisterRequest();
        secondUser.setFirstName("Jane");
        secondUser.setLastName("Smith");
        secondUser.setEmail("duplicate@test.com"); // Same email
        secondUser.setPhoneNumber("9876543211");
        secondUser.setPassword("password456");
        secondUser.setDateOfBirth(LocalDate.of(1992, 2, 2));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_InvalidCredentials_Failure() throws Exception {
        // Step 1: Register a user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.login@test.com");
        registerRequest.setPhoneNumber("9876543212");
        registerRequest.setPassword("correctpassword");
        registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Step 2: Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.login@test.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsersByStatus_Success() throws Exception {
        // Create test users with different statuses
        User activeUser = new User();
        activeUser.setUserId("ACTIVE001");
        activeUser.setFirstName("Active");
        activeUser.setLastName("User");
        activeUser.setEmail("active@test.com");
        activeUser.setPhoneNumber("9876543213");
        activeUser.setPassword("password");
        activeUser.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(activeUser);

        User inactiveUser = new User();
        inactiveUser.setUserId("INACTIVE001");
        inactiveUser.setFirstName("Inactive");
        inactiveUser.setLastName("User");
        inactiveUser.setEmail("inactive@test.com");
        inactiveUser.setPhoneNumber("9876543214");
        inactiveUser.setPassword("password");
        inactiveUser.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(inactiveUser);

        // Test getting active users
        mockMvc.perform(get("/users/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        // Test getting inactive users
        mockMvc.perform(get("/users/status/INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("INACTIVE"));
    }

    @Test
    void tokenValidation_Success() throws Exception {
        // Step 1: Register and login to get token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Token");
        registerRequest.setLastName("User");
        registerRequest.setEmail("token@test.com");
        registerRequest.setPhoneNumber("9876543215");
        registerRequest.setPassword("password123");
        registerRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("token@test.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        // Step 2: Validate the token
        mockMvc.perform(post("/auth/validate-token")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Step 3: Test invalid token
        mockMvc.perform(post("/auth/validate-token")
                .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}