package com.bankapp.service;

import com.bankapp.model.Loan;
import com.bankapp.repository.LoanRepository;
import com.bankapp.service.UserService.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LoanService {
    private static final Logger log = LoggerFactory.getLogger(LoanService.class);
    private final LoanRepository loanRepo = new LoanRepository();

    // Preset interest rates per loan type
    public static double getInterestRate(String loanType) {
        return switch (loanType.toLowerCase()) {
            case "home"      -> 6.2;
            case "auto"      -> 7.8;
            case "education" -> 5.5;
            default          -> 8.5; // personal
        };
    }

    /**
     * Calculates the monthly EMI (Equated Monthly Installment) for a loan.
     * EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
     * where P=principal, r=monthly interest rate, n=term in months
     *
     * @param principalCents Principal in cents
     * @param annualRatePct  e.g. 8.5 for 8.5%
     * @param termMonths     Loan term in months
     * @return Monthly EMI in cents
     */
    public static long calculateEmiCents(long principalCents, double annualRatePct, int termMonths) {
        double monthlyRate = annualRatePct / 100.0 / 12.0;
        if (monthlyRate == 0) return principalCents / termMonths;
        double factor = Math.pow(1 + monthlyRate, termMonths);
        double emi = principalCents * monthlyRate * factor / (factor - 1);
        return Math.round(emi);
    }

    public ServiceResult<Void> applyForLoan(int userId, int accountId, String loanType,
                                             long principalCents, int termMonths) {
        if (principalCents < 100_000L)  return ServiceResult.fail("Minimum loan amount is $1,000.");
        if (principalCents > 10_000_000_000L) return ServiceResult.fail("Maximum loan amount is $100,000,000.");
        if (termMonths < 12 || termMonths > 360) return ServiceResult.fail("Loan term must be between 12 and 360 months.");

        double rate = getInterestRate(loanType);
        Loan loan = Loan.builder()
                .userId(userId)
                .accountId(accountId)
                .loanType(loanType)
                .principalAmountCents(principalCents)
                .interestRate(rate)
                .loanTermMonths(termMonths)
                .outstandingBalanceCents(principalCents)
                .status("Under Review")
                .build();

        boolean inserted = loanRepo.insert(loan);
        if (!inserted) {
            log.error("Loan application failed for user_id {}", userId);
            return ServiceResult.fail("Failed to submit your loan application. Please try again.");
        }
        log.info("Loan application submitted for user_id {}, type={}, amount={}", userId, loanType, principalCents);
        return ServiceResult.ok(null);
    }

    public List<Loan> getLoans(int userId) {
        return loanRepo.findByUserId(userId);
    }
}
