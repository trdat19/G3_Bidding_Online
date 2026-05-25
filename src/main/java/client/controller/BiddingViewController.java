package client.controller;

import client.model.Item;
import client.util.StageUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import client.service.ClientNetworkService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.dto.common.AuctionDTO;
import javafx.application.Platform;
import shared.dto.common.BidDTO;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import shared.enums.Action;

import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.time.LocalDateTime;

public class BiddingViewController {
    @FXML
    private Label nameLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private Label leaderLabel;
    @FXML
    private Label bidCountLabel;
    @FXML
    private Label timeLeftLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField bidAmountField;
    @FXML
    private TableView<BidDTO> bidTable;
    @FXML
    private TableColumn<BidDTO, Void> indexColumn;
    @FXML
    private TableColumn<BidDTO, String> amountColumn;
    @FXML
    private TableColumn<BidDTO, String> bidderColumn;
    @FXML
    private TableColumn<BidDTO, String> timeColumn;
    @FXML
    private Label statusTextLabel;
    @FXML
    private ImageView productImageView;
    @FXML
    private Label imagePlaceholderLabel;
    @FXML
    private Label minIncrementLabel;
    @FXML
    private TextField autoBidIncrementField;
    @FXML
    private TextField autoBidMaxAmountField;
    @FXML
    private LineChart<Number, Number> priceHistoryChart;
    @FXML
    private NumberAxis priceChartXAxis;
    @FXML
    private NumberAxis priceChartYAxis;

    private final XYChart.Series<Number, Number> priceSeries = new XYChart.Series<>();


    private Item currentItem;
    private Timeline countdownTimeLine;
    private final Consumer<BaseResponse> realtimeListener = this::handleRealtimeEvent;
    private final ObservableList<BidDTO> bidHistory = FXCollections.observableArrayList();
    private final DateTimeFormatter bidTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private boolean returnedToDashboard = false;

    @FXML
    private void initialize() {


        bidderColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getBidderName())
        );
        ;
        amountColumn.setCellValueFactory(cell ->
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
        setupPriceHistoryChart();
    }

    public void setItem(Item item) {
        this.currentItem = item;
        setProductImage(item.getImageBytes());

        nameLabel.setText(item.getTitle());
        categoryLabel.setText(item.getCategory());
        descriptionLabel.setText(item.getDescription());
        currentPriceLabel.setText(String.valueOf(item.getCurrentPrice()));
        leaderLabel.setText(item.getLeader());
        bidCountLabel.setText(String.valueOf(item.getBidCount()));
        minIncrementLabel.setText(String.valueOf(item.getMinIncrement()));
        startCountDown(item.getEndTime());
        ClientNetworkService.getInstance().addEventListener(realtimeListener);
        ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.SUBSCRIBE_AUCTION, currentItem.getId()));
        loadBidHistory();
    }

    private void loadBidHistory() {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_BID_HISTORY, currentItem.getId()));

        if (response == null || !response.isSuccess() || response.getData() == null) {
            bidHistory.clear();
            return;
        }

        bidHistory.setAll((List<BidDTO>) response.getData());
        rebuildPriceChart();
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
            statusTextLabel.setText("FINISHED");

            if (countdownTimeLine != null) {
                countdownTimeLine.stop();
            }

            goToBidderDashboard();
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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handlePlaceBid() {
        if (currentItem == null || currentItem.getId() == null) {
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
                .sendRequest(new BaseRequest(Action.PLACE_BID, data));

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
                .sendRequest(new BaseRequest(Action.GET_AUCTION_DETAILS, currentItem.getId()));

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
        rebuildPriceChart();

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

        goToBidderDashboard();
    }

    private void handleAuctionCancelledEvent(BaseResponse response) {
        statusTextLabel.setText("CANCELLED");
        messageLabel.setText(response.getMessage());

        if (countdownTimeLine != null) {
            countdownTimeLine.stop();
        }
    }

    private void setProductImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 0) {
            productImageView.setImage(null);
            imagePlaceholderLabel.setVisible(true);
            return;
        }

        Image image = new Image(new ByteArrayInputStream(imageBytes));
        productImageView.setImage(image);
        imagePlaceholderLabel.setVisible(false);
    }

    private void goToBidderDashboard() {
        if (returnedToDashboard) {
            return;
        }

        returnedToDashboard = true;

        ClientNetworkService.getInstance().removeEventListener(realtimeListener);

        if (countdownTimeLine != null) {
            countdownTimeLine.stop();
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/bidder-dashboard.fxml"));
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            StageUtils.setMaximizedScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnableAutoBid() {
        String incrementText = autoBidIncrementField.getText().trim();
        String maxAmountText = autoBidMaxAmountField.getText().trim();

        if (incrementText.isEmpty() || maxAmountText.isEmpty()) {
            messageLabel.setText("Vui lòng nhập bước giá và số tiền tối đa.");
            return;
        }

        messageLabel.setText("Đã bật đấu giá tự động.");
    }

    @FXML
    private void handleDisableAutoBid() {
        autoBidIncrementField.clear();
        autoBidMaxAmountField.clear();
        messageLabel.setText("Đã tắt đấu giá tự động.");
    }

    private void setupPriceHistoryChart() {
        priceHistoryChart.getData().setAll(priceSeries);
        priceHistoryChart.setAnimated(false);
        priceHistoryChart.setLegendVisible(false);
        priceChartXAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(priceChartXAxis) {
            @Override
            public String toString(Number value) {
                long epochSeconds = value.longValue();
                java.time.Instant timePoint = java.time.Instant.ofEpochSecond(epochSeconds);
                java.time.ZoneId localZone = java.time.ZoneId.systemDefault();

                LocalDateTime bidTime = timePoint.atZone(localZone).toLocalDateTime();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                return bidTime.format(formatter);

            }
        });
        priceChartYAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(priceChartYAxis) {
            @Override
            public String toString (Number value){
                double price = value.doubleValue();

                if (price >= 1_000_000_000) {
                    return String.format("%.1fB", price / 1_000_000_000);
                }

                if (price >= 1_000_000) {
                    return String.format("%.1fM", price / 1_000_000);
                }

                if (price >= 1_000) {
                    return String.format("%.0fK", price / 1_000);
                }

                return String.format("%.0f", price);
            }
        });
    }

    private void rebuildPriceChart() {
        priceSeries.getData().clear();
        List<BidDTO> validBids = new ArrayList<>();

        for (BidDTO bid : bidHistory) {
            if (bid.getTimestamp() != null && bid.getAmount() != null) {
                validBids.add(bid);
            }
        }
        validBids.sort(Comparator.comparing(BidDTO::getTimestamp));

        int startIndex = Math.max(0, validBids.size() - 30);
        for (int i = startIndex; i < validBids.size(); i++) {
            BidDTO bid = validBids.get(i);

            long timeOnXAxis = bid.getTimestamp()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toEpochSecond();

            double priceOnYAxis = bid.getAmount().doubleValue();
            priceSeries.getData().add(new XYChart.Data<>(timeOnXAxis, priceOnYAxis));
        }

    }

}
