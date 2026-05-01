package client.controller;

import client.model.Item;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    @FXML private Label timeLeftLabel;
    private Item currentItem;
    private Timeline countdownTimeLine;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public void setItemData(Item item) {
        productNameLabel.setText(item.getTitle());
        categoryLabel.setText("Category: " + item.getCategory());
        descriptionLabel.setText(item.getDescription());
        startPriceLabel.setText(String.valueOf(item.getStartPrice()));
        currentPriceLabel.setText(String.valueOf(item.getCurrentPrice()));
        leadingBidderLabel.setText(item.getLeader());
        startTimeLabel.setText(item.getStartTime().format(formatter));
        endTimeLabel.setText(item.getEndTime().format(formatter));
        statusLabel.setText(item.getStatus());
        bidCountLabel.setText(item.getBidCount() + " bids");

        this.currentItem = item;
        startCountDown(item.getEndTime());
    }
    private void startCountDown(LocalDateTime endTime) {
        if (countdownTimeLine != null) {
            countdownTimeLine.stop();
        }
        countdownTimeLine = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateTimeLeft(endTime))
        );
        countdownTimeLine.setCycleCount(Timeline.INDEFINITE);
        countdownTimeLine.play();
        updateTimeLeft(endTime);
    }
    private void updateTimeLeft(LocalDateTime endTime) {
        java.time.Duration remaining = java.time.Duration.between(LocalDateTime.now(), endTime);
        long seconds = remaining.getSeconds();
        if (seconds <= 0) {
            timeLeftLabel.setText("00:00:00");
            statusLabel.setText("Finishes");

            if (countdownTimeLine != null) {
                countdownTimeLine.stop();
            }

            return;
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        timeLeftLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
    }


    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/bidding-view.fxml"));
            Parent root = loader.load();

            BiddingViewController controller = loader.getController();
            controller.setItem(currentItem);
            if (countdownTimeLine != null) {
                countdownTimeLine.stop();
            }
            Stage stage = (Stage) productNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

