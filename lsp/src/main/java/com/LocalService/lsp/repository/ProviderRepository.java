package com.LocalService.lsp.repository;

import com.LocalService.lsp.model.Provider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderRepository extends MongoRepository<Provider, String> {

    // Custom query to find providers by service category (case-insensitive)
    @Query("{ 'serviceCategory': { $regex: ?0, $options: 'i' } }")
    List<Provider> findByServiceCategory(String services);

    // Find providers by location (case-insensitive search)
    @Query("{ 'location': { $regex: ?0, $options: 'i' } }")
    List<Provider> findByLocation(String location);

    // Combined search: Service AND Location
    @Query("{ 'serviceCategory': { $regex: ?0, $options: 'i' }, 'location': { $regex: ?1, $options: 'i' } }")
    List<Provider> findByServiceCategoryAndLocation(String serviceCategory, String location);

    // Find by Work Type (e.g., "Remote", "On-Site")
    List<Provider> findByWorkType(String workType);
}