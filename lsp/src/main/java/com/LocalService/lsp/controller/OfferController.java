package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Offer;
import com.LocalService.lsp.model.Provider;
import com.LocalService.lsp.repository.OfferRepository;
import com.LocalService.lsp.repository.ProviderRepository;
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

    @Autowired
    private ProviderRepository providerRepository;

    /**
     * Provider: Create or Update an offer.
     * Snapshot provider details for fast read (NO joins).
     */
    @PostMapping
    public ResponseEntity<Offer> saveOffer(@RequestBody Offer offer) {

        if (offer.getCreatedAt() == null) {
            offer.setCreatedAt(LocalDateTime.now());
        }

        // 🔹 Fetch provider
        Provider provider = providerRepository
                .findById(offer.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // 🔹 Snapshot provider data (IMPORTANT)
        offer.setProviderName(provider.getName());
        offer.setProviderProfilePhoto(provider.getProfilePhotoUrl());
        offer.setServiceCategory(provider.getServiceCategory()); // List<String>

        // ✅ NEW (safe additions)
        offer.setLocation(provider.getCity()); // auto attach provider city
        offer.setServiceDeliveryType(provider.getServiceDeliveryType()); // LOCAL / REMOTE / HYBRID

        return ResponseEntity.ok(offerRepository.save(offer));
    }

    /**
     * Provider: Get all offers created by this provider.
     */
    @GetMapping("/provider/{providerId}")
    public List<Offer> getOffersByProvider(@PathVariable String providerId) {
        return offerRepository.findByProviderId(providerId);
    }

    /**
     * Buyer: Fetch eligible offers for a provider profile.
     * Rule: buyerCategory.rank >= offer.minCategory.rank
     */
    @GetMapping("/provider/{providerId}/eligible")
    public List<Offer> getEligibleOffers(
            @PathVariable String providerId,
            @RequestParam Offer.BuyerCategory category) {

        LocalDateTime now = LocalDateTime.now();

        return offerRepository.findByProviderIdAndIsActiveTrue(providerId)
                .stream()
                .filter(o -> category.getRank() >= o.getMinCategory().getRank())
                .filter(o -> o.getStartDate() == null || o.getStartDate().isBefore(now))
                .filter(o -> o.getEndDate() == null || o.getEndDate().isAfter(now))
                .collect(Collectors.toList());
    }

    /**
     * Public: Fetch all active offers (View Offers page)
     * Supports optional category & location filters
     */
    @GetMapping("/active")
    public List<Offer> getActiveOffers(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location
    ) {
        LocalDateTime now = LocalDateTime.now();

        return offerRepository.findByIsActiveTrue()
                .stream()

                // 🔹 Date validity
                .filter(o -> o.getStartDate() == null || !o.getStartDate().isAfter(now))
                .filter(o -> o.getEndDate() == null || !o.getEndDate().isBefore(now))

                // 🔹 Category filter (List<String>)
                .filter(o -> {
                    if (category == null || category.isBlank()) return true;
                    if (o.getServiceCategory() == null) return false;

                    return o.getServiceCategory()
                            .stream()
                            .anyMatch(c -> c.equalsIgnoreCase(category));
                })

                // 🔹 Location filter (string contains)
                .filter(o -> {
                    if (location == null || location.isBlank()) return true;
                    if (o.getLocation() == null) return false;

                    return o.getLocation()
                            .toLowerCase()
                            .contains(location.toLowerCase());
                })

                // 🔹 Featured offers first
                .sorted((a, b) -> Boolean.compare(b.isFeatured(), a.isFeatured()))

                .collect(Collectors.toList());
    }

    /**
     * Provider: Delete an offer
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOffer(@PathVariable String id) {
        offerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
