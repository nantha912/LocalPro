package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.*;
import com.LocalService.lsp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "*")
public class InsightsController {

    private static final Logger logger = LoggerFactory.getLogger(InsightsController.class);

    @Autowired private ProfileViewRepository viewRepo;
    @Autowired private LeadEventRepository leadRepo;
    @Autowired private TransactionRepository txRepo;

    @PostMapping("/view")
    public ResponseEntity<?> recordView(@RequestBody Map<String, String> payload) {
        String providerId = payload.get("providerId");
        String sessionId = payload.get("sessionId");
        viewRepo.save(new ProfileView(providerId, sessionId));
        return ResponseEntity.ok().build();
    }

    /**
     * Record Lead - DEDUPLICATION LOGIC
     * 1. Checks if a lead from the same customer/method/provider exists within the last hour.
     * 2. If duplicate found, returns 200 OK without saving to keep frontend silent.
     */
    @PostMapping("/lead")
    public ResponseEntity<?> recordLead(@RequestBody LeadEvent lead) {
        String providerId = lead.getProviderId();
        String customerId = lead.getCustomerId();
        String method = lead.getContactMethod();
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // Fetch recent leads for this provider to check for duplicates
        List<LeadEvent> recentLeads = leadRepo.findAllByProviderIdAndTimestampBetween(
                providerId, oneHourAgo, LocalDateTime.now());

        boolean isDuplicate = recentLeads.stream().anyMatch(e ->
                Objects.equals(e.getContactMethod(), method) &&
                        Objects.equals(e.getCustomerId(), customerId)
        );

        if (isDuplicate) {
            logger.info("Duplicate lead ignored for provider: {} (Method: {})", providerId, method);
            return ResponseEntity.ok().build();
        }

        if (lead.getTimestamp() == null) {
            lead.setTimestamp(LocalDateTime.now());
        }

        leadRepo.save(lead);
        logger.info("New unique lead recorded for provider: {} via {}", providerId, method);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<?> getProviderInsights(
            @PathVariable String providerId,
            @RequestParam int year,
            @RequestParam int month) {

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        logger.info("Fetching insights for {} between {} and {}", providerId, start, end);

        // 1. Unique Profile Views
        long views = viewRepo.findAllByProviderIdAndTimestampBetween(providerId, start, end)
                .stream().map(ProfileView::getSessionId).distinct().count();

        // 2. Lead Activity (Sorted: Most Recent First)
        List<LeadEvent> leads = leadRepo.findAllByProviderIdAndTimestampBetween(providerId, start, end)
                .stream()
                .sorted(Comparator.comparing(LeadEvent::getTimestamp).reversed())
                .collect(Collectors.toList());

        // 3. Financials
        List<Transaction> completedTxs = txRepo.findAllByProviderIdAndStatusAndCreatedAtBetween(
                providerId, "COMPLETED", start, end);

        double turnover = completedTxs.stream().mapToDouble(Transaction::getAmount).sum();

        Map<String, Object> response = new HashMap<>();
        response.put("views", views);
        response.put("leadsCount", leads.size());
        response.put("leadsHistory", leads);
        response.put("totalOrders", completedTxs.size());
        response.put("turnover", turnover);

        return ResponseEntity.ok(response);
    }
}