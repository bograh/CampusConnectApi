package com.campusconnect.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PhoneVerificationService {

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void sendVerificationCode(String phoneNumber) {
        String code = generateVerificationCode();
        verificationCodes.put(phoneNumber, code);
        
        scheduler.schedule(() -> verificationCodes.remove(phoneNumber), 15, TimeUnit.MINUTES);
        
        log.info("Verification code for {}: {}", phoneNumber, code);
        
        simulateSmsSending(phoneNumber, code);
    }

    public boolean verifyCode(String phoneNumber, String code) {
        String storedCode = verificationCodes.get(phoneNumber);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodes.remove(phoneNumber);
            return true;
        }
        return false;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private void simulateSmsSending(String phoneNumber, String code) {
        log.info("SMS would be sent to {}: Your CampusConnect verification code is: {}", phoneNumber, code);
    }
}
