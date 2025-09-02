package com.reservations.reservation_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyEmail;
    private String emergencyPhone;
    
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
}