package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Statement;
import com.LocalService.lsp.repository.StatementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statements")
@CrossOrigin(origins = "*")
public class StatementController {

    @Autowired
    private StatementRepository statementRepository;

    /**
     * Fetch all billing history for a specific provider.
     */
    @GetMapping("/provider/{providerId}")
    public List<Statement> getStatementsByProvider(@PathVariable String providerId) {
        return statementRepository.findByProviderId(providerId);
    }

    /**
     * Mark a statement as paid.
     * In MVP, this would be called after a successful UPI payment to the admin.
     */
    @PutMapping("/{id}/pay")
    public ResponseEntity<Statement> payStatement(@PathVariable String id) {
        return statementRepository.findById(id).map(s -> {
            s.setStatus("PAID");
            s.setPaidAt(LocalDateTime.now());
            return ResponseEntity.ok(statementRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }
}