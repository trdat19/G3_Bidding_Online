package client.controller;

import client.service.ClientNetworkService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import shared.dto.common.AuctionDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.AuctionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AdminRequestController implements AdminPageLifecycle {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private TableView<AuctionDTO> requestTable;
    @FXML private TableColumn<AuctionDTO, String> productNameColumn;
    @FXML private TableColumn<AuctionDTO, String> sellerColumn;
    @FXML private TableColumn<AuctionDTO, String> categoryColumn;
    @FXML private TableColumn<AuctionDTO, String> statusColumn;
    @FXML private TableColumn<AuctionDTO, Void> actionColumn;
    @FXML private TableColumn<AuctionDTO, String> startPriceColumn;
    @FXML private TableColumn<AuctionDTO, String> minIncrementColumn;
    @FXML private TableColumn<AuctionDTO, String> startTimeColumn;
    @FXML private TableColumn<AuctionDTO, String> endTimeColumn;

    private final ObservableList<AuctionDTO> requests = FXCollections.observableArrayList();
    private final FilteredList<AuctionDTO> filteredRequests =
            new FilteredList<>(requests, request -> true);
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Admin-Requests-Request");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean loading = new AtomicBoolean();
    private final Consumer<BaseResponse> realtimeListener = this::handleRealtimeEvent;
    private volatile boolean pageVisible;

    @FXML
    private void initialize() {
        productNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getItemName()));
        sellerColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSellerName()));
        categoryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getItemCategory()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStatus() != null ? data.getValue().getStatus().name() : ""));
        startPriceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getStartPrice())));
        minIncrementColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getMinIncrement())));
        startTimeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getStartTime())));
        endTimeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getEndTime())));

        statusFilterBox.getItems().add("ALL");
        for (AuctionStatus status : AuctionStatus.values()) {
            statusFilterBox.getItems().add(status.name());
        }
        statusFilterBox.setValue("ALL");
        requestTable.setItems(filteredRequests);
        requestTable.setPlaceholder(new Label("Đang tải dữ liệu..."));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        statusFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        setupActionColumn();
        ClientNetworkService.getInstance().addEventListener(realtimeListener);
    }

    @Override
    public void onPageShown() {
        pageVisible = true;
        loadRequests();
    }

    @Override
    public void onPageHidden() {
        pageVisible = false;
    }

    @Override
    public void dispose() {
        pageVisible = false;
        ClientNetworkService.getInstance().removeEventListener(realtimeListener);
        requestExecutor.shutdown();
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button approveButton = new Button("Duyệt");
            private final Button rejectButton = new Button("Từ chối");
            private final HBox box = new HBox(8, approveButton, rejectButton);

            {
                approveButton.setOnAction(e -> approveRequest(getTableView().getItems().get(getIndex())));
                rejectButton.setOnAction(e -> rejectRequest(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadRequests() {
        if (!pageVisible || !loading.compareAndSet(false, true)) {
            return;
        }

        requestTable.setPlaceholder(new Label("Đang tải dữ liệu..."));
        requestExecutor.execute(() -> {
            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.GET_CREATE_AUCTION_REQUESTS, null));
            List<AuctionDTO> loadedRequests = new ArrayList<>();
            if (response != null && response.isSuccess() && response.getData() instanceof List<?>) {
                for (Object object : (List<?>) response.getData()) {
                    loadedRequests.add((AuctionDTO) object);
                }
            }

            Platform.runLater(() -> {
                loading.set(false);
                if (!pageVisible) {
                    return;
                }

                if (response != null && response.isSuccess()) {
                    requests.setAll(loadedRequests);
                    applyFilters();
                    requestTable.setPlaceholder(new Label("Không có yêu cầu chờ duyệt."));
                } else {
                    requestTable.setPlaceholder(new Label("Không tải được dữ liệu."));
                }
            });
        });
    }

    private void approveRequest(AuctionDTO auction) {
        updateRequest(Action.ACCEPT_CREATE_AUCTION_REQUEST, auction.getId());
    }

    private void rejectRequest(AuctionDTO auction) {
        updateRequest(Action.REJECT_CREATE_AUCTION_REQUEST, auction.getId());
    }

    private void updateRequest(Action action, Long auctionId) {
        requestTable.setDisable(true);
        requestExecutor.execute(() -> {
            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(action, auctionId));

            Platform.runLater(() -> {
                requestTable.setDisable(false);
                if (!pageVisible) {
                    return;
                }

                if (response != null && response.isSuccess()) {
                    loadRequests();
                } else {
                    showAlert(response != null ? response.getMessage() : "Không kết nối được server");
                }
            });
        });
    }

    @FXML
    private void handleRefresh() {
        loadRequests();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    private void applyFilters() {
        String keyword = normalize(searchField.getText()).trim();
        String status = statusFilterBox.getValue();

        filteredRequests.setPredicate(request -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || normalize(request.getItemName()).contains(keyword)
                    || normalize(request.getSellerName()).contains(keyword)
                    || normalize(request.getItemCategory()).contains(keyword)
                    || String.valueOf(request.getId()).contains(keyword);
            boolean matchesStatus = status == null
                    || "ALL".equals(status)
                    || (request.getStatus() != null && request.getStatus().name().equals(status));
            return matchesKeyword && matchesStatus;
        });
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleRealtimeEvent(BaseResponse response) {
        if (!"ADMIN_REQUESTS_CHANGED".equals(response.getAction())) {
            return;
        }

        Platform.runLater(() -> {
            if (pageVisible) {
                loadRequests();
            }
        });
    }
}
