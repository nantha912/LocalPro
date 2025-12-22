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

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @Autowired
    private ProviderRepository repository;

    @Autowired
    private ProviderService providerService;

    @PostMapping
    public Provider saveProvider(@RequestBody Provider provider) {
        logger.info("Request received to save provider: {}", provider.getName());
        Provider savedProvider = repository.save(provider);
        logger.info("Provider saved successfully with ID: {}", savedProvider.getId());
        return savedProvider;
    }
}