package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Customer;
import com.LocalService.lsp.model.LoginRequest;
import com.LocalService.lsp.service.CustomerService;
import com.LocalService.lsp.service.S3Service;
import com.LocalService.lsp.repository.CustomerRepository;
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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Critical for frontend access
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3Service s3Service;

    /**
     * FETCH: Get public profile for a customer.
     */
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable String id) {
        logger.info("Fetching public profile for Customer ID: {}", id);
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setPassword(null); // Security: Hide hashed password
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PHOTO UPLOAD: Dedicated endpoint to handle profile picture uploads to S3.
     * This matches the POST request initiated in the Canvas profile page.
     */
    @PostMapping("/customer/{id}/profile-photo")
    public ResponseEntity<?> uploadCustomerPhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        logger.info("Profile photo upload request for Customer ID: {}", id);

        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        Customer customer = customerOpt.get();

        try {
            // 1. Delete old photo from S3 if it exists to keep storage clean
            if (customer.getProfilePhotoUrl() != null && !customer.getProfilePhotoUrl().isBlank()) {
                s3Service.deleteFile(customer.getProfilePhotoUrl());
            }

            // 2. Upload to S3 under 'customer_avatars' folder
            String photoUrl = s3Service.uploadFile(file, id, "customer_avatars");

            // 3. Persist URL to MongoDB
            customer.setProfilePhotoUrl(photoUrl);
            customerRepository.save(customer);

            logger.info("Successfully uploaded profile photo for customer: {}", id);
            return ResponseEntity.ok(customer);
        } catch (IOException e) {
            logger.error("S3 Upload failed for customer {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process photo upload.");
        }
    }

    /**
     * UPDATE: Profile details (Name, Photo URL, Password Handshake).
     * This method supports the metadata changes in the Edit Profile form.
     */
    @PutMapping("/customer/{id}")
    public ResponseEntity<?> updateCustomerProfile(
            @PathVariable String id,
            @RequestBody Map<String, String> updates) {

        logger.info("Metadata update request for customer ID: {}", id);

        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        Customer customer = customerOpt.get();

        // 1. Update basic info
        if (updates.containsKey("name")) {
            customer.setName(updates.get("name"));
        }

        if (updates.containsKey("profilePhotoUrl")) {
            customer.setProfilePhotoUrl(updates.get("profilePhotoUrl"));
        }

        // 2. Handle Password Change Handshake
        if (updates.get("currentPassword") != null && updates.get("newPassword") != null) {
            String currentPassword = updates.get("currentPassword");
            String newPassword = updates.get("newPassword");

            if (!passwordEncoder.matches(currentPassword, customer.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Current password verification failed."));
            }

            customer.setPassword(passwordEncoder.encode(newPassword));
            logger.info("Password successfully updated for user: {}", id);
        }

        Customer savedCustomer = customerRepository.save(customer);
        savedCustomer.setPassword(null);

        return ResponseEntity.ok(savedCustomer);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        try {
            Customer registeredCustomer = customerService.registerCustomer(customer);
            registeredCustomer.setPassword(null);
            return new ResponseEntity<>(registeredCustomer, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest loginRequest) {
        try {
            Customer customer = customerService.loginCustomer(loginRequest.getEmail(), loginRequest.getPassword());
            customer.setPassword(null);
            return ResponseEntity.ok(customer);
        } catch (UserPrincipalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (CredentialNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred during login.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Healthy");
    }
}