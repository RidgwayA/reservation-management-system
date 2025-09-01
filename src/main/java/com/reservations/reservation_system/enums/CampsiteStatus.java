package com.reservations.reservation_system.enums;

public enum CampsiteStatus {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    MAINTENANCE("Under Maintenance"),
    OUT_OF_ORDER("Out of Order");
    
    private final String description;
    
    CampsiteStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isBookable() {
        return this == AVAILABLE;
    }
    
    public boolean requiresMaintenance() {
        return this == MAINTENANCE || this == OUT_OF_ORDER;
    }
}