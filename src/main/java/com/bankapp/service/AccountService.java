package com.bankapp.service;

import com.bankapp.db.ConnectionPool;
import com.bankapp.model.Account;
import com.bankapp.model.Transaction;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.service.UserService.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Handles all account operations: creation, deposit, withdrawal, and transaction history.
 * All balance-changing operations run inside explicit JDBC transactions with BEGIN IMMEDIATE
 * to prevent concurrent modification issues (SQLite uses file-level locking).
 */
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    // Daily withdrawal limit in cents by account type
    private static final long SAVINGS_DAILY_LIMIT_CENTS = 500_000L;   // $5,000
    private static final long CURRENT_DAILY_LIMIT_CENTS = 2_000_000L; // $20,000
    // Large transaction threshold requiring extra confirmation
    private static final long LARGE_TXN_THRESHOLD_CENTS = 100_000L;   // $1,000

    private final AccountRepository accountRepo = new AccountRepository();
    private final TransactionRepository txnRepo = new TransactionRepository();

    /**
     * Creates a new bank account for a user.
     * Account number format: AC<userId>_<timestamp>
     */
    public ServiceResult<Account> createAccount(int userId, String accountType) {
        // Check if user already has this account type
        List<Account> existing = accountRepo.findAllByUserId(userId);
        for (Account a : existing) {
            if (a.getAccountType().equalsIgnoreCase(accountType)) {
                return ServiceResult.fail("You already have a " + accountType + " account.");
            }
        }
        if (existing.size() >= 3) {
            return ServiceResult.fail("You can have a maximum of 3 accounts.");
        }

        String accNum = "AC" + userId + "_" + System.currentTimeMillis();
        Account account = Account.builder()
                .userId(userId)
                .accountType(accountType)
                .accountNumber(accNum)
                .balanceCents(0L)
                .build();

        int id = accountRepo.insert(account);
        if (id == -1) return ServiceResult.fail("Failed to create account. Please try again.");

        account.setAccountId(id);
        log.info("Created {} account {} for user_id {}", accountType, accNum, userId);
        return ServiceResult.ok(account);
    }

    /**
     * Deposits an amount (in cents) into an account.
     * Logs the transaction. Returns the updated account.
     */
    public ServiceResult<Long> deposit(int accountId, long amountCents, String description) {
        if (amountCents <= 0) return ServiceResult.fail("Amount must be greater than zero.");
        if (amountCents > 99_999_999L) return ServiceResult.fail("Amount exceeds maximum allowed per transaction ($999,999.99).");

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                conn.createStatement().execute("BEGIN IMMEDIATE");
                accountRepo.deposit(conn, accountId, amountCents);
                txnRepo.log(conn, accountId, "Deposit", amountCents,
                        description == null || description.isBlank() ? "Cash deposit" : description);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                log.error("Deposit failed for account_id {}", accountId, e);
                return ServiceResult.fail("We couldn't process your deposit. Please try again.");
            }
        } catch (SQLException e) {
            log.error("Connection error during deposit for account_id {}", accountId, e);
            return ServiceResult.fail("A connection error occurred. Please try again.");
        }

        long newBalance = accountRepo.getBalance(accountId);
        return ServiceResult.ok(newBalance);
    }

    /**
     * Withdraws an amount (in cents) from an account with fund-sufficiency check.
     * Returns the updated balance on success.
     */
    public ServiceResult<Long> withdraw(int accountId, long amountCents, String description, String accountType) {
        if (amountCents <= 0) return ServiceResult.fail("Amount must be greater than zero.");

        // Check daily limit
        long dailyLimit = "Savings".equalsIgnoreCase(accountType) ? SAVINGS_DAILY_LIMIT_CENTS : CURRENT_DAILY_LIMIT_CENTS;
        if (amountCents > dailyLimit) {
            return ServiceResult.fail(String.format(
                    "Amount exceeds your %s account daily limit. Maximum: $%.2f",
                    accountType, dailyLimit / 100.0));
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                conn.createStatement().execute("BEGIN IMMEDIATE");
                boolean success = accountRepo.withdraw(conn, accountId, amountCents);
                if (!success) {
                    conn.rollback();
                    long available = accountRepo.getBalance(accountId);
                    return ServiceResult.fail(String.format(
                            "Insufficient funds. Available: $%.2f | Requested: $%.2f",
                            available / 100.0, amountCents / 100.0));
                }
                txnRepo.log(conn, accountId, "Withdrawal", amountCents,
                        description == null || description.isBlank() ? "Cash withdrawal" : description);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                log.error("Withdrawal failed for account_id {}", accountId, e);
                return ServiceResult.fail("We couldn't process your withdrawal. Please try again.");
            }
        } catch (SQLException e) {
            log.error("Connection error during withdrawal for account_id {}", accountId, e);
            return ServiceResult.fail("A connection error occurred. Please try again.");
        }

        long newBalance = accountRepo.getBalance(accountId);
        return ServiceResult.ok(newBalance);
    }

    public boolean isLargeTransaction(long amountCents) {
        return amountCents >= LARGE_TXN_THRESHOLD_CENTS;
    }

    public Optional<Account> getPrimaryAccount(int userId) {
        return accountRepo.findPrimaryByUserId(userId);
    }

    public List<Account> getAllAccounts(int userId) {
        return accountRepo.findAllByUserId(userId);
    }

    public long getBalance(int accountId) {
        return accountRepo.getBalance(accountId);
    }

    public List<Transaction> getRecentTransactions(int accountId, int limit) {
        return txnRepo.findRecentByAccountId(accountId, limit);
    }

    public List<Transaction> getAllTransactions(int accountId, String typeFilter) {
        return txnRepo.findByAccountId(accountId, typeFilter);
    }
}
