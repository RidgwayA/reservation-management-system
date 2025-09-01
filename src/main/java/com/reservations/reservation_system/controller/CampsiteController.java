package com.reservations.reservation_system.controller;

import com.reservations.reservation_system.dto.CampsiteDto;
import com.reservations.reservation_system.entity.Campsite;
import com.reservations.reservation_system.enums.CampsiteType;
import com.reservations.reservation_system.enums.CampsiteLocation;
import com.reservations.reservation_system.repository.CampsiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campsites")
@CrossOrigin(origins = "*")
public class CampsiteController {
    
    @Autowired
    private CampsiteRepository campsiteRepository;
    
    @GetMapping
    public List<CampsiteDto> getAllCampsites() {
        return campsiteRepository.findAll()
                .stream()
                .filter(Campsite::getActive)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CampsiteDto> getCampsite(@PathVariable Long id) {
        return campsiteRepository.findById(id)
                .filter(Campsite::getActive)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/available")
    public List<CampsiteDto> getAvailableCampsites() {
        return campsiteRepository.findAvailableCampsites()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/available/type/{siteType}")
    public List<CampsiteDto> getAvailableCampsitesByType(@PathVariable CampsiteType siteType) {
        return campsiteRepository.findAvailableCampsitesByType(siteType)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/available/dates")
    public List<CampsiteDto> getAvailableCampsitesForDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) CampsiteType siteType,
            @RequestParam(required = false) CampsiteLocation location) {
        
        if (siteType != null) {
            return campsiteRepository.findAvailableCampsitesForDateRangeAndType(startDate, endDate, siteType)
                    .stream()
                    .filter(campsite -> location == null || campsite.getLocation() == location)
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return campsiteRepository.findAvailableCampsitesForDateRange(startDate, endDate)
                    .stream()
                    .filter(campsite -> location == null || campsite.getLocation() == location)
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
    }
    
    @GetMapping("/site/{siteNumber}")
    public ResponseEntity<CampsiteDto> getCampsiteBySiteNumber(@PathVariable Integer siteNumber) {
        return campsiteRepository.findBySiteNumber(siteNumber)
                .filter(Campsite::getActive)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/maintenance")
    public ResponseEntity<CampsiteDto> markForMaintenance(@PathVariable Long id, @RequestBody String reason) {
        return campsiteRepository.findById(id)
                .map(campsite -> {
                    campsite.markForMaintenance(reason);
                    Campsite saved = campsiteRepository.save(campsite);
                    return ResponseEntity.ok(convertToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/available")
    public ResponseEntity<CampsiteDto> markAsAvailable(@PathVariable Long id) {
        return campsiteRepository.findById(id)
                .map(campsite -> {
                    campsite.markAsAvailable();
                    Campsite saved = campsiteRepository.save(campsite);
                    return ResponseEntity.ok(convertToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    private CampsiteDto convertToDto(Campsite campsite) {
        CampsiteDto dto = new CampsiteDto();
        dto.setId(campsite.getId());
        dto.setSiteNumber(campsite.getSiteNumber());
        dto.setSiteType(campsite.getSiteType());
        dto.setStatus(campsite.getStatus());
        dto.setLocation(campsite.getLocation());
        dto.setDailyRate(campsite.getDailyRate());
        dto.setMaxPartySize(campsite.getMaxPartySize());
        dto.setNotes(campsite.getNotes());
        return dto;
    }
}