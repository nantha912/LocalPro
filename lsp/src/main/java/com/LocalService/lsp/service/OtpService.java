package com.LocalService.lsp.service;

import com.LocalService.lsp.model.OtpRecord;
import com.LocalService.lsp.repository.OtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired private OtpRepository otpRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JavaMailSender mailSender;

    /**
     * Generates and sends a 6-digit OTP.
     * Enforces rate limiting (1/60s, 3/hr, 10/day).
     */
    public void generateAndSendOtp(String email) {
        Optional<OtpRecord> existing = otpRepository.findByEmail(email);

        if (existing.isPresent()) {
            OtpRecord record = existing.get();
            // Check Rate Limit: 60 seconds
            if (record.getLastSentAt() != null &&
                    ChronoUnit.SECONDS.between(record.getLastSentAt(), LocalDateTime.now()) < 60) {
                throw new IllegalStateException("Please wait 60 seconds before requesting a new OTP.");
            }
            // Check Daily Limit: 10
            if (record.getDailyCount() >= 10 &&
                    record.getLastSentAt().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
                throw new IllegalStateException("Daily OTP limit reached. Try again tomorrow.");
            }
        }

        String otp = String.format("%06d", secureRandom.nextInt(999999));
        String hashedOtp = passwordEncoder.encode(otp);

        OtpRecord record = existing.orElse(new OtpRecord());
        record.setEmail(email);
        record.setHashedOtp(hashedOtp);
        record.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        record.setAttemptCount(0);
        record.setLastSentAt(LocalDateTime.now());
        record.setDailyCount(record.getDailyCount() + 1);

        otpRepository.save(record);

        sendEmail(email, otp);
        logger.info("OTP generated and sent to: {}", email);
    }

    private void sendEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@taraas.com");
        message.setTo(to);
        message.setSubject("Your OTP for Taraas");
        message.setText("Your One-Time Password (OTP) is " + otp + ".\n\n" +
                "This OTP is valid for 5 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n" +
                "— Team Taraas");
        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String userOtp) {
        OtpRecord record = otpRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No OTP requested for this email."));

        if (LocalDateTime.now().isAfter(record.getExpiryTime())) {
            throw new IllegalStateException("OTP has expired. Please request a new one.");
        }

        if (record.getAttemptCount() >= 5) {
            otpRepository.deleteByEmail(email);
            throw new IllegalStateException("Too many failed attempts. Request a new OTP.");
        }

        if (passwordEncoder.matches(userOtp, record.getHashedOtp())) {
            otpRepository.deleteByEmail(email); // Single-use requirement
            return true;
        } else {
            record.setAttemptCount(record.getAttemptCount() + 1);
            otpRepository.save(record);
            return false;
        }
    }
}