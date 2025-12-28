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
     * Uses fuzzy matching (containing) to ensure better search UX.
     */
    public List<Provider> search(String service, String location) {
        logger.info("Search Layer: service='{}', location='{}'", service, location);

        // Normalize inputs
        String s = (service != null && !service.isBlank()) ? service.trim() : null;
        String l = (location != null && !location.isBlank()) ? location.trim() : null;

        List<Provider> results;

        if (s != null && l != null) {
            logger.info("Executing Combined Fuzzy Search [Service + Location]");
            results = providerRepository.findByServiceCategoryContainingIgnoreCaseAndLocationContainingIgnoreCase(s, l);
        } else if (s != null) {
            logger.info("Executing Fuzzy Search [Service Only]");
            results = providerRepository.findByServiceCategoryContainingIgnoreCase(s);
        } else if (l != null) {
            logger.info("Executing Fuzzy Search [Location Only]");
            results = providerRepository.findByLocationContainingIgnoreCase(l);
        } else {
            logger.info("No filters provided. Returning all providers.");
            results = providerRepository.findAll();
        }

        logger.info("Search complete. Found {} results.", results.size());
        return results;
    }

    public List<Provider> findByWorkType(String workType) {
        if (workType == null || workType.isBlank()) {
            return providerRepository.findAll();
        }
        return providerRepository.findByWorkType(workType);
    }
}