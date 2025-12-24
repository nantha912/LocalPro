package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import com.LocalService.lsp.service.ProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "*") // Ensure frontend can access these endpoints
public class ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @Autowired
    private ProviderRepository repository;

    @Autowired
    private ProviderService providerService;

    /**
     * Search providers by service and/or location.
     * This endpoint is required for the SearchResultsPage.jsx frontend.
     * * NOTE: To resolve the 'Ambiguous mapping' error, ensure that the
     * redundant 'ProviderSearchController.java' file is DELETED from your project.
     */
    @GetMapping("/search")
    public List<Provider> searchProviders(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String location) {
        logger.info("Search Request Received - Service: '{}', Location: '{}'", service, location);
        return providerService.search(service, location);
    }

    /**
     * Fetch a single provider by their unique ID.
     * This handles the request from your Profile and Details pages.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(@PathVariable String id) {
        logger.info("Request received to fetch provider with ID: {}", id);

        Optional<Provider> providerOpt = repository.findById(id);

        if (providerOpt.isPresent()) {
            Provider provider = providerOpt.get();
            logger.info("Found provider: {} for ID: {}", provider.getName(), id);
            return ResponseEntity.ok(provider);
        } else {
            logger.warn("Provider not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if a customer is a provider by searching for a Provider record
     * that matches the given customerId.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Provider> getProviderByCustomerId(@PathVariable String customerId) {
        logger.info("Checking if Customer ID {} has an associated Provider profile", customerId);

        // We use Optional<?> to handle potential type inference issues from the repository
        // and then cast it explicitly to Provider.
        Optional<?> providerOpt = repository.findByCustomerId(customerId);

        if (providerOpt.isPresent()) {
            Object result = providerOpt.get();
            if (result instanceof Provider) {
                Provider provider = (Provider) result;
                logger.info("Customer {} is a provider with Provider ID: {}", customerId, provider.getId());
                return ResponseEntity.ok(provider);
            }
        }

        logger.info("Customer {} is not a provider yet.", customerId);
        return ResponseEntity.notFound().build();
    }

    /**
     * Save or Update a provider profile.
     */
    @PostMapping
    public Provider saveProvider(@RequestBody Provider provider) {
        logger.info("Request received to save provider: {}", provider.getName());
        Provider savedProvider = repository.save(provider);
        logger.info("Provider saved successfully with ID: {}", savedProvider.getId());
        return savedProvider;
    }
}