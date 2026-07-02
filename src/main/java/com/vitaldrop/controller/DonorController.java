package com.vitaldrop.controller;

import com.vitaldrop.entity.Donor;
import com.vitaldrop.service.DonorService;
import com.vitaldrop.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donors")
@CrossOrigin(origins = "*")
public class DonorController {

    @Autowired
    private DonorService donorService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<?> registerDonor(@RequestBody Donor donor) {
        String email = donor.getEmail();

        // 1. Ask OtpService if this email has completed verification clearance
        if (!otpService.isEmailVerified(email)) {
            return ResponseEntity.badRequest()
                    .body("Email verification token expired or missing. Please verify OTP first.");
        }

        // 2. Save the user if cleared
        Donor savedDonor = donorService.registerDonor(donor);

        // 3. Consume the clearance so the registration request can't be maliciously replayed
        otpService.clearVerificationStatus(email);

        return ResponseEntity.ok(savedDonor);
    }

    @PostMapping("/login")
    public Donor loginDonor(@RequestParam String email,
                            @RequestParam String password) {

        return donorService.loginDonor(email, password);
    }

    @PutMapping("/availability/{id}")
    public Donor updateAvailability(@PathVariable Long id) {
        return donorService.toggleAvailability(id);
    }

    @GetMapping("/all")
    public List<Donor> getAllDonors() {

        return donorService.getAllDonors();
    }

    @GetMapping("/search")
    public List<Donor> searchDonors(

            @RequestParam String bloodGroup,

            @RequestParam(required = false) String city

    ) {

        return donorService.searchAvailableDonors(
                bloodGroup,
                city
        );
    }

    @PutMapping("/reset-password")
    public String resetPassword(

            @RequestParam String email,

            @RequestParam String password

    ) {

        donorService.resetPassword(email, password);

        return "Password updated successfully";
    }

    @DeleteMapping("/{donorId}")
    public String deleteDonor(
            @PathVariable Long donorId) {

        donorService.deleteDonor(donorId);

        return "Donor Deleted Successfully";
    }

    // Add this endpoint inside your DonorController class
    @GetMapping("/search/name")
    public List<Donor> searchDonorsByName(@RequestParam String name) {
        return donorService.searchDonorsByName(name);
    }
}