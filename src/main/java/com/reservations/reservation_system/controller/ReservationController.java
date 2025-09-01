package com.reservations.reservation_system.controller;

import com.reservations.reservation_system.dto.ReservationDto;
import com.reservations.reservation_system.entity.Reservation;
import com.reservations.reservation_system.repository.ReservationRepository;
import com.reservations.reservation_system.repository.CustomerRepository;
import com.reservations.reservation_system.repository.CampsiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CampsiteRepository campsiteRepository;
    
    @GetMapping
    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .filter(r -> r.getActive())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .filter(r -> r.getActive())
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/confirmation/{confirmationNumber}")
    public ResponseEntity<ReservationDto> getReservationByConfirmation(@PathVariable String confirmationNumber) {
        return reservationRepository.findByConfirmationNumber(confirmationNumber)
                .filter(r -> r.getActive())
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public List<ReservationDto> getReservationsByCustomer(@PathVariable Long customerId) {
        return reservationRepository.findActiveReservationsByCustomerId(customerId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/dates")
    public List<ReservationDto> getReservationsForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reservationRepository.findActiveReservationsForDateRange(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/checkin/today")
    public List<ReservationDto> getReservationsCheckingInToday() {
        return reservationRepository.findReservationsCheckingInToday(LocalDate.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/checkout/today")
    public List<ReservationDto> getReservationsCheckingOutToday() {
        return reservationRepository.findReservationsCheckingOutToday(LocalDate.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @PutMapping("/{id}/checkin")
    public ResponseEntity<ReservationDto> checkIn(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    reservation.checkIn();
                    Reservation saved = reservationRepository.save(reservation);
                    return ResponseEntity.ok(convertToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/checkout")
    public ResponseEntity<ReservationDto> checkOut(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    reservation.checkOut();
                    Reservation saved = reservationRepository.save(reservation);
                    return ResponseEntity.ok(convertToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationDto> cancelReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    reservation.cancel();
                    Reservation saved = reservationRepository.save(reservation);
                    return ResponseEntity.ok(convertToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setConfirmationNumber(reservation.getConfirmationNumber());
        dto.setStatus(reservation.getStatus());
        dto.setPartyMembers(reservation.getPartyMembers());
        dto.setPartySize(reservation.getPartySize());
        dto.setCheckInTime(reservation.getCheckInTime());
        dto.setCheckOutTime(reservation.getCheckOutTime());
        dto.setNotes(reservation.getNotes());
        
        if (reservation.getStayPeriod() != null) {
            dto.setStartDate(reservation.getStayPeriod().getStartDate());
            dto.setEndDate(reservation.getStayPeriod().getEndDate());
        }
        
        if (reservation.getVehicleInfo() != null) {
            dto.setVehicleLicensePlate(reservation.getVehicleInfo().getLicensePlate());
            dto.setVehicleMake(reservation.getVehicleInfo().getMake());
            dto.setVehicleModel(reservation.getVehicleInfo().getModel());
            dto.setRvLengthFeet(reservation.getVehicleInfo().getRvLengthFeet());
        }
        
        if (reservation.getCampsiteTotal() != null) {
            dto.setCampsiteTotal(reservation.getCampsiteTotal().getAmount());
        }
        if (reservation.getAtvTotal() != null) {
            dto.setAtvTotal(reservation.getAtvTotal().getAmount());
        }
        if (reservation.getTotalAmount() != null) {
            dto.setTotalAmount(reservation.getTotalAmount().getAmount());
        }
        if (reservation.getPaidAmount() != null) {
            dto.setPaidAmount(reservation.getPaidAmount().getAmount());
        }
        
        // Note: Customer and Campsite DTOs would be set here in a real implementation
        // For MVP, we're keeping it simple
        
        return dto;
    }
}