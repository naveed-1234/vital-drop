package com.vitaldrop.service;

import com.vitaldrop.dto.AdminStats;
import com.vitaldrop.entity.Admin;
import com.vitaldrop.repository.AdminRepository;
import com.vitaldrop.repository.DonorRepository;
import com.vitaldrop.repository.RecipientRepository;
import com.vitaldrop.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AdminService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Admin login(
            String username,
            String password) {

        Admin admin =
                adminRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid Username"));

        if (!passwordEncoder.matches(
                password,
                admin.getPassword())) {

            throw new RuntimeException(
                    "Invalid Password");
        }

        return admin;
    }

    public long getTotalDonors() {
        return donorRepository.count();
    }

    public long getTotalRecipients() {
        return recipientRepository.count();
    }

    public long getAvailableDonors() {
        return donorRepository.countByAvailabilityTrue();
    }

    public long getPendingRequests() {
        return requestRepository.countByStatus("PENDING");
    }

    public long getAcceptedRequests() {
        return requestRepository.countByStatus("ACCEPTED");
    }

    public AdminStats getDashboardStats() {

        AdminStats stats = new AdminStats();

        stats.setTotalDonors(
                donorRepository.count());

        stats.setTotalRecipients(
                recipientRepository.count());

        stats.setAvailableDonors(
                donorRepository.countByAvailabilityTrue());

        stats.setPendingRequests(
                requestRepository.countByStatus("PENDING"));

        stats.setAcceptedRequests(
                requestRepository.countByStatus("ACCEPTED"));

        return stats;
    }
}