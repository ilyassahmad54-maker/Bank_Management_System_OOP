# NeoBank — Desktop Banking Application v2.0

A fully-featured Object-Oriented desktop banking application built in Java 21, using **JavaFX** for the UI, **SQLite** for zero-config local persistence, and a clean **MVC + Service + Repository** architecture.

---

## 🛠️ Tools & Technologies

| Layer | Technology |
|---|---|
| Language | Java SE 21 |
| UI Framework | JavaFX 21 (FXML + CSS) |
| Database | SQLite (embedded, no server needed) |
| Connection Pool | HikariCP 5.1.0 (WAL mode) |
| Password Security | BCrypt (jbcrypt, work factor 12) |
| PDF Export | Apache PDFBox 3.0.1 |
| Build Tool | Apache Maven 3.9.9 (bundled) |
| Models | Lombok (@Data @Builder) |
| Logging | SLF4J + Logback |

---

## 🚀 How to Run

```bash
# From project root — Maven is bundled, no install needed
./tools/apache-maven-3.9.9/bin/mvn javafx:run
```

The database is created automatically at `~/.bankapp/data/bankdata.db` on first launch.  
No MySQL, no external DB, no setup required.

---

## 📂 Project Structure

```
Bank_Management_System_OOP/
├── pom.xml                          ← Maven build config
├── README.md
├── .gitignore
├── tools/apache-maven-3.9.9/        ← Bundled Maven (no install needed)
└── src/main/
    ├── java/com/bankapp/
    │   ├── Main.java                ← ENTRY POINT — boots DB then launches JavaFX
    │   ├── config/
    │   │   └── SessionManager.java  ← Thread-safe singleton: logged-in User + Account
    │   ├── db/
    │   │   ├── ConnectionPool.java  ← HikariCP pool, SQLite WAL mode
    │   │   └── MigrationManager.java← Auto-creates schema via PRAGMA user_version
    │   ├── model/                   ← Lombok POJOs — all money stored as long cents
    │   │   ├── User.java
    │   │   ├── Account.java
    │   │   ├── Transaction.java
    │   │   ├── Loan.java
    │   │   └── UtilityBill.java
    │   ├── repository/              ← Raw JDBC, PreparedStatements only
    │   │   ├── UserRepository.java
    │   │   ├── AccountRepository.java
    │   │   ├── TransactionRepository.java
    │   │   ├── LoanRepository.java
    │   │   └── UtilityBillRepository.java
    │   ├── service/                 ← Business logic — all return ServiceResult<T>
    │   │   ├── UserService.java     ← login, register, changePassword
    │   │   ├── AccountService.java  ← deposit, withdraw, history
    │   │   ├── TransferService.java ← atomic fund transfers
    │   │   ├── BillService.java     ← utility bill payments
    │   │   ├── LoanService.java     ← loan application + EMI calculator
    │   │   └── StatementExportService.java ← PDF statement export
    │   ├── controller/              ← JavaFX FXML controllers (UI only, no business logic)
    │   │   ├── LoginController.java
    │   │   ├── RegisterController.java
    │   │   └── DashboardController.java
    │   └── util/
    │       ├── ValidationUtils.java ← CNIC, username, password, amount, phone, age
    │       ├── PasswordHasher.java  ← BCrypt hash + verify
    │       └── CurrencyFormatter.java ← cents ↔ "$1,050.50"
    └── resources/
        ├── fxml/
        │   ├── login.fxml
        │   ├── register.fxml
        │   └── dashboard.fxml
        ├── css/
        │   ├── light.css            ← Navy + teal professional theme
        │   └── dark.css             ← Charcoal + teal dark theme
        └── migrations/
            └── V1__init_schema.sql  ← Full DB schema
```

---

## ✅ Features

### 1. User Registration & Authentication
- 3-step wizard: Personal Info → Credentials → Account Type
- Real-time password strength meter (Weak / Medium / Strong)
- BCrypt password hashing (work factor 12) — plaintext never stored
- Inline field-level validation (CNIC format, phone, age 18–100, username rules)

### 2. Dashboard
- Live balance card with masked account number
- Monthly stats (deposits & withdrawals for the **current month only**)
- 5 most recent transactions with type badges
- Quick action buttons: Deposit, Withdraw, Transfer, Pay Bill, Apply Loan

### 3. Deposit & Withdrawal
- Quick-select amount buttons ($50 / $100 / $500 / $1000)
- Large transaction confirmation dialog (≥ $1,000)
- Daily withdrawal limits: Savings $5,000 | Current $20,000
- Atomic JDBC transactions with `BEGIN IMMEDIATE` + rollback on failure

### 4. Fund Transfer
- Transfer funds to any account by account number
- Atomic debit + credit in a single database transaction
- Confirmation dialog before execution
- Both sender and recipient get a transaction log entry

### 5. Transaction History
- Full history with filter by type: All / Deposit / Withdrawal / Transfer
- **PDF Statement Export** — choose a folder, generates a formatted multi-page PDF

### 6. Utility Bill Payments
- Pay Electricity, Water, Gas, or Internet bills
- Toggle-button bill type selector with due date picker
- Full payment history displayed below the form

### 7. Loan Services
- Loan types: Home (6.2%), Auto (7.8%), Personal (8.5%), Education (5.5%)
- Interactive sliders for principal ($1k–$100k) and term (12–60 months)
- Live EMI calculator: `P·r·(1+r)^n / ((1+r)^n - 1)`
- Application submitted with "Under Review" status

### 8. Settings
- Dark / Light theme toggle
- Account information display

---

## 🗄️ Database Schema

SQLite, auto-migrated via `PRAGMA user_version`. Schema in `V1__init_schema.sql`.

```sql
users          — user_id, username (UNIQUE), password (BCrypt), age, cnic, address, phone
accounts       — account_id, user_id, account_type, account_number (UNIQUE), balance (INTEGER cents)
transactions   — transaction_id, account_id, type, amount (cents), description, timestamp
utility_bills  — bill_id, user_id, bill_type, amount (cents), status, due_date, payment_date, ts
loans          — loan_id, user_id, account_id, loan_type, principal_amount, interest_rate,
                 loan_term_months, outstanding_balance, status, created_at
daily_limits   — account_id, spent_today (cents), last_reset_date  [schema ready]
security_events— event_id, user_id, account_id, event_type, details, timestamp  [schema ready]
```

> All monetary values stored as **INTEGER cents** (e.g. $10.50 → `1050`) — no float precision issues.

---

## 🏗️ Architecture Notes

- **ServiceResult\<T\>** — Java record returned by all service methods. Controllers check `result.success()` and display `result.errorMessage()` inline. No exceptions thrown to the UI layer.
- **BEGIN IMMEDIATE** — all balance-changing operations use explicit SQLite transactions with immediate locking.
- **SessionManager** — thread-safe double-checked locking singleton holding the logged-in `User` and `Account`.
- **Cents storage** — `CurrencyFormatter.toCents()` / `format()` handle all conversions at the UI boundary.
- **Single-page dashboard** — `DashboardController` swaps panels into a `StackPane` (`contentArea`) without reloading the FXML scene.
