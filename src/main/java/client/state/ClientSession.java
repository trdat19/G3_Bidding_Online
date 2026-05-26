package client.state;

import java.math.BigDecimal;

public class ClientSession {
    private static String fullName;
    private static BigDecimal walletBalance = BigDecimal.ZERO;

    public static String getFullName() {
        return fullName;
    }

    public static void setFullName(String fullName) {
        ClientSession.fullName = fullName;
    }

    public static BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public static void setWalletBalance(BigDecimal walletBalance) {
        ClientSession.walletBalance = walletBalance != null ? walletBalance : BigDecimal.ZERO;
    }

    public static void depositToWallet(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        walletBalance = walletBalance.add(amount);
    }

    public static void clear() {
        fullName = null;
        walletBalance = BigDecimal.ZERO;
    }
}
