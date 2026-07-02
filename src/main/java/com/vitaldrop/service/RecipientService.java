package com.vitaldrop.service;

import com.vitaldrop.entity.Recipient;
import com.vitaldrop.repository.RecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class RecipientService {

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpCacheManager otpCacheManager; // Swapped out database repository for our new In-Memory Cache Manager

    public Recipient registerRecipient(Recipient recipient) {


        recipient.setPassword(
                passwordEncoder.encode(
                        recipient.getPassword()
                )
        );

        // Clear token upon successful account creation pipeline transition
        otpCacheManager.clearOtp(recipient.getEmail());

        return recipientRepository.save(recipient);
    }

    public Recipient loginRecipient(
            String email,
            String password) {

        Recipient recipient =
                recipientRepository.findByEmail(email);

        if (recipient == null) {
            throw new RuntimeException(
                    "Recipient not found");
        }

        if(!passwordEncoder.matches(
                password,
                recipient.getPassword())){

            throw new RuntimeException("Invalid Password");
        }

        return recipient;
    }

    public void resetPassword(String email, String newPassword) {

        Recipient recipient = recipientRepository.findByEmail(email);

        if (recipient == null) {
            throw new RuntimeException("Email not registered");
        }

        recipient.setPassword(passwordEncoder.encode(newPassword));
        recipientRepository.save(recipient); // Clean JPA standard save operation override
    }

    public List<Recipient> getAllRecipients() {
        return recipientRepository.findAll();
    }

    public void deleteRecipient(Long recipientId) {
        recipientRepository.deleteById(recipientId);
    }

    // Add this method inside your RecipientService class
    public List<Recipient> searchRecipientsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return recipientRepository.findAll();
        }
        return recipientRepository.findByFullNameContainingIgnoreCase(name.trim());
    }
}