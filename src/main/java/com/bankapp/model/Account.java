package com.bankapp.model;

public class Account {
    private int accountId;
    private int userId;
    private String accountType;
    private String accountNumber;
    private long balanceCents;

    public Account() {}
    public Account(int accountId, int userId, String accountType, String accountNumber, long balanceCents) {
        this.accountId = accountId; this.userId = userId; this.accountType = accountType;
        this.accountNumber = accountNumber; this.balanceCents = balanceCents;
    }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public long getBalanceCents() { return balanceCents; }
    public void setBalanceCents(long balanceCents) { this.balanceCents = balanceCents; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private int accountId; private int userId; private String accountType;
        private String accountNumber; private long balanceCents;
        public Builder accountId(int v) { accountId = v; return this; }
        public Builder userId(int v) { userId = v; return this; }
        public Builder accountType(String v) { accountType = v; return this; }
        public Builder accountNumber(String v) { accountNumber = v; return this; }
        public Builder balanceCents(long v) { balanceCents = v; return this; }
        public Account build() { return new Account(accountId, userId, accountType, accountNumber, balanceCents); }
    }
}
