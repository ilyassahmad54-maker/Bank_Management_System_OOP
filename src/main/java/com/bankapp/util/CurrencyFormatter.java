package com.bankapp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Converts between integer cents (stored in DB) and display-formatted currency strings.
 * All monetary values in the application are stored as long (cents) to avoid
 * floating-point precision issues (following fintech best practices).
 */
public class CurrencyFormatter {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private CurrencyFormatter() {}

    /**
     * Formats cents as a currency string. e.g. 105050L → "$1,050.50"
     */
    public static String format(long cents) {
        BigDecimal amount = BigDecimal.valueOf(cents, 2);
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Converts a dollar string entered by user (e.g. "500.00" or "500") to cents (50000L).
     * @throws IllegalArgumentException if the input is invalid or negative.
     */
    public static long toCents(String dollarString) {
        if (dollarString == null || dollarString.isBlank()) {
            throw new IllegalArgumentException("Amount cannot be empty.");
        }
        try {
            BigDecimal val = new BigDecimal(dollarString.trim()).setScale(2, RoundingMode.HALF_UP);
            if (val.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            return val.movePointRight(2).longValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: '" + dollarString + "'");
        }
    }

    /**
     * Converts cents (long) back to a BigDecimal dollar value.
     */
    public static BigDecimal toDollars(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }
}
