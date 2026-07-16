package com.bankapp.config;

import com.bankapp.model.Account;
import com.bankapp.model.User;

/**
 * Singleton session manager for the currently logged-in user.
 * Stores user and account context across the application lifecycle.
 */
public class SessionManager {
    private static volatile SessionManager instance;
    private User currentUser;
    private Account currentAccount;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) instance = new SessionManager();
            }
        }
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User u) { this.currentUser = u; }

    public Account getCurrentAccount() { return currentAccount; }
    public void setCurrentAccount(Account a) { this.currentAccount = a; }

    public void clearSession() {
        currentUser = null;
        currentAccount = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
