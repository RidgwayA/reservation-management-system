package com.reservations.reservation_system.controller;

import com.reservations.reservation_system.dto.ReservationDto;
import com.reservations.reservation_system.dto.CustomerDto;
import com.reservations.reservation_system.dto.CampsiteDto;
import com.reservations.reservation_system.entity.Reservation;
import com.reservations.reservation_system.repository.ReservationRepository;
import com.reservations.reservation_system.repository.CustomerRepository;
import com.reservations.reservation_system.repository.CampsiteRepository;
import com.reservations.reservation_system.valueobject.DateTimeRange;
import com.reservations.reservation_system.valueobject.VehicleInfo;
import com.reservations.reservation_system.valueobject.Money;
import com.reservations.reservation_system.valueobject.ContactInfo;
import com.reservations.reservation_system.entity.Customer;
import com.reservations.reservation_system.entity.Campsite;
import com.reservations.reservation_system.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Autowired
    private EmailService emailService;
    
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
    
    @PostMapping
    @Transactional
    public ResponseEntity<ReservationDto> createReservation(@RequestBody ReservationDto reservationDto) {
        try {
            System.out.println("DEBUG: Received reservation request: " + reservationDto);
            
            Reservation reservation = convertFromDto(reservationDto);
            System.out.println("DEBUG: Converted to entity: " + reservation);
            
            // Generate confirmation number
            String confirmationNumber = generateConfirmationNumber();
            System.out.println("DEBUG: Generated confirmation number: " + confirmationNumber);
            reservation.confirm(confirmationNumber);
            
            Reservation saved = reservationRepository.save(reservation);
            System.out.println("DEBUG: Saved reservation with ID: " + saved.getId());
            
            // Send confirmation email asynchronously (don't let email failure break booking)
            try {
                emailService.sendConfirmationEmail(saved);
                System.out.println("DEBUG: Confirmation email triggered for: " + saved.getConfirmationNumber());
            } catch (Exception e) {
                System.err.println("WARNING: Failed to send confirmation email for " + saved.getConfirmationNumber() + ": " + e.getMessage());
                // Continue - email failure shouldn't break the booking process
            }
            
            ReservationDto result = convertToDto(saved);
            System.out.println("DEBUG: Returning DTO: " + result);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create reservation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
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
        
        // Populate Customer DTO
        if (reservation.getCustomer() != null) {
            CustomerDto customerDto = new CustomerDto();
            customerDto.setId(reservation.getCustomer().getId());
            customerDto.setFirstName(reservation.getCustomer().getFirstName());
            customerDto.setLastName(reservation.getCustomer().getLastName());
            
            if (reservation.getCustomer().getContactInfo() != null) {
                customerDto.setEmail(reservation.getCustomer().getContactInfo().getEmail());
                customerDto.setPhone(reservation.getCustomer().getContactInfo().getPhone());
            }
            
            customerDto.setEmergencyContactName(reservation.getCustomer().getEmergencyContactName());
            customerDto.setEmergencyContactRelationship(reservation.getCustomer().getEmergencyContactRelationship());
            
            if (reservation.getCustomer().getEmergencyContact() != null) {
                customerDto.setEmergencyEmail(reservation.getCustomer().getEmergencyContact().getEmail());
                customerDto.setEmergencyPhone(reservation.getCustomer().getEmergencyContact().getPhone());
            }
            
            dto.setCustomer(customerDto);
        }
        
        // Populate Campsite DTO
        if (reservation.getCampsite() != null) {
            CampsiteDto campsiteDto = new CampsiteDto();
            campsiteDto.setId(reservation.getCampsite().getId());
            campsiteDto.setSiteNumber(reservation.getCampsite().getSiteNumber());
            campsiteDto.setSiteType(reservation.getCampsite().getSiteType());
            campsiteDto.setStatus(reservation.getCampsite().getStatus());
            campsiteDto.setDailyRate(reservation.getCampsite().getDailyRate());
            campsiteDto.setMaxPartySize(reservation.getCampsite().getMaxPartySize());
            campsiteDto.setNotes(reservation.getCampsite().getNotes());
            
            dto.setCampsite(campsiteDto);
        }
        
        return dto;
    }
    
    private Reservation convertFromDto(ReservationDto dto) {
        Reservation reservation = new Reservation();
        
        // Handle customer - either find existing or create new
        Customer customer;
        if (dto.getCustomer() != null && dto.getCustomer().getId() != null) {
            customer = customerRepository.findById(dto.getCustomer().getId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        } else if (dto.getCustomer() != null) {
            // Create new customer
            customer = new Customer();
            customer.setFirstName(dto.getCustomer().getFirstName());
            customer.setLastName(dto.getCustomer().getLastName());
            
            if (dto.getCustomer().getEmail() != null || dto.getCustomer().getPhone() != null) {
                ContactInfo contactInfo = new ContactInfo(
                    dto.getCustomer().getEmail(),
                    dto.getCustomer().getPhone()
                );
                customer.setContactInfo(contactInfo);
            }
            
            customer.setEmergencyContactName(dto.getCustomer().getEmergencyContactName());
            customer.setEmergencyContactRelationship(dto.getCustomer().getEmergencyContactRelationship());
            
            if (dto.getCustomer().getEmergencyEmail() != null || dto.getCustomer().getEmergencyPhone() != null) {
                ContactInfo emergencyContact = new ContactInfo(
                    dto.getCustomer().getEmergencyEmail(),
                    dto.getCustomer().getEmergencyPhone()
                );
                customer.setEmergencyContact(emergencyContact);
            }
            
            customer = customerRepository.save(customer);
        } else {
            throw new IllegalArgumentException("Customer information is required");
        }
        
        reservation.setCustomer(customer);
        
        // Handle campsite
        if (dto.getCampsite() != null && dto.getCampsite().getId() != null) {
            Campsite campsite = campsiteRepository.findById(dto.getCampsite().getId())
                .orElseThrow(() -> new IllegalArgumentException("Campsite not found"));
            reservation.setCampsite(campsite);
        } else {
            throw new IllegalArgumentException("Campsite information is required");
        }
        
        // Set basic fields
        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            DateTimeRange stayPeriod = new DateTimeRange(
                dto.getStartDate(),
                dto.getEndDate(),
                java.time.LocalTime.of(15, 0), // 3 PM check-in
                java.time.LocalTime.of(11, 0)   // 11 AM check-out
            );
            reservation.setStayPeriod(stayPeriod);
        }
        
        reservation.setStatus(dto.getStatus() != null ? dto.getStatus() : com.reservations.reservation_system.enums.ReservationStatus.PENDING);
        reservation.setPartyMembers(dto.getPartyMembers());
        reservation.setPartySize(dto.getPartySize());
        reservation.setNotes(dto.getNotes());
        
        // Handle vehicle info
        if (dto.getVehicleLicensePlate() != null || dto.getVehicleMake() != null || 
            dto.getVehicleModel() != null || dto.getRvLengthFeet() != null) {
            VehicleInfo vehicleInfo = new VehicleInfo(
                dto.getVehicleLicensePlate(),
                dto.getVehicleMake(),
                dto.getVehicleModel(),
                dto.getRvLengthFeet()
            );
            reservation.setVehicleInfo(vehicleInfo);
        }
        
        // Handle money amounts
        if (dto.getCampsiteTotal() != null) {
            reservation.setCampsiteTotal(new Money(dto.getCampsiteTotal(), "USD"));
        }
        if (dto.getAtvTotal() != null) {
            reservation.setAtvTotal(new Money(dto.getAtvTotal(), "USD"));
        }
        if (dto.getTotalAmount() != null) {
            reservation.setTotalAmount(new Money(dto.getTotalAmount(), "USD"));
        }
        if (dto.getPaidAmount() != null) {
            reservation.setPaidAmount(new Money(dto.getPaidAmount(), "USD"));
        }
        
        return reservation;
    }
    
    private String generateConfirmationNumber() {
        // Generate a unique confirmation number with format: RV + current timestamp + random 3 digits
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 900) + 100; // 3-digit random number (100-999)
        return "RV" + timestamp + random;
    }
}