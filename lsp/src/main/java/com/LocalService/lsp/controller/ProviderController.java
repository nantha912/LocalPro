package com.LocalService.lsp.controller;

import com.LocalService.lsp.dto.ProviderSearchResultDTO;
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
@CrossOrigin(origins = "*")
public class ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @Autowired
    private ProviderRepository repository;

    @Autowired
    private ProviderService providerService;

    /**
     * Search providers with aggregated metrics (Review count, Orders completed).
     * Returns the DTO to include calculated fields.
     */
    @GetMapping("/search")
    public List<ProviderSearchResultDTO> searchProviders(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String location) {
        logger.info("Aggregated Search Request: service='{}', location='{}'", service, location);

        // Calls the corrected aggregation service
        return providerService.searchWithMetrics(service, location);
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
}