package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import client.service.ClientNetworkService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.math.BigDecimal;

public class SellerWalletPopupController {

    @FXML private Label balanceLabel;
    @FXML private Label soldRevenueLabel;
    @FXML private Label soldCountLabel;
    @FXML private Label productCountLabel;
    @FXML private Label walletMessageLabel;

    @FXML
    private void initialize() {
        loadWallet();
        soldRevenueLabel.setText("$0.00");
        soldCountLabel.setText("0");
        productCountLabel.setText("0");
        walletMessageLabel.setText("");
    }

    private void loadWallet() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_WALLET, null));

        if (response != null && response.isSuccess() && response.getData() != null) {
            BigDecimal balance = new BigDecimal(response.getData().toString());
            balanceLabel.setText("$" + balance.toPlainString());
        } else {
            balanceLabel.setText("$0.00");
            walletMessageLabel.setText(response != null ? response.getMessage() : "Không kết nối được server");
        }
    }

    @FXML
    private void handleWithdraw() {
        walletMessageLabel.setText("Chức năng rút tiền chưa nối server/database.");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) balanceLabel.getScene().getWindow();
        stage.close();
    }
}