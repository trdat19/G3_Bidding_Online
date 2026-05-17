package client.controller;

import client.model.Item;
import client.service.ClientNetworkService;
import client.util.StageUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import shared.dto.common.AuctionDTO;
import shared.dto.response.BaseResponse;
import shared.dto.request.BaseRequest;
import shared.enums.Action;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidderDashboardController {
    @FXML
    private FlowPane auctionContainer;

    @FXML
    private Label bidderNameLabel;

    private final List<Item> itemList = new ArrayList<>();
    @FXML
    public void initialize() {
        bidderNameLabel.setText("Bidder");
        loadAuctionsFromServer();
    }
    private void loadAuctionsFromServer() {
        BaseRequest request = new BaseRequest(Action.GET_AUCTION_LIST, null);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);
        itemList.clear();
        if (response != null || !response.isSuccess() || response.getData() == null) {
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
    }
    private Item toItem(AuctionDTO auction) {
        return new Item(
                auction.getItemName(),
                auction.getItemCategory(),
                auction.getItemDescription(),
                auction.getStartPrice()!= null ? auction.getStartPrice().doubleValue(): 0,
                auction.getDisplayPrice()!= null ? auction.getStartPrice().doubleValue() : 0,
                auction.getLeaderName()!= null ? auction.getLeaderName() : "Chưa có",
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStatus()!= null ? auction.getStatus().name() : "",
                auction.getBidCount()
        );
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

        Label imagePlaceholder = new Label("Image");
        imagePlaceholder.getStyleClass().add("image-placeholder");

        imageBox.getChildren().addAll(categoryBadge, statusBadge, imagePlaceholder);

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

        Label timeValue = new Label(formatTimeLeft(item.getEndTime()));
        timeValue.getStyleClass().add("time-left");

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
    private String formatTimeLeft(LocalDateTime endTime) {
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auction-detail.fxml"));
            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setItemData(item);

            Stage stage = (Stage) auctionContainer.getScene().getWindow();
            StageUtils.setMaximizedScene(stage, root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
