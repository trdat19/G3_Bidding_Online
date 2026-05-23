package client.controller;

import client.model.Item;
import shared.dto.common.ItemDTO;
import client.service.ClientNetworkService;
import client.session.ClientSession;
import client.util.StageUtils;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import shared.enums.Action;

import java.util.function.Consumer;
import java.io.ByteArrayInputStream;

public class SellerDashboardController {

    @FXML
    private Label sellerNameLabel;

    @FXML
    private FlowPane productContainer;

    private final Consumer<BaseResponse> realtimeListener = this::handleRealtimeEvent;

    private final List<Item> itemList = new ArrayList<>();
    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        sellerNameLabel.setText(ClientSession.getCurrentUserFullName());
        refreshProducts();

        ClientNetworkService.getInstance().addEventListener(realtimeListener);
    }

    @FXML
    private void handleOpenSellerWalletPopup(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/seller-wallet-popup.fxml"));

            Stage popup = new Stage();
            popup.setTitle("Ví bán hàng");
            popup.setScene(new Scene(root));
            popup.initOwner(((Node) event.getSource()).getScene().getWindow());
            popup.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Label categoryBadge = new Label(item.getCategory());
        categoryBadge.getStyleClass().add("category-badge");
        StackPane.setMargin(categoryBadge, new Insets(14, 0, 0, 14));
        StackPane.setAlignment(categoryBadge, javafx.geometry.Pos.TOP_LEFT);
        imageBox.setPrefHeight(150);

        if (item.getImageBytes() != null && item.getImageBytes().length > 0) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(item.getImageBytes())));
            imageView.setFitWidth(340);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            imageBox.getChildren().add(imageView);
        } else {
            Label imageText = new Label("Image");
            imageText.setStyle("-fx-font-size: 48px; -fx-text-fill: #cbd5e1;");
            imageBox.getChildren().add(imageText);
        }

        imageBox.getChildren().add(categoryBadge);

        VBox body = new VBox(10);
        body.setPadding(new Insets(18));

        Label titleLabel = new Label(item.getTitle());
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
        Label priceLabel = new Label("NGÀY TẠO");
        Label priceValue = new Label(
                item.getStartTime() != null ? item.getStartTime().toLocalDate().toString() : "-"
        );
        priceBox.getChildren().addAll(priceLabel, priceValue);

        VBox statusBox = new VBox(6);
        statusBox.setPrefWidth(150);
        Label statusLabel = new Label("TRẠNG THÁI");
        statusLabel.getStyleClass().add("meta-label");
        Label statusValue = new Label(getStatusText(item.getStatus()));
        statusValue.getStyleClass().addAll("status-pill", getStatusStyleClass(item.getStatus()));
        Label statusDesc = new Label(getStatusDescription(item.getStatus()));
        statusDesc.getStyleClass().add("status-desc");
        statusDesc.setWrapText(true);
        statusDesc.setMaxWidth(324);
        statusBox.getChildren().addAll(statusLabel, statusValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        priceRow.getChildren().addAll(priceBox, spacer, statusBox);

        //Doi UI theo tung status
        HBox actionRow = new HBox(12);

        Button createAuctionButton = new Button("Tạo đấu giá");
        createAuctionButton.getStyleClass().add("edit-button");
        createAuctionButton.setOnAction(e -> handleCreateAuction(item));

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> handleEditProduct(item));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDeleteProduct(item));



        actionRow.getChildren().addAll(editButton, deleteButton, createAuctionButton);

        body.getChildren().addAll(titleLabel, descLabel, priceRow, statusDesc, actionRow);
        card.getChildren().addAll(imageBox, body);
        return card;
    }
    //các helper để setText theo từng Status: PENDING, ACTIVE SOLD,CANCELLED
    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "Đã thêm";
            case "WAITING_APPROVAL" -> "Chờ duyệt";
            case "ACTIVE" -> "Đang được đấu giá";
            case "SOLD" -> "Đã bán";
            case "CANCELLED" -> "Ế hàng";
            default -> status;
        };
    }
    private String getStatusDescription(String status) {
        return switch (status) {
            case "PENDING" -> "Sản phẩm đã được thêm, có thể tạo đấu giá.";
            case "WAITING_APPROVAL" -> "Sản phẩm đang chờ admin duyệt phiên đấu giá.";
            case "ACTIVE" -> "Sản phẩm đang được đấu giá, không thể chỉnh sửa.";
            case "SOLD" -> "Sản phẩm đã bán thành công.";
            case "CANCELLED" -> "Phiên đấu giá đã kết thúc nhưng không có ai đặt giá. Có thể tạo lại phiên đấu giá mới";
            default -> "";
        };
    }
    private String getStatusStyleClass(String status) {
        return switch (status) {
            case "PENDING" -> "status-pending";
            case "WAITING_APPROVAL" -> "status-waiting";
            case "ACTIVE" -> "status-active";
            case "SOLD" -> "status-sold";
            case "CANCELLED" -> "status-cancelled";
            default -> "status-pending";
        };
    }
    @FXML
    private void handleLogout(ActionEvent event) {
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
    public void addNewProduct(Item item) {
        itemList.add(item);
        loadProducts();

    }
    @FXML
    private void handleRefresh() {
        refreshProducts();
    }

    private void handleEditProduct(Item item) {
        if (!canManageItem(item)) {
            showCannotActionAlert("chỉnh sửa", item);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit-product-view.fxml"));
            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setData(item, this);

            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa sản phẩm");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteProduct(Item item) {
        if (!canManageItem(item)) {
            showCannotActionAlert("xóa", item);
            return;
        }
        if (item.getId() == null) {
            System.out.println("Khong co id san pham de xoa");
            return;
        }

        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.DELETE_ITEM, item.getId()));

        if (response != null && response.isSuccess()) {
            refreshProducts();
        } else {
            System.out.println(response != null ? response.getMessage() : "Khong ket noi duoc server");
        }
    }
    //Hàm nối với SetupAuctionView khi admin duyệt
    private void handleCreateAuction(Item item) {
        if (!canManageItem(item)) {
            showCannotActionAlert("tạo đấu giá cho", item);
            return;
        }
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
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_SELLER_ITEMS, null));

        if (response == null || !response.isSuccess()) {
            System.out.println(response != null ? response.getMessage() : "Khong ket noi duoc server");
            return;
        }

        itemList.clear();

        List<?> serverItems = (List<?>) response.getData();
        for (Object obj : serverItems) {
            ItemDTO serverItem = (ItemDTO) obj;

            Item item = new Item(
                    serverItem.getName(),
                    serverItem.getCategory().name(),
                    serverItem.getDescription(),
                    1,
                    1,
                    "",
                    serverItem.getCreatedAt(),
                    serverItem.getCreatedAt(),
                    serverItem.getStatus().name(),
                    0
            );

            item.setId(serverItem.getId());
            item.setImageUrl(serverItem.getImageUrl());
            item.setImageBytes(serverItem.getImageBytes());
            item.setImageContentType(serverItem.getImageContentType());
            itemList.add(item);
        }

        loadProducts();
    }
    public void setSellerName(String sellerName) {
        sellerNameLabel.setText(sellerName);
    }

    private boolean canManageItem(Item item) {
        return "PENDING".equals(item.getStatus())
                || "CANCELLED".equals(item.getStatus());
    }

    private void showCannotActionAlert(String action, Item item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Không thể " + action);
        alert.setHeaderText(null);
        alert.setContentText("Không thể " + action + " sản phẩm khi sản phẩm "
                + getStatusDescriptionForAlert(item.getStatus()) + ".");
        alert.showAndWait();
    }

    private String getStatusDescriptionForAlert(String status) {
        return switch (status) {
            case "WAITING_APPROVAL" -> "Đang chờ admin duyệt";
            case "ACTIVE" -> "Đang được đấu giá";
            case "SOLD" -> "Đã bán";
            case "CANCELLED" -> "Đã bị hủy";
            case "PENDING" -> "Đã được tạo";
            default -> "Ở trạng thái " + status;
        };
    }

    private void handleRealtimeEvent(BaseResponse response) {
        if (!"SELLER_ITEMS_CHANGED".equals(response.getAction())) {
            return;
        }

        Platform.runLater(this::refreshProducts);
    }
}