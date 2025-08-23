package com.LocalService.lsp.service;

import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    public List<Provider> search(String service, String location) {
        // For now, we'll just search by service. Location can be added later.
        if (service != null && !service.trim().isEmpty()) {
            return providerRepository.findByServiceCategory(service);
        }
        // Return an empty list if no search term is provided
        return Collections.emptyList();
    }
}