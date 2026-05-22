package server.service;

import server.dao.UserDAO;
import shared.exception.InsufficientBalanceException;

import java.math.BigDecimal;

public class WalletService {
    private static WalletService instance;
    private final UserDAO userDAO = new UserDAO();

    private WalletService() {}

    public static WalletService getInstance() {
        if (instance == null) {
            instance = new WalletService();
        }
        return instance;
    }

    public BigDecimal getBalance(Long userId) {
        return userDAO.getBalance(userId);
    }

    public BigDecimal deposit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
        }

        boolean ok = userDAO.increaseBalance(userId, amount);
        if (!ok) {
            throw new RuntimeException("Không thể nạp tiền");
        }

        return userDAO.getBalance(userId);
    }

    public void checkCanBid(Long userId, BigDecimal amount) {
        BigDecimal balance = userDAO.getBalance(userId);

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(balance);
        }
    }

    public BigDecimal payForWinningBid(Long userId, BigDecimal amount) {
        boolean ok = userDAO.decreaseBalanceIfEnough(userId, amount);

        if (!ok) {
            throw new InsufficientBalanceException(userDAO.getBalance(userId));
        }

        return userDAO.getBalance(userId);
    }
}