package com.vitaldrop.service;

import com.vitaldrop.entity.Donor;
import com.vitaldrop.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class DonorService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpCacheManager otpCacheManager; // Swapped out database repository for our new In-Memory Cache Manager

    public Donor registerDonor(Donor donor) {


        // Encrypt password before saving
        donor.setPassword(
                passwordEncoder.encode(donor.getPassword())
        );

        // Clear the token upon successful account creation pipeline verification transition
        otpCacheManager.clearOtp(donor.getEmail());

        return donorRepository.save(donor);
    }

    public Donor loginDonor(String email, String password) {

        Donor donor = donorRepository.findByEmail(email);

        if (donor == null) {
            throw new RuntimeException("Email not registered");
        }

        if (!passwordEncoder.matches(
                password,
                donor.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        return donor;
    }

    public Donor toggleAvailability(Long id) {

        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        donor.setAvailability(!donor.isAvailability());

        return donorRepository.save(donor);
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public List<Donor> getAvailableDonorsByBloodGroup(
            String bloodGroup) {

        return donorRepository
                .findByBloodGroupAndAvailabilityTrue(
                        bloodGroup);
    }

    public List<Donor> searchAvailableDonors(
            String bloodGroup,
            String city
    ) {

        System.out.println("Searching for: " + bloodGroup);

        if (city == null || city.trim().isEmpty()) {

            return donorRepository
                    .findByBloodGroupAndAvailabilityTrue(
                            bloodGroup.trim()
                    );
        }

        return donorRepository
                .findByBloodGroupAndCityAndAvailabilityTrue(
                        bloodGroup.trim(),
                        city.trim()
                );
    }

    public void resetPassword(String email, String newPassword) {

        Donor donor = donorRepository.findByEmail(email);

        if (donor == null) {
            throw new RuntimeException("Email not registered");
        }

        donor.setPassword(passwordEncoder.encode(newPassword));
        donorRepository.save(donor); // Clean JPA standard save operation override instead of custom update queries
    }

    public void deleteDonor(Long donorId) {
        donorRepository.deleteById(donorId);
    }

    public Donor getDonorById(Long donorId) {
        return donorRepository.findById(donorId)
                .orElseThrow(() ->
                        new RuntimeException("Donor not found"));
    }

    // Add this method inside your DonorService class
    public List<Donor> searchDonorsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return donorRepository.findAll();
        }
        return donorRepository.findByFullNameContainingIgnoreCase(name.trim());
    }
}