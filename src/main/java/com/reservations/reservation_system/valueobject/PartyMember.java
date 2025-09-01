package com.reservations.reservation_system.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyMember {
    
    @NotBlank(message = "Party member name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 120, message = "Age must be reasonable")
    @Column(name = "age")
    private Integer age;
    
    public boolean isChild() {
        return age != null && age < 15;
    }
    
    public boolean isTeen() {
        return age != null && age >= 15 && age <= 17;
    }
    
    public boolean isAdult() {
        return age != null && age >= 18;
    }
    
    public boolean requiresAtvFee() {
        return !isChild();
    }
}