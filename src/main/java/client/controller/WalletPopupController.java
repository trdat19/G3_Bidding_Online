package client.controller;

import client.state.ClientSession;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WalletPopupController {
    @FXML private Label walletBalanceLabel;
    @FXML private TextField depositAmountField;
    @FXML private Label walletMessageLabel;

    @FXML
    public void initialize() {
        updateWalletBalance();
    }

    @FXML
    private void handleDeposit() {
        try {
            BigDecimal amount = parseDepositAmount(depositAmountField.getText());
            ClientSession.depositToWallet(amount);
            depositAmountField.clear();
            updateWalletBalance();
            setWalletMessage("Đã nạp " + formatMoney(amount) + " vào ví.", "wallet-message-success");
        } catch (IllegalArgumentException e) {
            setWalletMessage(e.getMessage(), "wallet-message-error");
        }
    }

    @FXML
    private void handleQuickDeposit50() {
        setQuickDepositAmount("50");
    }

    @FXML
    private void handleQuickDeposit100() {
        setQuickDepositAmount("100");
    }

    @FXML
    private void handleQuickDeposit500() {
        setQuickDepositAmount("500");
    }

    @FXML
    private void handleQuickDeposit1000() {
        setQuickDepositAmount("1000");
    }

    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void setQuickDepositAmount(String amount) {
        depositAmountField.setText(amount);
        setWalletMessage("Chọn mức nạp nhanh hoặc nhập số tiền.", null);
        depositAmountField.requestFocus();
        depositAmountField.positionCaret(depositAmountField.getText().length());
    }

    private BigDecimal parseDepositAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập số tiền cần nạp.");
        }

        String normalizedAmount = rawAmount.trim()
                .replace("$", "")
                .replace(" ", "");

        if (normalizedAmount.contains(",") && normalizedAmount.contains(".")) {
            normalizedAmount = normalizedAmount.replace(",", "");
        } else {
            normalizedAmount = normalizedAmount.replace(",", ".");
        }

        try {
            BigDecimal amount = new BigDecimal(normalizedAmount).setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0.");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số tiền không hợp lệ.");
        }
    }

    private void updateWalletBalance() {
        walletBalanceLabel.setText(formatMoney(ClientSession.getWalletBalance()));
    }

    private String formatMoney(BigDecimal amount) {
        return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void setWalletMessage(String message, String statusStyleClass) {
        walletMessageLabel.setText(message);
        walletMessageLabel.getStyleClass().removeAll("wallet-message-success", "wallet-message-error");
        if (statusStyleClass != null && !statusStyleClass.isBlank()) {
            walletMessageLabel.getStyleClass().add(statusStyleClass);
        }
    }
}
