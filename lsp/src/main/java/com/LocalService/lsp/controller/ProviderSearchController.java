package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderSearchController {

    @Autowired
    private ProviderRepository providerRepository;

    @GetMapping("/search")
    public List<Provider> searchProviders(@RequestParam(required = false) String service,
                                          @RequestParam(required = false) String location) {

        // 1. If both are empty, return all providers
        if ((service == null || service.isBlank()) && (location == null || location.isBlank())) {
            return providerRepository.findAll();
        }

        // 2. If both service and location are provided
        if (service != null && !service.isBlank() && location != null && !location.isBlank()) {
            return providerRepository.findByServiceCategoryAndLocation(service, location);
        }

        // 3. If only location is provided
        else if (location != null && !location.isBlank()) {
            return providerRepository.findByLocation(location);
        }

        // 4. If only service is provided
        else {
            return providerRepository.findByServiceCategory(service);
        }
    }
}