package client.controller;

import client.util.StageUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import shared.dto.common.ItemDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import client.service.ClientNetworkService;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

public class SellerDashboardController {

    @FXML
    private Label sellerNameLabel;

    @FXML
    private FlowPane productContainer;

    private final List<ItemDTO> itemList = new ArrayList<>();

    @FXML
    public void initialize() {
        loadProductsFromServer();
    }
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            sellerNameLabel.setText("Bidder");
        }
        else {
            sellerNameLabel.setText(fullName);
        }
    }
    private void loadProductsFromServer() {
        BaseRequest request = new BaseRequest(Action.GET_SELLER_ITEMS, null);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        itemList.clear();
        if (response != null && response.isSuccess() && response.getData() != null)  {
            itemList.addAll((List<ItemDTO>) response.getData());
        }
        else {
        System.out.println(response != null ? response.getMessage() : "Không kết nối được server");
       }
        loadProducts();
    }
    private void loadProducts() {
        productContainer.getChildren().clear();
        for (ItemDTO item :itemList) {
            productContainer.getChildren().add(createProductCard(item));
        }
    }

    private VBox createProductCard(ItemDTO item) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(360);
        card.setSpacing(0);

        StackPane imageBox = new StackPane();
        imageBox.getStyleClass().add("product-image");
        imageBox.setPrefHeight(150);

        if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            ImageView imageView = new ImageView(new Image(item.getImageUrl(), true));
            imageView.setFitWidth(360);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageBox.getChildren().add(imageView);
        }
        else {
            Label imageText = new Label("Image");
            imageText.setStyle("-fx-font-size: 48px; -fx-text-fill: #cbd5e1;");
            imageBox.getChildren().add(imageText);
        }

        Label categoryBadge = new Label(item.getCategory().name());
        categoryBadge.getStyleClass().add("category-badge");
        StackPane.setMargin(categoryBadge, new Insets(14, 0, 0, 14));
        StackPane.setAlignment(categoryBadge, javafx.geometry.Pos.TOP_LEFT);

        imageBox.getChildren().add(categoryBadge);

        VBox body = new VBox(10);
        body.setPadding(new Insets(18));

        Label titleLabel = new Label(item.getName());
        titleLabel.getStyleClass().add("product-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(324);

        Label descLabel = new Label(item.getDescription());
        descLabel.getStyleClass().add("product-desc");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(324);

        HBox priceRow = new HBox(12);
        VBox priceBox = new VBox(6);
        priceBox.setPrefWidth(140);
        Label priceLabel = new Label("GIÁ KHỞI ĐIỂM");
        priceLabel.getStyleClass().add("meta-label");
        Label priceValue = new Label(item.getPriceStart() + "$");
        priceValue.getStyleClass().add("meta-value");
        priceBox.getChildren().addAll(priceLabel, priceValue);

        VBox statusBox = new VBox(6);
        statusBox.setPrefWidth(150);
        Label statusLabel = new Label("TRẠNG THÁI");
        statusLabel.getStyleClass().add("meta-label");
        Label statusValue = new Label(getStatusText(item.getStatus().name()));
        statusValue.getStyleClass().addAll("status-pill", getStatusStyleClass(item.getStatus().name()));
        Label statusDesc = new Label(getStatusDescription(item.getStatus().name()));
        statusDesc.getStyleClass().add("status-desc");
        statusDesc.setWrapText(true);
        statusDesc.setMaxWidth(324);
        statusBox.getChildren().addAll(statusLabel, statusValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        priceRow.getChildren().addAll(priceBox, spacer, statusBox);

        //Doi UI theo tung status
        HBox actionRow = new HBox(12);
        String status = item.getStatus()!= null ? item.getStatus().name() : "";
        if ("PENDING".equals(status)) {
            Button editButton = new Button("Edit");
            editButton.getStyleClass().add("edit-button");
            editButton.setOnAction(e -> handleEditProduct(item.getName()));

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setOnAction(e -> handleDeleteProduct(item));

            Button createAuctionButton = new Button("Tạo đấu giá");
            createAuctionButton.getStyleClass().add("edit-button");
            createAuctionButton.setOnAction(e -> handleCreateAuction(item));

            actionRow.getChildren().addAll(editButton, deleteButton, createAuctionButton);

        }
        else if ("WAITING_APPROVAL".equals(status)) {
            Label waitingLabel = new Label("Chờ admin duyệt");
            waitingLabel.getStyleClass().add("status-pill");
            actionRow.getChildren().add(waitingLabel);

        } else if ("ACTIVE".equals(status)) {
            Button createAuctionButton = new Button("Tạo đấu giá");
            createAuctionButton.getStyleClass().add("edit-button");
            createAuctionButton.setOnAction(e -> handleCreateAuction(item));

            actionRow.getChildren().addAll(createAuctionButton);

        } else if ("SOLD".equals(status)) {
            Button viewButton = new Button("Xem chi tiết");
            viewButton.getStyleClass().add("edit-button");

            actionRow.getChildren().add(viewButton);

        } else if ("CANCELLED".equals(status)) {
            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setOnAction(e -> handleDeleteProduct(item));

            actionRow.getChildren().add(deleteButton);
        }
        body.getChildren().addAll(titleLabel, descLabel, priceRow, statusDesc, actionRow);
        card.getChildren().addAll(imageBox, body);
        return card;
    }
    //các helper để setText theo từng Status: PENDING, ACTIVE SOLD,CANCELLED
    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "Chờ duyệt";
            case "ACTIVE" -> "Đã duyệt";
            case "SOLD" -> "Đã bán";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
    private String getStatusDescription(String status) {
        return switch (status) {
            case "PENDING" -> "Sản phẩm đang chờ admin kiểm duyệt.";
            case "ACTIVE" -> "Sản phẩm đã được duyệt, có thể tạo phiên đấu giá.";
            case "SOLD" -> "Sản phẩm đã bán thành công.";
            case "CANCELLED" -> "Sản phẩm hoặc phiên đấu giá đã bị hủy.";
            default -> "";
        };
    }
    private String getStatusStyleClass(String status) {
        return switch (status) {
            case "PENDING" -> "status-pending";
            case "ACTIVE" -> "status-active";
            case "SOLD" -> "status-sold";
            case "CANCELLED" -> "status-cancelled";
            default -> "status-pending";
        };
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
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double dialogWidth = Math.min(720, Math.max(360, screenBounds.getWidth() - 80));
            double dialogHeight = Math.min(640, Math.max(420, screenBounds.getHeight() - 80));

            stage.setScene(new Scene(root, dialogWidth, dialogHeight));
            stage.setMinWidth(Math.min(520, dialogWidth));
            stage.setMinHeight(Math.min(420, dialogHeight));
            stage.setMaxHeight(screenBounds.getHeight() - 40);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRefresh() {
        System.out.println("Refresh clicked");
        loadProductsFromServer();
    }

    private void handleEditProduct(String productName) {
        System.out.println("Edit product: " + productName);
    }

    private void handleDeleteProduct(ItemDTO item) {
        itemList.remove(item);
        loadProductsFromServer();
    }
    //Hàm nối với SetupAuctionView khi admin duyệt
    private void handleCreateAuction(ItemDTO item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/setup-auction-view.fxml"));
            Parent root = loader.load();

            SetupAuctionViewController controller = loader.getController();
            controller.setData(item, this);

            Stage stage = new Stage();
            stage.setTitle("Thiết lập phiên đấu giá");
            stage.setScene(new Scene(root));
            stage.show();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void refreshProducts() {
        loadProductsFromServer();
    }
}
