package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Transaction;
import com.LocalService.lsp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    // 1. Initiate Transaction (When user clicks "Pay")
    @PostMapping("/initiate")
    public ResponseEntity<Transaction> initiateTransaction(@RequestBody Transaction transaction) {
        transaction.setStatus("INITIATED");
        transaction.setCreatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.ok(saved);
    }

    // 2. Customer Confirms Payment (After returning from UPI app)
    @PutMapping("/{id}/confirm-payment")
    public ResponseEntity<Transaction> confirmPayment(@PathVariable String id) {
        Optional<Transaction> tx = transactionRepository.findById(id);
        if (tx.isPresent()) {
            Transaction t = tx.get();
            t.setStatus("CUSTOMER_CONFIRMED");
            return ResponseEntity.ok(transactionRepository.save(t));
        }
        return ResponseEntity.notFound().build();
    }

    // 3. Provider Verifies Receipt (Provider Dashboard)
    @PutMapping("/{id}/verify")
    public ResponseEntity<Transaction> verifyTransaction(@PathVariable String id) {
        Optional<Transaction> tx = transactionRepository.findById(id);
        if (tx.isPresent()) {
            Transaction t = tx.get();
            t.setStatus("COMPLETED");
            return ResponseEntity.ok(transactionRepository.save(t));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * NEW: Fetch all transactions for a specific customer.
     * This is used by the CustomerProfilePage.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCustomer(@PathVariable String customerId) {
        List<Transaction> list = transactionRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(list);
    }
}