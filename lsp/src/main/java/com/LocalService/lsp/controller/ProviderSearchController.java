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
    public List<Provider> searchProviders(@RequestParam(required = false) String area,
                                          @RequestParam(required = false) String query) {
        if ((area == null || area.isBlank()) && (query == null || query.isBlank())) {
            return providerRepository.findAll();
        }

        if (area != null && !area.isBlank() && query != null && !query.isBlank()) {
            return providerRepository.findByCityIgnoreCaseContainingAndProfessionIgnoreCaseContaining(area, query);
        } else if (area != null && !area.isBlank()) {
            return providerRepository.findByCityIgnoreCaseContaining(area);
        } else {
            return providerRepository.findByProfessionIgnoreCaseContaining(query);
        }
    }
}
