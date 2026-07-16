package com.bankapp.model;

import java.time.LocalDateTime;

public class Loan {
    private int loanId;
    private int userId;
    private int accountId;
    private String loanType;
    private long principalAmountCents;
    private double interestRate;
    private int loanTermMonths;
    private long outstandingBalanceCents;
    private String status;
    private LocalDateTime createdAt;

    public Loan() {}
    public Loan(int loanId, int userId, int accountId, String loanType, long principalAmountCents,
                double interestRate, int loanTermMonths, long outstandingBalanceCents, String status, LocalDateTime createdAt) {
        this.loanId = loanId; this.userId = userId; this.accountId = accountId; this.loanType = loanType;
        this.principalAmountCents = principalAmountCents; this.interestRate = interestRate;
        this.loanTermMonths = loanTermMonths; this.outstandingBalanceCents = outstandingBalanceCents;
        this.status = status; this.createdAt = createdAt;
    }

    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public long getPrincipalAmountCents() { return principalAmountCents; }
    public void setPrincipalAmountCents(long principalAmountCents) { this.principalAmountCents = principalAmountCents; }
    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public int getLoanTermMonths() { return loanTermMonths; }
    public void setLoanTermMonths(int loanTermMonths) { this.loanTermMonths = loanTermMonths; }
    public long getOutstandingBalanceCents() { return outstandingBalanceCents; }
    public void setOutstandingBalanceCents(long outstandingBalanceCents) { this.outstandingBalanceCents = outstandingBalanceCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private int loanId; private int userId; private int accountId; private String loanType;
        private long principalAmountCents; private double interestRate; private int loanTermMonths;
        private long outstandingBalanceCents; private String status; private LocalDateTime createdAt;
        public Builder loanId(int v) { loanId = v; return this; }
        public Builder userId(int v) { userId = v; return this; }
        public Builder accountId(int v) { accountId = v; return this; }
        public Builder loanType(String v) { loanType = v; return this; }
        public Builder principalAmountCents(long v) { principalAmountCents = v; return this; }
        public Builder interestRate(double v) { interestRate = v; return this; }
        public Builder loanTermMonths(int v) { loanTermMonths = v; return this; }
        public Builder outstandingBalanceCents(long v) { outstandingBalanceCents = v; return this; }
        public Builder status(String v) { status = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public Loan build() { return new Loan(loanId, userId, accountId, loanType, principalAmountCents, interestRate, loanTermMonths, outstandingBalanceCents, status, createdAt); }
    }
}
