package client.controller;

import client.model.Item;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class AuctionDetailController {
    @FXML private Label productNameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label leadingBidderLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label statusLabel;
    @FXML private Label bidCountLabel;

    public void setItemData(Item item) {
        productNameLabel.setText(item.getTitle());
        categoryLabel.setText("Category: " + item.getCategory());
        descriptionLabel.setText(item.getDescription());
        startPriceLabel.setText(String.valueOf(item.getStartPrice()));
        currentPriceLabel.setText(String.valueOf(item.getCurrentPrice()));
        leadingBidderLabel.setText(item.getLeader());
        startTimeLabel.setText(item.getStartTime().toString());
        endTimeLabel.setText(item.getEndTime().toString());
        statusLabel.setText(item.getStatus());
        bidCountLabel.setText(item.getBidCount() + " bids");
    }

    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleBack(ActionEvent event) {
        loadScene("/view/bidder-dashboard.fxml", event);
    }

    @FXML
    private void handleJoinAuction(ActionEvent event) {
        loadScene("/view/biding-view.fxml", event);
    }
}
