package client.controller;

import client.service.ClientNetworkService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import shared.dto.common.AuctionDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminAuctionsController implements AdminPageLifecycle {
    @FXML private TableView<AuctionDTO> auctionTable;
    @FXML private TableColumn<AuctionDTO, Long> idColumn;
    @FXML private TableColumn<AuctionDTO, String> productColumn;
    @FXML private TableColumn<AuctionDTO, String> sellerColumn;
    @FXML private TableColumn<AuctionDTO, String> leaderColumn;
    @FXML private TableColumn<AuctionDTO, String> currentPriceColumn;
    @FXML private TableColumn<AuctionDTO, String> statusColumn;
    @FXML private TableColumn<AuctionDTO, String> endTimeColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterBox;

    private final ObservableList<AuctionDTO> allAuctions = FXCollections.observableArrayList();
    private final FilteredList<AuctionDTO> filteredAuctions =
            new FilteredList<>(allAuctions, auction -> true);
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Admin-Auctions-Request");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean loading = new AtomicBoolean();
    private volatile boolean pageVisible;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        productColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemName()));
        sellerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSellerName()));
        leaderColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLeaderName() != null ? data.getValue().getLeaderName() : "Chưa có"));
        currentPriceColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDisplayPrice() != null
                        ? data.getValue().getDisplayPrice().toPlainString() : "0"));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStatus() != null ? data.getValue().getStatus().name() : ""));
        endTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEndTime() != null ? data.getValue().getEndTime().toString() : ""));

        statusFilterBox.setItems(FXCollections.observableArrayList(
                "ALL", "WAITING_APPROVAL", "OPEN", "RUNNING", "FINISHED", "CANCELLED"));
        statusFilterBox.setValue("ALL");
        auctionTable.setItems(filteredAuctions);
        auctionTable.setPlaceholder(new Label("Đang tải dữ liệu..."));
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    @Override
    public void onPageShown() {
        pageVisible = true;
        loadAuctions();
    }

    @Override
    public void onPageHidden() {
        pageVisible = false;
    }

    @FXML
    private void handleRefresh() {
        loadAuctions();
    }

    @SuppressWarnings("unchecked")
    private void loadAuctions() {
        if (!pageVisible || !loading.compareAndSet(false, true)) {
            return;
        }

        auctionTable.setPlaceholder(new Label("Đang tải dữ liệu..."));
        requestExecutor.execute(() -> {
            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.GET_ALL_AUCTIONS, null));

            Platform.runLater(() -> {
                loading.set(false);
                if (!pageVisible) {
                    return;
                }

                if (response != null && response.isSuccess()) {
                    allAuctions.setAll((List<AuctionDTO>) response.getData());
                    applyFilters();
                    auctionTable.setPlaceholder(new Label("Không có phiên đấu giá."));
                } else {
                    auctionTable.setPlaceholder(new Label("Không tải được dữ liệu."));
                }
            });
        });
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();
        String status = statusFilterBox.getValue();

        filteredAuctions.setPredicate(auction -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || String.valueOf(auction.getId()).contains(keyword)
                    || normalize(auction.getItemName()).contains(keyword)
                    || normalize(auction.getSellerName()).contains(keyword)
                    || normalize(auction.getLeaderName()).contains(keyword)
                    || (auction.getStatus() != null
                    && auction.getStatus().name().toLowerCase().contains(keyword));
            boolean matchesStatus = status == null
                    || "ALL".equals(status)
                    || (auction.getStatus() != null && auction.getStatus().name().equals(status));
            return matchesKeyword && matchesStatus;
        });
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
