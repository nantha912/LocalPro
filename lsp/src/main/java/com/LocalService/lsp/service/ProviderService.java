package com.LocalService.lsp.service;

import com.LocalService.lsp.dto.ProviderSearchResultDTO;
import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProviderService {

    private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Aggregated Search Logic: "Single Trip" Rule
     * This method calculates average reviews and order counts directly in the DB.
     * * FIXED: Resolved compilation error "Target type of a lambda conversion must be an interface"
     * by using the AggregationOperation interface directly for complex $addFields logic.
     */
    public List<ProviderSearchResultDTO> searchWithMetrics(String service, String location) {
        logger.info("Aggregated Search Initiated -> service: '{}', location: '{}'", service, location);

        List<AggregationOperation> operations = new ArrayList<>();

        // 1. BUILD MATCH CRITERIA (Filter providers first to reduce processing load)
        List<Criteria> criteriaList = new ArrayList<>();
        if (service != null && !service.isBlank()) {
            criteriaList.add(Criteria.where("serviceCategory").regex(service.trim(), "i"));
        }
        if (location != null && !location.isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("city").regex(location.trim(), "i"),
                    Criteria.where("location").regex(location.trim(), "i")
            ));
        }
        if (!criteriaList.isEmpty()) {
            operations.add(Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))));
        }

        // 2. PREPARE ID FOR JOINING
        // Convert ObjectId _id to String to match 'providerId' in Reviews/Transactions collections
        operations.add(context -> new Document("$addFields",
                new Document("idStr", new Document("$toString", "$_id"))));

        // 3. LOOKUP REVIEWS
        operations.add(Aggregation.lookup("reviews", "idStr", "providerId", "rawReviews"));

        // 4. CALCULATE REVIEW METRICS
        // FIXED: Using raw AggregationOperation lambda to handle the $ifNull correctly
        operations.add(context -> new Document("$addFields",
                new Document("reviewCount", new Document("$size", "$rawReviews"))
                        .append("averageRating", new Document("$ifNull",
                                List.of(new Document("$avg", "$rawReviews.rating"), 0.0)))));

        // 5. LOOKUP TRANSACTIONS & COUNT COMPLETED ORDERS
        operations.add(Aggregation.lookup("transactions", "idStr", "providerId", "rawTransactions"));

        // Use standard Spring API for order filtering as it doesn't require a lambda
        operations.add(Aggregation.addFields()
                .addFieldWithValue("completedOrders",
                        ArrayOperators.Size.lengthOfArray(
                                ArrayOperators.Filter.filter("rawTransactions")
                                        .as("tx")
                                        .by(ComparisonOperators.Eq.valueOf("tx.status").equalToValue("COMPLETED"))
                        )
                ).build());

        // 6. FINAL MAPPING & CLEANUP
        // Map the internal MongoDB ID to the 'id' field expected by the frontend DTO
        operations.add(context -> new Document("$addFields", new Document("id", new Document("$toString", "$_id"))));

        // Map result to DTO. Fields not in DTO (rawReviews, rawTransactions, idStr) are automatically discarded.
        operations.add(Aggregation.project(ProviderSearchResultDTO.class));

        Aggregation aggregation = Aggregation.newAggregation(operations);

        List<ProviderSearchResultDTO> results = mongoTemplate.aggregate(
                aggregation,
                "providers",
                ProviderSearchResultDTO.class
        ).getMappedResults();

        logger.info("Aggregated search complete. Calculated metrics for {} results.", results.size());
        return results;
    }

    public List<Provider> findByWorkType(String workType) {
        if (workType == null || workType.isBlank()) {
            return providerRepository.findAll();
        }
        return providerRepository.findByWorkType(workType);
    }
}