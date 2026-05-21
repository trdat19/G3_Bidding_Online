package client.controller;

import client.model.Item;
import client.service.ClientNetworkService;
import client.state.ClientSession;
import client.util.StageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import javafx.util.Duration;
import shared.dto.common.AuctionDTO;
import shared.dto.response.BaseResponse;
import shared.dto.request.BaseRequest;
import shared.enums.Action;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidderDashboardController {
    @FXML
    private FlowPane auctionContainer;

    @FXML
    private Label bidderNameLabel;

    @FXML
    private Label walletBalanceLabel;

    @FXML
    private TextField depositAmountField;

    @FXML
    private Label walletMessageLabel;

    private final List<Item> itemList = new ArrayList<>();
    private final List<CountdownView> countdownViews = new ArrayList<>();
    private Timeline countdownTimeline;

    @FXML
    public void initialize() {
        bidderNameLabel.setText(ClientSession.getFullName());
        loadAuctionsFromServer();
    }
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            bidderNameLabel.setText("Bidder");
        }
        else {
            bidderNameLabel.setText(fullName);
        }
    }
    private void loadAuctionsFromServer() {
        BaseRequest request = new BaseRequest(Action.GET_AUCTION_LIST, null);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);
        itemList.clear();
        countdownViews.clear();
        if (response == null || !response.isSuccess() || response.getData() == null) {
            stopCountdownTimer();
            auctionContainer.getChildren().clear();
            auctionContainer.getChildren().add (new Label(
                    response != null ? response.getMessage() : "Khong ket noi duoc server"
            ));
            return;
        }
        List<AuctionDTO> auctions = (List<AuctionDTO>) response.getData();
        for (AuctionDTO auction : auctions) {
            itemList.add(toItem(auction));
        }
        loadAuctions();
    }
    private void loadAuctions() {
        auctionContainer.getChildren().clear();
        countdownViews.clear();
        for (Item item : itemList) {
            auctionContainer.getChildren().add(createProductCard(item));
        }
        startCountdownTimer();
    }
    private Item toItem(AuctionDTO auction) {
        Item item = new Item(
                auction.getItemName(),
                auction.getItemCategory(),
                auction.getItemDescription(),
                auction.getStartPrice()!= null ? auction.getStartPrice().doubleValue(): 0,
                auction.getDisplayPrice()!= null ? auction.getDisplayPrice().doubleValue() : 0,
                auction.getLeaderName()!= null ? auction.getLeaderName() : "Chưa có",
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStatus()!= null ? auction.getStatus().name() : "",
                auction.getBidCount()
        );
        item.setImageUrl(auction.getItemImageUrl());
        return item;
    }

    private VBox createProductCard(Item item) {
        VBox card = new VBox();
        card.setPrefWidth(330);
        card.setSpacing(16);
        card.getStyleClass().add("auction-card");

        StackPane imageBox = new StackPane();
        imageBox.setPrefHeight(210);
        imageBox.setPrefWidth(330);
        imageBox.getStyleClass().add("image-box");

        Label categoryBadge = new Label(item.getCategory());
        categoryBadge.getStyleClass().add("category-badge");
        StackPane.setAlignment(categoryBadge, Pos.TOP_LEFT);
        StackPane.setMargin(categoryBadge, new Insets(12, 0, 0, 12));

        Label statusBadge = new Label(item.getStatus());
        statusBadge.getStyleClass().add("status-pill");
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(12, 12, 0, 0));

        if(item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            ImageView imageView = new ImageView(new Image(item.getImageUrl(), true));
            imageView.setFitWidth(330);
            imageView.setFitHeight(210);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageBox.getChildren().add(imageView);
        }
        else {
            Label imagePlaceholder = new Label("Image");
            imagePlaceholder.getStyleClass().add("image-placeholder");
            imageBox.getChildren().add(imagePlaceholder);
        }

        imageBox.getChildren().addAll(categoryBadge, statusBadge);

        VBox infoBox = new VBox();
        infoBox.setSpacing(8);

        Label titleLabel = new Label(item.getTitle());
        titleLabel.getStyleClass().add("item-title");

        Label descLabel = new Label(item.getDescription());
        descLabel.getStyleClass().add("item-desc");
        descLabel.setWrapText(true);

        infoBox.getChildren().addAll(titleLabel, descLabel);

        HBox metaBox = new HBox();
        metaBox.setSpacing(16);
        metaBox.getStyleClass().add("quick-meta-box");

        VBox priceBox = new VBox();
        priceBox.setSpacing(4);

        Label priceText = new Label("GIÁ HIỆN TẠI");
        priceText.getStyleClass().add("meta-label");

        Label priceValue = new Label(item.getCurrentPrice() + "$");
        priceValue.getStyleClass().add("price-label");

        priceBox.getChildren().addAll(priceText, priceValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox timeBox = new VBox();
        timeBox.setSpacing(4);
        timeBox.setAlignment(Pos.CENTER_RIGHT);

        Label timeText = new Label("CÒN LẠI");
        timeText.getStyleClass().add("meta-label");

        Label timeValue = new Label(formatTimeLeft(getCountdownTarget(item)));
        timeValue.getStyleClass().add("time-left");
        countdownViews.add(new CountdownView(timeText, timeValue, item));

        timeBox.getChildren().addAll(timeText, timeValue);

        metaBox.getChildren().addAll(priceBox, spacer, timeBox);

        Button detailButton = new Button("Xem chi tiết");
        detailButton.getStyleClass().add("detail-button");
        detailButton.setMaxWidth(Double.MAX_VALUE);
        detailButton.setOnAction(event -> viewDetail(item));

        card.getChildren().addAll(
                imageBox,
                infoBox,
                metaBox,
                detailButton
        );

        return card;
    }
    @FXML private void handleOpenWalletPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/wallet-popup.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ví");
            stage.show();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML private void handleRefresh() {
        loadAuctionsFromServer();
    }

    private void startCountdownTimer() {
        stopCountdownTimer();
        if (countdownViews.isEmpty()) {
            return;
        }

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateCountdowns())
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
        updateCountdowns();
    }
    private void updateCountdowns() {
        for (CountdownView countdownView : countdownViews) {
            countdownView.label.setText(getCountdownTitle(countdownView.item));
            countdownView.valueLabel.setText(formatTimeLeft(getCountdownTarget(countdownView.item)));
        }
    }
    private void stopCountdownTimer() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }
    private String formatTimeLeft(LocalDateTime endTime) {
        if (endTime == null) {
            return "--:--:--";
        }

        java.time.Duration remaining = java.time.Duration.between(
                LocalDateTime.now(),
                endTime
        );

        long seconds = remaining.getSeconds();

        if (seconds <= 0) {
            return "00:00:00";
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            stopCountdownTimer();

            BaseRequest logoutRequest = new BaseRequest(Action.LOGOUT, null);
            BaseResponse response = ClientNetworkService.getInstance().sendRequest(logoutRequest);

            if (response != null && response.isSuccess()) {
                ClientSession.clear();

                Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                StageUtils.setMaximizedScene(stage, root);
                stage.show();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Đăng xuất thất bại!");
                alert.setHeaderText(null);
                alert.setContentText(response != null
                                    ? response.getMessage()
                                    : "Không kết nối được server!");
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void viewDetail(Item item) {
        try {
            stopCountdownTimer();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction-detail.fxml"));
            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setItemData(item);

            Stage stage = (Stage) auctionContainer.getScene().getWindow();
            Stage popup = new Stage();
            popup.initOwner(stage);
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setTitle("Chi tiết phiên đấu giá");
            popup.setScene(new Scene(root, 1040, 760));
            popup.setMinWidth(960);
            popup.setMinHeight(680);
            popup.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class CountdownView {
        private final Label label;
        private final Label valueLabel;
        private final Item item;

        private CountdownView(Label label, Label valueLabel, Item item) {
            this.label = label;
            this.valueLabel = valueLabel;
            this.item = item;
        }
    }
    //Các helper để đổi UI theo từng trường hợp khi chưa bắt đầu đấu giá, khi đã bdau
    private boolean isBeforeStart(Item item) {
        return item.getStartTime() != null && LocalDateTime.now().isBefore(item.getStartTime());
    }

    private LocalDateTime getCountdownTarget(Item item) {
        return isBeforeStart(item) ? item.getStartTime() : item.getEndTime();
    }

    private String getCountdownTitle(Item item) {
        return isBeforeStart(item) ? "BẮT ĐẦU SAU" : "CÒN LẠI";
    }
}
