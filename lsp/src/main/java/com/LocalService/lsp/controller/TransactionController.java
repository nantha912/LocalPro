package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Transaction;
import com.LocalService.lsp.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * SSE Registry: Maps User IDs to lists of emitters.
     * Thread-safe collection to support multiple concurrent sessions (multi-tab testing).
     */
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * SSE Stream Endpoints
     * These allow the React frontend (EventSource) to "subscribe" to transaction updates.
     */
    @GetMapping(value = "/customer/{customerId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamToCustomer(@PathVariable String customerId) {
        logger.info("Stream req received from customer");
        SseEmitter emitter = new SseEmitter(1800_000L); // 30-minute timeout
        addEmitter(customerId, emitter);
        emitter.onCompletion(() -> removeEmitter(customerId, emitter));
        emitter.onTimeout(() -> removeEmitter(customerId, emitter));
        return emitter;
    }

    @GetMapping(value = "/provider/{providerId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamToProvider(@PathVariable String providerId) {
        logger.info("Stream req received from customer");
        SseEmitter emitter = new SseEmitter(1800_000L);
        addEmitter(providerId, emitter);
        emitter.onCompletion(() -> removeEmitter(providerId, emitter));
        emitter.onTimeout(() -> removeEmitter(providerId, emitter));
        return emitter;
    }

    private void addEmitter(String id, SseEmitter emitter) {
        emitters.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>()).add(emitter);
        logger.info("SSE Session opened for: {}. Total Active: {}", id, emitters.get(id).size());
    }

    private void removeEmitter(String id, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(id);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) emitters.remove(id);
        }
    }

    /**
     * Real-time Broadcast: Pushes the updated transaction object to the relevant parties.
     */
    private void broadcast(Transaction tx) {
        sendUpdate(tx.getProviderId(), tx);
        sendUpdate(tx.getCustomerId(), tx);
    }

    private void sendUpdate(String id, Transaction tx) {
        List<SseEmitter> userEmitters = emitters.get(id);
        if (userEmitters != null) {
            for (SseEmitter emitter : userEmitters) {
                try {
                    // event name "PAYMENT_UPDATE" must match frontend .addEventListener('PAYMENT_UPDATE')
                    emitter.send(SseEmitter.event().name("PAYMENT_UPDATE").data(tx));
                } catch (IOException e) {
                    removeEmitter(id, emitter);
                }
            }
        }
    }

    @PostMapping("/initiate")
    public ResponseEntity<Transaction> initiate(@RequestBody Transaction transaction) {
        logger.info("payment initiate request received");
        transaction.setStatus("INITIATED");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setBilled(false);
        Transaction saved = transactionRepository.save(transaction);
        broadcast(saved);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/confirm-payment")
    public ResponseEntity<Transaction> confirmPayment(@PathVariable String id) {
        return transactionRepository.findById(id).map(t -> {
            t.setStatus("CUSTOMER_CONFIRMED");
            Transaction saved = transactionRepository.save(t);
            broadcast(saved); // Triggers Provider Alert
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<Transaction> verifyTransaction(@PathVariable String id) {
        return transactionRepository.findById(id).map(t -> {
            t.setStatus("COMPLETED");
            Transaction saved = transactionRepository.save(t);
            broadcast(saved); // Triggers Customer Handshake/Review Popup
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}/reject")
    public ResponseEntity<Transaction> rejectTransaction(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        return transactionRepository.findById(id).map(t -> {
            t.setStatus("REJECTED");

            // Optional: store rejection reason (future-proof)
            if (payload != null && payload.containsKey("reason")) {
                t.setRejectionReason(payload.get("reason"));
            }

            Transaction saved = transactionRepository.save(t);
            broadcast(saved); // 🔔 notify customer in real-time
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/customer/{customerId}")
    public List<Transaction> getByCustomer(@PathVariable String customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    @GetMapping("/provider/{providerId}")
    public List<Transaction> getByProvider(@PathVariable String providerId) {
        return transactionRepository.findByProviderId(providerId);
    }
}