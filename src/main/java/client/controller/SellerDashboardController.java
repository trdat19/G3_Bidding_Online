package client.controller;

import client.model.Item;
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
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SellerDashboardController {

    @FXML
    private Label sellerNameLabel;

    @FXML
    private FlowPane productContainer;

    private final List<Item> itemList = new ArrayList<>();

    @FXML
    public void initialize() {
        sellerNameLabel.setText("Nguyễn Việt Anh");
        itemList.add(new Item(
                "MacBook Pro M3 Max",
                "Electronics",
                "Laptop hiệu năng cao dành cho đồ họa và lập trình",
                2000,
                2500,
                "user123",
                LocalDate.parse("2024-04-18"),
                LocalDate.parse("2026-04-18"),
                "OPEN",
                12
        ));

        itemList.add(new Item(
                "Bức tranh Hoa hướng dương",
                "Art",
                "Tác phẩm nghệ thuật phong cách cổ điển",
                30000000,
                40000000,
                "bidder02",
                LocalDate.parse("2024-04-18"),
                LocalDate.parse("2024-04-18"),
                "OPEN",
                8
        ));

        itemList.add(new Item(
                "Ferrari 250 GTO 1962",
                "Hypercar",
                "Mẫu siêu xe sưu tầm phiên bản hiếm",
                50000000,
                51000000,
                "bidder07",
                LocalDate.parse("2024-04-18"),
                LocalDate.parse("2024-04-18"),
                "FINISHED",
                20
        ));
        loadProducts();
    }

    private void loadProducts() {
        productContainer.getChildren().clear();
        for (Item items : itemList) {
            productContainer.getChildren().add(createProductCard(items));
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
            stage.setScene(new Scene(root));
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
        itemList.add(item);
        loadProducts();

    }
    @FXML
    private void handleRefresh() {
        System.out.println("Refresh clicked");
        loadProducts();
    }

    private void handleEditProduct(String productName) {
        System.out.println("Edit product: " + productName);
    }

    private void handleDeleteProduct(Item item) {
        itemList.remove(item);
        loadProducts();
    }
}
