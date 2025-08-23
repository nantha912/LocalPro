package com.LocalService.lsp.repository;

import com.LocalService.lsp.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

    /**
     * Finds a customer by their email address.
     * @param email The email to search for.
     * @return An Optional containing the customer if found.
     */
    Optional<Customer> findByEmail(String email);

}
