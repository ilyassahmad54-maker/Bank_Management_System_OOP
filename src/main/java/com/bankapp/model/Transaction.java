package com.bankapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    private int transactionId;
    private int accountId;
    private String type; // "Deposit", "Withdrawal", "Transfer"
    private long amountCents; // stored as cents
    private String description;
    private LocalDateTime timestamp;
}
