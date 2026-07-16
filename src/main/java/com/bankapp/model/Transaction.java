package com.bankapp.model;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private int accountId;
    private String type;
    private long amountCents;
    private String description;
    private LocalDateTime timestamp;

    public Transaction() {}
    public Transaction(int transactionId, int accountId, String type, long amountCents, String description, LocalDateTime timestamp) {
        this.transactionId = transactionId; this.accountId = accountId; this.type = type;
        this.amountCents = amountCents; this.description = description; this.timestamp = timestamp;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private int transactionId; private int accountId; private String type;
        private long amountCents; private String description; private LocalDateTime timestamp;
        public Builder transactionId(int v) { transactionId = v; return this; }
        public Builder accountId(int v) { accountId = v; return this; }
        public Builder type(String v) { type = v; return this; }
        public Builder amountCents(long v) { amountCents = v; return this; }
        public Builder description(String v) { description = v; return this; }
        public Builder timestamp(LocalDateTime v) { timestamp = v; return this; }
        public Transaction build() { return new Transaction(transactionId, accountId, type, amountCents, description, timestamp); }
    }
}
