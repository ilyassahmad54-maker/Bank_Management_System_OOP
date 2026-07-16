package com.bankapp.util;

import java.util.regex.Pattern;

/**
 * Centralized input validation utility.
 * All validation logic lives here — controllers call these methods and display
 * the returned error strings inline beneath each form field.
 */
public class ValidationUtils {

    private static final Pattern CNIC_PATTERN = Pattern.compile("^\\d{5}-\\d{7}-\\d$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d{1,9}(\\.\\d{1,2})?$");

    private ValidationUtils() {}

    /** Validates Pakistani CNIC format: 00000-0000000-0 */
    public static String validateCnic(String cnic) {
        if (cnic == null || cnic.isBlank()) return "CNIC is required.";
        if (!CNIC_PATTERN.matcher(cnic.trim()).matches())
            return "CNIC must be in format: 00000-0000000-0";
        return null; // valid
    }

    /** Validates username: 3-30 chars, alphanumeric + underscore only */
    public static String validateUsername(String username) {
        if (username == null || username.isBlank()) return "Username is required.";
        String u = username.trim();
        if (u.length() < 3) return "Username must be at least 3 characters.";
        if (u.length() > 30) return "Username must be under 30 characters.";
        if (!u.matches("[a-zA-Z0-9_]+")) return "Username can only contain letters, numbers, and underscores.";
        return null;
    }

    /** Validates password strength. Returns null if strong enough. */
    public static String validatePassword(String password) {
        if (password == null || password.isBlank()) return "Password is required.";
        if (password.length() < 8) return "Password must be at least 8 characters.";
        if (!password.matches(".*[A-Z].*")) return "Password must contain at least one uppercase letter.";
        if (!password.matches(".*[0-9].*")) return "Password must contain at least one number.";
        return null;
    }

    /** Validates that two password strings match. */
    public static String validatePasswordMatch(String password, String confirm) {
        if (!password.equals(confirm)) return "Passwords do not match.";
        return null;
    }

    /** Returns a password strength label: "Weak", "Medium", or "Strong" */
    public static String passwordStrength(String password) {
        if (password == null || password.length() < 6) return "Weak";
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        int score = (hasUpper ? 1 : 0) + (hasNumber ? 1 : 0) + (hasSpecial ? 1 : 0) + (password.length() >= 12 ? 1 : 0);
        if (score >= 3) return "Strong";
        if (score == 2) return "Medium";
        return "Weak";
    }

    /** Validates a monetary amount string like "500" or "49.99". */
    public static String validateAmount(String amount) {
        if (amount == null || amount.isBlank()) return "Amount is required.";
        if (!AMOUNT_PATTERN.matcher(amount.trim()).matches())
            return "Enter a valid amount (e.g. 100 or 49.99).";
        return null;
    }

    /** Validates phone number (international format). */
    public static String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return "Phone number is required.";
        if (!PHONE_PATTERN.matcher(phone.trim()).matches())
            return "Enter a valid phone number (10-15 digits, optional + prefix).";
        return null;
    }

    /** Validates age: must be between 18 and 100. */
    public static String validateAge(String ageStr) {
        if (ageStr == null || ageStr.isBlank()) return "Age is required.";
        try {
            int age = Integer.parseInt(ageStr.trim());
            if (age < 18) return "You must be at least 18 years old.";
            if (age > 100) return "Please enter a valid age.";
        } catch (NumberFormatException e) {
            return "Age must be a number.";
        }
        return null;
    }
}
