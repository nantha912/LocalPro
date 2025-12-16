package com.LocalService.lsp.repository;

import com.LocalService.lsp.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    // Find transactions by provider (for dashboard)
    List<Transaction> findByProviderId(String providerId);

    // Find transactions by customer (for history)
    List<Transaction> findByCustomerId(String customerId);

    // Find completed but unbilled transactions (for admin billing)
    List<Transaction> findByStatusAndBilledFalse(String status);
}
