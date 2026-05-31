package client.controller;

import client.service.ClientNetworkService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.math.BigDecimal;

/**
 * Điều khiển ví người dùng
 */
public class WalletPopupController {

    @FXML private Label walletBalanceLabel;
    @FXML private TextField depositAmountField;
    @FXML private Label walletMessageLabel;



    @FXML
    private void initialize() {
        loadBalance();
    }

    private void loadBalance() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_WALLET, null));

        if (response != null && response.isSuccess() && response.getData() != null) {
            BigDecimal balance = (BigDecimal) response.getData();
            walletBalanceLabel.setText("$" + balance.toPlainString());
        } else {
            walletMessageLabel.setText(response != null ? response.getMessage() : "Không kết nối được server");
        }
    }

    @FXML
    private void handleQuickDeposit50() {
        depositAmountField.setText("50");
    }

    @FXML
    private void handleQuickDeposit100() {
        depositAmountField.setText("100");
    }

    @FXML
    private void handleQuickDeposit500() {
        depositAmountField.setText("500");
    }

    @FXML
    private void handleQuickDeposit1000() {
        depositAmountField.setText("1000");
    }

    @FXML
    private void handleDeposit() {
        try {
            BigDecimal amount = new BigDecimal(depositAmountField.getText().trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                walletMessageLabel.setText("Số tiền nạp phải lớn hơn 0");
                return;
            }

            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.DEPOSIT_WALLET, amount));

            if (response != null && response.isSuccess()) {
                BigDecimal newBalance = (BigDecimal) response.getData();
                walletBalanceLabel.setText("$" + newBalance.toPlainString());
                walletMessageLabel.setText("Nạp tiền thành công");
                depositAmountField.clear();
            } else {
                walletMessageLabel.setText(response != null ? response.getMessage() : "Không kết nối được server");
            }

        } catch (Exception e) {
            walletMessageLabel.setText("Số tiền không hợp lệ");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) walletBalanceLabel.getScene().getWindow();
        stage.close();
    }

}