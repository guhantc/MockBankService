package com.hdfc.bank.customer.controller;

import com.hdfc.bank.customer.dto.CustomerDto;
import com.hdfc.bank.customer.entity.Customer;
import com.hdfc.bank.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        CustomerDto createdCustomer = customerService.createCustomer(customerDto);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable String customerId) {
        CustomerDto customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDto> getCustomerByEmail(@PathVariable String email) {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/pan/{panNumber}")
    public ResponseEntity<CustomerDto> getCustomerByPan(@PathVariable String panNumber) {
        CustomerDto customer = customerService.getCustomerByPan(panNumber);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<CustomerDto> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerDto>> getCustomersByStatus(@PathVariable Customer.CustomerStatus status) {
        List<CustomerDto> customers = customerService.getCustomersByStatus(status);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/kyc-status/{kycStatus}")
    public ResponseEntity<List<CustomerDto>> getCustomersByKycStatus(@PathVariable Customer.KycStatus kycStatus) {
        List<CustomerDto> customers = customerService.getCustomersByKycStatus(kycStatus);
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable String customerId, 
                                                     @Valid @RequestBody CustomerDto customerDto) {
        CustomerDto updatedCustomer = customerService.updateCustomer(customerId, customerDto);
        return ResponseEntity.ok(updatedCustomer);
    }

    @PatchMapping("/{customerId}/kyc-status")
    public ResponseEntity<CustomerDto> updateKycStatus(@PathVariable String customerId, 
                                                      @RequestBody Customer.KycStatus kycStatus) {
        CustomerDto updatedCustomer = customerService.updateKycStatus(customerId, kycStatus);
        return ResponseEntity.ok(updatedCustomer);
    }

    @PatchMapping("/{customerId}/status")
    public ResponseEntity<CustomerDto> updateCustomerStatus(@PathVariable String customerId, 
                                                           @RequestBody Customer.CustomerStatus status) {
        CustomerDto updatedCustomer = customerService.updateCustomerStatus(customerId, status);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Customer Service is running");
    }
}