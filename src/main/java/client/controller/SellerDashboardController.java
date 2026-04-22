package client.controller;

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
import javafx.event.ActionEvent;
import java.io.IOException;

public class SellerDashboardController {

    @FXML
    private Label sellerNameLabel;

    @FXML
    private FlowPane productContainer;

    @FXML
    public void initialize() {
        sellerNameLabel.setText("truongthanhdat");
        loadProducts();
    }

    private void loadProducts() {
        productContainer.getChildren().clear();

        productContainer.getChildren().add(createProductCard(
                "ELECTRONICS",
                "MacBook Pro M3 Max",
                "Laptop hiệu năng cao dành cho đồ họa và lập trình",
                "$2500",
                "OPEN"
        ));

        productContainer.getChildren().add(createProductCard(
                "ART",
                "Bức tranh Hoa hướng dương",
                "Tác phẩm nghệ thuật phong cách cổ điển",
                "$40M",
                "OPEN"
        ));

        productContainer.getChildren().add(createProductCard(
                "HYPERCAR",
                "Ferrari 250 GTO 1962",
                "Mẫu siêu xe sưu tầm phiên bản hiếm",
                "$51M",
                "FINISHED"
        ));
    }

    private VBox createProductCard(String category, String title, String description, String price, String status) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(360);
        card.setSpacing(0);

        StackPane imageBox = new StackPane();
        imageBox.getStyleClass().add("product-image");
        imageBox.setPrefHeight(150);

        Label imageText = new Label("Image");
        imageText.setStyle("-fx-font-size: 48px; -fx-text-fill: #cbd5e1;");

        Label categoryBadge = new Label(category);
        categoryBadge.getStyleClass().add("category-badge");
        StackPane.setMargin(categoryBadge, new Insets(14, 0, 0, 14));
        StackPane.setAlignment(categoryBadge, javafx.geometry.Pos.TOP_LEFT);

        imageBox.getChildren().addAll(imageText, categoryBadge);

        VBox body = new VBox(10);
        body.setPadding(new Insets(18));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("product-title");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("product-desc");
        descLabel.setWrapText(true);

        HBox priceRow = new HBox();
        VBox priceBox = new VBox(6);
        Label priceLabel = new Label("GIÁ KHỞI ĐIỂM");
        priceLabel.getStyleClass().add("meta-label");
        Label priceValue = new Label(price);
        priceValue.getStyleClass().add("meta-value");
        priceBox.getChildren().addAll(priceLabel, priceValue);

        VBox statusBox = new VBox(6);
        Label statusLabel = new Label("TRẠNG THÁI");
        statusLabel.getStyleClass().add("meta-label");
        Label statusValue = new Label(status);
        if ("OPEN".equals(status)) {
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
        editButton.setOnAction(e -> handleEditProduct(title));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDeleteProduct(title));

        actionRow.getChildren().addAll(editButton, deleteButton);

        body.getChildren().addAll(titleLabel, descLabel, priceRow, actionRow);

        card.getChildren().addAll(imageBox, body);
        return card;
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add-product-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Thêm sản phẩm");
            stage.setScene(new Scene(root));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refresh clicked");
        loadProducts();
    }

    private void handleEditProduct(String productName) {
        System.out.println("Edit product: " + productName);
    }

    private void handleDeleteProduct(String productName) {
        System.out.println("Delete product: " + productName);
    }
}
