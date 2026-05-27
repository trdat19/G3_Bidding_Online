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
import shared.dto.common.ItemDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.ItemCategory;
import shared.enums.ItemStatus;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminProductsController implements AdminPageLifecycle {
    @FXML private TableView<ItemDTO> productTable;
    @FXML private TableColumn<ItemDTO, Long> idColumn;
    @FXML private TableColumn<ItemDTO, String> nameColumn;
    @FXML private TableColumn<ItemDTO, String> categoryColumn;
    @FXML private TableColumn<ItemDTO, String> sellerColumn;
    @FXML private TableColumn<ItemDTO, String> startPriceColumn;
    @FXML private TableColumn<ItemDTO, String> currentPriceColumn;
    @FXML private TableColumn<ItemDTO, String> statusColumn;
    @FXML private TableColumn<ItemDTO, String> createdAtColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterBox;
    @FXML private ComboBox<String> statusFilterBox;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ObservableList<ItemDTO> allItems = FXCollections.observableArrayList();
    private final FilteredList<ItemDTO> filteredItems = new FilteredList<>(allItems, item -> true);
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Admin-Products-Request");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean loading = new AtomicBoolean();
    private volatile boolean pageVisible;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        categoryColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCategory())));
        sellerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSellerName()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getStatus())));
        startPriceColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPriceStart() == null ? "" : "$" + c.getValue().getPriceStart().toPlainString()));
        currentPriceColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCurrentPrice() == null ? "" : "$" + c.getValue().getCurrentPrice().toPlainString()));
        createdAtColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() == null ? "" : c.getValue().getCreatedAt().format(formatter)));

        categoryFilterBox.getItems().add("ALL");
        for (ItemCategory category : ItemCategory.values()) {
            categoryFilterBox.getItems().add(category.name());
        }
        statusFilterBox.getItems().add("ALL");
        for (ItemStatus status : ItemStatus.values()) {
            statusFilterBox.getItems().add(status.name());
        }
        categoryFilterBox.setValue("ALL");
        statusFilterBox.setValue("ALL");
        productTable.setItems(filteredItems);
        productTable.setPlaceholder(new Label("Đang tải dữ liệu..."));

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        categoryFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    @Override
    public void onPageShown() {
        pageVisible = true;
        loadItems();
    }

    @Override
    public void onPageHidden() {
        pageVisible = false;
    }

    @FXML
    private void handleRefresh() {
        loadItems();
    }

    @SuppressWarnings("unchecked")
    private void loadItems() {
        if (!pageVisible || !loading.compareAndSet(false, true)) {
            return;
        }

        productTable.setPlaceholder(new Label("Đang tải dữ liệu..."));
        requestExecutor.execute(() -> {
            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.GET_ALL_ITEMS, null));

            Platform.runLater(() -> {
                loading.set(false);
                if (!pageVisible) {
                    return;
                }

                if (response != null && response.isSuccess()) {
                    allItems.setAll((List<ItemDTO>) response.getData());
                    applyFilters();
                    productTable.setPlaceholder(new Label("Không có sản phẩm."));
                } else {
                    productTable.setPlaceholder(new Label("Không tải được dữ liệu."));
                }
            });
        });
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase().trim();
        String category = categoryFilterBox.getValue();
        String status = statusFilterBox.getValue();

        filteredItems.setPredicate(item -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || String.valueOf(item.getId()).contains(keyword)
                    || normalize(item.getName()).contains(keyword)
                    || normalize(item.getSellerName()).contains(keyword)
                    || String.valueOf(item.getCategory()).toLowerCase().contains(keyword)
                    || String.valueOf(item.getStatus()).toLowerCase().contains(keyword);
            boolean matchesCategory = category == null || "ALL".equals(category)
                    || String.valueOf(item.getCategory()).equals(category);
            boolean matchesStatus = status == null || "ALL".equals(status)
                    || String.valueOf(item.getStatus()).equals(status);
            return matchesKeyword && matchesCategory && matchesStatus;
        });
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
