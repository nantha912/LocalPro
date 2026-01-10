package com.LocalService.lsp.service;

import com.LocalService.lsp.dto.ProviderSearchResultDTO;
import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.ProviderRepository;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProviderService - City-Centric Ranking Engine
 * Updates:
 * 1. Standardized Search: Now specifically targets the 'city' field for geographic filtering.
 * 2. Mode Separation: Logic splits cleanly between NEARBY (Proximity + Trust) and REMOTE (Global Trust).
 * 3. Aggregation Logic: Combines GeoJSON coordinates with relational trust data (Reviews/Transactions).
 */
@Service
public class ProviderService {

    private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * SEARCH ENGINE v2.1: City-Centric Mode Support
     * FIXED: Replaced 'location' with 'city' to align with model field standardization.
     */
    public List<ProviderSearchResultDTO> searchWithRanking(String service, Double lat, Double lon, String city, String mode) {
        logger.info("Executing City-Centric Search -> Mode: {}, Service: {}, City: {}", mode, service, city);

        List<AggregationOperation> operations = new ArrayList<>();
        boolean isRemoteMode = "REMOTE".equalsIgnoreCase(mode);

        // 1. ELIGIBILITY & PROXIMITY FILTERING
        if (!isRemoteMode && lat != null && lon != null) {
            // NEARBY MODE (GPS available): Use $geoNear for distance ranking
            Document geoQuery = new Document("serviceDeliveryType", new Document("$in", Arrays.asList("LOCAL", "HYBRID")));
            if (service != null && !service.isBlank()) {
                geoQuery.append("serviceCategory", new Document("$regex", service.trim()).append("$options", "i"));
            }
            // Optional: If a city is also provided, restrict geo search to that city's boundaries
            if (city != null && !city.isBlank()) {
                geoQuery.append("city", new Document("$regex", city.trim()).append("$options", "i"));
            }

            operations.add(new CustomAggregationOperation(new Document("$geoNear", new Document()
                    .append("near", new Document("type", "Point").append("coordinates", Arrays.asList(lon, lat)))
                    .append("distanceField", "dist.calculated")
                    .append("maxDistance", 50000) // 50km radius
                    .append("spherical", true)
                    .append("query", geoQuery)
            )));
        } else {
            // REMOTE MODE or NEARBY FALLBACK (Text-based search)
            Criteria criteria = new Criteria();
            if (isRemoteMode) {
                criteria.and("serviceDeliveryType").in("REMOTE", "HYBRID");
            } else {
                criteria.and("serviceDeliveryType").in("LOCAL", "HYBRID");
                // Match against the saved 'city' field specifically
                if (city != null && !city.isBlank()) {
                    criteria.and("city").regex(city.trim(), "i");
                }
            }

            if (service != null && !service.isBlank()) {
                criteria.and("serviceCategory").regex(service.trim(), "i");
            }
            operations.add(Aggregation.match(criteria));
        }

        // 2. JOIN TRUST DATA (Reviews & Transactions)
        operations.add(Aggregation.addFields().addFieldWithValue("idStr", ConvertOperators.ToString.toString("$_id")).build());
        operations.add(Aggregation.lookup("reviews", "idStr", "providerId", "rawReviews"));
        operations.add(Aggregation.lookup("transactions", "idStr", "providerId", "rawTransactions"));

        // 3. METRIC CALCULATIONS
        operations.add(Aggregation.addFields()
                .addFieldWithValue("reviewCount", ArrayOperators.Size.lengthOfArray("rawReviews"))
                .addFieldWithValue("averageRating", new Document("$ifNull", Arrays.asList(new Document("$avg", "$rawReviews.rating"), 0.0)))
                .addFieldWithValue("completedOrders", ArrayOperators.Size.lengthOfArray(
                        ArrayOperators.Filter.filter("rawTransactions")
                                .as("tx").by(ComparisonOperators.Eq.valueOf("tx.status").equalToValue("COMPLETED"))))
                .build());

        // 4. WEIGHTED SCORING
        Document scoringFormula = isRemoteMode ?
                // Remote Scoring: Focus on Ratings (40%) and Volume (30%)
                new Document("$add", Arrays.asList(
                        new Document("$multiply", Arrays.asList("$averageRating", 8)),
                        new Document("$min", Arrays.asList(new Document("$multiply", Arrays.asList("$completedOrders", 0.3)), 30)),
                        20
                )) :
                // Nearby Scoring: Focus on Proximity (30%) and Ratings (30%)
                new Document("$add", Arrays.asList(
                        new Document("$multiply", Arrays.asList("$averageRating", 6)),
                        new Document("$min", Arrays.asList(new Document("$multiply", Arrays.asList("$completedOrders", 0.2)), 20)),
                        new Document("$cond", Arrays.asList(new Document("$lt", Arrays.asList("$dist.calculated", 5000)), 30, 10)),
                        20
                ));

        operations.add(Aggregation.addFields().addFieldWithValue("searchScore", scoringFormula).build());

        // 5. FINAL SORT & CLEANUP
        operations.add(Aggregation.sort(Sort.Direction.DESC, "searchScore"));
        operations.add(Aggregation.project().andExclude("rawReviews", "rawTransactions", "idStr"));
        operations.add(Aggregation.addFields().addFieldWithValue("id", ConvertOperators.ToString.toString("$_id")).build());

        return mongoTemplate.aggregate(Aggregation.newAggregation(operations), "providers", ProviderSearchResultDTO.class).getMappedResults();
    }

    /**
     * Custom aggregation class to support complex MongoDB documents like $geoNear
     */
    private static class CustomAggregationOperation implements AggregationOperation {
        private final Document document;
        public CustomAggregationOperation(Document document) { this.document = document; }
        @Override public Document toDocument(AggregationOperationContext context) { return document; }
    }
}