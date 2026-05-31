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
import javafx.util.StringConverter;

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
    @FXML private Label minIncrementLabel;
    @FXML private TextField autoBidIncrementField;
    @FXML private TextField autoBidMaxAmountField;
    @FXML private LineChart<Number, Number> priceHistoryChart;
    @FXML private NumberAxis priceChartXAxis;
    @FXML private NumberAxis priceChartYAxis;

    private final XYChart.Series<Number, Number> priceSeries = new XYChart.Series<>();
    private final List<String> priceChartTimeLabels = new ArrayList<>();

    private Item currentItem;
    private Timeline countdownTimeLine;
    private final Consumer<BaseResponse> realtimeListener = this::handleRealtimeEvent;
    private final ObservableList<BidDTO> bidHistory = FXCollections.observableArrayList();
    private final DateTimeFormatter bidTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private boolean returnedToDashboard = false;
    private boolean auctionFinishedAlertShown = false;

    @FXML
    private void initialize() {
        bidderColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getBidderName()));

        amountColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAmount().toPlainString()));

        timeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getTimestamp() != null
                                ? cell.getValue().getTimestamp().format(bidTimeFormatter)
                                : ""
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
        refreshAuctionDetail();
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

            messageLabel.setText("Phiên đấu giá đã kết thúc, đang chờ kết quả");
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

        if (bid.getAmount().doubleValue() >= currentItem.getCurrentPrice()) {
            currentItem.setCurrentPrice(bid.getAmount().doubleValue());
            currentItem.setLeader(bid.getBidderName());

            currentPriceLabel.setText(String.valueOf(currentItem.getCurrentPrice()));
            leaderLabel.setText(currentItem.getLeader());
        }

        if (bid.getId() != null) {
            bidHistory.removeIf(existingBid -> Objects.equals(existingBid.getId(), bid.getId()));
        }
        bidHistory.addFirst(bid);
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
        if (auctionFinishedAlertShown || returnedToDashboard) {
            return;
        }
        auctionFinishedAlertShown = true;
        statusTextLabel.setText("FINISHED");
        timeLeftLabel.setText("00:00:00");
        messageLabel.setText(response.getMessage());

        if (countdownTimeLine != null) {
            countdownTimeLine.stop();
        }

        showAuctionFinishedAlert(response.getMessage());
    }
    private void showAuctionFinishedAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kết quả phiên đấu giá");
        alert.setHeaderText("Phiên đấu giá đã kết thúc");
        alert.setContentText(message);
        ButtonType exitButton = new ButtonType("Thoát", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(exitButton);

        if (nameLabel.getScene() != null) {
            alert.initOwner(nameLabel.getScene().getWindow());
        }

        alert.showAndWait();
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
        if (imageBytes == null || imageBytes.length <= 0) {
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
        priceChartXAxis.setAutoRanging(false);
        priceChartXAxis.setForceZeroInRange(false);
        priceChartXAxis.setMinorTickVisible(false);

        priceChartXAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                int bidOrder = value.intValue();

                if (Math.abs(value.doubleValue() - bidOrder) > 0.001) {
                    return "";
                }

                if (bidOrder < 0 || bidOrder >= priceChartTimeLabels.size()) {
                    return "";
                }

                return priceChartTimeLabels.get(bidOrder);
            }

            @Override
            public Number fromString(String value) {
                return 0;
            }
        });
    }

    private void rebuildPriceChart() {
        priceSeries.getData().clear();
        priceChartTimeLabels.clear();

        List<BidDTO> validBids = new ArrayList<>();

        for (BidDTO bid : bidHistory) {
            if (bid.getTimestamp() != null && bid.getAmount() != null) {
                validBids.add(bid);
            }
        }

        validBids.sort(
                Comparator.comparing(
                                BidDTO::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(BidDTO::getTimestamp)
                        .thenComparing(BidDTO::getAmount)
        );

        int startIndex = Math.max(0, validBids.size() - 30);
        List<BidDTO> visibleBids = validBids.subList(startIndex, validBids.size());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (int index = 0; index < visibleBids.size(); index++) {
            BidDTO bid = visibleBids.get(index);

            priceChartTimeLabels.add(
                    bid.getTimestamp().format(timeFormatter)
            );

            priceSeries.getData().add(
                    new XYChart.Data<>(index, bid.getAmount().doubleValue())
            );
        }

        int pointCount = visibleBids.size();

        if (pointCount == 0) {
            priceChartXAxis.setLowerBound(0);
            priceChartXAxis.setUpperBound(1);
            priceChartXAxis.setTickUnit(1);
            return;
        }

        if (pointCount == 1) {
            priceChartXAxis.setLowerBound(-0.5);
            priceChartXAxis.setUpperBound(0.5);
            priceChartXAxis.setTickUnit(1);
            return;
        }

        priceChartXAxis.setLowerBound(0);
        priceChartXAxis.setUpperBound(pointCount - 1);
        priceChartXAxis.setTickUnit(Math.max(1, Math.ceil((pointCount - 1) / 6.0)));
    }

    /** kích hoạt auto bid */
    @FXML
    private void handleEnableAutoBid() {
        String incrementText = autoBidIncrementField.getText().trim();
        String maxAmountText = autoBidMaxAmountField.getText().trim();

        if (incrementText.isEmpty() || maxAmountText.isEmpty()) {
            messageLabel.setText("Vui lòng nhập bước giá và số tiền tối đa.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("auctionId", currentItem.getId());
        data.put("maxAmount", maxAmountText);
        data.put("stepAmount", incrementText);

        BaseRequest request = new BaseRequest(Action.REGISTER_AUTO_BID_RULE, data);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            messageLabel.setText("Đã bật đấu giá tự động.");
        }
        else {
            messageLabel.setText(response != null ? response.getMessage() : "Có lôi xảy ra!");
        }

    }

    @FXML
    private void handleDisableAutoBid() {
        if (currentItem == null || currentItem.getId() == null) {
            messageLabel.setText("Không tìm thấy phiên!");
            return;
        }

        BaseRequest request = new BaseRequest(Action.REMOVE_AUTO_BID_RULE, currentItem.getId());
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            autoBidIncrementField.clear();
            autoBidMaxAmountField.clear();
            messageLabel.setText("Đã tắt đấu giá tự động.");
        }
        else {
            messageLabel.setText(response != null ? response.getMessage() : "Có lỗi xảy ra!");
        }
    }
}
