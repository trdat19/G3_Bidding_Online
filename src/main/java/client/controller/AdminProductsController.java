package client.controller;

import client.service.ClientNetworkService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
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

public class AdminProductsController {

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

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ObservableList<ItemDTO> allItems =
            FXCollections.observableArrayList();

    private final FilteredList<ItemDTO> filteredItems =
            new FilteredList<>(allItems, item -> true);

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getId()));

        nameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getName()));

        categoryColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCategory())));

        sellerColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSellerName()));

        statusColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getStatus())));
        startPriceColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getPriceStart() == null
                                ? ""
                                : "$" + c.getValue().getPriceStart().toPlainString()
                ));

        currentPriceColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getCurrentPrice() == null
                                ? ""
                                : "$" + c.getValue().getCurrentPrice().toPlainString()
                ));

        createdAtColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getCreatedAt() == null
                                ? ""
                                : c.getValue().getCreatedAt().format(formatter)
                ));

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

        searchField.textProperty()
                .addListener((obs, oldValue, newValue) -> applyFilters());

        categoryFilterBox.valueProperty()
                .addListener((obs, oldValue, newValue) -> applyFilters());

        statusFilterBox.valueProperty()
                .addListener((obs, oldValue, newValue) -> applyFilters());

        loadItems();
    }

    @FXML
    private void handleRefresh() {
        loadItems();
    }

    private void loadItems() {
        BaseRequest request = new BaseRequest(Action.GET_ALL_ITEMS, null);

        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(request);

        if (response != null && response.isSuccess()) {
            List<ItemDTO> items = (List<ItemDTO>) response.getData();
            allItems.setAll(items);
            applyFilters();
        } else {
            showAlert(response == null
                    ? "Không kết nối được server"
                    : response.getMessage());
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase().trim();

        String category = categoryFilterBox.getValue();
        String status = statusFilterBox.getValue();

        filteredItems.setPredicate(item -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || normalize(item.getName()).contains(keyword)
                    || normalize(item.getSellerName()).contains(keyword);

            boolean matchesCategory = category == null
                    || "ALL".equals(category)
                    || String.valueOf(item.getCategory()).equals(category);

            boolean matchesStatus = status == null
                    || "ALL".equals(status)
                    || String.valueOf(item.getStatus()).equals(status);

            return matchesKeyword && matchesCategory && matchesStatus;
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
}