package client.controller;

import client.model.Item;
import client.util.StageUtils;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
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
    @FXML private ImageView imageView;
    @FXML private Label imagePlaceholderLabel;
    private Item currentItem;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setItemData(Item item) {
        productNameLabel.setText(item.getTitle());
        categoryLabel.setText("Category: " + item.getCategory());
        descriptionLabel.setText(item.getDescription());
        startPriceLabel.setText(String.valueOf(item.getStartPrice()));
        currentPriceLabel.setText(String.valueOf(item.getCurrentPrice()));
        leadingBidderLabel.setText(item.getLeader());
        startTimeLabel.setText(formatDateTime(item.getStartTime()));
        endTimeLabel.setText(formatDateTime(item.getEndTime()));
        statusLabel.setText(item.getStatus());
        bidCountLabel.setText(item.getBidCount() + " bids");
        setProductImage(item.getImageUrl());

        this.currentItem = item;
    }
    private void setProductImage(String imageUrl) {
        boolean hasImage = imageUrl != null && !imageUrl.isBlank();

        imageView.setVisible(hasImage);
        imageView.setManaged(hasImage);
        imagePlaceholderLabel.setVisible(!hasImage);
        imagePlaceholderLabel.setManaged(!hasImage);

        if (hasImage) {
            imageView.setImage(new Image(imageUrl, true));
        }
    }
    private String formatDateTime(LocalDateTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "--/--/---- --:--";
    }

    private void loadScene(String fxmlPath, ActionEvent event) {
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
        if (currentItem.getStartTime() != null
                && LocalDateTime.now().isBefore(currentItem.getStartTime())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Phiên đấu giá chưa bắt đầu!");
            alert.showAndWait();
            return;
        }
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
}

