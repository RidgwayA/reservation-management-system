package com.reservations.reservation_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "reservation.business")
@Data
public class BusinessRulesConfig {
    
    // Reservation Rules
    private int maxPartySize = 50;
    private int minBookingDurationMinutes = 15;
    private int maxAdvanceBookingDays = 365;
    private int defaultBookingDurationMinutes = 120;
    
    // Pricing Rules  
    private boolean enableDynamicPricing = true;
    private boolean enableSeasonalPricing = true;
    private boolean enablePeakHourPricing = true;
    private double defaultPeakMultiplier = 1.5;
    private double defaultOffPeakMultiplier = 0.8;
    
    // Customer Rules
    private int maxReservationsPerCustomer = 10;
    private int maxActiveReservationsPerCustomer = 3;
    private boolean enableLoyaltyProgram = true;
    private int loyaltyPointsPerDollar = 1;
    
    // Security Rules
    private int maxFailedLoginAttempts = 5;
    private int accountLockHours = 1;
    private int passwordResetTokenHours = 24;
    private boolean enableAccountVerification = true;
    
    // Notification Rules
    private boolean enableEmailNotifications = true;
    private boolean enableSmsNotifications = false;
    private int reminderHoursBeforeReservation = 24;
    private int cancellationGracePeriodHours = 2;
    
    // Resource Rules
    private boolean enableResourceMaintenance = true;
    private boolean enableResourceAttributes = true;
    private boolean enableResourcePricing = true;
    
    // Conflict Resolution
    private boolean enableAutomaticConflictResolution = true;
    private boolean enableOverlappingReservations = false;
    private int bufferMinutesBetweenReservations = 0;
    
    // Reporting Rules
    private boolean enableAdvancedReporting = true;
    private boolean enableDataExport = true;
    private int dataRetentionDays = 2555; // 7 years
    
    // Multi-tenant Rules (for future SaaS expansion)
    private boolean enableMultiTenant = false;
    private int maxResourcesPerTenant = 1000;
    private int maxUsersPerTenant = 100;
    
    // Industry-Specific Rules
    private boolean enableAllDayReservations = true;
    private boolean enableMultiDayReservations = true;
    private boolean enableRecurringReservations = false;
    private boolean enableWaitList = false;
    
    // Payment Rules
    private boolean requireDepositForReservation = false;
    private double defaultDepositPercentage = 0.20; // 20%
    private boolean enableOnlinePayments = false;
    private int paymentDueDays = 7;
}