package com.hdfc.bank.customer.service;

import com.hdfc.bank.customer.dto.CustomerDto;
import com.hdfc.bank.customer.entity.Customer;
import com.hdfc.bank.customer.exception.CustomerNotFoundException;
import com.hdfc.bank.customer.exception.DuplicateCustomerException;
import com.hdfc.bank.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public CustomerDto createCustomer(CustomerDto customerDto) {
        // Validate business rules
        validateCustomerCreation(customerDto);
        
        // Convert DTO to Entity
        Customer customer = convertToEntity(customerDto);
        
        // Save customer
        Customer savedCustomer = customerRepository.save(customer);
        
        return convertToDto(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        return convertToDto(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
        return convertToDto(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByPan(String panNumber) {
        Customer customer = customerRepository.findByPanNumber(panNumber)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with PAN: " + panNumber));
        return convertToDto(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getCustomersByStatus(Customer.CustomerStatus status) {
        return customerRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getCustomersByKycStatus(Customer.KycStatus kycStatus) {
        return customerRepository.findByKycStatus(kycStatus).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CustomerDto updateCustomer(String customerId, CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        // Update allowed fields
        existingCustomer.setFirstName(customerDto.getFirstName());
        existingCustomer.setLastName(customerDto.getLastName());
        existingCustomer.setPhoneNumber(customerDto.getPhoneNumber());
        existingCustomer.setAddress(customerDto.getAddress());
        existingCustomer.setCity(customerDto.getCity());
        existingCustomer.setState(customerDto.getState());
        existingCustomer.setPinCode(customerDto.getPinCode());
        existingCustomer.setAnnualIncome(customerDto.getAnnualIncome());
        existingCustomer.setOccupation(customerDto.getOccupation());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return convertToDto(updatedCustomer);
    }

    public CustomerDto updateKycStatus(String customerId, Customer.KycStatus kycStatus) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.setKycStatus(kycStatus);
        Customer updatedCustomer = customerRepository.save(customer);
        return convertToDto(updatedCustomer);
    }

    public CustomerDto updateCustomerStatus(String customerId, Customer.CustomerStatus status) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.setStatus(status);
        Customer updatedCustomer = customerRepository.save(customer);
        return convertToDto(updatedCustomer);
    }

    public void deleteCustomer(String customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        
        // Soft delete - mark as CLOSED instead of physical delete
        customer.setStatus(Customer.CustomerStatus.CLOSED);
        customerRepository.save(customer);
    }

    // Banking business logic validations
    private void validateCustomerCreation(CustomerDto customerDto) {
        // Check age - must be at least 18 years old
        if (Period.between(customerDto.getDateOfBirth(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Customer must be at least 18 years old");
        }

        // Check for duplicate email
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new DuplicateCustomerException("Customer with email already exists: " + customerDto.getEmail());
        }

        // Check for duplicate phone
        if (customerRepository.existsByPhoneNumber(customerDto.getPhoneNumber())) {
            throw new DuplicateCustomerException("Customer with phone number already exists: " + customerDto.getPhoneNumber());
        }

        // Check for duplicate PAN
        if (customerRepository.existsByPanNumber(customerDto.getPanNumber())) {
            throw new DuplicateCustomerException("Customer with PAN already exists: " + customerDto.getPanNumber());
        }

        // Check for duplicate Aadhaar
        if (customerRepository.existsByAadhaarNumber(customerDto.getAadhaarNumber())) {
            throw new DuplicateCustomerException("Customer with Aadhaar already exists: " + customerDto.getAadhaarNumber());
        }

        // Validate annual income
        if (customerDto.getAnnualIncome() != null && customerDto.getAnnualIncome() < 0) {
            throw new IllegalArgumentException("Annual income cannot be negative");
        }
    }

    // Entity-DTO conversion methods
    private Customer convertToEntity(CustomerDto dto) {
        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setPanNumber(dto.getPanNumber());
        customer.setAadhaarNumber(dto.getAadhaarNumber());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setPinCode(dto.getPinCode());
        customer.setAnnualIncome(dto.getAnnualIncome());
        customer.setOccupation(dto.getOccupation());
        return customer;
    }

    private CustomerDto convertToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setPanNumber(customer.getPanNumber());
        dto.setAadhaarNumber(customer.getAadhaarNumber());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setPinCode(customer.getPinCode());
        dto.setStatus(customer.getStatus());
        dto.setKycStatus(customer.getKycStatus());
        dto.setAnnualIncome(customer.getAnnualIncome());
        dto.setOccupation(customer.getOccupation());
        return dto;
    }
}