package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import com.LocalService.lsp.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    @Autowired
    private ProviderRepository repository;

    @PostMapping
    public Provider saveProvider(@RequestBody Provider provider) {
        return repository.save(provider);
    }

    @Autowired
    private ProviderService providerService;

    @GetMapping("/{id}")
    public ResponseEntity<Provider> getProviderById(@PathVariable String id) {
        Optional<Provider> provider = repository.findById(id);

        return provider.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }



}

