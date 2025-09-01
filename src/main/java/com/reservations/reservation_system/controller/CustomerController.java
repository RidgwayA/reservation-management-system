package com.reservations.reservation_system.controller;

import com.reservations.reservation_system.dto.CustomerDto;
import com.reservations.reservation_system.entity.Customer;
import com.reservations.reservation_system.repository.CustomerRepository;
import com.reservations.reservation_system.valueobject.ContactInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @GetMapping
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findActiveCustomers()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public List<CustomerDto> searchCustomers(@RequestParam String term) {
        return customerRepository.searchCustomers(term)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @PostMapping
    public CustomerDto createCustomer(@RequestBody CustomerDto customerDto) {
        Customer customer = convertToEntity(customerDto);
        Customer saved = customerRepository.save(customer);
        return convertToDto(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @RequestBody CustomerDto customerDto) {
        return customerRepository.findById(id)
                .map(customer -> {
                    updateEntityFromDto(customer, customerDto);
                    Customer saved = customerRepository.save(customer);
                    return ResponseEntity.ok(convertToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.markAsDeleted();
                    customerRepository.save(customer);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private CustomerDto convertToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        
        if (customer.getContactInfo() != null) {
            dto.setEmail(customer.getContactInfo().getEmail());
            dto.setPhone(customer.getContactInfo().getPhone());
        }
        
        dto.setEmergencyContactName(customer.getEmergencyContactName());
        dto.setEmergencyContactRelationship(customer.getEmergencyContactRelationship());
        
        if (customer.getEmergencyContact() != null) {
            dto.setEmergencyEmail(customer.getEmergencyContact().getEmail());
            dto.setEmergencyPhone(customer.getEmergencyContact().getPhone());
        }
        
        return dto;
    }
    
    private Customer convertToEntity(CustomerDto dto) {
        Customer customer = new Customer();
        updateEntityFromDto(customer, dto);
        return customer;
    }
    
    private void updateEntityFromDto(Customer customer, CustomerDto dto) {
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        
        ContactInfo contactInfo = new ContactInfo(dto.getEmail(), dto.getPhone());
        customer.setContactInfo(contactInfo);
        
        customer.setEmergencyContactName(dto.getEmergencyContactName());
        customer.setEmergencyContactRelationship(dto.getEmergencyContactRelationship());
        
        ContactInfo emergencyContact = new ContactInfo(dto.getEmergencyEmail(), dto.getEmergencyPhone());
        customer.setEmergencyContact(emergencyContact);
    }
}