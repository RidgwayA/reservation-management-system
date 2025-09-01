package com.reservations.reservation_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    
    @GetMapping("/")
    public String bookingPage(Model model) {
        model.addAttribute("title", "Book Your Stay - Pine Ridge RV Park");
        model.addAttribute("content", "booking/index");
        return "booking/index";
    }
    
    @GetMapping("/staff")
    public String staffDashboard(Model model) {
        model.addAttribute("title", "Staff Dashboard - Pine Ridge RV Park");
        model.addAttribute("content", "staff/dashboard");
        return "staff/dashboard";
    }
    
    @GetMapping("/reservations/lookup")
    public String reservationLookup(Model model) {
        model.addAttribute("title", "Find Your Reservation - Pine Ridge RV Park");
        model.addAttribute("content", "reservations/lookup");
        return "reservations/lookup";
    }
}