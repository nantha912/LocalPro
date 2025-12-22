package com.LocalService.lsp.service;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProviderService {

    private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);

    @Autowired
    private ProviderRepository providerRepository;

    public List<Provider> search(String service, String location) {
        logger.info("Searching providers with service: '{}' and location: '{}'", service, location);

        // Case 1: Search by Service AND Location
        if (service != null && !service.trim().isEmpty() && location != null && !location.trim().isEmpty()) {
            logger.debug("Executing search by Service AND Location");
            return providerRepository.findByServiceCategoryAndLocation(service, location);
        }

        // Case 2: Search by Service only
        if (service != null && !service.trim().isEmpty()) {
            logger.debug("Executing search by Service only");
            return providerRepository.findByServiceCategory(service);
        }

        // Case 3: Search by Location only
        if (location != null && !location.trim().isEmpty()) {
            logger.debug("Executing search by Location only");
            return providerRepository.findByLocation(location);
        }

        // Case 4: No search terms provided
        logger.warn("No search terms provided. Returning empty list.");
        return Collections.emptyList();
    }
}