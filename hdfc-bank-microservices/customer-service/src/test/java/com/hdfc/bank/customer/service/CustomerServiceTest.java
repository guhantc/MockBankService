package com.hdfc.bank.customer.service;

import com.hdfc.bank.customer.dto.CustomerDto;
import com.hdfc.bank.customer.entity.Customer;
import com.hdfc.bank.customer.exception.CustomerNotFoundException;
import com.hdfc.bank.customer.exception.DuplicateCustomerException;
import com.hdfc.bank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerDto validCustomerDto;
    private Customer customer;

    @BeforeEach
    void setUp() {
        validCustomerDto = new CustomerDto();
        validCustomerDto.setFirstName("John");
        validCustomerDto.setLastName("Doe");
        validCustomerDto.setEmail("john.doe@email.com");
        validCustomerDto.setPhoneNumber("+919876543210");
        validCustomerDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        validCustomerDto.setPanNumber("ABCDE1234F");
        validCustomerDto.setAadhaarNumber("123456789012");
        validCustomerDto.setAddress("123 Main Street");
        validCustomerDto.setCity("Mumbai");
        validCustomerDto.setState("Maharashtra");
        validCustomerDto.setPinCode("400001");
        validCustomerDto.setAnnualIncome(500000.0);
        validCustomerDto.setOccupation("Software Engineer");

        customer = new Customer();
        customer.setCustomerId("CUST123456789");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@email.com");
        customer.setPhoneNumber("+919876543210");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setPanNumber("ABCDE1234F");
        customer.setAadhaarNumber("123456789012");
        customer.setAddress("123 Main Street");
        customer.setCity("Mumbai");
        customer.setState("Maharashtra");
        customer.setPinCode("400001");
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setKycStatus(Customer.KycStatus.PENDING);
        customer.setAnnualIncome(500000.0);
        customer.setOccupation("Software Engineer");
    }

    @Test
    void createCustomer_ValidData_Success() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(customerRepository.existsByPanNumber(anyString())).thenReturn(false);
        when(customerRepository.existsByAadhaarNumber(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        CustomerDto result = customerService.createCustomer(validCustomerDto);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@email.com", result.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_UnderageCustomer_ThrowsException() {
        // Arrange
        validCustomerDto.setDateOfBirth(LocalDate.now().minusYears(16));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            customerService.createCustomer(validCustomerDto);
        });
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateCustomerException.class, () -> {
            customerService.createCustomer(validCustomerDto);
        });
    }

    @Test
    void createCustomer_DuplicatePan_ThrowsException() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(customerRepository.existsByPanNumber(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateCustomerException.class, () -> {
            customerService.createCustomer(validCustomerDto);
        });
    }

    @Test
    void getCustomerById_ExistingCustomer_Success() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));

        // Act
        CustomerDto result = customerService.getCustomerById("CUST123456789");

        // Assert
        assertNotNull(result);
        assertEquals("CUST123456789", result.getCustomerId());
        assertEquals("John", result.getFirstName());
    }

    @Test
    void getCustomerById_NonExistingCustomer_ThrowsException() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getCustomerById("INVALID_ID");
        });
    }

    @Test
    void getAllCustomers_MultipleCustomers_Success() {
        // Arrange
        Customer customer2 = new Customer();
        customer2.setCustomerId("CUST987654321");
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setEmail("jane.smith@email.com");

        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer, customer2));

        // Act
        List<CustomerDto> result = customerService.getAllCustomers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
    }

    @Test
    void updateCustomer_ValidData_Success() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerDto updateDto = new CustomerDto();
        updateDto.setFirstName("John Updated");
        updateDto.setLastName("Doe Updated");
        updateDto.setPhoneNumber("+919876543211");
        updateDto.setAddress("456 Updated Street");
        updateDto.setCity("Delhi");
        updateDto.setState("Delhi");
        updateDto.setPinCode("110001");
        updateDto.setAnnualIncome(600000.0);
        updateDto.setOccupation("Senior Software Engineer");

        // Act
        CustomerDto result = customerService.updateCustomer("CUST123456789", updateDto);

        // Assert
        assertNotNull(result);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateKycStatus_ValidStatus_Success() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        CustomerDto result = customerService.updateKycStatus("CUST123456789", Customer.KycStatus.COMPLETED);

        // Assert
        assertNotNull(result);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_ExistingCustomer_Success() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        customerService.deleteCustomer("CUST123456789");

        // Assert
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_NonExistingCustomer_ThrowsException() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.deleteCustomer("INVALID_ID");
        });
    }

    @Test
    void getCustomersByStatus_ActiveCustomers_Success() {
        // Arrange
        when(customerRepository.findByStatus(Customer.CustomerStatus.ACTIVE))
                .thenReturn(Arrays.asList(customer));

        // Act
        List<CustomerDto> result = customerService.getCustomersByStatus(Customer.CustomerStatus.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Customer.CustomerStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    void getCustomersByKycStatus_PendingKyc_Success() {
        // Arrange
        when(customerRepository.findByKycStatus(Customer.KycStatus.PENDING))
                .thenReturn(Arrays.asList(customer));

        // Act
        List<CustomerDto> result = customerService.getCustomersByKycStatus(Customer.KycStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Customer.KycStatus.PENDING, result.get(0).getKycStatus());
    }
}