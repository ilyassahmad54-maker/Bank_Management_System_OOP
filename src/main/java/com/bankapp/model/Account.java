package com.bankapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    private int accountId;
    private int userId;
    private String accountType; // "Savings" or "Current"
    private String accountNumber;
    private long balanceCents; // stored as integer cents to avoid float precision issues
}
