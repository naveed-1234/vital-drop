package com.vitaldrop.controller;

import com.vitaldrop.entity.Recipient;
import com.vitaldrop.service.OtpService;
import com.vitaldrop.service.RecipientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipients")
public class RecipientController {

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<?> registerDonor(@RequestBody Recipient recipient) {
        String email = recipient.getEmail();

        // 1. Ask OtpService if this email has completed verification clearance
        if (!otpService.isEmailVerified(email)) {
            return ResponseEntity.badRequest()
                    .body("Email verification token expired or missing. Please verify OTP first.");
        }

        // 2. Save the user if cleared
        Recipient savedRecipient = recipientService.registerRecipient(recipient);

        // 3. Consume the clearance so the registration request can't be maliciously replayed
        otpService.clearVerificationStatus(email);

        return ResponseEntity.ok(savedRecipient);
    }

    @PostMapping("/login")
    public Recipient loginRecipient(
            @RequestParam String email,
            @RequestParam String password) {

        return recipientService
                .loginRecipient(email, password);
    }

    @PutMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String password) {

        recipientService.resetPassword(email, password);

        return "Password updated successfully";
    }

    @GetMapping("/all")
    public List<Recipient> getAllRecipients() {

        return recipientService.getAllRecipients();

    }

    @DeleteMapping("/{recipientId}")
    public String deleteRecipient(
            @PathVariable Long recipientId) {

        recipientService.deleteRecipient(recipientId);

        return "Recipient Deleted Successfully";

    }

    // Add this endpoint inside your RecipientController class
    @GetMapping("/search/name")
    public List<Recipient> searchRecipientsByName(@RequestParam String name) {
        return recipientService.searchRecipientsByName(name);
    }
}