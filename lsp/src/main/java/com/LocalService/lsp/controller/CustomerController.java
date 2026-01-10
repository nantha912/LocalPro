package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Customer;
import com.LocalService.lsp.model.LoginRequest;
import com.LocalService.lsp.model.Offer;
import com.LocalService.lsp.model.Transaction;
import com.LocalService.lsp.repository.CustomerRepository;
import com.LocalService.lsp.repository.TransactionRepository;
import com.LocalService.lsp.service.CustomerService;
import com.LocalService.lsp.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.CredentialNotFoundException;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3Service s3Service;

    /* =====================================================
       🔹 BUYER TIER COMPUTATION (BACKEND SOURCE OF TRUTH)
       ===================================================== */

    private Offer.BuyerCategory calculateBuyerCategory(double totalSpent) {
        if (totalSpent >= 100000) return Offer.BuyerCategory.GOLD;
        if (totalSpent >= 10000) return Offer.BuyerCategory.SILVER;
        if (totalSpent >= 1000) return Offer.BuyerCategory.VERIFIED;
        return Offer.BuyerCategory.NOT_VERIFIED;
    }

    private double calculateTotalSpent(String customerId) {

        // 🔹 Calculate 12 months back from now
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);

        // 🔹 Fetch only COMPLETED transactions within last 12 months
        List<Transaction> completed =
                transactionRepository.findByCustomerIdAndStatusAndCreatedAtAfter(
                        customerId,
                        "COMPLETED",
                        twelveMonthsAgo
                );

        // 🔹 Sum amounts safely
        return completed.stream()
                .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0)
                .sum();
    }


    /* =====================================================
       EXISTING APIs (SAFE EXTENSION ONLY)
       ===================================================== */

    @GetMapping("/customer/exists")
    public ResponseEntity<Boolean> checkIfEmailExists(@RequestParam String email) {
        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * FETCH: Customer profile with computed buyerCategory
     */
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable String id) {
        logger.info("Fetching public profile for Customer ID: {}", id);

        return customerRepository.findById(id)
                .map(customer -> {

                    double totalSpent = calculateTotalSpent(id);
                    Offer.BuyerCategory buyerCategory = calculateBuyerCategory(totalSpent);

                    customer.setPassword(null);

                    return ResponseEntity.ok(
                            Map.of(
                                    "id", customer.getId(),
                                    "name", customer.getName(),
                                    "email", customer.getEmail(),
                                    "profilePhotoUrl", customer.getProfilePhotoUrl(),
                                    "buyerCategory", buyerCategory.name(),
                                    "totalSpent", totalSpent
                            )
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PHOTO UPLOAD
     */
    @PostMapping("/customer/{id}/profile-photo")
    public ResponseEntity<?> uploadCustomerPhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");

        Customer customer = customerOpt.get();

        try {
            if (customer.getProfilePhotoUrl() != null && !customer.getProfilePhotoUrl().isBlank()) {
                s3Service.deleteFile(customer.getProfilePhotoUrl());
            }

            String photoUrl = s3Service.uploadFile(file, id, "customer_avatars");
            customer.setProfilePhotoUrl(photoUrl);
            customerRepository.save(customer);

            return ResponseEntity.ok(customer);
        } catch (IOException e) {
            logger.error("S3 Upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process photo upload.");
        }
    }

    /**
     * UPDATE PROFILE
     */
    @PutMapping("/customer/{id}")
    public ResponseEntity<?> updateCustomerProfile(
            @PathVariable String id,
            @RequestBody Map<String, String> updates) {

        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");

        Customer customer = customerOpt.get();

        if (updates.containsKey("name")) customer.setName(updates.get("name"));
        if (updates.containsKey("profilePhotoUrl")) customer.setProfilePhotoUrl(updates.get("profilePhotoUrl"));

        if (updates.get("currentPassword") != null && updates.get("newPassword") != null) {
            if (!passwordEncoder.matches(updates.get("currentPassword"), customer.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Current password verification failed."));
            }
            customer.setPassword(passwordEncoder.encode(updates.get("newPassword")));
        }

        Customer saved = customerRepository.save(customer);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    /**
     * LOGIN: attach buyerCategory
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest loginRequest) {
        try {
            Customer customer = customerService.loginCustomer(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            double totalSpent = calculateTotalSpent(customer.getId());
            Offer.BuyerCategory buyerCategory = calculateBuyerCategory(totalSpent);

            customer.setPassword(null);

            return ResponseEntity.ok(
                    Map.of(
                            "id", customer.getId(),
                            "name", customer.getName(),
                            "email", customer.getEmail(),
                            "profilePhotoUrl", customer.getProfilePhotoUrl(),
                            "buyerCategory", buyerCategory.name(),
                            "totalSpent", totalSpent
                    )
            );
        } catch (UserPrincipalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (CredentialNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Login failed.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Healthy");
    }
}
