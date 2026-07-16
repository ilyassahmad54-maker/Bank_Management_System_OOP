package com.bankapp.controller;

import com.bankapp.config.SessionManager;
import com.bankapp.model.Account;
import com.bankapp.model.Transaction;
import com.bankapp.model.User;
import com.bankapp.service.AccountService;
import com.bankapp.service.BillService;
import com.bankapp.service.LoanService;
import com.bankapp.service.StatementExportService;
import com.bankapp.service.TransferService;
import com.bankapp.service.UserService.ServiceResult;
import com.bankapp.util.CurrencyFormatter;
import com.bankapp.util.ValidationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @FXML private Label pageTitle, usernameLabel, balanceLabel, accountNumberLabel;
    @FXML private Label accountTypeLabel, monthDepositLabel, monthWithdrawLabel;
    @FXML private VBox dashboardPane, recentTxnList;
    @FXML private Label noTxnLabel, themeToggle;
    @FXML private Button navDashboard, navTransactions, navDeposit, navWithdraw, navBills, navLoans, navSettings, navTransfer;
    @FXML private ScrollPane contentScroll;
    @FXML private StackPane contentArea;

    private final AccountService accountService = new AccountService();
    private final BillService billService = new BillService();
    private final LoanService loanService = new LoanService();
    private final TransferService transferService = new TransferService();
    private final StatementExportService statementService = new StatementExportService();

    private boolean isDarkMode = false;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) { navigateTo("/fxml/login.fxml"); return; }

        usernameLabel.setText(user.getUsername());
        showDashboardPane();
    }

    private void showDashboardPane() {
        contentArea.getChildren().setAll(dashboardPane);
        dashboardPane.setVisible(true);
        dashboardPane.setManaged(true);
        refreshDashboard();
    }

    private void refreshDashboard() {
        Account account = SessionManager.getInstance().getCurrentAccount();
        if (account != null) {
            balanceLabel.setText(CurrencyFormatter.format(account.getBalanceCents()));
            // Mask account number — show last 4 digits
            String accNum = account.getAccountNumber();
            String masked = "****" + accNum.substring(Math.max(0, accNum.length() - 4));
            accountNumberLabel.setText(masked);
            accountTypeLabel.setText(account.getAccountType());

            // Reload from DB to get current balance
            long freshBalance = accountService.getBalance(account.getAccountId());
            account.setBalanceCents(freshBalance);
            balanceLabel.setText(CurrencyFormatter.format(freshBalance));

            loadMonthlyStats(account.getAccountId());
            loadRecentTransactions(account.getAccountId());
        } else {
            balanceLabel.setText("No account");
            accountNumberLabel.setText("Create an account to get started");
            noTxnLabel.setVisible(true);
            noTxnLabel.setManaged(true);
        }
    }

    private void loadMonthlyStats(int accountId) {
        List<Transaction> txns = accountService.getAllTransactions(accountId, null);
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        long deposits = 0, withdrawals = 0;
        for (Transaction t : txns) {
            if (t.getTimestamp() == null || t.getTimestamp().toLocalDate().isBefore(firstOfMonth)) continue;
            if ("Deposit".equals(t.getType())) deposits += t.getAmountCents();
            else if ("Withdrawal".equals(t.getType())) withdrawals += t.getAmountCents();
        }
        monthDepositLabel.setText(CurrencyFormatter.format(deposits));
        monthWithdrawLabel.setText(CurrencyFormatter.format(withdrawals));
    }

    private void loadRecentTransactions(int accountId) {
        List<Transaction> recent = accountService.getRecentTransactions(accountId, 5);
        recentTxnList.getChildren().clear();

        if (recent.isEmpty()) {
            noTxnLabel.setVisible(true);
            noTxnLabel.setManaged(true);
            return;
        }
        noTxnLabel.setVisible(false);
        noTxnLabel.setManaged(false);

        for (Transaction t : recent) {
            HBox row = buildTransactionRow(t);
            recentTxnList.getChildren().add(row);
        }
    }

    private HBox buildTransactionRow(Transaction t) {
        boolean isCredit = "Deposit".equals(t.getType());
        String emoji = isCredit ? "📥" : "📤";
        String amountText = (isCredit ? "+" : "-") + CurrencyFormatter.format(t.getAmountCents());
        String amountColor = isCredit ? "#2e7d32" : "#c62828";

        HBox row = new HBox();
        row.setPadding(new Insets(12, 0, 12, 0));
        row.setStyle("-fx-border-color: transparent transparent #e0e4f0 transparent; -fx-border-width: 0 0 1 0;");

        VBox left = new VBox(2);
        Label desc = new Label(emoji + "  " + (t.getDescription() != null ? t.getDescription() : t.getType()));
        desc.setStyle("-fx-font-size:13px; -fx-text-fill:-fx-text-primary;");
        Label date = new Label(t.getTimestamp() != null ? t.getTimestamp().format(DATE_FMT) : "");
        date.setStyle("-fx-font-size:11px; -fx-text-fill:-fx-text-muted;");
        left.getChildren().addAll(desc, date);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox();
        right.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label amount = new Label(amountText);
        amount.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + amountColor + ";");
        Label badge = new Label(t.getType());
        badge.getStyleClass().add(isCredit ? "badge-deposit" : "badge-withdrawal");
        right.getChildren().addAll(amount, badge);

        row.getChildren().addAll(left, spacer, right);
        return row;
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @FXML private void showDashboard()     { setActive(navDashboard, "Dashboard");      showDashboardPane(); }
    @FXML private void showTransactions()  { setActive(navTransactions, "Transactions"); showTransactionPanel(); }
    @FXML private void showDeposit()       { setActive(navDeposit, "Deposit");           showTransactionDialog(true); }
    @FXML private void showWithdraw()      { setActive(navWithdraw, "Withdraw");          showTransactionDialog(false); }
    @FXML private void showTransfer()      { setActive(navTransfer, "Transfer");          showTransferPanel(); }
    @FXML private void showBills()         { setActive(navBills, "Utility Bills");        showBillsPanel(); }
    @FXML private void showLoans()         { setActive(navLoans, "Loans");               showLoansPanel(); }
    @FXML private void showSettings()      { setActive(navSettings, "Settings");         showSettingsPanel(); }

    private void setActive(Button active, String title) {
        pageTitle.setText(title);
        for (Button b : List.of(navDashboard, navTransactions, navDeposit, navWithdraw, navTransfer, navBills, navLoans, navSettings)) {
            b.getStyleClass().remove("active");
        }
        active.getStyleClass().add("active");
    }

    // ─── Deposit / Withdraw ───────────────────────────────────────────────────

    private void showTransactionDialog(boolean isDeposit) {
        Account account = SessionManager.getInstance().getCurrentAccount();
        if (account == null) {
            showAlert(Alert.AlertType.WARNING, "No Account", "Please create an account first.", null);
            return;
        }

        String title = isDeposit ? "Deposit Funds" : "Withdraw Funds";
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType confirmType = new ButtonType(isDeposit ? "Deposit" : "Withdraw", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, cancelType);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        Label currentBal = new Label("Current balance: " + CurrencyFormatter.format(account.getBalanceCents()));
        currentBal.setStyle("-fx-text-fill: #616161;");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount (e.g. 500.00)");
        TextField descField = new TextField();
        descField.setPromptText("Description (optional)");
        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #c62828; -fx-font-size: 11px;");

        // Quick select buttons
        HBox quickBtns = new HBox(8);
        for (String amt : new String[]{"50", "100", "500", "1000"}) {
            Button qBtn = new Button("$" + amt);
            qBtn.setStyle("-fx-background-color: #f0f2ff; -fx-border-color: #c5cae9; -fx-border-radius:6; -fx-background-radius:6; -fx-cursor:hand;");
            qBtn.setOnAction(e -> amountField.setText(amt));
            quickBtns.getChildren().add(qBtn);
        }

        content.getChildren().addAll(
                new Label(isDeposit ? "💵 Amount to deposit" : "💸 Amount to withdraw"),
                amountField, quickBtns,
                new Label("Description (optional)"), descField, errorLbl, currentBal
        );
        dialog.getDialogPane().setContent(content);

        // Confirm action
        dialog.setResultConverter(btn -> btn == confirmType ? amountField.getText() : null);
        dialog.showAndWait().ifPresent(amtStr -> {
            String valErr = ValidationUtils.validateAmount(amtStr);
            if (valErr != null) { showAlert(Alert.AlertType.ERROR, "Invalid Amount", valErr, null); return; }

            long cents;
            try { cents = CurrencyFormatter.toCents(amtStr); }
            catch (IllegalArgumentException e) { showAlert(Alert.AlertType.ERROR, "Invalid Amount", e.getMessage(), null); return; }

            // Warn for large transactions
            if (accountService.isLargeTransaction(cents)) {
                boolean confirmed = showConfirm(
                        "Large Transaction",
                        String.format("You are about to %s %s.\nThis is a large transaction. Proceed?",
                                isDeposit ? "deposit" : "withdraw", CurrencyFormatter.format(cents))
                );
                if (!confirmed) return;
            }

            ServiceResult<Long> result = isDeposit
                    ? accountService.deposit(account.getAccountId(), cents, descField.getText())
                    : accountService.withdraw(account.getAccountId(), cents, descField.getText(), account.getAccountType());

            if (result.success()) {
                account.setBalanceCents(result.data());
                showAlert(Alert.AlertType.INFORMATION, "Success ✓",
                        (isDeposit ? "Deposit" : "Withdrawal") + " of " + CurrencyFormatter.format(cents) + " was successful!", null);
                // Refresh dashboard live
                Platform.runLater(this::refreshDashboard);
            } else {
                showAlert(Alert.AlertType.ERROR, "Transaction Failed", result.errorMessage(), null);
            }
        });
    }

    // ─── Transfer Panel ───────────────────────────────────────────────────────

    private void showTransferPanel() {
        Account account = SessionManager.getInstance().getCurrentAccount();
        VBox panel = new VBox(20);
        panel.getChildren().add(new Label("Fund Transfer") {{ setStyle("-fx-font-size:18px; -fx-font-weight:bold;"); }});

        if (account == null) {
            panel.getChildren().add(new Label("No account found. Create an account first."));
            replaceContent(panel); return;
        }

        VBox card = new VBox(14); card.getStyleClass().add("card"); card.setMaxWidth(480);

        Label balLbl = new Label("Available: " + CurrencyFormatter.format(accountService.getBalance(account.getAccountId())));
        balLbl.setStyle("-fx-text-fill:#616161;");

        TextField targetField = new TextField();
        targetField.setPromptText("Recipient account number (e.g. AC5_1234567890)");
        TextField amtField = new TextField();
        amtField.setPromptText("Amount (e.g. 250.00)");
        TextField descField = new TextField();
        descField.setPromptText("Note (optional)");
        Label errLbl = new Label(); errLbl.setStyle("-fx-text-fill:#c62828;");

        // Quick amounts
        HBox quickBtns = new HBox(8);
        for (String amt : new String[]{"50", "100", "500", "1000"}) {
            Button qb = new Button("$" + amt);
            qb.setStyle("-fx-background-color:#f0f2ff; -fx-border-color:#c5cae9; -fx-border-radius:6; -fx-background-radius:6; -fx-cursor:hand;");
            qb.setOnAction(e -> amtField.setText(amt));
            quickBtns.getChildren().add(qb);
        }

        Button sendBtn = new Button("Send Money →"); sendBtn.getStyleClass().add("btn-primary");
        sendBtn.setOnAction(e -> {
            String amtErr = com.bankapp.util.ValidationUtils.validateAmount(amtField.getText());
            if (amtErr != null) { errLbl.setText(amtErr); return; }
            if (targetField.getText().isBlank()) { errLbl.setText("Please enter a recipient account number."); return; }

            long cents = CurrencyFormatter.toCents(amtField.getText());
            boolean confirmed = showConfirm("Confirm Transfer",
                    String.format("Transfer %s to account %s?\nThis action cannot be undone.",
                            CurrencyFormatter.format(cents), targetField.getText().trim()));
            if (!confirmed) return;

            ServiceResult<Long> result = transferService.transfer(
                    account.getAccountId(), targetField.getText(), cents,
                    descField.getText(), account.getAccountType());

            if (result.success()) {
                account.setBalanceCents(result.data());
                balLbl.setText("Available: " + CurrencyFormatter.format(result.data()));
                showAlert(Alert.AlertType.INFORMATION, "Transfer Successful ✓",
                        "Transferred " + CurrencyFormatter.format(cents) + " to " + targetField.getText().trim(), null);
                targetField.clear(); amtField.clear(); descField.clear(); errLbl.setText("");
                Platform.runLater(this::refreshDashboard);
            } else {
                errLbl.setText(result.errorMessage());
            }
        });

        card.getChildren().addAll(
                balLbl,
                new Label("Recipient Account Number:"), targetField,
                new Label("Amount:"), amtField, quickBtns,
                new Label("Note (optional):"), descField,
                errLbl, sendBtn
        );
        panel.getChildren().add(card);
        replaceContent(panel);
    }

    // ─── Transactions Panel ───────────────────────────────────────────────────

    private void showTransactionPanel() {
        Account account = SessionManager.getInstance().getCurrentAccount();
        User user = SessionManager.getInstance().getCurrentUser();
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(0));

        if (account == null) {
            panel.getChildren().add(new Label("No account found. Create an account first."));
        } else {
            ComboBox<String> filterBox = new ComboBox<>();
            filterBox.getItems().addAll("All", "Deposit", "Withdrawal", "Transfer");
            filterBox.setValue("All");

            TableView<Transaction> table = buildTransactionTable();
            loadTransactionTable(table, account.getAccountId(), "All");
            filterBox.setOnAction(e -> loadTransactionTable(table, account.getAccountId(), filterBox.getValue()));

            Button exportBtn = new Button("📄 Export PDF");
            exportBtn.getStyleClass().add("btn-outline");
            exportBtn.setOnAction(e -> exportStatement(user, account));

            HBox filterRow = new HBox(12);
            filterRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
            filterRow.getChildren().addAll(new Label("Filter:"), filterBox, spacer, exportBtn);
            VBox.setVgrow(table, Priority.ALWAYS);
            panel.getChildren().addAll(new Label("Transaction History"), filterRow, table);
        }

        replaceContent(panel);
    }

    private void exportStatement(User user, Account account) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Save Statement PDF");
        File dir = chooser.showDialog(pageTitle.getScene().getWindow());
        if (dir == null) return;

        List<Transaction> txns = accountService.getAllTransactions(account.getAccountId(), null);
        String fileName = "statement_" + account.getAccountNumber() + "_"
                + java.time.LocalDate.now() + ".pdf";
        String path = dir.getAbsolutePath() + File.separator + fileName;

        File result = statementService.exportStatement(user, account, txns, path);
        if (result != null) {
            showAlert(Alert.AlertType.INFORMATION, "Export Successful ✓",
                    "Statement saved to:\n" + result.getAbsolutePath(), null);
        } else {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not generate PDF. Please try again.", null);
        }
    }

    private TableView<Transaction> buildTransactionTable() {
        TableView<Transaction> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getTimestamp() != null ? c.getValue().getTimestamp().format(DATE_FMT) : ""));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getType()));
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) { setGraphic(null); return; }
                Label badge = new Label(type);
                badge.getStyleClass().add("Deposit".equals(type) ? "badge-deposit" : "badge-withdrawal");
                setGraphic(badge);
            }
        });

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));

        TableColumn<Transaction, String> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(c -> {
            Transaction t = c.getValue();
            String sign = "Deposit".equals(t.getType()) ? "+" : "-";
            return new javafx.beans.property.SimpleStringProperty(sign + CurrencyFormatter.format(t.getAmountCents()));
        });
        amtCol.setStyle("-fx-alignment: CENTER_RIGHT;");

        table.getColumns().addAll(dateCol, typeCol, descCol, amtCol);
        return table;
    }

    private void loadTransactionTable(TableView<Transaction> table, int accountId, String filter) {
        List<Transaction> txns = accountService.getAllTransactions(accountId, filter);
        table.getItems().setAll(txns);
    }

    // ─── Bills Panel ──────────────────────────────────────────────────────────

    private void showBillsPanel() {
        Account account = SessionManager.getInstance().getCurrentAccount();
        User user = SessionManager.getInstance().getCurrentUser();

        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0));
        panel.getChildren().add(new Label("Utility Bill Payment") {{
            setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
        }});

        if (account == null || user == null) {
            panel.getChildren().add(new Label("No account found."));
        } else {
            // Bill payment form card
            VBox formCard = new VBox(14);
            formCard.getStyleClass().add("card");
            formCard.setMaxWidth(460);

            // Bill type grid
            HBox typeGrid = new HBox(12);
            ToggleGroup tg = new ToggleGroup();
            for (String[] bt : new String[][]{{"⚡", "Electricity"}, {"💧", "Water"}, {"🔥", "Gas"}, {"📶", "Internet"}}) {
                ToggleButton tb = new ToggleButton(bt[0] + "\n" + bt[1]);
                tb.setToggleGroup(tg);
                tb.setUserData(bt[1]);
                tb.setStyle("-fx-pref-width:90; -fx-pref-height:70; -fx-background-radius:8; -fx-cursor:hand; -fx-alignment:center;");
                typeGrid.getChildren().add(tb);
            }
            ((ToggleButton) typeGrid.getChildren().get(0)).setSelected(true);

            TextField amtField = new TextField();
            amtField.setPromptText("Amount (e.g. 150.00)");
            DatePicker datePicker = new DatePicker(java.time.LocalDate.now().plusDays(7));
            Label errLbl = new Label();
            errLbl.setStyle("-fx-text-fill: #c62828;");
            Button payBtn = new Button("Pay Bill");
            payBtn.getStyleClass().add("btn-accent");

            payBtn.setOnAction(e -> {
                Toggle sel = tg.getSelectedToggle();
                if (sel == null) { errLbl.setText("Please select a bill type."); return; }
                String billType = (String) sel.getUserData();
                String amtErr = ValidationUtils.validateAmount(amtField.getText());
                if (amtErr != null) { errLbl.setText(amtErr); return; }

                long cents = CurrencyFormatter.toCents(amtField.getText());
                ServiceResult<Void> result = billService.payBill(user.getUserId(), billType, cents, datePicker.getValue());
                if (result.success()) {
                    showAlert(Alert.AlertType.INFORMATION, "Bill Paid ✓", billType + " bill of " + CurrencyFormatter.format(cents) + " paid successfully!", null);
                    amtField.clear(); errLbl.setText("");
                } else {
                    errLbl.setText(result.errorMessage());
                }
            });

            formCard.getChildren().addAll(
                    new Label("Select bill type:"), typeGrid,
                    new Label("Amount:"), amtField,
                    new Label("Due Date:"), datePicker,
                    errLbl, payBtn
            );

            // History
            VBox histCard = new VBox(12);
            histCard.getStyleClass().add("card");
            histCard.getChildren().add(new Label("Payment History") {{setStyle("-fx-font-weight:bold;");}});
            billService.getBillHistory(user.getUserId()).forEach(b -> {
                HBox row = new HBox();
                row.setPadding(new Insets(8, 0, 8, 0));
                row.setStyle("-fx-border-color: transparent transparent #e0e4f0 transparent; -fx-border-width:0 0 1 0;");
                VBox left = new VBox(2);
                left.getChildren().add(new Label(b.getBillType()) {{setStyle("-fx-font-weight:bold;");}});
                left.getChildren().add(new Label(b.getDueDate() != null ? "Due: " + b.getDueDate().toString() : "") {{setStyle("-fx-font-size:11px; -fx-text-fill:#9e9e9e;");}});
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label amt = new Label(CurrencyFormatter.format(b.getAmountCents()));
                amt.setStyle("-fx-font-weight:bold; -fx-text-fill:#2e7d32;");
                row.getChildren().addAll(left, sp, amt);
                histCard.getChildren().add(row);
            });

            panel.getChildren().addAll(formCard, histCard);
        }

        replaceContent(panel);
    }

    // ─── Loans Panel ─────────────────────────────────────────────────────────

    private void showLoansPanel() {
        Account account = SessionManager.getInstance().getCurrentAccount();
        User user = SessionManager.getInstance().getCurrentUser();

        VBox panel = new VBox(20);
        panel.getChildren().add(new Label("Loan Services") {{ setStyle("-fx-font-size:18px; -fx-font-weight:bold;"); }});

        if (account == null || user == null) {
            panel.getChildren().add(new Label("No account found.")); replaceContent(panel); return;
        }

        VBox formCard = new VBox(16); formCard.getStyleClass().add("card"); formCard.setMaxWidth(500);

        // Loan type cards
        HBox typeRow = new HBox(12);
        ToggleGroup tg = new ToggleGroup();
        for (String[] lt : new String[][]{{"🏠", "Home", "6.2%"}, {"🚗", "Auto", "7.8%"}, {"💼", "Personal", "8.5%"}, {"🎓", "Education", "5.5%"}}) {
            ToggleButton tb = new ToggleButton(lt[0] + "\n" + lt[1] + "\n" + lt[2]);
            tb.setToggleGroup(tg);
            tb.setUserData(lt[1]);
            tb.setStyle("-fx-pref-width:100; -fx-pref-height:80; -fx-background-radius:8; -fx-cursor:hand; -fx-alignment:center; -fx-font-size:11px;");
            typeRow.getChildren().add(tb);
        }
        ((ToggleButton) typeRow.getChildren().get(0)).setSelected(true);

        // Amount slider
        Slider principalSlider = new Slider(1000, 100000, 10000);
        principalSlider.setShowTickLabels(true); principalSlider.setShowTickMarks(true);
        Label principalLabel = new Label(CurrencyFormatter.format((long)(principalSlider.getValue() * 100)));

        Slider termSlider = new Slider(12, 60, 24);
        termSlider.setShowTickLabels(true); termSlider.setMajorTickUnit(12);
        Label termLabel = new Label((int) termSlider.getValue() + " months");

        Label emiLabel = new Label(); emiLabel.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#1a237e;");

        Runnable calcEmi = () -> {
            Toggle sel = tg.getSelectedToggle();
            String loanType = sel != null ? (String) sel.getUserData() : "Personal";
            long principalCents = (long)(principalSlider.getValue() * 100);
            int months = (int) termSlider.getValue();
            double rate = LoanService.getInterestRate(loanType);
            long emiCents = LoanService.calculateEmiCents(principalCents, rate, months);
            principalLabel.setText(CurrencyFormatter.format(principalCents));
            termLabel.setText(months + " months");
            emiLabel.setText("Monthly EMI: " + CurrencyFormatter.format(emiCents));
        };
        principalSlider.valueProperty().addListener((o,a,b) -> calcEmi.run());
        termSlider.valueProperty().addListener((o,a,b) -> calcEmi.run());
        tg.selectedToggleProperty().addListener((o,a,b) -> calcEmi.run());
        calcEmi.run();

        Label errLbl = new Label(); errLbl.setStyle("-fx-text-fill:#c62828;");
        Button applyBtn = new Button("Apply for Loan"); applyBtn.getStyleClass().add("btn-primary");

        applyBtn.setOnAction(e -> {
            Toggle sel = tg.getSelectedToggle();
            if (sel == null) { errLbl.setText("Select a loan type."); return; }
            String loanType = (String) sel.getUserData();
            long principalCents = (long)(principalSlider.getValue() * 100);
            int months = (int) termSlider.getValue();

            ServiceResult<Void> result = loanService.applyForLoan(user.getUserId(), account.getAccountId(), loanType, principalCents, months);
            if (result.success()) {
                showAlert(Alert.AlertType.INFORMATION, "Application Submitted ✓",
                        "Your " + loanType + " loan application for " + CurrencyFormatter.format(principalCents) + " is under review.", null);
                errLbl.setText("");
            } else {
                errLbl.setText(result.errorMessage());
            }
        });

        formCard.getChildren().addAll(
                new Label("Select loan type:"), typeRow,
                new Label("Principal amount:"), principalSlider, principalLabel,
                new Label("Loan term:"), termSlider, termLabel,
                emiLabel, errLbl, applyBtn
        );

        panel.getChildren().add(formCard);
        replaceContent(panel);
    }

    // ─── Settings Panel ───────────────────────────────────────────────────────

    private void showSettingsPanel() {
        User user = SessionManager.getInstance().getCurrentUser();
        VBox panel = new VBox(20);
        panel.getChildren().add(new Label("Settings") {{ setStyle("-fx-font-size:18px; -fx-font-weight:bold;"); }});

        VBox card = new VBox(14); card.getStyleClass().add("card"); card.setMaxWidth(460);
        card.getChildren().add(new Label("Account Information") {{ setStyle("-fx-font-weight:bold;"); }});
        if (user != null) {
            card.getChildren().add(new Label("Username: " + user.getUsername()));
            card.getChildren().add(new Label("CNIC: " + user.getCnic()));
            card.getChildren().add(new Label("Phone: " + user.getPhone()));
            card.getChildren().add(new Label("Address: " + user.getAddress()));
        }
        Separator sep = new Separator();
        Label themeTitle = new Label("Theme") {{ setStyle("-fx-font-weight:bold;"); }};
        Button toggleBtn = new Button(isDarkMode ? "Switch to Light Mode ☀️" : "Switch to Dark Mode 🌙");
        toggleBtn.getStyleClass().add("btn-outline");
        toggleBtn.setOnAction(e -> { toggleTheme(); toggleBtn.setText(isDarkMode ? "Switch to Light Mode ☀️" : "Switch to Dark Mode 🌙"); });
        card.getChildren().addAll(sep, themeTitle, toggleBtn);

        panel.getChildren().add(card);
        replaceContent(panel);
    }

    // ─── Utility Methods ──────────────────────────────────────────────────────

    private void replaceContent(VBox newContent) {
        dashboardPane.setVisible(false);
        dashboardPane.setManaged(false);
        newContent.setPadding(new Insets(0));
        contentArea.getChildren().setAll(newContent);
    }

    @FXML
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        Scene scene = themeToggle.getScene();
        scene.getStylesheets().clear();
        String css = getClass().getResource(isDarkMode ? "/css/dark.css" : "/css/light.css").toExternalForm();
        scene.getStylesheets().add(css);
        themeToggle.setText(isDarkMode ? "☀️" : "🌙");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        navigateTo("/fxml/login.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) pageTitle.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/light.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            log.error("Failed to navigate to {}", fxmlPath, e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg, ButtonType... btns) {
        Alert a = btns != null && btns.length > 0
                ? new Alert(type, msg, btns)
                : new Alert(type, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }

    private boolean showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle(title); a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
}
