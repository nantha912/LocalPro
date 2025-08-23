package com.LocalService.lsp.repository;

import com.LocalService.lsp.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProviderId(String providerId);
}

