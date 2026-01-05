package com.LocalService.lsp.controller;

import com.LocalService.lsp.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/otp")
@CrossOrigin(origins = "*")
public class OtpController {

    @Autowired private OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            otpService.generateAndSendOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        try {
            boolean isValid = otpService.verifyOtp(email, otp);
            if (isValid) {
                return ResponseEntity.ok(Map.of("message", "Verification successful."));
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid OTP code."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }
}