package com.reservations.reservation_system.config;

public final class ValidationConstants {
    
    // General
    public static final int MIN_ID_VALUE = 1;
    public static final int MAX_STRING_LENGTH = 255;
    
    // Names and Text
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_NOTES_LENGTH = 1000;
    
    // Contact Information
    public static final int MAX_EMAIL_LENGTH = 150;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PHONE_PATTERN = "^[\\+]?[1-9]\\d{1,14}$";
    
    // User Management
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    
    // Address
    public static final int MAX_STREET_ADDRESS_LENGTH = 255;
    public static final int MAX_CITY_LENGTH = 100;
    public static final int MAX_STATE_LENGTH = 50;
    public static final int MAX_COUNTRY_LENGTH = 50;
    public static final int MAX_POSTAL_CODE_LENGTH = 10;
    public static final String POSTAL_CODE_PATTERN = "^[A-Za-z0-9\\s\\-]{3,10}$";
    
    // Business Rules
    public static final int MIN_PARTY_SIZE = 1;
    public static final int MAX_PARTY_SIZE = 50;
    public static final int MIN_BOOKING_DURATION_MINUTES = 15;
    public static final int MAX_ADVANCE_BOOKING_DAYS = 365;
    
    // Resource Management
    public static final int MAX_RESOURCE_IDENTIFIER_LENGTH = 50;
    public static final int MAX_RESOURCE_SIZE_DESCRIPTION_LENGTH = 50;
    public static final int MAX_ATTRIBUTE_KEY_LENGTH = 50;
    public static final int MAX_ATTRIBUTE_VALUE_LENGTH = 255;
    
    // Pricing
    public static final String DECIMAL_MIN_VALUE = "0.0";
    public static final String DECIMAL_MAX_VALUE = "999999.99";
    public static final int DECIMAL_PRECISION = 10;
    public static final int DECIMAL_SCALE = 2;
    
    // Confirmation Numbers
    public static final int CONFIRMATION_NUMBER_LENGTH = 12;
    public static final String CONFIRMATION_NUMBER_PATTERN = "^[A-Z0-9]{12}$";
    
    // Security
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCK_HOURS = 1;
    public static final int PASSWORD_RESET_TOKEN_HOURS = 24;
    
    // Private constructor to prevent instantiation
    private ValidationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}