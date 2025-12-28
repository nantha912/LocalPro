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
import java.util.ArrayList;

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
     * Search endpoint - delegates to Service layer for fuzzy matching logic.
     */
    @GetMapping("/search")
    public List<Provider> searchProviders(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String location) {
        logger.info("API Search: service={}, location={}", service, location);
        return providerService.search(service, location);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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

    /**
     * Upload a portfolio photo.
     * Includes null-safety fix for getPortfolioPhotos().
     */
    @PostMapping("/{id}/photos")
    public ResponseEntity<?> uploadPortfolioPhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        Optional<Provider> providerOpt = repository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.notFound().build();

        Provider provider = providerOpt.get();

        // Ensure the list is initialized even if the DB record didn't have it
        if (provider.getPortfolioPhotos() == null) {
            provider.setPortfolioPhotos(new ArrayList<>());
        }

        if (provider.getPortfolioPhotos().size() >= 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Limit of 10 photos reached.");
        }

        try {
            String photoUrl = s3Service.uploadFile(file, id);
            provider.getPortfolioPhotos().add(photoUrl);
            repository.save(provider);
            return ResponseEntity.ok(provider);
        } catch (IOException e) {
            logger.error("S3 Upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}/photos")
    public ResponseEntity<Provider> deletePortfolioPhoto(
            @PathVariable String id,
            @RequestParam("url") String photoUrl) {

        Optional<Provider> providerOpt = repository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.notFound().build();

        Provider provider = providerOpt.get();
        if (provider.getPortfolioPhotos() != null && provider.getPortfolioPhotos().remove(photoUrl)) {
            s3Service.deleteFile(photoUrl);
            repository.save(provider);
            return ResponseEntity.ok(provider);
        }
        return ResponseEntity.badRequest().build();
    }
}