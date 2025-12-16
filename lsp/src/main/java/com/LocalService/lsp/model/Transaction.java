package com.LocalService.lsp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String providerId;
    private String customerId;
    private String serviceId;
    private Double amount;
    private String status; // "INITIATED", "CUSTOMER_CONFIRMED", "PROVIDER_VERIFIED", "COMPLETED"
    private LocalDateTime createdAt;
    private String transactionNote; // Order ID or description
    private boolean billed = false;

    public Transaction(String id, String providerId, String customerId, String serviceId, Double amount, String status, LocalDateTime createdAt, String transactionNote, boolean billed) {
        this.id = id;
        this.providerId = providerId;
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.transactionNote = transactionNote;
        this.billed = billed;
    }

    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public Double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getTransactionNote() {
        return transactionNote;
    }
    public boolean isBilled() {
        return billed;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setTransactionNote(String transactionNote) {
        this.transactionNote = transactionNote;
    }
    public void setBilled(boolean billed) {
        this.billed = billed;
    }
}
