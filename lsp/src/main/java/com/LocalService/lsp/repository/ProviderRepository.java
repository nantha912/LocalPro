package com.LocalService.lsp.repository;

import com.LocalService.lsp.model.Provider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProviderRepository extends MongoRepository<Provider, String> {

    List<Provider> findByCityIgnoreCaseContaining(String city);

    // This query uses a regex for a case-insensitive search on the 'serviceCategory' field.
    @Query("{ 'serviceCategory': { $regex: ?0, $options: 'i' } }")
    List<Provider> findByServiceCategory(String service);

    List<Provider> findByProfessionIgnoreCaseContaining(String profession);

    List<Provider> findByCityIgnoreCaseContainingAndProfessionIgnoreCaseContaining(String city, String profession);
}
