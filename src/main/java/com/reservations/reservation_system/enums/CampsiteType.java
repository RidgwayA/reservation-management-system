package com.reservations.reservation_system.enums;

import java.math.BigDecimal;

public enum CampsiteType {
    FULL_HOOKUP("Full RV Hookup (Water/Electric)", new BigDecimal("40.00"), 15),
    TENT("Tent Camping", new BigDecimal("15.00"), 8);
    
    private final String description;
    private final BigDecimal dailyRate;
    private final int maxPartySize;
    
    CampsiteType(String description, BigDecimal dailyRate, int maxPartySize) {
        this.description = description;
        this.dailyRate = dailyRate;
        this.maxPartySize = maxPartySize;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getDailyRate() {
        return dailyRate;
    }
    
    public int getMaxPartySize() {
        return maxPartySize;
    }
    
    public boolean isRvSite() {
        return this == FULL_HOOKUP;
    }
    
    public boolean isTentSite() {
        return this == TENT;
    }
}