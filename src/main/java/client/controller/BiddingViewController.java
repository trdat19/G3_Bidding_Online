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
import server.model.core.Bid;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.dto.common.AuctionDTO;
import javafx.application.Platform;
import shared.dto.common.BidDTO;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
    @FXML private TableView<BidDTO> bidTable;
    @FXML private TableColumn<BidDTO, Void> indexColumn;
    @FXML private TableColumn<BidDTO, String> amountColumn;
    @FXML private TableColumn<BidDTO, String> bidderColumn;
    @FXML private TableColumn<BidDTO, String> timeColumn;
    @FXML private Label statusTextLabel;
    @FXML private ImageView productImageView;
    @FXML private Label imagePlaceholderLabel;


    private Item currentItem;
    private Timeline countdownTimeLine;
    private final Consumer<BaseResponse> realtimeListener = this::handleRealtimeEvent;
    private final ObservableList<BidDTO> bidHistory = FXCollections.observableArrayList();
    private final DateTimeFormatter bidTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    private void initialize()
    {


        bidderColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getBidderName())
        );;
        amountColumn.setCellValueFactory(cell->
                new SimpleStringProperty(cell.getValue().getAmount().toPlainString())
        );
        timeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getTimestamp() != null
                        ? cell.getValue().getTimestamp().format(bidTimeFormatter) : ""
                )
        );
        indexColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        bidTable.setItems(bidHistory);
    }

    public void setItem(Item item) {
        this.currentItem = item;
        setProductImage(item.getImageUrl());

        nameLabel.setText(item.getTitle());
        categoryLabel.setText(item.getCategory());
        descriptionLabel.setText(item.getDescription());
        currentPriceLabel.setText(String.valueOf(item.getCurrentPrice()));
        leaderLabel.setText(item.getLeader());
        bidCountLabel.setText(String.valueOf(item.getBidCount()));
        startCountDown(item.getEndTime());
        ClientNetworkService.getInstance().addEventListener(realtimeListener);
        ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest("SUBSCRIBE_AUCTION", currentItem.getId()));
        loadBidHistory();
    }

    private void loadBidHistory() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest("GET_BID_HISTORY", currentItem.getId()));

        if (response == null || !response.isSuccess() || response.getData() == null) {
            bidHistory.clear();
            return;
        }

        bidHistory.setAll((List<BidDTO>) response.getData());
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
        ClientNetworkService.getInstance().removeEventListener(realtimeListener);
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

    private void handleRealtimeEvent(BaseResponse response) {
        if (response.getAction() == null || currentItem == null) {
            return;
        }

        Platform.runLater(() -> {
            switch (response.getAction()) {
                case "NEW_BID" -> handleNewBidEvent(response);
                case "AUCTION_EXTENDED" -> handleAuctionExtendedEvent(response);
                case "AUCTION_FINISHED" -> handleAuctionFinishedEvent(response);
                case "AUCTION_CANCELLED" -> handleAuctionCancelledEvent(response);
                case "AUCTION_STARTED" -> statusTextLabel.setText("RUNNING");
            }
        });
    }

    private void handleNewBidEvent(BaseResponse response) {
        BidDTO bid = (BidDTO) response.getData();

        if (!bid.getAuctionId().equals(currentItem.getId())) {
            return;
        }

        currentItem.setCurrentPrice(bid.getAmount().doubleValue());
        currentItem.setLeader(bid.getBidderName());

        currentPriceLabel.setText(String.valueOf(currentItem.getCurrentPrice()));
        leaderLabel.setText(currentItem.getLeader());

        bidHistory.add(0, bid);

        refreshAuctionDetail();
    }

    private void handleAuctionExtendedEvent(BaseResponse response) {
        LocalDateTime newEndTime = (LocalDateTime) response.getData();

        currentItem.setEndTime(newEndTime);
        startCountDown(newEndTime);

        messageLabel.setText(response.getMessage());
    }

    private void handleAuctionFinishedEvent(BaseResponse response) {
        statusTextLabel.setText("FINISHED");
        timeLeftLabel.setText("00:00:00");
        messageLabel.setText(response.getMessage());

        if (countdownTimeLine != null) {
            countdownTimeLine.stop();
        }
    }

    private void handleAuctionCancelledEvent(BaseResponse response) {
        statusTextLabel.setText("CANCELLED");
        messageLabel.setText(response.getMessage());

        if (countdownTimeLine != null) {
            countdownTimeLine.stop();
        }
    }

    private void setProductImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            productImageView.setImage(null);
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        Image image = new Image(imageUrl, true);
        productImageView.setImage(image);
        imagePlaceholderLabel.setVisible(false);
    }

}
