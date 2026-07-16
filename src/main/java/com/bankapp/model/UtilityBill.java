package com.bankapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UtilityBill {
    private int billId;
    private int userId;
    private String billType;
    private long amountCents;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;

    public UtilityBill() {}
    public UtilityBill(int billId, int userId, String billType, long amountCents, String status,
                       LocalDate dueDate, LocalDateTime paymentDate, LocalDateTime createdAt) {
        this.billId = billId; this.userId = userId; this.billType = billType; this.amountCents = amountCents;
        this.status = status; this.dueDate = dueDate; this.paymentDate = paymentDate; this.createdAt = createdAt;
    }

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private int billId; private int userId; private String billType; private long amountCents;
        private String status; private LocalDate dueDate; private LocalDateTime paymentDate; private LocalDateTime createdAt;
        public Builder billId(int v) { billId = v; return this; }
        public Builder userId(int v) { userId = v; return this; }
        public Builder billType(String v) { billType = v; return this; }
        public Builder amountCents(long v) { amountCents = v; return this; }
        public Builder status(String v) { status = v; return this; }
        public Builder dueDate(LocalDate v) { dueDate = v; return this; }
        public Builder paymentDate(LocalDateTime v) { paymentDate = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public UtilityBill build() { return new UtilityBill(billId, userId, billType, amountCents, status, dueDate, paymentDate, createdAt); }
    }
}
