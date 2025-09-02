package com.reservations.reservation_system.entity;

import com.reservations.reservation_system.enums.CampsiteStatus;
import com.reservations.reservation_system.enums.CampsiteType;
import com.reservations.reservation_system.enums.CampsiteLocation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "campsites", 
       uniqueConstraints = @UniqueConstraint(columnNames = "site_number"))
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Campsite extends BaseEntity {
    
    @NotNull(message = "Site number is required")
    @Min(value = 1, message = "Site number must be at least 1")
    @Max(value = 200, message = "Site number cannot exceed 200")
    @Column(name = "site_number", nullable = false, unique = true)
    private Integer siteNumber;
    
    @NotNull(message = "Campsite type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "site_type", nullable = false)
    private CampsiteType siteType;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CampsiteStatus status = CampsiteStatus.AVAILABLE;
    
    @NotNull(message = "Location is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "location", nullable = false)
    private CampsiteLocation location;
    
    @Column(name = "notes")
    private String notes;
    
    // Derived methods following Open/Closed Principle
    public BigDecimal getDailyRate() {
        return siteType.getDailyRate();
    }
    
    public int getMaxPartySize() {
        return siteType.getMaxPartySize();
    }
    
    public boolean isAvailableForBooking() {
        return getActive() && status.isBookable();
    }
    
    public boolean isRvSite() {
        return siteType.isRvSite();
    }
    
    public boolean isTentSite() {
        return siteType.isTentSite();
    }
    
    public String getDisplayName() {
        return String.format("Site %d (%s)", siteNumber, siteType.getDescription());
    }
    
    public void markForMaintenance(String reason) {
        this.status = CampsiteStatus.MAINTENANCE;
        this.notes = reason;
    }
    
    public void markAsAvailable() {
        this.status = CampsiteStatus.AVAILABLE;
    }
    
    public void markAsOccupied() {
        this.status = CampsiteStatus.OCCUPIED;
    }
}