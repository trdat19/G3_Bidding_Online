package client.controller;

import client.model.Item;
import client.util.StageUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import shared.dto.common.UserDTO;

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
        loadProducts();
    }

    private UserDTO currentUser;

    private void loadProducts() {
        auctionContainer.getChildren().clear();
        for (Item items : itemList) {
            auctionContainer.getChildren().add(createProductCard(items));
        }
    }
    private VBox createProductCard(Item item) {
        VBox card = new VBox();
        card.setPrefWidth(330);
        card.setSpacing(16);
        card.getStyleClass().add("auction-class");

        StackPane imageBox = new StackPane();
        imageBox.setPrefHeight(210);
        imageBox.setPrefWidth(200);
        imageBox.getStyleClass().add("image-box");

        Label categoryBadge = new Label(item.getCategory());
        categoryBadge.getStyleClass().add("category-badge");
        StackPane.setAlignment(categoryBadge, Pos.TOP_LEFT);
        StackPane.setMargin(categoryBadge, new Insets(12, 0, 0, 12));

        Label imagePlaceholder = new Label("Image");
        imagePlaceholder.getStyleClass().add("image-placeholder");

        imageBox.getChildren().addAll(categoryBadge, imagePlaceholder);

        VBox infoBox = new VBox();
        infoBox.setSpacing(8);

        Label titleLabel = new Label(item.getTitle());
        titleLabel.getStyleClass().add("item-title");

        Label descLabel = new Label(item.getDescription());
        descLabel.getStyleClass().add("item-desc");

        infoBox.getChildren().addAll(titleLabel, descLabel);

        Separator separator = new Separator();

        HBox metaBox = new HBox();

        VBox priceBox = new VBox();
        priceBox.setSpacing(4);

        Label priceText = new Label("GIÁ HIỆN TẠI");
        priceText.getStyleClass().add("meta-label");

        Label priceValue = new Label(String.valueOf(item.getStartPrice()) + "$");
        priceValue.setFont(new Font(22));

        priceBox.getChildren().addAll(priceText, priceValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox timeBox = new VBox();
        timeBox.setSpacing(4);
        timeBox.setAlignment(Pos.CENTER_RIGHT);

        Label timeText = new Label("TRẠNG THÁI");
        timeText.getStyleClass().add("meta-label");

        Label timeValue = new Label(item.getStatus());
        timeValue.getStyleClass().add("time-label");

        timeBox.getChildren().addAll(timeText, timeValue);

        metaBox.getChildren().addAll(priceBox, spacer, timeBox);

        Button detailButton = new Button("Xem chi tiết");
        detailButton.getStyleClass().add("detail-button");
        detailButton.setOnAction(event -> viewDetail(item));

        card.getChildren().addAll(
                imageBox,
                infoBox,
                separator,
                metaBox,
                detailButton
        );

        return card;
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

    public void setCurrentUser(UserDTO user)
    {
        this.currentUser = user;
        bidderNameLabel.setText(user.getFullname());
    }
}
