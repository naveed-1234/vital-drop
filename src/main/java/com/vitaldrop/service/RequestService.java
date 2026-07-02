package com.vitaldrop.service;

import com.vitaldrop.entity.Donor;
import com.vitaldrop.entity.Request;
import com.vitaldrop.repository.DonorRepository;
import com.vitaldrop.repository.RecipientRepository;
import com.vitaldrop.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vitaldrop.dto.RequestDetailsDTO;
import com.vitaldrop.entity.Recipient;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private EmailService emailService; // Integrated for automated notification lifecycles

    public Request sendRequest(Request request) {

        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() ->
                        new RuntimeException("Donor not found"));

        Recipient recipient = recipientRepository.findById(request.getRecipientId())
                .orElseThrow(() ->
                        new RuntimeException("Recipient not found"));

        if (!donor.isAvailability()) {
            throw new RuntimeException("Donor is currently unavailable.");
        }

        List<Request> existingRequests =
                requestRepository.findByDonorId(request.getDonorId());

        for (Request existing : existingRequests) {

            if (existing.getRecipientId().equals(request.getRecipientId())
                    && existing.getStatus().equals("PENDING")) {

                throw new RuntimeException(
                        "You have already sent a request to this donor.");
            }
        }

        request.setRequestDate(LocalDateTime.now());
        request.setStatus("PENDING");

        Request savedRequest = requestRepository.save(request);

        // TRIGGER 1: Inform Donor of incoming pending match
        String emailBody = "Hello " + donor.getFullName() + ",\n\n" +
                "You have received a new blood request from " + recipient.getFullName() + " located in " + donor.getCity() + ".\n" +
                "Please log into your Vital Drop dashboard to review and manage this request.\n\n" +
                "Thank you,\nVital Drop Organization";

        emailService.sendEmail(donor.getEmail(), "Urgent: New Blood Request Received", emailBody);

        return savedRequest;
    }

    public List<Request> getDonorRequests(Long donorId) {
        return requestRepository.findByDonorId(donorId);
    }

    public List<Request> getRecipientRequests(Long recipientId) {
        return requestRepository.findByRecipientId(recipientId);
    }

    public Request acceptRequest(Long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        request.setStatus("ACCEPTED");

        Donor donor = donorRepository.findById(
                        request.getDonorId())
                .orElseThrow(() ->
                        new RuntimeException("Donor not found"));

        Recipient recipient = recipientRepository.findById(
                        request.getRecipientId())
                .orElseThrow(() ->
                        new RuntimeException("Recipient not found"));

        donor.setAvailability(false);
        donorRepository.save(donor);

        Request savedRequest = requestRepository.save(request);

        // TRIGGER 2: Inform Recipient that request has been ACCEPTED
        String emailBody = "Great news " + recipient.getFullName() + ",\n\n" +
                "Your blood request (ID: " + requestId + ") has been ACCEPTED by donor " + donor.getFullName() + ".\n" +
                "The donor is preparing for allocation dispatch. You can track this in your dashboard.\n\n" +
                "Thank you,\nVital Drop Organization";

        emailService.sendEmail(recipient.getEmail(), "Update: Blood Request Accepted!", emailBody);

        return savedRequest;
    }

    public Request rejectRequest(Long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        request.setStatus("REJECTED");

        Recipient recipient = recipientRepository.findById(
                        request.getRecipientId())
                .orElseThrow(() ->
                        new RuntimeException("Recipient not found"));

        Request savedRequest = requestRepository.save(request);

        // TRIGGER 3: Inform Recipient that request has been REJECTED
        String emailBody = "Hello " + recipient.getFullName() + ",\n\n" +
                "We regret to inform you that your blood request (ID: " + requestId + ") was declined by the selected donor.\n" +
                "Please return to the search directory console to find other matches available in your area.\n\n" +
                "Thank you,\nVital Drop Organization";

        emailService.sendEmail(recipient.getEmail(), "Update: Blood Request Status Update", emailBody);

        return savedRequest;
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    public List<RequestDetailsDTO> getAllRequestDetails() {

        List<Request> requests = requestRepository.findAll();
        List<RequestDetailsDTO> requestDetails = new ArrayList<>();

        for (Request request : requests) {
            requestDetails.add(convertToDTO(request));
        }

        return requestDetails;
    }

    public Request completeRequest(Long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        if (!request.getStatus().equals("ACCEPTED")) {
            throw new RuntimeException(
                    "Only accepted requests can be completed");
        }

        request.setStatus("COMPLETED");
        Request savedRequest = requestRepository.save(request);

        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() ->
                        new RuntimeException("Donor not found"));

        // Direct download URL configuration mapping
        String targetCertificateLink = "http://localhost:8080/certificate/download/" + requestId;

        // TRIGGER 4: Send deep gratitude email along with credentials access link
        String emailBody = "Thank you, " + donor.getFullName() + "!\n\n" +
                "Your voluntary donation transaction has been marked as COMPLETED. " +
                "Your selfless act has directly contributed toward saving a life and strengthening our community.\n\n" +
                "In recognition of your generosity, your official Certificate of Appreciation has been generated successfully.\n" +
                "You can download your document at any time by clicking the secure link below:\n" +
                targetCertificateLink + "\n\n" +
                "With utmost gratitude,\nVital Drop Organization Core";

        emailService.sendEmail(donor.getEmail(), "Thank You for Saving a Life! - Your Certificate is Ready", emailBody);

        return savedRequest;
    }

    public List<Request> getDonationHistory(Long donorId) {
        return requestRepository.findByDonorIdAndStatus(
                donorId,
                "COMPLETED"
        );
    }

    public List<Request> getRecipientHistory(Long recipientId) {
        return requestRepository.findByRecipientIdAndStatus(
                recipientId,
                "COMPLETED"
        );
    }

    public Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));
    }

    /**
     * Reusable private helper to transform a single Request entity into a populated RequestDetailsDTO
     */
    private RequestDetailsDTO convertToDTO(Request request) {
        Donor donor = donorRepository.findById(request.getDonorId()).orElse(null);
        Recipient recipient = recipientRepository.findById(request.getRecipientId()).orElse(null);

        RequestDetailsDTO dto = new RequestDetailsDTO();
        dto.setRequestId(request.getRequestId());
        dto.setRequestDate(request.getRequestDate());
        dto.setStatus(request.getStatus());

        dto.setDonorName(donor != null ? donor.getFullName() : "Unknown");
        dto.setRecipientName(recipient != null ? recipient.getFullName() : "Unknown");
        dto.setBloodGroup(donor != null ? donor.getBloodGroup() : "N/A");
        dto.setCity(donor != null ? donor.getCity() : "N/A");

        return dto;
    }

    public List<RequestDetailsDTO> getDonorRequestsDetailed(Long donorId) {
        List<Request> requests = requestRepository.findByDonorId(donorId);
        List<RequestDetailsDTO> dtoList = new ArrayList<>();
        for (Request req : requests) {
            dtoList.add(convertToDTO(req));
        }
        return dtoList;
    }

    public List<RequestDetailsDTO> getRecipientRequestsDetailed(Long recipientId) {
        List<Request> requests = requestRepository.findByRecipientId(recipientId);
        List<RequestDetailsDTO> dtoList = new ArrayList<>();
        for (Request req : requests) {
            dtoList.add(convertToDTO(req));
        }
        return dtoList;
    }

    public List<RequestDetailsDTO> getDonationHistoryDetailed(Long donorId) {
        List<Request> requests = requestRepository.findByDonorIdAndStatus(donorId, "COMPLETED");
        List<RequestDetailsDTO> dtoList = new ArrayList<>();
        for (Request req : requests) {
            dtoList.add(convertToDTO(req));
        }
        return dtoList;
    }

    public List<RequestDetailsDTO> getRecipientHistoryDetailed(Long recipientId) {
        List<Request> requests = requestRepository.findByRecipientIdAndStatus(recipientId, "COMPLETED");
        List<RequestDetailsDTO> dtoList = new ArrayList<>();
        for (Request req : requests) {
            dtoList.add(convertToDTO(req));
        }
        return dtoList;
    }
}