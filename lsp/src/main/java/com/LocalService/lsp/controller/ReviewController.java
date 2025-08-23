package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Review;
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

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/{providerId}/reviews")
    public List<Review> getReviews(@PathVariable String providerId) {
        return reviewRepository.findByProviderId(providerId);
    }

    @PostMapping("/{providerId}/reviews")
    public Review addReview(@PathVariable String providerId, @RequestBody Review review) {
        review.setProviderId(providerId);
        return reviewRepository.save(review);
    }
}

