# PROJECT(OOPS)2K25 - Desktop Banking Application

An Object-Oriented Programming (OOP) desktop banking application built in Java, utilizing **Swing** for the user interface and **MySQL** for data persistence.

---

## 🛠️ Tools & Technologies Used

*   **Programming Language**: Java SE 21
*   **GUI Framework**: Java Swing & AWT (Abstract Window Toolkit) for designing windows, forms, and custom UI panels.
*   **Database**: MySQL Database Server (running locally on port 3306, default DB name: `bankdb`).
*   **Database Connector**: JDBC (Java Database Connectivity) with the MySQL Connector/J driver (`mysql-connector-j-9.3.0.jar`).
*   **IDE / Build Environment**: Eclipse IDE project configuration.

---

## 🚀 Features Implemented So Far

The application contains several features to manage user accounts, log transactions, pay bills, and apply for loans:

### 1. User Registration & Authentication
*   **User Registration (`RegisterForm.java`)**: Allows users to register by providing a Username, Password, Age, CNIC, and Address, and selecting an account type (Savings or Current). Upon registration, a unique bank account number (format: `AC<userId>_<timestamp>`) is generated with an initial balance of `0.00`.
*   **User Login (`LoginForm.java`)**: Authenticates users using their username and password stored in the database. Successful login routes the user to their Dashboard.

### 2. Main Dashboard (`Dashboard.java`)
*   Serves as the main navigation hub.
*   Displays the logged-in user's profile details (CNIC, Address, Age, Balance, and Account Type).
*   Provides buttons to trigger all actions: Create Account, Deposit, Withdraw, View Transaction History, Pay Utility Bill, and View Bill Payment History.

### 3. Banking & Transaction Management
*   **Deposits**: Users can add cash to their account. Updates the database balance and logs the activity as a "Deposit" type in the transactions history.
*   **Withdrawals**: Users can withdraw cash. 
    *   *Security / Rollback*: Includes database transaction isolation and row locking (`FOR UPDATE`) to verify sufficient funds before deducting. Logs the activity as a "Withdrawal" in transaction history.
*   **Transaction History (`TransactionHistory.java`)**: Displays a scrollable, timestamped list of all deposits and withdrawals with their respective amounts.

### 4. Utility Bill Payments
*   **Utility Bill Form (`UtilityBillPaymentForm.java`)**: Users can pay electricity, water, gas, or internet bills.
*   **Bill History (`BillPaymentHistoryForm.java`)**: View logs of paid utility bills, including amount, status (e.g., "Paid"), due date, and payment timestamp.

### 5. Loan Services
*   **Loan Application (`LoanApplicationForm.java`)**: Apply for different loan types (Personal, Home, Auto) with options to input the Principal Amount, Interest Rate (%), and Loan Term (in months). The outstanding balance matches the principal initially.

---

## 📂 Project Structure

```text
PROJECT(OOPS)2K25/
├── .classpath                   # Eclipse IDE classpath configuration
├── .project                     # Eclipse IDE project description
├── src/
│   ├── BalanceEnquiry.java      # Shell class for future balance inquiry logic
│   └── bank/                    # Main source package
│       ├── Main.java            # Entry point of the application
│       ├── DBConnection.java    # Database connection manager (MySQL JDBC configuration)
│       ├── User.java            # User data model
│       ├── Account.java         # Account data model
│       ├── Transaction.java     # Transaction data model
│       ├── Loan.java            # Loan data model
│       ├── LoanPayment.java     # Loan payment data model
│       ├── UtilityBill.java     # Utility bill data model
│       ├── LoginForm.java       # User authentication interface
│       ├── RegisterForm.java    # New account registration interface
│       ├── Dashboard.java       # Main user dashboard interface
│       ├── TransactionHistory.java   # Display window for deposits/withdrawals history
│       ├── UtilityBillPaymentForm.java  # Payment interface for utility bills
│       └── BillPaymentHistoryForm.java # Display window for bill payment history
```

---

## 🗄️ Database Setup (Reference Schema)

To run the application, configure your MySQL database `bankdb` with the following tables:

```sql
CREATE DATABASE bankdb;
USE bankdb;

-- 1. Users Table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    age INT,
    cnic VARCHAR(20),
    address VARCHAR(150)
);

-- 2. Accounts Table
CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    account_type VARCHAR(20),
    account_number VARCHAR(50) UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 3. Transactions Table
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT,
    type VARCHAR(20),
    amount DECIMAL(15, 2),
    description VARCHAR(100),
    ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

-- 4. Utility Bills Table
CREATE TABLE utility_bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    bill_type VARCHAR(30),
    amount DECIMAL(15, 2),
    status VARCHAR(20),
    due_date DATE,
    ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 5. Loans Table
CREATE TABLE loans (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    account_id INT,
    loan_type VARCHAR(20),
    principal_amount DECIMAL(15, 2),
    interest_rate DECIMAL(5, 2),
    loan_term_months INT,
    outstanding_balance DECIMAL(15, 2),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
```
