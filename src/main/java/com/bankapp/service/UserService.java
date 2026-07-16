package com.bankapp.service;

import com.bankapp.model.User;
import com.bankapp.repository.UserRepository;
import com.bankapp.util.PasswordHasher;
import com.bankapp.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service layer for user authentication and registration.
 * All business logic lives here — the controller only delegates to this class.
 */
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepo = new UserRepository();

    /** Result object returned by service methods to avoid throwing exceptions to the UI layer. */
    public record ServiceResult<T>(boolean success, String errorMessage, T data) {
        public static <T> ServiceResult<T> ok(T data) { return new ServiceResult<>(true, null, data); }
        public static <T> ServiceResult<T> fail(String msg) { return new ServiceResult<>(false, msg, null); }
    }

    /**
     * Attempts to log in a user with username + password.
     * @return ServiceResult with User on success, error message on failure.
     */
    public ServiceResult<User> login(String username, String password) {
        if (username == null || username.isBlank()) return ServiceResult.fail("Please enter your username.");
        if (password == null || password.isBlank()) return ServiceResult.fail("Please enter your password.");

        Optional<User> found = userRepo.findByUsername(username.trim());
        if (found.isEmpty()) return ServiceResult.fail("No account found with that username.");

        User user = found.get();
        if (!PasswordHasher.verify(password, user.getPassword())) {
            return ServiceResult.fail("Incorrect password. Please try again.");
        }
        log.info("User '{}' logged in successfully.", username);
        return ServiceResult.ok(user);
    }

    /**
     * Registers a new user. Validates all fields, hashes the password.
     * @return ServiceResult with the new user_id on success.
     */
    public ServiceResult<Integer> register(String username, String password, String confirmPassword,
                                            int age, String cnic, String address, String phone) {
        // Validate inputs
        String err;
        if ((err = ValidationUtils.validateUsername(username)) != null) return ServiceResult.fail(err);
        if ((err = ValidationUtils.validatePassword(password)) != null) return ServiceResult.fail(err);
        if ((err = ValidationUtils.validatePasswordMatch(password, confirmPassword)) != null) return ServiceResult.fail(err);
        if ((err = ValidationUtils.validateCnic(cnic)) != null) return ServiceResult.fail(err);
        if ((err = ValidationUtils.validatePhone(phone)) != null) return ServiceResult.fail(err);
        if (address == null || address.isBlank()) return ServiceResult.fail("Address is required.");
        if (age < 18 || age > 100) return ServiceResult.fail("Age must be between 18 and 100.");

        if (userRepo.usernameExists(username.trim())) return ServiceResult.fail("That username is already taken. Please choose another.");

        User user = User.builder()
                .username(username.trim())
                .password(PasswordHasher.hash(password))
                .age(age)
                .cnic(cnic.trim())
                .address(address.trim())
                .phone(phone.trim())
                .build();

        int userId = userRepo.insert(user);
        if (userId == -1) {
            log.error("Registration failed for username '{}'", username);
            return ServiceResult.fail("Registration failed due to a system error. Please try again.");
        }
        log.info("New user '{}' registered with user_id {}", username, userId);
        return ServiceResult.ok(userId);
    }

    /**
     * Changes user password after verifying the current password.
     */
    public ServiceResult<Void> changePassword(int userId, String currentPassword, String newPassword, String confirmNew) {
        String err;
        if ((err = ValidationUtils.validatePassword(newPassword)) != null) return ServiceResult.fail(err);
        if ((err = ValidationUtils.validatePasswordMatch(newPassword, confirmNew)) != null) return ServiceResult.fail(err);

        Optional<User> found = userRepo.findById(userId);
        if (found.isEmpty()) return ServiceResult.fail("User not found.");

        if (!PasswordHasher.verify(currentPassword, found.get().getPassword())) {
            return ServiceResult.fail("Current password is incorrect.");
        }
        userRepo.updatePassword(userId, PasswordHasher.hash(newPassword));
        return ServiceResult.ok(null);
    }
}
