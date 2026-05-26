package client.session;

import server.model.user.User;

import java.math.BigDecimal;

public final class ClientSession {
    private static User currentUser;
    private static BigDecimal walletBalance = BigDecimal.ZERO;

    private ClientSession() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public static String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }

    public static BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public static void setWalletBalance(BigDecimal balance) {
        walletBalance = balance != null ? balance : BigDecimal.ZERO;
    }

    public static void clear() {
        currentUser = null;
        walletBalance = BigDecimal.ZERO;
    }
}