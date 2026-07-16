package com.bankapp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for BCrypt password hashing and verification.
 * Never stores or compares plaintext passwords.
 */
public class PasswordHasher {

    private static final int BCRYPT_WORK_FACTOR = 12;

    private PasswordHasher() {}

    /**
     * Hashes a plaintext password using BCrypt.
     * @param plainPassword The raw password string from the user.
     * @return A BCrypt hash string.
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     * @param plainPassword The raw password input.
     * @param hashedPassword The stored BCrypt hash.
     * @return true if the password matches, false otherwise.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
