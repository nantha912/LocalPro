package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import com.LocalService.lsp.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

//    @GetMapping("/search")
//    public ResponseEntity<List<Provider>> searchProviders(
//            @RequestParam(required = false) String service,
//            @RequestParam(required = false) String location) {
//
//        List<Provider> providers = providerService.search(service, location);
//        return ResponseEntity.ok(providers);
//    }

}

