package com.reservations.reservation_system.entity;

import com.reservations.reservation_system.valueobject.ContactInfo;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "email", column = @Column(name = "email")),
        @AttributeOverride(name = "phone", column = @Column(name = "phone"))
    })
    private ContactInfo contactInfo;
    
    @Valid
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "email", column = @Column(name = "emergency_email")),
        @AttributeOverride(name = "phone", column = @Column(name = "emergency_phone"))
    })
    private ContactInfo emergencyContact;
    
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;
    
    @Size(max = 50, message = "Emergency contact relationship must not exceed 50 characters")
    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;
    
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
    
    public boolean hasEmergencyContact() {
        return emergencyContactName != null && 
               emergencyContact != null && 
               emergencyContact.isComplete();
    }
    
    public String getPrimaryContactMethod() {
        return contactInfo != null ? contactInfo.getPrimaryContact() : null;
    }
}