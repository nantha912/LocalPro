package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Offer;
import com.LocalService.lsp.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/offers")
@CrossOrigin(origins = "*")
public class OfferController {

    @Autowired
    private OfferRepository offerRepository;

    /**
     * Provider: Create or Update an offer.
     */
    @PostMapping
    public ResponseEntity<Offer> saveOffer(@RequestBody Offer offer) {
        if (offer.getCreatedAt() == null) offer.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(offerRepository.save(offer));
    }

    /**
     * Provider: Get all their offers.
     */
    @GetMapping("/provider/{providerId}")
    public List<Offer> getOffersByProvider(@PathVariable String providerId) {
        return offerRepository.findByProviderId(providerId);
    }

    /**
     * Buyer: Fetch eligible offers for a provider profile.
     * Logic: currentCategory.rank >= offer.minCategory.rank
     */
    @GetMapping("/provider/{providerId}/eligible")
    public List<Offer> getEligibleOffers(
            @PathVariable String providerId,
            @RequestParam Offer.BuyerCategory category) {

        List<Offer> activeOffers = offerRepository.findByProviderIdAndIsActiveTrue(providerId);
        LocalDateTime now = LocalDateTime.now();

        return activeOffers.stream()
                .filter(o -> category.getRank() >= o.getMinCategory().getRank())
                .filter(o -> o.getStartDate() == null || o.getStartDate().isBefore(now))
                .filter(o -> o.getEndDate() == null || o.getEndDate().isAfter(now))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOffer(@PathVariable String id) {
        offerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}