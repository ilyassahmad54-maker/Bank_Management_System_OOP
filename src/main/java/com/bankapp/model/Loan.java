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
public class Loan {
    private int loanId;
    private int userId;
    private int accountId;
    private String loanType;            // "Personal", "Home", "Auto", "Education"
    private long principalAmountCents;  // stored as cents
    private double interestRate;        // e.g. 8.5
    private int loanTermMonths;
    private long outstandingBalanceCents; // stored as cents
    private String status;              // "Applied", "Under Review", "Approved", "Disbursed"
    private LocalDateTime createdAt;
}
