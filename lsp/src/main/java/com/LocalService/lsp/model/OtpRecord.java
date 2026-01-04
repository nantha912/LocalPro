package com.LocalService.lsp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * OtpRecord - Secure storage for email verification.
 * Follows "Hashed Storage" requirement.
 */
@Document(collection = "otp_records")
public class OtpRecord {
    @Id
    private String id;
    private String email;
    private String hashedOtp;
    private LocalDateTime expiryTime;
    private int attemptCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Fields for rate limiting
    private int dailyCount = 0;
    private LocalDateTime lastSentAt;

    public OtpRecord() {}

    public OtpRecord(String email, String hashedOtp, LocalDateTime expiryTime) {
        this.email = email;
        this.hashedOtp = hashedOtp;
        this.expiryTime = expiryTime;
        this.lastSentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getHashedOtp() { return hashedOtp; }
    public void setHashedOtp(String hashedOtp) { this.hashedOtp = hashedOtp; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getDailyCount() { return dailyCount; }
    public void setDailyCount(int dailyCount) { this.dailyCount = dailyCount; }
    public LocalDateTime getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(LocalDateTime lastSentAt) { this.lastSentAt = lastSentAt; }
}