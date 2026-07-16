package com.bankapp.service;

import com.bankapp.db.ConnectionPool;
import com.bankapp.model.Account;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.service.UserService.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles fund transfers between accounts.
 * Both debit and credit are executed atomically in a single JDBC transaction.
 */
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private final AccountRepository accountRepo = new AccountRepository();
    private final TransactionRepository txnRepo = new TransactionRepository();

    /**
     * Transfers amountCents from sourceAccountId to the account identified by targetAccountNumber.
     * Returns the new balance of the source account on success.
     */
    public ServiceResult<Long> transfer(int sourceAccountId, String targetAccountNumber,
                                        long amountCents, String description, String sourceAccountType) {
        if (amountCents <= 0) return ServiceResult.fail("Amount must be greater than zero.");
        if (targetAccountNumber == null || targetAccountNumber.isBlank())
            return ServiceResult.fail("Please enter a target account number.");

        // Resolve target account
        Optional<Account> targetOpt = accountRepo.findByAccountNumber(targetAccountNumber.trim());
        if (targetOpt.isEmpty())
            return ServiceResult.fail("Account number not found: " + targetAccountNumber.trim());

        Account target = targetOpt.get();
        if (target.getAccountId() == sourceAccountId)
            return ServiceResult.fail("Cannot transfer to the same account.");

        // Daily limit check (reuse same limits as withdrawal)
        long dailyLimit = "Savings".equalsIgnoreCase(sourceAccountType) ? 500_000L : 2_000_000L;
        if (amountCents > dailyLimit)
            return ServiceResult.fail(String.format(
                    "Amount exceeds your %s account daily limit ($%.2f).", sourceAccountType, dailyLimit / 100.0));

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                conn.createStatement().execute("BEGIN IMMEDIATE");

                boolean debited = accountRepo.withdraw(conn, sourceAccountId, amountCents);
                if (!debited) {
                    conn.rollback();
                    long available = accountRepo.getBalance(sourceAccountId);
                    return ServiceResult.fail(String.format(
                            "Insufficient funds. Available: $%.2f | Requested: $%.2f",
                            available / 100.0, amountCents / 100.0));
                }

                accountRepo.deposit(conn, target.getAccountId(), amountCents);

                String desc = description == null || description.isBlank()
                        ? "Transfer to " + targetAccountNumber.trim()
                        : description;
                txnRepo.log(conn, sourceAccountId, "Transfer", amountCents, desc);
                txnRepo.log(conn, target.getAccountId(), "Transfer", amountCents,
                        "Transfer from AC" + sourceAccountId);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                log.error("Transfer failed: src={} dst={}", sourceAccountId, targetAccountNumber, e);
                return ServiceResult.fail("Transfer failed. Please try again.");
            }
        } catch (SQLException e) {
            log.error("Connection error during transfer", e);
            return ServiceResult.fail("A connection error occurred. Please try again.");
        }

        long newBalance = accountRepo.getBalance(sourceAccountId);
        log.info("Transfer: src={} dst={} amount={}", sourceAccountId, targetAccountNumber, amountCents);
        return ServiceResult.ok(newBalance);
    }
}
