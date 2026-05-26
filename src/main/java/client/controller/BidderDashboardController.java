package client.controller;

import client.model.Item;
import client.service.ClientNetworkService;
import client.session.ClientSession;
import client.util.StageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.math.BigDecimal;

import javafx.event.ActionEvent;
import javafx.util.Duration;
import shared.dto.common.AuctionDTO;
import shared.dto.response.BaseResponse;
import shared.dto.request.BaseRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import shared.enums.Action;

public class BidderDashboardController
{
    @FXML private FlowPane auctionContainer;

    @FXML private Label bidderNameLabel;

    @FXML private Label bidderWalletBalanceLabel;

    @FXML private Label sectionTitleLabel;

    @FXML private Label sectionSubtitleLabel;

    @FXML private Button homeButton;

    @FXML private Button wonAuctionsButton;

    private boolean showingWonAuctions = false;

    private final List<Item> itemList = new ArrayList<>();
    private final List<Timeline> countdownTimelines = new ArrayList<>();
    private final Consumer<BaseResponse> realtimeListener = this::handleRealtimeEvent;
    private Timeline refreshTimeLine;

    @FXML
    public void initialize() {
        bidderNameLabel.setText(ClientSession.getCurrentUserFullName());
        loadWalletBalance();
        loadAuctionsFromServer();
        loadWonAuctionsFromServer();

        ClientNetworkService.getInstance().addEventListener(realtimeListener);
        ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.SUBSCRIBE_AUCTION_LIST, null));
    }

    private void loadWalletBalance() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_WALLET, null));

        if (response != null && response.isSuccess() && response.getData() != null) {
            BigDecimal balance = new BigDecimal(response.getData().toString());
            bidderWalletBalanceLabel.setText("$" + balance.toPlainString());
        } else {
            bidderWalletBalanceLabel.setText("$0.00");
        }
    }

    @FXML
    private void handleRefresh() {
        loadAuctionsFromServer();
        loadWonAuctionsFromServer();
    }

    @FXML
    private void handleOpenWalletPopup(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/wallet-popup.fxml"));

            Stage popup = new Stage();
            popup.setTitle("Ví của tôi");
            popup.setScene(new Scene(root));
            popup.initOwner(((Node) event.getSource()).getScene().getWindow());
            popup.setOnHidden(e -> loadWalletBalance());

            popup.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRealtimeEvent(BaseResponse response) {
        if (!"AUCTION_LIST_CHANGED".equals(response.getAction()))
        {
            return;
        }
        Platform.runLater(() -> {
            loadAuctionsFromServer();
            loadWonAuctionsFromServer();
        });

    }

    private void loadAuctionsFromServer() {
        BaseRequest request = new BaseRequest(Action.GET_AUCTION_LIST, null);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        itemList.clear();
        stopCountdowns();
        auctionContainer.getChildren().clear();

        if (response == null || !response.isSuccess() || response.getData() == null) {
            auctionContainer.getChildren().add(new Label(
                    response != null ? response.getMessage() : "Khong ket noi duoc server"
            ));
            return;
        }

        List<?> auctions = (List<?>) response.getData();

        for (Object obj : auctions) {
            AuctionDTO auction = (AuctionDTO) obj;

            Item item = toItem(auction);

            itemList.add(item);
            auctionContainer.getChildren().add(createProductCard(item));
        }
    }
    @FXML
    private void handleShowHome() {
        showingWonAuctions = false;

        sectionTitleLabel.setText("Phiên đấu giá đang diễn ra");
        sectionSubtitleLabel.setText("Chọn một sản phẩm để xem chi tiết và tham gia đấu giá.");

        homeButton.getStyleClass().setAll("sidebar-menu-button-active");
        wonAuctionsButton.getStyleClass().setAll("sidebar-menu-button");

        loadAuctionsFromServer();
    }
    @FXML
    private void handleShowWonAuctions() {
        showingWonAuctions = true;

        sectionTitleLabel.setText("Sản phẩm đã thắng");
        sectionSubtitleLabel.setText("Những sản phẩm bạn đã thắng sau khi phiên đấu giá kết thúc.");

        homeButton.getStyleClass().setAll("sidebar-menu-button");
        wonAuctionsButton.getStyleClass().setAll("sidebar-menu-button-active");

        loadWonAuctionsFromServer();
    }
    private void loadWonAuctionsFromServer() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_WON_AUCTIONS, null));

        stopCountdowns();
        itemList.clear();
        auctionContainer.getChildren().clear();

        if (response == null || !response.isSuccess() || response.getData() == null) {
            showEmptyMessage("Bạn chưa thắng phiên đấu giá nào.");
            return;
        }

        List<?> auctions = (List<?>) response.getData();

        if (auctions.isEmpty()) {
            showEmptyMessage("Bạn chưa thắng phiên đấu giá nào.");
            return;
        }

        for (Object obj : auctions) {
            AuctionDTO auction = (AuctionDTO) obj;
            Item item = toItem(auction);

            itemList.add(item);
            auctionContainer.getChildren().add(createProductCard(item));
        }
    }
    private void showEmptyMessage(String message) {
        Label emptyLabel = new Label(message);
        emptyLabel.getStyleClass().add("section-subtitle");
        auctionContainer.getChildren().add(emptyLabel);
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
        item.setId(auction.getId());
        item.setImageUrl(auction.getItemImageUrl());
        item.setMinIncrement(auction.getMinIncrement() != null ? auction.getMinIncrement().doubleValue() : 0);
        item.setImageBytes(auction.getImageBytes());
        item.setImageContentType(auction.getImageContentType());
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

        Image image = null;

        if(item.getImageBytes() != null && item.getImageBytes().length > 0 ) {
            image = new Image(new ByteArrayInputStream(item.getImageBytes()));
        }
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(330);
            imageView.setFitHeight(210);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageBox.getChildren().add(imageView);
        } else {
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

        Label timeValue = new Label();
        timeValue.getStyleClass().add("time-left");
        startCountdown(timeText, timeValue, statusBadge, item.getStartTime(), item.getEndTime());

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
    private void stopCountdowns() {
        for (Timeline timeline : countdownTimelines) {
            timeline.stop();
        }
        countdownTimelines.clear();
    }

    private void startCountdown(Label titleLabel, Label valueLabel, Label statusLabel,
                                LocalDateTime startTime, LocalDateTime endTime) {
        updateAuctionTimeUI(titleLabel, valueLabel, statusLabel, startTime, endTime);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event ->
                        updateAuctionTimeUI(titleLabel, valueLabel, statusLabel, startTime, endTime)
                )
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        countdownTimelines.add(timeline);
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
    private void updateAuctionTimeUI(Label titleLabel, Label valueLabel, Label statusLabel,
                                     LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startTime != null && now.isBefore(startTime)) {
            statusLabel.setText("Sắp diễn ra");
            titleLabel.setText("BẮT ĐẦU SAU");
            valueLabel.setText(formatDuration(now, startTime));
            return;
        }

        if (endTime != null && now.isBefore(endTime)) {
            statusLabel.setText("Đang diễn ra");
            titleLabel.setText("CÒN LẠI");
            valueLabel.setText(formatDuration(now, endTime));
            return;
        }
        statusLabel.setText("Đã kết thúc");
        titleLabel.setText("ĐÃ KẾT THÚC");
        valueLabel.setText("00:00:00");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        stopAutoRefresh();
        stopCountdowns();
        ClientNetworkService.getInstance().removeEventListener(realtimeListener);
        ClientNetworkService.getInstance().sendRequest(new BaseRequest(Action.LOGOUT, null));
        ClientSession.clear();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            StageUtils.setMaximizedScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void viewDetail(Item item) {
        stopAutoRefresh();
        stopCountdowns();
        try {
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
    public void setBidderName(String bidderName) {
        bidderNameLabel.setText(bidderName);
    }
    private void startAutoRefresh()
    {
        refreshTimeLine = new Timeline(new KeyFrame(Duration.seconds(5), event -> loadAuctionsFromServer()));
        refreshTimeLine.setCycleCount(Timeline.INDEFINITE);
        refreshTimeLine.play();
    }

    private void stopAutoRefresh()
    {
        if(refreshTimeLine != null)
        {
            refreshTimeLine.stop();
            refreshTimeLine = null;
        }
    }

}