package com.reservations.reservation_system.repository;

import com.reservations.reservation_system.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByContactInfoEmail(String email);
    
    Optional<Customer> findByContactInfoPhone(String phone);
    
    List<Customer> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    @Query("SELECT c FROM Customer c WHERE c.active = true")
    List<Customer> findActiveCustomers();
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.contactInfo.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> searchCustomers(@Param("searchTerm") String searchTerm);
}