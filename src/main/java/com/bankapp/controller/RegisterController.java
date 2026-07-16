package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.service.AccountService;
import com.bankapp.service.UserService;
import com.bankapp.service.UserService.ServiceResult;
import com.bankapp.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RegisterController {
    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    // Step 1 fields
    @FXML private TextField addressField, cnicField, phoneField, ageField;
    @FXML private Label addressError, cnicError, phoneError, ageError;

    // Step 2 fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ProgressBar strengthBar;
    @FXML private Label usernameError, passwordError, confirmError, strengthLabel;

    // Step 3 fields
    @FXML private VBox savingsCard, currentCard;

    // Step panes
    @FXML private VBox step1Pane, step2Pane, step3Pane;
    @FXML private Label step1Circle, step2Circle, step3Circle, stepLabel;
    @FXML private Label errorLabel;
    @FXML private Button nextBtn, backBtn;

    private int currentStep = 1;
    private String selectedAccountType = "Savings";

    private final UserService userService = new UserService();
    private final AccountService accountService = new AccountService();

    @FXML
    public void initialize() {
        selectSavings(); // default

        // Real-time password strength
        passwordField.textProperty().addListener((o, a, b) -> {
            String strength = ValidationUtils.passwordStrength(b);
            strengthLabel.setText("Strength: " + strength);
            switch (strength) {
                case "Strong" -> {
                    strengthBar.setProgress(1.0);
                    strengthBar.getStyleClass().setAll("progress-bar", "strength-strong");
                }
                case "Medium" -> {
                    strengthBar.setProgress(0.6);
                    strengthBar.getStyleClass().setAll("progress-bar", "strength-medium");
                }
                default -> {
                    strengthBar.setProgress(0.3);
                    strengthBar.getStyleClass().setAll("progress-bar", "strength-weak");
                }
            }
        });

        // Real-time confirm match check
        confirmPasswordField.textProperty().addListener((o, a, b) -> {
            String match = ValidationUtils.validatePasswordMatch(passwordField.getText(), b);
            if (match != null) showError(confirmError, match);
            else hideError(confirmError);
        });
    }

    @FXML
    private void handleNext() {
        hideError(errorLabel);
        if (currentStep == 1) {
            if (!validateStep1()) return;
            goToStep(2);
        } else if (currentStep == 2) {
            if (!validateStep2()) return;
            goToStep(3);
        } else {
            // Step 3 — final submit
            submitRegistration();
        }
    }

    @FXML
    private void handleBack() {
        hideError(errorLabel);
        if (currentStep > 1) goToStep(currentStep - 1);
    }

    @FXML
    private void selectSavings() {
        selectedAccountType = "Savings";
        savingsCard.getStyleClass().add("selected");
        currentCard.getStyleClass().remove("selected");
    }

    @FXML
    private void selectCurrent() {
        selectedAccountType = "Current";
        currentCard.getStyleClass().add("selected");
        savingsCard.getStyleClass().remove("selected");
    }

    @FXML
    private void goToLogin() {
        navigate("/fxml/login.fxml");
    }

    private boolean validateStep1() {
        boolean valid = true;
        String err;
        if ((err = ValidationUtils.validateCnic(cnicField.getText())) != null) { showError(cnicError, err); valid = false; } else hideError(cnicError);
        if ((err = ValidationUtils.validatePhone(phoneField.getText())) != null) { showError(phoneError, err); valid = false; } else hideError(phoneError);
        if ((err = ValidationUtils.validateAge(ageField.getText())) != null) { showError(ageError, err); valid = false; } else hideError(ageError);
        if (addressField.getText().isBlank()) { showError(addressError, "Address is required."); valid = false; } else hideError(addressError);
        return valid;
    }

    private boolean validateStep2() {
        boolean valid = true;
        String err;
        if ((err = ValidationUtils.validateUsername(usernameField.getText())) != null) { showError(usernameError, err); valid = false; } else hideError(usernameError);
        if ((err = ValidationUtils.validatePassword(passwordField.getText())) != null) { showError(passwordError, err); valid = false; } else hideError(passwordError);
        if ((err = ValidationUtils.validatePasswordMatch(passwordField.getText(), confirmPasswordField.getText())) != null) { showError(confirmError, err); valid = false; } else hideError(confirmError);
        return valid;
    }

    private void submitRegistration() {
        String ageErr = com.bankapp.util.ValidationUtils.validateAge(ageField.getText());
        if (ageErr != null) { showError(errorLabel, ageErr); return; }
        int age = Integer.parseInt(ageField.getText().trim());

        ServiceResult<Integer> result = userService.register(
                usernameField.getText(), passwordField.getText(), confirmPasswordField.getText(),
                age, cnicField.getText(), addressField.getText(), phoneField.getText()
        );

        if (!result.success()) {
            showError(errorLabel, result.errorMessage());
            return;
        }

        int userId = result.data();
        ServiceResult<Account> accResult = accountService.createAccount(userId, selectedAccountType);

        String message = accResult.success()
                ? "Account created!\nNumber: " + accResult.data().getAccountNumber()
                : "User registered but account creation failed. Please contact support.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("Registration Successful 🎉");
        alert.showAndWait();

        navigate("/fxml/login.fxml");
    }

    private void goToStep(int step) {
        currentStep = step;
        step1Pane.setVisible(step == 1); step1Pane.setManaged(step == 1);
        step2Pane.setVisible(step == 2); step2Pane.setManaged(step == 2);
        step3Pane.setVisible(step == 3); step3Pane.setManaged(step == 3);
        backBtn.setVisible(step > 1); backBtn.setManaged(step > 1);
        nextBtn.setText(step == 3 ? "Create Account ✓" : "Continue →");

        // Update step indicators
        updateStepCircle(step1Circle, step >= 1, step > 1);
        updateStepCircle(step2Circle, step >= 2, step > 2);
        updateStepCircle(step3Circle, step >= 3, false);
        stepLabel.setText("Step " + step + ": " + switch (step) {
            case 1 -> "Personal Information";
            case 2 -> "Account Setup";
            default -> "Choose Account Type";
        });
    }

    private void updateStepCircle(Label circle, boolean active, boolean done) {
        circle.getStyleClass().clear();
        circle.getStyleClass().add("wizard-step-circle");
        if (done) circle.getStyleClass().add("done");
        else if (active) circle.getStyleClass().add("active");
        circle.setStyle("-fx-alignment:center; -fx-font-weight:bold; -fx-text-fill:" + (active ? "white" : "grey") + ";");
    }

    private void navigate(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) nextBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("/css/light.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            log.error("Failed to navigate to {}", path, e);
        }
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
        label.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
