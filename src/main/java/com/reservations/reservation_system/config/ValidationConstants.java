package com.reservations.reservation_system.config;

public final class ValidationConstants {
    
    // Contact Information
    public static final int MAX_EMAIL_LENGTH = 150;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final String PHONE_PATTERN = "^[\\+]?[1-9]\\d{1,14}$";
    
    // Pricing
    public static final String DECIMAL_MIN_VALUE = "0.0";
    public static final int DECIMAL_PRECISION = 10;
    public static final int DECIMAL_SCALE = 2;
    
    private ValidationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}