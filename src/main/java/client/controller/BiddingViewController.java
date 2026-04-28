package client.controller;

import client.model.Item;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

public class BiddingViewController {
    @FXML private Label nameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label leaderLabel;
    @FXML private Label bidCountLabel;
    @FXML private Label timeLeftLabel;
    @FXML private Label messageLabel;
    @FXML private TextField bidAmountField;
    @FXML private TableView<?> bidTable;
    public void setItem(Item item) {

    }
    @FXML
    private void handleBack() {

    }
    @FXML
    private void handlePlaceBid() {

    }
}
