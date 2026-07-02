package com.vitaldrop.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmailService {

    // 1. Paste your Brevo API key here
    private final String BREVO_API_KEY = "xsmtpsib-00d04f059a3eeed2172a64cf5a80759fdb0bd9c29223b50f1fb11e1515cc2a3a-2W6ArJxST4idoBLZ";
    private final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendEmail(String toEmail, String subject, String body) {
        RestTemplate restTemplate = new RestTemplate();

        // Set the headers required by Brevo's API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", BREVO_API_KEY);

        // Build the JSON payload structure
        Map<String, Object> requestBody = new HashMap<>();

        // Sender info (Must be the email address you used to register on Brevo)
        Map<String, String> sender = new HashMap<>();
        sender.put("name", "VitalDrop Admin");
        sender.put("email", "nm197374@gmail.com");
        requestBody.put("sender", sender);

        // Recipient info
        Map<String, String> toUser = new HashMap<>();
        toUser.put("email", toEmail);
        requestBody.put("to", Collections.singletonList(toUser));

        // Subject and plain text content
        requestBody.put("subject", subject);
        requestBody.put("textContent", body);

        // Compile and send the request over port 443
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, entity, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("OTP email bypassed firewall and delivered successfully via HTTP API!");
            }
        } catch (Exception e) {
            System.err.println("Brevo API delivery failed: " + e.getMessage());
            throw new RuntimeException("Email delivery failed: " + e.getMessage());
        }
    }

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}