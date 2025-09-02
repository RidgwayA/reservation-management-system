package com.reservations.reservation_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.reservations.reservation_system.entity.Reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${rvpark.name}")
    private String parkName;
    
    @Value("${rvpark.email}")
    private String parkEmail;
    
    @Value("${rvpark.phone}")
    private String parkPhone;
    
    @Value("${rvpark.address}")
    private String parkAddress;
    
    @Value("${spring.mail.username:}")
    private String mailUsername;
    
    public void sendConfirmationEmail(Reservation reservation) {
        try {
            if (mailUsername == null || mailUsername.trim().isEmpty()) {
                log.info("Email not configured - skipping confirmation email for reservation: {}", 
                    reservation.getConfirmationNumber());
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(parkEmail);
            message.setTo(reservation.getCustomer().getContactInfo().getEmail());
            message.setSubject("Reservation Confirmation - " + parkName);
            message.setText(buildConfirmationEmailBody(reservation));
            
            mailSender.send(message);
            log.info("Confirmation email sent successfully for reservation: {}", 
                reservation.getConfirmationNumber());
            
        } catch (Exception e) {
            log.error("Failed to send confirmation email for reservation: {}. Error: {}", 
                reservation.getConfirmationNumber(), e.getMessage());
        }
    }
    
    private String buildConfirmationEmailBody(Reservation reservation) {
        StringBuilder body = new StringBuilder();
        
        body.append("Dear ").append(reservation.getCustomer().getFirstName())
            .append(" ").append(reservation.getCustomer().getLastName()).append(",\n\n");
        
        body.append("Thank you for choosing ").append(parkName).append("! ")
            .append("Your reservation has been confirmed.\n\n");
        
        body.append("RESERVATION DETAILS:\n");
        body.append("Confirmation Number: ").append(reservation.getConfirmationNumber()).append("\n");
        body.append("Guest Name: ").append(reservation.getCustomer().getFirstName())
            .append(" ").append(reservation.getCustomer().getLastName()).append("\n");
        body.append("Check-in: ").append(reservation.getStayPeriod().getStartDate()).append("\n");
        body.append("Check-out: ").append(reservation.getStayPeriod().getEndDate()).append("\n");
        body.append("Site Number: ").append(reservation.getCampsite().getSiteNumber()).append("\n");
        body.append("Site Type: ").append(reservation.getCampsite().getSiteType()).append("\n");
        body.append("Party Size: ").append(reservation.getPartySize()).append(" guests\n");
        
        if (reservation.getVehicleInfo() != null && 
            reservation.getVehicleInfo().getLicensePlate() != null && 
            !reservation.getVehicleInfo().getLicensePlate().trim().isEmpty()) {
            body.append("Vehicle: ").append(reservation.getVehicleInfo().getMake())
                .append(" ").append(reservation.getVehicleInfo().getModel())
                .append(" (").append(reservation.getVehicleInfo().getLicensePlate()).append(")\n");
        }
        
        body.append("\nTOTAL AMOUNT: $").append(reservation.getTotalAmount().getAmount()).append("\n");
        body.append("Amount Paid: $").append(reservation.getPaidAmount().getAmount()).append("\n");
        
        if (reservation.getPaidAmount().getAmount().compareTo(reservation.getTotalAmount().getAmount()) < 0) {
            body.append("Balance Due: $")
                .append(reservation.getTotalAmount().getAmount().subtract(reservation.getPaidAmount().getAmount())).append("\n");
        }
        
        body.append("\nIMPORTANT INFORMATION:\n");
        body.append("• Check-in time: 1:00 PM\n");
        body.append("• Check-out time: 11:00 AM\n");
        body.append("• Please bring this confirmation email or your confirmation number\n");
        body.append("• Quiet hours: 10:00 PM - 8:00 AM\n\n");
        
        if (reservation.getNotes() != null && !reservation.getNotes().trim().isEmpty()) {
            body.append("Special Notes: ").append(reservation.getNotes()).append("\n\n");
        }
        
        body.append("CONTACT US:\n");
        body.append("Phone: ").append(parkPhone).append("\n");
        body.append("Email: ").append(parkEmail).append("\n");
        body.append("Address: ").append(parkAddress).append("\n\n");
        
        body.append("We look forward to hosting you at ").append(parkName).append("!\n\n");
        body.append("Best regards,\n");
        body.append("The ").append(parkName).append(" Team");
        
        return body.toString();
    }
}