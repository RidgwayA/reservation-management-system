package com.reservations.reservation_system.dto;

import com.reservations.reservation_system.enums.CampsiteStatus;
import com.reservations.reservation_system.enums.CampsiteType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampsiteDto {
    private Long id;
    private Integer siteNumber;
    private CampsiteType siteType;
    private CampsiteStatus status;
    private BigDecimal dailyRate;
    private Integer maxPartySize;
    private String notes;
    
    public String getDisplayName() {
        return String.format("Site %d (%s)", siteNumber, siteType.getDescription());
    }
    
    public boolean isAvailable() {
        return status == CampsiteStatus.AVAILABLE;
    }
}