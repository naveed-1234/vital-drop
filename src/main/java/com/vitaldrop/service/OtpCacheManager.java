package com.vitaldrop.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpCacheManager {

    private static class OtpData {
        final String token;
        final LocalDateTime expiryTime;

        OtpData(String token, int expiryMinutes) {
            this.token = token;
            // FIXED: Using Java standard .plusMinutes() syntax instead of .addMinutes()
            this.expiryTime = LocalDateTime.now().plusMinutes(expiryMinutes);
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    private final ConcurrentHashMap<String, OtpData> cache = new ConcurrentHashMap<>();

    public void storeOtp(String email, String token) {
        cache.put(email, new OtpData(token, 5)); // 5-minute expiry threshold window
    }

    public String getOtp(String email) {
        OtpData data = cache.get(email);
        if (data == null) return null;

        if (data.isExpired()) {
            cache.remove(email);
            return null;
        }
        return data.token;
    }

    public void clearOtp(String email) {
        cache.remove(email);
    }

    @Scheduled(fixedRate = 60000) // Sweeps memory matrix every 60 seconds to scrub dead keys
    public void periodicCacheCleanup() {
        cache.forEach((email, data) -> {
            if (data.isExpired()) {
                cache.remove(email);
            }
        });
    }
}