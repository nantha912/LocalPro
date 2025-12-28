package com.LocalService.lsp.repository;

import com.LocalService.lsp.model.Provider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends MongoRepository<Provider, String> {

    /**
     * Fuzzy search for service category (case-insensitive partial match).
     * Used by search(service, null) in ProviderService.
     */
    List<Provider> findByServiceCategoryContainingIgnoreCase(String serviceCategory);

    /**
     * Fuzzy search for location (case-insensitive partial match).
     * Used by search(null, location) in ProviderService.
     */
    List<Provider> findByLocationContainingIgnoreCase(String location);

    /**
     * Combined fuzzy search for both service and location.
     * Used by search(service, location) in ProviderService.
     */
    List<Provider> findByServiceCategoryContainingIgnoreCaseAndLocationContainingIgnoreCase(String serviceCategory, String location);

    /**
     * Exact match for work type.
     * Used by findByWorkType(workType) in ProviderService.
     */
    List<Provider> findByWorkType(String workType);

    /**
     * Finds a provider profile associated with a specific Customer ID.
     * Critical for onboarding checks and profile redirection.
     */
    Optional<Provider> findByCustomerId(String customerId);
}