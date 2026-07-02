package com.vitaldrop.service;

import com.vitaldrop.repository.DonorRepository;
import com.vitaldrop.repository.RecipientRepository;
import com.vitaldrop.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PublicStatsService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private RequestRepository requestRepository;

    /**
     * Aggregates real-time platform operational metrics directly from database row counts
     */
    public Map<String, Object> getPlatformMetrics() {
        Map<String, Object> stats = new HashMap<>();

        long totalDonors = donorRepository.count();
        long totalRecipients = recipientRepository.count();
        stats.put("totalDonors", totalDonors);
        stats.put("totalRecipients", totalRecipients);

        long completedCount = requestRepository.findAll().stream()
                .filter(request -> "COMPLETED".equals(request.getStatus()))
                .count();
        stats.put("completedRequests", completedCount);

        // NEW: Calculate the distribution of blood groups automatically
        Map<String, Long> groupDistribution = new HashMap<>();
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        for (String group : bloodGroups) {
            long count = donorRepository.findAll().stream()
                    .filter(donor -> group.equals(donor.getBloodGroup()))
                    .count();
            groupDistribution.put(group, count);
        }
        stats.put("bloodGroups", groupDistribution);

        return stats;
    }


}