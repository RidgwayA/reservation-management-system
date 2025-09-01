package com.reservations.reservation_system.enums;

public enum CampsiteLocation {
    WOODS("Woods", 20),
    ATV_PARK("ATV Park", 15),
    LAKE("Lake", 20),
    BASE_CAMP("Base Camp", 20);
    
    private final String displayName;
    private final int totalSites;
    
    CampsiteLocation(String displayName, int totalSites) {
        this.displayName = displayName;
        this.totalSites = totalSites;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getTotalSites() {
        return totalSites;
    }
}