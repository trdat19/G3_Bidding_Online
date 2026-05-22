package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SellerWalletPopupController {

    @FXML private Label balanceLabel;
    @FXML private Label soldRevenueLabel;
    @FXML private Label soldCountLabel;
    @FXML private Label productCountLabel;
    @FXML private Label walletMessageLabel;

    @FXML
    private void initialize() {
        balanceLabel.setText("$0.00");
        soldRevenueLabel.setText("$0.00");
        soldCountLabel.setText("0");
        productCountLabel.setText("0");
        walletMessageLabel.setText("");
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