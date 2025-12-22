package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderSearchController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderSearchController.class);

    @Autowired
    private ProviderRepository providerRepository;

    @GetMapping("/search")
    public List<Provider> searchProviders(@RequestParam(required = false) String service,
                                          @RequestParam(required = false) String location,
                                          @RequestParam(required = false) String workType) {

        logger.info("Search Request Received - Service: '{}', Location: '{}', WorkType: '{}'", service, location, workType);

        List<Provider> results;

        // 1. If all are empty, return all providers
        if ((service == null || service.isBlank()) &&
                (location == null || location.isBlank()) &&
                (workType == null || workType.isBlank())) {

            logger.info("No filters provided. Returning all providers.");
            results = providerRepository.findAll();
        }
        // Logic for WorkType filter (if present, prioritize it or filter by it)
        else if (workType != null && !workType.isBlank()) {
            // Note: Ideally combine with other filters, but sticking to your current logic structure
            logger.info("Filtering by WorkType: {}", workType);
            results = providerRepository.findByWorkType(workType);
        }
        // 2. If both service and location are provided
        else if (service != null && !service.isBlank() && location != null && !location.isBlank()) {
            logger.info("Filtering by Service AND Location");
            results = providerRepository.findByServiceCategoryAndLocation(service, location);
        }
        // 3. If only location is provided
        else if (location != null && !location.isBlank()) {
            logger.info("Filtering by Location only");
            results = providerRepository.findByLocation(location);
        }
        // 4. If only service is provided
        else {
            logger.info("Filtering by Service only");
            results = providerRepository.findByServiceCategory(service);
        }

        logger.info("Search completed. Found {} providers.", results.size());
        return results;
    }

    // ... keep other methods like getProviderById ...
}