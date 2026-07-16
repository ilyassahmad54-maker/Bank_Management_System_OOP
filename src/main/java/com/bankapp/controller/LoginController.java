package com.bankapp.controller;

import com.bankapp.config.SessionManager;
import com.bankapp.model.Account;
import com.bankapp.model.User;
import com.bankapp.service.AccountService;
import com.bankapp.service.UserService;
import com.bankapp.service.UserService.ServiceResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Label errorLabel;
    @FXML private Label usernameError;
    @FXML private Label passwordError;

    private boolean passwordShown = false;
    private final UserService userService = new UserService();
    private final AccountService accountService = new AccountService();

    @FXML
    public void initialize() {
        passwordVisible.setManaged(false);
        passwordVisible.setVisible(false);
        usernameField.textProperty().addListener((o, a, b) -> hideError(usernameError));
        passwordField.textProperty().addListener((o, a, b) -> hideError(passwordError));
    }

    @FXML
    private void togglePassword() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordShown ? passwordVisible.getText() : passwordField.getText();

        hideError(errorLabel);
        hideError(usernameError);
        hideError(passwordError);

        if (username.isBlank()) { showError(usernameError, "Username is required."); return; }
        if (password.isBlank()) { showError(passwordError, "Password is required."); return; }

        ServiceResult<User> result = userService.login(username, password);
        if (!result.success()) {
            showError(errorLabel, result.errorMessage());
            return;
        }

        User user = result.data();
        SessionManager.getInstance().setCurrentUser(user);

        Optional<Account> account = accountService.getPrimaryAccount(user.getUserId());
        account.ifPresent(a -> SessionManager.getInstance().setCurrentAccount(a));

        log.info("User '{}' logged in.", user.getUsername());
        navigate("/fxml/dashboard.fxml");
    }

    @FXML
    private void goToRegister() {
        navigate("/fxml/register.fxml");
    }

    private void navigate(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/light.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("Navigation failed: {} — {}", fxmlPath, cause.getMessage(), cause);
            showError(errorLabel, "Navigation error: " + cause.getMessage());
        }
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
