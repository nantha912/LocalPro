package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import com.LocalService.lsp.service.ProviderService;
import com.LocalService.lsp.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "*") // Critical: Allows your React frontend to communicate with these endpoints
public class ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @Autowired
    private ProviderRepository repository;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private S3Service s3Service;

    /**
     * SEARCH: Consolidated search endpoint.
     * Handles fuzzy matching for services and locations.
     * Note: Delete 'ProviderSearchController.java' after implementing this to avoid conflicts.
     */
    @GetMapping("/search")
    public List<Provider> searchProviders(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String location) {
        logger.info("Search Request: service='{}', location='{}'", service, location);
        return providerService.search(service, location);
    }

    /**
     * READ: Fetch a single provider by unique ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(@PathVariable String id) {
        logger.info("Fetching provider details for ID: {}", id);
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * READ: Check if a Customer ID already has a registered Provider profile.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Provider> getProviderByCustomerId(@PathVariable String customerId) {
        logger.info("Checking provider profile for Customer ID: {}", customerId);
        return repository.findByCustomerId(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * CREATE / UPDATE: Save profile data.
     * Handles the new List<String> serviceCategory automatically via JSON mapping.
     */
    @PostMapping
    public Provider saveProvider(@RequestBody Provider provider) {
        logger.info("Request to save/update provider: {}", provider.getName());
        // MongoDB automatically updates if the payload contains an 'id'
        return repository.save(provider);
    }

    /**
     * PHOTO: Upload/Update the main Profile Photo (Avatar).
     */
    @PostMapping("/{id}/profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        Optional<Provider> providerOpt = repository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Provider not found");

        Provider provider = providerOpt.get();

        try {
            // Remove previous photo from S3 to save space/costs
            if (provider.getProfilePhotoUrl() != null) {
                s3Service.deleteFile(provider.getProfilePhotoUrl());
            }

            // Upload new file to the 'avatars' folder
            String photoUrl = s3Service.uploadFile(file, id, "avatars");
            provider.setProfilePhotoUrl(photoUrl);
            repository.save(provider);

            logger.info("Profile photo updated for: {}", id);
            return ResponseEntity.ok(provider);
        } catch (IOException e) {
            logger.error("Avatar upload failed for provider {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload photo.");
        }
    }

    /**
     * PORTFOLIO: Upload a new portfolio photo.
     */
    @PostMapping("/{id}/photos")
    public ResponseEntity<?> uploadPortfolioPhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        Optional<Provider> providerOpt = repository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.notFound().build();

        Provider provider = providerOpt.get();

        // Limit check
        if (provider.getPortfolioPhotos().size() >= 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Portfolio limit (10) reached.");
        }

        try {
            String photoUrl = s3Service.uploadFile(file, id, "portfolio");
            provider.getPortfolioPhotos().add(photoUrl);
            repository.save(provider);
            return ResponseEntity.ok(provider);
        } catch (IOException e) {
            logger.error("Portfolio upload failed: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed.");
        }
    }

    /**
     * PORTFOLIO: Delete a specific portfolio photo.
     */
    @DeleteMapping("/{id}/photos")
    public ResponseEntity<?> deletePortfolioPhoto(
            @PathVariable String id,
            @RequestParam String url) {

        Optional<Provider> providerOpt = repository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.notFound().build();

        Provider provider = providerOpt.get();

        if (provider.getPortfolioPhotos().remove(url)) {
            s3Service.deleteFile(url);
            repository.save(provider);
            return ResponseEntity.ok(provider);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Photo URL not found in portfolio.");
    }
}