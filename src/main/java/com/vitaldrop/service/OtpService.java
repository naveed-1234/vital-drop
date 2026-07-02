package com.vitaldrop.service;

import com.vitaldrop.repository.DonorRepository;
import com.vitaldrop.repository.RecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpCacheManager otpCacheManager;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    // NEW: Tracks which emails are fully cleared to complete account registration
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();

    public String sendOtp(String email) {
        if (donorRepository.findByEmail(email) != null) {
            throw new RuntimeException("This email is already registered as a Donor.");
        }
        if (recipientRepository.findByEmail(email) != null) {
            throw new RuntimeException("This email is already registered as a Recipient.");
        }

        String otp = emailService.generateOtp();
        otpCacheManager.storeOtp(email, otp);

        emailService.sendEmail(
                email,
                "Vital Drop Email Verification",
                "Your OTP is: " + otp
        );

        return "OTP Sent Successfully";
    }

    public String verifyOtp(String email, String userOtp) {
        String cachedOtp = otpCacheManager.getOtp(email);

        if (cachedOtp == null) {
            return "No OTP found or code has expired. Please request a new one.";
        }

        if (cachedOtp.equals(userOtp)) {
            // Remove the raw numeric OTP code so it can't be reused
            otpCacheManager.clearOtp(email);

            // CHANGED: Instead of leaving no trace, grant registration clearance to this email!
            verifiedEmails.put(email, true);
            return "OTP Verified Successfully";
        }

        return "Invalid OTP";
    }

    // NEW: Expose a read check for your Donor/Recipient Controllers to check right before saving
    public boolean isEmailVerified(String email) {
        return verifiedEmails.getOrDefault(email, false);
    }

    // NEW: Clear verification clearance immediately after account saving is finalized
    public void clearVerificationStatus(String email) {
        verifiedEmails.remove(email);
    }
}