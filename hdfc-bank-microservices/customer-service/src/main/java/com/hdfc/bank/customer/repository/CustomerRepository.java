package com.hdfc.bank.customer.repository;

import com.hdfc.bank.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    
    Optional<Customer> findByPanNumber(String panNumber);
    
    Optional<Customer> findByAadhaarNumber(String aadhaarNumber);
    
    List<Customer> findByStatus(Customer.CustomerStatus status);
    
    List<Customer> findByKycStatus(Customer.KycStatus kycStatus);
    
    @Query("SELECT c FROM Customer c WHERE c.firstName LIKE %:name% OR c.lastName LIKE %:name%")
    List<Customer> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT c FROM Customer c WHERE c.city = :city AND c.state = :state")
    List<Customer> findByCityAndState(@Param("city") String city, @Param("state") String state);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByPanNumber(String panNumber);
    
    boolean existsByAadhaarNumber(String aadhaarNumber);
}