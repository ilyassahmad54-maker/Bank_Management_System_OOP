package com.bankapp;

import com.bankapp.db.ConnectionPool;
import com.bankapp.db.MigrationManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoBank — Application Entry Point
 *
 * Boot sequence:
 *   1. Run DB migrations (creates ~/.bankapp/data/bankdata.db if needed)
 *   2. Launch JavaFX login screen
 *
 * All layers are wired through dependency injection at the service level:
 *   Main → LoginController  → UserService    → UserRepository    → ConnectionPool
 *       → DashboardController → AccountService → AccountRepository → ConnectionPool
 *                             → TransferService
 *                             → BillService   → UtilityBillRepository
 *                             → LoanService   → LoanRepository
 *                             → StatementExportService (PDFBox)
 *       → RegisterController → UserService + AccountService
 *
 * Database: SQLite at ~/.bankapp/data/bankdata.db (auto-created, no setup needed)
 * Theme:    Light by default; toggle in Settings panel
 */
public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // ── JavaFX lifecycle ──────────────────────────────────────────────────────

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/light.css").toExternalForm());

            primaryStage.setTitle("NeoBank — Professional Banking");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.setWidth(1100);
            primaryStage.setHeight(700);
            primaryStage.centerOnScreen();
            primaryStage.show();
            primaryStage.toFront();   // force window to front on macOS
            primaryStage.requestFocus();

            log.info("NeoBank started successfully.");
        } catch (Exception e) {
            log.error("Failed to load login screen.", e);
            showFatalError("Startup Error", "Could not load the login screen.\n" + e.getMessage());
        }
    }

    @Override
    public void stop() {
        ConnectionPool.close();
        log.info("NeoBank stopped. Connection pool closed.");
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // macOS: bring app to front when launched from terminal
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
                new ProcessBuilder("osascript", "-e",
                    "tell application \"System Events\" to set frontmost of every process whose unix id is "
                    + ProcessHandle.current().pid() + " to true")
                    .start().waitFor();
            }
        } catch (Exception ignored) {}

        // Step 1: Run DB migrations before JavaFX starts
        try {
            log.info("Initializing database...");
            MigrationManager.runMigrations();
            log.info("Database ready.");
        } catch (Exception e) {
            log.error("Database initialization failed. Cannot start.", e);
            // Show error on JavaFX thread if possible, then exit
            Platform.startup(() -> showFatalError(
                    "Database Error",
                    "Failed to initialize the database.\n\n" + e.getMessage()
                            + "\n\nPlease check that ~/.bankapp/data/ is writable."
            ));
            return;
        }

        // Step 2: Launch JavaFX application
        launch(args);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void showFatalError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
        System.exit(1);
    }
}
