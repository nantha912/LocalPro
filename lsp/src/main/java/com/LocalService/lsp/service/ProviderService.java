package com.LocalService.lsp.service;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderService {

    private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);

    @Autowired
    private ProviderRepository providerRepository;

    /**
     * Searches for providers using service category and/or location.
     * Uses fuzzy matching (containing) and ignores case sensitivity.
     */
    public List<Provider> search(String service, String location) {
        logger.info("=== Service Search Layer Started ===");
        logger.info("Inputs - service: '{}', location: '{}'", service, location);

        // Normalize inputs: Convert empty strings or whitespace-only strings to null
        String s = (service != null && !service.isBlank()) ? service.trim() : null;
        String l = (location != null && !location.isBlank()) ? location.trim() : null;

        List<Provider> results;

        if (s != null && l != null) {
            logger.info("Executing Combined Fuzzy Search: Service AND Location");
            results = providerRepository.findByServiceCategoryContainingIgnoreCaseAndLocationContainingIgnoreCase(s, l);
        } else if (s != null) {
            logger.info("Executing Fuzzy Search: Service only");
            results = providerRepository.findByServiceCategoryContainingIgnoreCase(s);
        } else if (l != null) {
            logger.info("Executing Fuzzy Search: Location only");
            results = providerRepository.findByLocationContainingIgnoreCase(l);
        } else {
            logger.info("No filters provided. Returning all records from database.");
            results = providerRepository.findAll();
        }

        logger.info("Database Query Result: Found {} providers matching the criteria.", results.size());
        logger.info("=== Service Search Layer Finished ===");

        return results;
    }

    /**
     * Search providers by work type (e.g., Remote, On-site).
     */
    public List<Provider> findByWorkType(String workType) {
        if (workType == null || workType.isBlank()) {
            return providerRepository.findAll();
        }
        return providerRepository.findByWorkType(workType);
    }
}