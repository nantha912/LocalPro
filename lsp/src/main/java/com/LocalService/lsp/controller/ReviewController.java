package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Review;
import com.LocalService.lsp.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews") // Updated base path for better consistency
@CrossOrigin(origins = "*")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Fetch all reviews for a specific provider (used on Provider Profile page)
     */
    @GetMapping("/provider/{providerId}")
    public List<Review> getReviewsByProvider(@PathVariable String providerId) {
        logger.info("Fetching reviews for providerId: {}", providerId);
        return reviewRepository.findByProviderId(providerId);
    }

    /**
     * NEW: Fetch all reviews written by a specific customer (used on Customer Profile page)
     */
    @GetMapping("/customer/{customerId}")
    public List<Review> getReviewsByCustomer(@PathVariable String customerId) {
        logger.info("Fetching reviews for customerId: {}", customerId);
        return reviewRepository.findByCustomerId(customerId);
    }

    /**
     * Submit a new review
     */
    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        logger.info("Received request to add review for provider: {}", review.getProviderId());

        // Ensure timestamp is set
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // FIXED: Now correctly logging the generated Review ID
        logger.info("Review saved successfully. ID: {}", savedReview.getId());

        return ResponseEntity.ok(savedReview);
    }

    /**
     * Legacy support for the /{providerId}/reviews path if needed by older components
     */
    @PostMapping("/provider/{providerId}")
    public Review addReviewLegacy(@PathVariable String providerId, @RequestBody Review review) {
        review.setProviderId(providerId);
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }
}