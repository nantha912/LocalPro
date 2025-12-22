package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.LocalService.lsp.repository.ReviewRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/{providerId}/reviews")
    public List<Review> getReviews(@PathVariable String providerId) {
        logger.info("Request received to get reviews for providerId: {}", providerId);
        List<Review> reviews = reviewRepository.findByProviderId(providerId);
        logger.info("Found {} reviews for providerId: {}", reviews.size(), providerId);
        return reviews;
    }

    @PostMapping("/{providerId}/reviews")
    public Review addReview(@PathVariable String providerId, @RequestBody Review review) {
        logger.info("Request received to add review for providerId: {}", providerId);
        review.setProviderId(providerId);
        Review savedReview = reviewRepository.save(review);
        logger.info("Review added successfully for providerId: {}. Review ID: {}", providerId, savedReview.getProviderId()); // Assuming Review has getId()
        return savedReview;
    }
}