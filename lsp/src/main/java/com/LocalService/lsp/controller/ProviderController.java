package com.LocalService.lsp.controller;

import com.LocalService.lsp.dto.ProviderSearchResultDTO;
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

/**
 * ProviderController - City-Centric Marketplace Edition
 * Handles professional profile management and the Mode-Aware Search Engine.
 */
@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "*")
public class ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @Autowired
    private ProviderRepository repository;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private S3Service s3Service;

    /**
     * SEARCH: Mode-Aware Weighted Ranking
     */
    @GetMapping("/search")
    public List<ProviderSearchResultDTO> searchProviders(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "NEARBY") String mode) {

        logger.info("Marketplace Search Triggered -> Mode: {}, Service: {}, City: {}, Lat: {}, Lon: {}", mode, service, city);
        return providerService.searchWithRanking(service, lat, lon, city, mode);
    }

    /**
     * FETCH BY ID: Retrieves profile data.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PROFILE PHOTO UPLOAD: Dedicated endpoint for the main provider avatar.
     */
    @PostMapping("/{id}/profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        Optional<Provider> providerOpt = repository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.notFound().build();

        Provider provider = providerOpt.get();

        try {
            // 1. Delete old photo if it exists
            if (provider.getProfilePhotoUrl() != null && !provider.getProfilePhotoUrl().isEmpty()) {
                s3Service.deleteFile(provider.getProfilePhotoUrl());
            }

            // 2. Upload to S3
            String photoUrl = s3Service.uploadFile(file, id, "provider_profile");
            provider.setProfilePhotoUrl(photoUrl);

            // 3. Save the updated provider record
            Provider updated = repository.save(provider);
            logger.info("Profile photo updated for provider: {}", id);
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            logger.error("Photo upload failed for provider {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Photo upload failed.");
        }
    }

    /**
     * PORTFOLIO PHOTO UPLOAD: Adds a new photo to the provider's gallery.
     * Maps to POST /api/providers/{id}/photos
     */
    @PostMapping("/{id}/photos")
    public ResponseEntity<Provider> uploadPortfolioPhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        Optional<Provider> providerOpt = repository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.notFound().build();

        Provider provider = providerOpt.get();

        try {
            // Upload to S3 under 'portfolio' subfolder
            String photoUrl = s3Service.uploadFile(file, id, "portfolio");

            // Add to the list
            provider.getPortfolioPhotos().add(photoUrl);

            Provider updated = repository.save(provider);
            logger.info("Portfolio photo added to provider: {}", id);
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            logger.error("Portfolio upload failed: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PORTFOLIO PHOTO DELETE: Removes a photo from S3 and the provider's list.
     * Maps to DELETE /api/providers/{id}/photos?url=...
     */
    @DeleteMapping("/{id}/photos")
    public ResponseEntity<Provider> deletePortfolioPhoto(
            @PathVariable String id,
            @RequestParam("url") String url) {

        Optional<Provider> providerOpt = repository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.notFound().build();

        Provider provider = providerOpt.get();

        try {
            // 1. Delete from S3 storage
            s3Service.deleteFile(url);

            // 2. Remove from the local list in MongoDB
            provider.getPortfolioPhotos().remove(url);

            Provider updated = repository.save(provider);
            logger.info("Portfolio photo deleted for provider: {}", id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Failed to delete portfolio photo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Provider> getProviderByCustomerId(@PathVariable String customerId) {
        return repository.findByCustomerId(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Provider saveProvider(@RequestBody Provider provider) {
        return repository.save(provider);
    }
}