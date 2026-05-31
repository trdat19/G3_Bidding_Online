package client.controller;

import client.model.Item;
import shared.dto.common.AuctionDTO;
import shared.dto.common.ItemDTO;
import client.service.ClientNetworkService;
import client.session.ClientSession;
import client.util.StageUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import shared.enums.Action;

import java.util.function.Consumer;
import java.io.ByteArrayInputStream;

public class SellerDashboardController {

    @FXML private Label sellerNameLabel;
    @FXML private FlowPane productContainer;
    @FXML private Label sellerWalletBalanceLabel;
    @FXML private Button productsButton;
    @FXML private Button approvedAuctionsButton;
    @FXML private Label sectionTitleLabel;
    @FXML private Label sectionSubtitleLabel;
    @FXML private VBox approvedAuctionPane;
    @FXML private TableView<AuctionDTO> approvedAuctionTable;
    @FXML private TableColumn<AuctionDTO, Long> auctionIdColumn;
    @FXML private TableColumn<AuctionDTO, String> auctionProductColumn;
    @FXML private TableColumn<AuctionDTO, String> auctionStartPriceColumn;
    @FXML private TableColumn<AuctionDTO, String> auctionCurrentPriceColumn;
    @FXML private TableColumn<AuctionDTO, String> auctionStartTimeColumn;
    @FXML private TableColumn<AuctionDTO, String> auctionEndTimeColumn;
    @FXML private TableColumn<AuctionDTO, String> auctionStatusColumn;
    @FXML private TableColumn<AuctionDTO, Integer> auctionBidCountColumn;

