package client.controller;

import client.model.Item;
import client.util.StageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Duration;
import client.service.ClientNetworkService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.dto.common.AuctionDTO;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.time.LocalDateTime;

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
    private Item currentItem;
    private Timeline countdownTimeLine;
    public void setItem(Item item) {
        this.currentItem = item;

        nameLabel.setText(item.getTitle());
        categoryLabel.setText(item.getCategory());
        descriptionLabel.setText(item.getDescription());
        currentPriceLabel.setText(String.valueOf(item.getCurrentPrice()));
        leaderLabel.setText(item.getLeader());
        bidCountLabel.setText(String.valueOf(item.getBidCount()));
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
        if(currentItem == null || currentItem.getId() == null)
        {
            messageLabel.setText("Không tìm thấy phiên đâ giá");
            return;
        }
        String amountText = bidAmountField.getText().trim();

        if (amountText.isEmpty()) {
            messageLabel.setText("Vui lòng nhập giá đấu.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("auctionId", currentItem.getId());
        data.put("amount", amountText);

        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest("PLACE_BID", data));

        if (response != null && response.isSuccess()) {
            messageLabel.setText("Đặt giá thành công.");
            bidAmountField.clear();
            refreshAuctionDetail();
        } else {
            messageLabel.setText(response != null ? response.getMessage() : "Không kết nối được server.");
        }

    }

    private void refreshAuctionDetail() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest("GET_AUCTION_DETAILS", currentItem.getId()));

        if (response == null || !response.isSuccess() || response.getData() == null) {
            return;
        }

        AuctionDTO auction = (AuctionDTO) response.getData();

        currentItem.setCurrentPrice(
                auction.getDisplayPrice() != null ? auction.getDisplayPrice().doubleValue() : 0
        );
        currentItem.setLeader(
                auction.getLeaderName() != null ? auction.getLeaderName() : "Chưa có"
        );

        currentItem.setBidCount(auction.getBidCount());

        currentPriceLabel.setText(String.valueOf(currentItem.getCurrentPrice()));
        leaderLabel.setText(currentItem.getLeader());
        bidCountLabel.setText(currentItem.getBidCount() + " bids");
    }
}
