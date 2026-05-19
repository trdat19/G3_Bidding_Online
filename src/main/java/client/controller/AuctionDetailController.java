package client.controller;

import client.model.Item;
import client.util.StageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    private Timeline countdownTimeline;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Item currentItem;

    public void setItemData(Item item) {
        productNameLabel.setText(item.getTitle());
        categoryLabel.setText("Category: " + item.getCategory());
        descriptionLabel.setText(item.getDescription());
        startPriceLabel.setText(String.valueOf(item.getStartPrice()));
        currentPriceLabel.setText(String.valueOf(item.getCurrentPrice()));
        leadingBidderLabel.setText(item.getLeader());
        startTimeLabel.setText(formatDateTime(item.getStartTime()));
        endTimeLabel.setText(formatDateTime(item.getEndTime()));
        bidCountLabel.setText(item.getBidCount() + " bids");
        startCountdown(item.getStartTime(), item.getEndTime());

        this.currentItem = item;
    }

    private void loadScene(String fxmlPath, ActionEvent event) {
        stopCountdown();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            StageUtils.setMaximizedScene(stage, root);
            stage.show();
            ;
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
        stopCountdown();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/bidding-view.fxml"));
            Parent root = loader.load();

            BiddingViewController controller = loader.getController();
            controller.setItem(currentItem);

            Stage stage = (Stage) productNameLabel.getScene().getWindow();
            StageUtils.setMaximizedScene(stage, root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "--/--/---- --:--";
        }

        return dateTime.format(dateTimeFormatter);
    }

    private void startCountdown(LocalDateTime startTime, LocalDateTime endTime) {
        stopCountdown();

        updateAuctionTimeUI(startTime, endTime);

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event ->
                        updateAuctionTimeUI(startTime, endTime)
                )
        );

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateAuctionTimeUI(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startTime != null && now.isBefore(startTime)) {
            statusLabel.setText("Sắp diễn ra");
            timeLeftLabel.setText(formatDuration(now, startTime));
            return;
        }

        if (endTime != null && now.isBefore(endTime)) {
            statusLabel.setText("Đang diễn ra");
            timeLeftLabel.setText(formatDuration(now, endTime));
            return;
        }

        statusLabel.setText("Đã kết thúc");
        timeLeftLabel.setText("00:00:00");
    }

    private String formatDuration(LocalDateTime from, LocalDateTime to) {
        java.time.Duration remaining = java.time.Duration.between(from, to);
        long seconds = remaining.getSeconds();

        if (seconds <= 0) {
            return "00:00:00";
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private String formatTimeLeft(LocalDateTime endTime) {
        if (endTime == null) {
            return "--:--:--";
        }

        java.time.Duration remaining =
                java.time.Duration.between(LocalDateTime.now(), endTime);

        long seconds = remaining.getSeconds();

        if (seconds <= 0) {
            return "00:00:00";
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
