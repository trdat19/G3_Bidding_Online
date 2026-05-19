package client.controller;

import client.model.Item;
import client.util.StageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    @FXML private Label statusTextLabel;
    @FXML private ImageView productImageView;
    @FXML private Label imagePlaceholderLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;

    private Item currentItem;
    private Timeline countdownTimeLine;
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public void setItem(Item item) {
        this.currentItem = item;

        nameLabel.setText(item.getTitle());
        categoryLabel.setText(item.getCategory());
        descriptionLabel.setText(item.getDescription());
        currentPriceLabel.setText(String.valueOf(item.getStartPrice()));
        leaderLabel.setText(item.getLeader());
        bidCountLabel.setText(String.valueOf(item.getBidCount()));
        startCountDown(item.getEndTime());
        setProductImage(item.getImageUrl());
        startTimeLabel.setText(formatDateTime(item.getStartTime()));
        endTimeLabel.setText(formatDateTime(item.getEndTime()));
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
            statusTextLabel.setText("Finishes");

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
    private void setProductImage(String imageUrl) {
        boolean hasImage = imageUrl != null && !imageUrl.isBlank();

        productImageView.setVisible(hasImage);
        productImageView.setManaged(hasImage);
        imagePlaceholderLabel.setVisible(!hasImage);
        imagePlaceholderLabel.setManaged(!hasImage);

        if (hasImage) {
            productImageView.setImage(new Image(imageUrl, true));
        }
    }

    private String formatDateTime(LocalDateTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "--/--/---- --:--";
    }
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction-detail.fxml"));
            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setItemData(currentItem);
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            StageUtils.setMaximizedScene(stage, root);
            stage.show();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML
    private void handlePlaceBid() {

    }
}
