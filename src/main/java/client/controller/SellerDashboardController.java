package client.controller;

import client.model.Item;
import client.util.StageUtils;
import client.service.ClientNetworkService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import shared.dto.ItemDTO;
import shared.dto.common.UserDTO;
import shared.dto.request.item.DeleteItemRequest;
import shared.dto.request.item.GetSellerItemsRequest;
import shared.dto.response.BaseResponse;
import shared.dto.response.item.SellerItemsResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SellerDashboardController {

    @FXML
    private Label sellerNameLabel;

    @FXML
    private FlowPane productContainer;

    @FXML
    public void initialize() {
        sellerNameLabel.setText("");
    }

    private UserDTO currentUser;

    private void loadProductsFromServer() {
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(new GetSellerItemsRequest());

        productContainer.getChildren().clear();

        if (response instanceof SellerItemsResponse sellerItemsResponse) {
            for (ItemDTO dto : sellerItemsResponse.getItems()) {
                Item item = new Item(
                        dto.getName(),
                        dto.getCategory().name(),
                        dto.getDescription(),
                        dto.getPriceStart().doubleValue(),
                        dto.getPriceStart().doubleValue(),
                        "None",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        dto.getStatus().name(),
                        0
                );
                item.setId(dto.getId());

                productContainer.getChildren().add(createProductCard(item));
            }
        } else {
            System.out.println(response != null ? response.getMessage() : "Không kết nối được server");
        }
    }

    private VBox createProductCard(Item item) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(360);
        card.setSpacing(0);

        StackPane imageBox = new StackPane();
        imageBox.getStyleClass().add("product-image");
        imageBox.setPrefHeight(150);

        Label imageText = new Label("Image");
        imageText.setStyle("-fx-font-size: 48px; -fx-text-fill: #cbd5e1;");

        Label categoryBadge = new Label(item.getCategory());
        categoryBadge.getStyleClass().add("category-badge");
        StackPane.setMargin(categoryBadge, new Insets(14, 0, 0, 14));
        StackPane.setAlignment(categoryBadge, javafx.geometry.Pos.TOP_LEFT);

        imageBox.getChildren().addAll(imageText, categoryBadge);

        VBox body = new VBox(10);
        body.setPadding(new Insets(18));

        Label titleLabel = new Label(item.getTitle());
        titleLabel.getStyleClass().add("product-title");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(item.getDescription());
        descLabel.getStyleClass().add("product-desc");
        descLabel.setWrapText(true);

        HBox priceRow = new HBox();
        VBox priceBox = new VBox(6);
        Label priceLabel = new Label("GIÁ KHỞI ĐIỂM");
        priceLabel.getStyleClass().add("meta-label");
        Label priceValue = new Label(String.valueOf(item.getStartPrice() + "$"));
        priceValue.getStyleClass().add("meta-value");
        priceBox.getChildren().addAll(priceLabel, priceValue);

        VBox statusBox = new VBox(6);
        Label statusLabel = new Label("TRẠNG THÁI");
        statusLabel.getStyleClass().add("meta-label");
        Label statusValue = new Label(item.getStatus());
        if ("OPEN".equals(item.getStatus())) {
            statusValue.getStyleClass().add("status-open");
        } else {
            statusValue.getStyleClass().add("status-finished");
        }
        statusBox.getChildren().addAll(statusLabel, statusValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        priceRow.getChildren().addAll(priceBox, spacer, statusBox);

        HBox actionRow = new HBox(12);
        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> handleEditProduct(item.getTitle()));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDeleteProduct(item));

        actionRow.getChildren().addAll(editButton, deleteButton);

        body.getChildren().addAll(titleLabel, descLabel, priceRow, actionRow);

        card.getChildren().addAll(imageBox, body);
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
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add-product-view.fxml"));
            Parent root = loader.load();

            AddProductController addProductController = loader.getController();
            addProductController.setSellerDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Thêm sản phẩm");
            stage.setScene(new Scene(root));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addNewProduct(Item item) {
        loadProductsFromServer();

    }
    @FXML
    private void handleRefresh() {
        loadProductsFromServer();
    }

    private void handleEditProduct(String productName) {
        System.out.println("Edit product: " + productName);
    }

    private void handleDeleteProduct(Item item) {
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(new DeleteItemRequest(item.getId()));
        if (response != null && response.isSuccess()) {
            loadProductsFromServer();
        } else {
            System.out.println(response != null ? response.getMessage() : "Không kết nối được server");
        }
    }

    public  void setCurrentUser(UserDTO user)
    {
        this.currentUser = user;
        sellerNameLabel.setText(user.getFullname());
        loadProductsFromServer();

    }
}