    private final Consumer<BaseResponse> realtimeListener = this::handleRealtimeEvent;
    private final List<Item> itemList = new ArrayList<>();
    private final ObservableList<AuctionDTO> approvedAuctions = FXCollections.observableArrayList();
    private final DateTimeFormatter tableDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Seller-Dashboard-Request");
        thread.setDaemon(true);
        return thread;
    });
    private boolean showingApprovedAuctions;
    private volatile boolean dashboardActive = true;

    @FXML
    public void initialize() {
        sellerNameLabel.setText(ClientSession.getCurrentUserFullName());
        setupApprovedAuctionTable();
        showProductsLoadingState();
        refreshProducts();
        loadSellerWalletBalance();

        ClientNetworkService.getInstance().addEventListener(realtimeListener);
    }

    @FXML
    private void handleOpenSellerWalletPopup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/seller-wallet-popup.fxml"));
            Parent root = loader.load();
            SellerWalletPopupController controller = loader.getController();
            controller.setOnWalletUpdated(this::loadSellerWalletBalance);

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
        if (itemList.isEmpty()) {
            showProductsMessage("Bạn chưa có sản phẩm nào.");
            return;
        }

        for (Item items : itemList) {
            productContainer.getChildren().add(createProductCard(items));
        }
    }

    private void showProductsLoadingState() {
        showProductsMessage("Đang tải sản phẩm...");
    }

    private void showProductsMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("section-subtitle");
        productContainer.getChildren().setAll(messageLabel);
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
            Image image = new Image(
                    new ByteArrayInputStream(item.getImageBytes()),
                    340, 150, true, true);
            ImageView imageView = new ImageView(image);
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
        Label soldPriceLabel = new Label("Giá bán: $" + item.getCurrentPrice());
        soldPriceLabel.getStyleClass().add("sold-price");
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

        body.getChildren().addAll(titleLabel, descLabel, priceRow, statusDesc);

        if ("SOLD".equals(item.getStatus())) {
            body.getChildren().add(soldPriceLabel);
        }

        body.getChildren().add(actionRow);
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
        dashboardActive = false;
        ClientNetworkService.getInstance().removeEventListener(realtimeListener);
        ClientNetworkService.getInstance().sendRequest(new BaseRequest(Action.LOGOUT, null));
        requestExecutor.shutdown();
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
    private void handleShowProducts() {
        showingApprovedAuctions = false;
        sectionTitleLabel.setText("Sản phẩm của bạn");
        sectionSubtitleLabel.setText(
                "Thêm mới, chỉnh sửa, xóa hoặc tạo đấu giá tùy theo trạng thái sản phẩm.");
        productsButton.getStyleClass().setAll("sidebar-menu-button-active");
        approvedAuctionsButton.getStyleClass().setAll("sidebar-menu-button");
        productContainer.setManaged(true);
        productContainer.setVisible(true);
        approvedAuctionPane.setManaged(false);
        approvedAuctionPane.setVisible(false);
        refreshProducts();
    }

    @FXML
    private void handleShowApprovedAuctions() {
        showingApprovedAuctions = true;
        sectionTitleLabel.setText("Quản lý sản phẩm đấu giá");
        sectionSubtitleLabel.setText(
                "Theo dõi các phiên đấu giá của bạn đã được admin phê duyệt.");
        productsButton.getStyleClass().setAll("sidebar-menu-button");
        approvedAuctionsButton.getStyleClass().setAll("sidebar-menu-button-active");
        productContainer.setManaged(false);
        productContainer.setVisible(false);
        approvedAuctionPane.setManaged(true);
        approvedAuctionPane.setVisible(true);
        loadApprovedAuctions();
    }

    @FXML
    private void handleRefresh() {
        if (showingApprovedAuctions) {
            loadApprovedAuctions();
        } else {
            refreshProducts();
        }
    }

    @FXML
    private void handleChangePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.initOwner(sellerNameLabel.getScene().getWindow());
        dialog.getDialogPane().setHeaderText("Cập nhật mật khẩu tài khoản seller");

        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Nhập mật khẩu cũ");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nhập mật khẩu mới");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Xác nhận mật khẩu mới");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.addRow(0, new Label("Mật khẩu cũ"), oldPasswordField);
        form.addRow(1, new Label("Mật khẩu mới"), newPasswordField);
        form.addRow(2, new Label("Xác nhận mật khẩu mới"), confirmPasswordField);
        dialog.getDialogPane().setContent(form);

        ButtonType updateButtonType = new ButtonType(
                "Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);
        Node updateButton = dialog.getDialogPane().lookupButton(updateButtonType);

        updateButton.addEventFilter(ActionEvent.ACTION, event -> {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String validationMessage = validatePasswordChange(
                    oldPassword, newPassword, confirmPassword);

            if (validationMessage != null) {
                showMessage(Alert.AlertType.WARNING, "Đổi mật khẩu", validationMessage);
                event.consume();
                return;
            }

            Map<String, String> data = new HashMap<>();
            data.put("oldPassword", oldPassword);
            data.put("newPassword", newPassword);
            data.put("confirmPassword", confirmPassword);

            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.CHANGE_PASSWORD, data));
            if (response == null || !response.isSuccess()) {
                String message = response != null
                        ? response.getMessage()
                        : "Không kết nối được server.";
                showMessage(Alert.AlertType.ERROR, "Đổi mật khẩu thất bại", message);
                event.consume();
                return;
            }

            if (ClientSession.getCurrentUser() != null) {
                ClientSession.getCurrentUser().setPassword(newPassword);
            }
            showMessage(Alert.AlertType.INFORMATION, "Đổi mật khẩu", response.getMessage());
        });

        dialog.showAndWait();
    }

    private String validatePasswordChange(
            String oldPassword, String newPassword, String confirmPassword) {
        if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            return "Vui lòng nhập đầy đủ ba trường mật khẩu.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Xác nhận mật khẩu mới không khớp.";
        }
        if (newPassword.equals(oldPassword)) {
            return "Mật khẩu mới phải khác mật khẩu cũ.";
        }
        return null;
    }

    private void showMessage(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
        if (!dashboardActive) {
            return;
        }

        if (itemList.isEmpty() && !showingApprovedAuctions) {
            showProductsLoadingState();
        }

        requestExecutor.execute(() -> {
            if (!dashboardActive) {
                return;
            }

            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.GET_SELLER_ITEMS, null));

            if (!dashboardActive) {
                return;
            }

            if (response == null || !response.isSuccess()) {
                Platform.runLater(() -> {
                    if (dashboardActive && !showingApprovedAuctions) {
                        showProductsMessage("Không tải được danh sách sản phẩm.");
                    }
                });
                return;
            }

            List<Item> loadedItems = new ArrayList<>();
            for (Object obj : (List<?>) response.getData()) {
                ItemDTO serverItem = (ItemDTO) obj;
                double startPrice = serverItem.getPriceStart() != null
                        ? serverItem.getPriceStart().doubleValue()
                        : 0;
                double currentPrice = serverItem.getCurrentPrice() != null
                        ? serverItem.getCurrentPrice().doubleValue()
                        : startPrice;
                Item item = new Item(
                        serverItem.getName(),
                        serverItem.getCategory().name(),
                        serverItem.getDescription(),
                        startPrice,
                        currentPrice,
                        "",
                        serverItem.getCreatedAt(),
                        serverItem.getCreatedAt(),
                        serverItem.getStatus().name(),
                        0L
                );

                item.setId(serverItem.getId());
                item.setImageUrl(serverItem.getImageUrl());
                item.setImageBytes(serverItem.getImageBytes());
                item.setImageContentType(serverItem.getImageContentType());
                loadedItems.add(item);
            }

            Platform.runLater(() -> {
                if (!dashboardActive) {
                    return;
                }

                itemList.clear();
                itemList.addAll(loadedItems);
                if (!showingApprovedAuctions) {
                    loadProducts();
                }
            });
        });
    }
    private boolean canManageItem(Item item) {
        String status = item.getStatus();
        return "PENDING".equals(status) || "CANCELLED".equals(status);
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

        Platform.runLater(() -> {
            if (showingApprovedAuctions) {
                loadApprovedAuctions();
            } else {
                refreshProducts();
            }
            loadSellerWalletBalance();
            if (response.getData() instanceof Map<?, ?> data) {
                Object eventType = data.get("eventType");
                if ("AUCTION_SOLD".equals(eventType)) {
                    showMessage(
                            Alert.AlertType.INFORMATION,
                            "Sản phẩm đã bán",
                            response.getMessage()
                    );
                } else if ("AUCTION_FINISHED_NO_BID".equals(eventType)) {
                    showMessage(
                            Alert.AlertType.INFORMATION,
                            "Phiên đấu giá kết thúc mà không ai đặt giá",
                            response.getMessage()
                    );

                }
            }
        });
    }

    private void setupApprovedAuctionTable() {
        auctionIdColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        auctionProductColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getItemName()));
        auctionStartPriceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatMoney(cell.getValue().getStartPrice())));
        auctionCurrentPriceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatMoney(cell.getValue().getDisplayPrice())));
        auctionStartTimeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatDateTime(cell.getValue().getStartTime())));
        auctionEndTimeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(formatDateTime(cell.getValue().getEndTime())));
        auctionStatusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatus() != null
                        ? cell.getValue().getStatus().name() : ""));
        auctionBidCountColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(Integer.parseInt((cell.getValue().getBidCount().toString()))));
        approvedAuctionTable.setItems(approvedAuctions);
    }

    private void loadApprovedAuctions() {
        if (!dashboardActive) {
            return;
        }

        approvedAuctionTable.setPlaceholder(new Label("Đang tải dữ liệu..."));
        requestExecutor.execute(() -> {
            if (!dashboardActive) {
                return;
            }

            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.GET_SELLER_APPROVED_AUCTIONS, null));

            if (!dashboardActive) {
                return;
            }

            Platform.runLater(() -> {
                if (!dashboardActive) {
                    return;
                }

                approvedAuctions.clear();
                if (response != null && response.isSuccess() && response.getData() != null) {
                    for (Object object : (List<?>) response.getData()) {
                        approvedAuctions.add((AuctionDTO) object);
                    }
                    approvedAuctionTable.setPlaceholder(
                            new Label("Chưa có phiên đấu giá nào được duyệt."));
                    return;
                }

                approvedAuctionTable.setPlaceholder(new Label("Không tải được dữ liệu."));
                if (showingApprovedAuctions) {
                    showMessage(Alert.AlertType.INFORMATION, "Phiên đấu giá",
                            response != null ? response.getMessage() : "Không kết nối được server.");
                }
            });
        });
    }

    private String formatMoney(BigDecimal amount) {
        return amount != null ? "$" + amount.toPlainString() : "$0.00";
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(tableDateFormat) : "-";
    }

    private void loadSellerWalletBalance() {
        if (!dashboardActive) {
            return;
        }

        requestExecutor.execute(() -> {
            if (!dashboardActive) {
                return;
            }

            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.GET_WALLET, null));
            String balanceText = "$0.00";
            if (response != null && response.isSuccess() && response.getData() != null) {
                BigDecimal balance = new BigDecimal(response.getData().toString());
                balanceText = "$" + balance.toPlainString();
            }

            String displayBalance = balanceText;
            Platform.runLater(() -> {
                if (dashboardActive) {
                    sellerWalletBalanceLabel.setText(displayBalance);
                }
            });
        });
    }
}
