package com.reservations.reservation_system.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleInfo {
    
    @Pattern(regexp = "^[A-Z0-9\\-\\s]{2,10}$", message = "Please provide a valid license plate")
    @Column(name = "license_plate")
    private String licensePlate;
    
    @Size(max = 50, message = "Vehicle make must not exceed 50 characters")
    @Column(name = "vehicle_make")
    private String make;
    
    @Size(max = 50, message = "Vehicle model must not exceed 50 characters") 
    @Column(name = "vehicle_model")
    private String model;
    
    @Min(value = 10, message = "RV length must be at least 10 feet")
    @Max(value = 100, message = "RV length cannot exceed 100 feet")
    @Column(name = "rv_length_feet")
    private Integer rvLengthFeet;
    
    public boolean isRv() {
        return rvLengthFeet != null && rvLengthFeet > 0;
    }
    
    public boolean hasCompleteInfo() {
        return licensePlate != null && !licensePlate.trim().isEmpty();
    }
    
    public String getDisplayName() {
        if (make != null && model != null) {
            return String.format("%s %s", make, model);
        }
        return isRv() ? String.format("RV (%d ft)", rvLengthFeet) : "Vehicle";
    }
}