package com.reservations.reservation_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewController {
    
    @GetMapping("/")
    public String bookingPage(Model model) {
        model.addAttribute("title", "Book Your Stay - Pine Ridge RV Park");
        model.addAttribute("content", "booking/index");
        return "booking/index";
    }
    
    @GetMapping("/staff")
    public String staffDashboard(HttpSession session, Model model) {

        if (!AuthController.isAuthenticated(session)) {
            return "redirect:/staff/login";
        }
        
        model.addAttribute("title", "Staff Dashboard - Pine Ridge RV Park");
        model.addAttribute("content", "staff/dashboard");
        model.addAttribute("staffUsername", session.getAttribute("staffUsername"));
        return "staff/dashboard";
    }
    
    @GetMapping("/reservations/lookup")
    public String reservationLookup(Model model) {
        model.addAttribute("title", "Find Your Reservation - Pine Ridge RV Park");
        model.addAttribute("content", "reservations/lookup");
        return "reservations/lookup";
    }
}