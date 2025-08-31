package com.reservations.reservation_system.valueobject;

import com.reservations.reservation_system.config.ValidationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {
    
    @Email(message = "Please provide a valid email address")
    @Size(max = ValidationConstants.MAX_EMAIL_LENGTH, message = "Email must not exceed " + ValidationConstants.MAX_EMAIL_LENGTH + " characters")
    @Column(name = "email", length = ValidationConstants.MAX_EMAIL_LENGTH)
    private String email;
    
    @Pattern(regexp = ValidationConstants.PHONE_PATTERN, message = "Please provide a valid phone number")
    @Size(max = ValidationConstants.MAX_PHONE_LENGTH, message = "Phone number must not exceed " + ValidationConstants.MAX_PHONE_LENGTH + " characters")
    @Column(name = "phone", length = ValidationConstants.MAX_PHONE_LENGTH)
    private String phone;
    
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }
    
    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }
    
    public boolean isComplete() {
        return hasEmail() || hasPhone();
    }
    
    public String getPrimaryContact() {
        return hasEmail() ? email : phone;
    }
}