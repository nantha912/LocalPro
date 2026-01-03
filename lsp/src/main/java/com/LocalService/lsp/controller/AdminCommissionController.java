package com.LocalService.lsp.controller;

import com.LocalService.lsp.service.StatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/commission")
@CrossOrigin(origins = "*")
public class AdminCommissionController {

    @Autowired private StatementService statementService;

    @PostMapping("/calculate")
    public ResponseEntity<?> triggerCalculation(@RequestBody Map<String, Object> payload) {
        String billingMonth = (String) payload.get("billingMonth");
        boolean force = (boolean) payload.getOrDefault("forceRecalculate", false);

        if (billingMonth == null) return ResponseEntity.badRequest().body("billingMonth required (YYYY-MM)");

        statementService.calculateForMonth(billingMonth, force, "ADMIN");
        return ResponseEntity.ok(Map.of("message", "Calculation process initiated"));
    }
}