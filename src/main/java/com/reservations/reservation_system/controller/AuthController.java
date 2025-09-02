package com.reservations.reservation_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/staff")
public class AuthController {
    
    private static final String DEMO_USERNAME = "staff";
    private static final String DEMO_PASSWORD = "demo123";
    
    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (isAuthenticated(session)) {
            return "redirect:/staff";
        }
        return "staff/login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password)) {
            session.setAttribute("staffLoggedIn", true);
            session.setAttribute("staffUsername", username);
            return "redirect:/staff";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "staff/login";
        }
    }
    
    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/staff/login";
    }
    
    public static boolean isAuthenticated(HttpSession session) {
        Boolean loggedIn = (Boolean) session.getAttribute("staffLoggedIn");
        return loggedIn != null && loggedIn;
    }
}