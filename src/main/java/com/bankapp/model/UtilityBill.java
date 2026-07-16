package com.bankapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityBill {
    private int billId;
    private int userId;
    private String billType; // "Electricity", "Water", "Gas", "Internet"
    private long amountCents; // stored as cents
    private String status;    // "Paid", "Pending"
    private LocalDate dueDate;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
}
