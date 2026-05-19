package client.controller;

import client.service.ClientNetworkService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import shared.dto.common.AuctionDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.util.List;

public class AdminRequestController {

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

    @FXML
    private void initialize() {
        productNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getItemName()));

        sellerColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSellerName()));

        categoryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getItemCategory()));

        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().name()));

        startPriceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getStartPrice())));

        minIncrementColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getMinIncrement())));

        startTimeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getStartTime())));

        endTimeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getEndTime())));

        requestTable.setItems(requests);
        setupActionColumn();
        loadRequests();
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
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.GET_CREATE_AUCTION_REQUESTS, null));

        requests.clear();

        if (response == null || !response.isSuccess()) {
            showAlert(response != null ? response.getMessage() : "Không kết nối được server");
            return;
        }

        List<?> data = (List<?>) response.getData();
        for (Object obj : data) {
            requests.add((AuctionDTO) obj);
        }
    }

    private void approveRequest(AuctionDTO auction) {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.ACCEPT_CREATE_AUCTION_REQUEST, auction.getId()));

        if (response != null && response.isSuccess()) {
            loadRequests();
        } else {
            showAlert(response != null ? response.getMessage() : "Không kết nối được server");
        }
    }

    private void rejectRequest(AuctionDTO auction) {
        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.REJECT_CREATE_AUCTION_REQUEST, auction.getId()));

        if (response != null && response.isSuccess()) {
            loadRequests();
        } else {
            showAlert(response != null ? response.getMessage() : "Không kết nối được server");
        }
    }

    @FXML
    private void handleRefresh() {
        loadRequests();
    }

    @FXML
    private void handleSearch() {
        loadRequests();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}