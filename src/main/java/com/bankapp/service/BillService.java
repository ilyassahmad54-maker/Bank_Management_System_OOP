package com.bankapp.service;

import com.bankapp.model.UtilityBill;
import com.bankapp.repository.UtilityBillRepository;
import com.bankapp.service.UserService.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BillService {
    private static final Logger log = LoggerFactory.getLogger(BillService.class);
    private final UtilityBillRepository billRepo = new UtilityBillRepository();

    /**
     * Pays a utility bill. Validates the bill type, amount, and due date.
     */
    public ServiceResult<Void> payBill(int userId, String billType, long amountCents, LocalDate dueDate) {
        if (amountCents <= 0) return ServiceResult.fail("Amount must be greater than zero.");
        if (dueDate == null) return ServiceResult.fail("Please select a due date.");
        if (!isValidBillType(billType)) return ServiceResult.fail("Invalid bill type selected.");

        UtilityBill bill = UtilityBill.builder()
                .userId(userId)
                .billType(billType)
                .amountCents(amountCents)
                .status("Paid")
                .dueDate(dueDate)
                .paymentDate(LocalDateTime.now())
                .build();

        boolean saved = billRepo.insert(bill);
        if (!saved) {
            log.error("Failed to save utility bill for user_id {}", userId);
            return ServiceResult.fail("Bill payment failed. Please try again.");
        }
        log.info("Utility bill paid: user_id={}, type={}, amount={}", userId, billType, amountCents);
        return ServiceResult.ok(null);
    }

    public List<UtilityBill> getBillHistory(int userId) {
        return billRepo.findByUserId(userId);
    }

    private boolean isValidBillType(String billType) {
        return billType != null && (billType.equals("Electricity") || billType.equals("Water")
                || billType.equals("Gas") || billType.equals("Internet"));
    }
}
