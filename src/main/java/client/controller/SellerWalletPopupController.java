package client.controller;

import client.service.ClientNetworkService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import shared.dto.common.SellerWalletDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.math.BigDecimal;

/**
 * Cửa sổ ví Seller
 */
public class SellerWalletPopupController {

    @FXML private Label balanceLabel;
    @FXML private Label soldRevenueLabel;
    @FXML private Label soldCountLabel;
    @FXML private Label walletMessageLabel;
    @FXML private TextField withdrawAmountField;
    @FXML private Button withdrawButton;

    private BigDecimal availableBalance = BigDecimal.ZERO;
    private Runnable onWalletUpdated;

    @FXML
    private void initialize() {
        walletMessageLabel.setText("");
        withdrawAmountField.textProperty().addListener(
                (observable, oldValue, newValue) -> updateWithdrawState());
        loadSummary();
    }

    public void setOnWalletUpdated(Runnable onWalletUpdated) {
        this.onWalletUpdated = onWalletUpdated;
    }

    private void loadSummary() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_SELLER_WALLET_SUMMARY, null));

        if (response != null && response.isSuccess()
                && response.getData() instanceof SellerWalletDTO summary) {
            applySummary(summary);
            return;
        }

        balanceLabel.setText("$0.00");
        soldRevenueLabel.setText("$0.00");
        soldCountLabel.setText("0");
        walletMessageLabel.setText(response != null
                ? response.getMessage()
                : "Không kết nối được server.");
    }

    @FXML
    private void handleWithdraw() {
        BigDecimal amount = parseAmount();
        if (amount == null) {
            return;
        }

        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.WITHDRAW_SELLER_WALLET, amount));

        if (response != null && response.isSuccess()
                && response.getData() instanceof SellerWalletDTO summary) {
            applySummary(summary);
            withdrawAmountField.clear();
            walletMessageLabel.setText("Rút tiền thành công.");
            if (onWalletUpdated != null) {
                onWalletUpdated.run();
            }
            return;
        }

        walletMessageLabel.setText(response != null
                ? response.getMessage()
                : "Không kết nối được server.");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) balanceLabel.getScene().getWindow();
        stage.close();
    }

    private void applySummary(SellerWalletDTO summary) {
        availableBalance = summary.getAvailableBalance();
        balanceLabel.setText("$" + availableBalance.toPlainString());
        soldRevenueLabel.setText("$" + summary.getTotalRevenue().toPlainString());
        soldCountLabel.setText(String.valueOf(summary.getSoldProductCount()));
        updateWithdrawState();
    }

    private void updateWithdrawState() {
        String input = withdrawAmountField.getText().trim();
        if (input.isEmpty()) {
            withdrawButton.setDisable(true);
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                withdrawButton.setDisable(true);
                walletMessageLabel.setText("Số tiền rút phải lớn hơn 0.");
                return;
            }
            if (amount.compareTo(availableBalance) > 0) {
                withdrawButton.setDisable(true);
                walletMessageLabel.setText("Số dư không đủ để rút số tiền này.");
                return;
            }
            withdrawButton.setDisable(false);
            walletMessageLabel.setText("");
        } catch (NumberFormatException e) {
            withdrawButton.setDisable(true);
            walletMessageLabel.setText("Số tiền rút không hợp lệ.");
        }
    }

    private BigDecimal parseAmount() {
        try {
            BigDecimal amount = new BigDecimal(withdrawAmountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                walletMessageLabel.setText("Số tiền rút phải lớn hơn 0.");
                return null;
            }
            if (amount.compareTo(availableBalance) > 0) {
                walletMessageLabel.setText("Số dư không đủ để rút số tiền này.");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            walletMessageLabel.setText("Số tiền rút không hợp lệ.");
            return null;
        }
    }
}
