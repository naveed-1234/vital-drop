package com.vitaldrop.controller;

import com.vitaldrop.dto.RequestDetailsDTO;
import com.vitaldrop.entity.Request;
import com.vitaldrop.service.RequestService;
import com.vitaldrop.service.PublicStatsService; // 1. IMPORT THIS NEW SERVICE
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Import Map

@RestController
@RequestMapping("/requests")
@CrossOrigin(origins = "*")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private PublicStatsService publicStatsService; // 2. INJECT THE STATS SERVICE

    // ==========================================================================
    // ADDED: THE MISSING LANDING PAGE PUBLIC DATA PORT
    // ==========================================================================
    // CHANGE: Update return type signature from Map<String, Long> to Map<String, Object>
    @GetMapping("/public/stats")
    public Map<String, Object> getPublicStats() {
        return publicStatsService.getPlatformMetrics();
    }

    @PostMapping("/send")
    public Request sendRequest(@RequestBody Request request) {
        return requestService.sendRequest(request);
    }

    @GetMapping("/donor/{donorId}")
    public List<RequestDetailsDTO> getDonorRequests(@PathVariable Long donorId) {
        return requestService.getDonorRequestsDetailed(donorId);
    }

    @GetMapping("/recipient/{recipientId}")
    public List<RequestDetailsDTO> getRecipientRequests(@PathVariable Long recipientId) {
        return requestService.getRecipientRequestsDetailed(recipientId);
    }

    @PutMapping("/accept/{requestId}")
    public Request acceptRequest(@PathVariable Long requestId) {
        return requestService.acceptRequest(requestId);
    }

    @PutMapping("/reject/{requestId}")
    public Request rejectRequest(@PathVariable Long requestId) {
        return requestService.rejectRequest(requestId);
    }

    @GetMapping("/all")
    public List<Request> getAllRequests() {
        return requestService.getAllRequests();
    }

    @GetMapping("/details")
    public List<RequestDetailsDTO> getAllRequestDetails() {
        return requestService.getAllRequestDetails();
    }

    @PutMapping("/complete/{requestId}")
    public Request completeRequest(@PathVariable Long requestId) {
        return requestService.completeRequest(requestId);
    }

    @GetMapping("/history/donor/{donorId}")
    public List<RequestDetailsDTO> donationHistory(@PathVariable Long donorId) {
        return requestService.getDonationHistoryDetailed(donorId);
    }

    @GetMapping("/history/recipient/{recipientId}")
    public List<RequestDetailsDTO> recipientHistory(@PathVariable Long recipientId) {
        return requestService.getRecipientHistoryDetailed(recipientId);
    }
}