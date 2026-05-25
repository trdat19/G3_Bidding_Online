package client.controller;

import client.service.ClientNetworkService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import shared.dto.common.UserDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.HBox;
import shared.enums.UserStatus;

import java.time.format.DateTimeFormatter;
import java.util.List;
public class AdminUsersController {
    @FXML private TableView<UserDTO> userTable;
    @FXML private TableColumn<UserDTO, Long> idColumn;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, String> roleColumn;
    @FXML private TableColumn<UserDTO, String> statusColumn;
    @FXML private TableColumn<UserDTO, String> createAtColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterBox;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private TableColumn<UserDTO, String> fullnameColumn;
    @FXML private TableColumn<UserDTO, String> emailColumn;
    @FXML private TableColumn<UserDTO, Void> actionColumn;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ObservableList<UserDTO> allUsers = FXCollections.observableArrayList();
    private final FilteredList<UserDTO> filteredUsers = new FilteredList<>(allUsers, user -> true);

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c-> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        usernameColumn.setCellValueFactory(c-> new SimpleStringProperty(c.getValue().getUsername()));
        roleColumn.setCellValueFactory(c-> new SimpleStringProperty(String.valueOf(c.getValue().getRole())));
        statusColumn.setCellValueFactory(c-> new SimpleStringProperty(String.valueOf(c.getValue().getStatus())));
        createAtColumn.setCellValueFactory(c-> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null
                        ? c.getValue().getCreatedAt().format(dateTimeFormatter)
                        : ""));
        fullnameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullname()));
        emailColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        roleFilterBox.setItems(FXCollections.observableArrayList("ALL", "ADMIN", "SELLER", "BIDDER"));
        statusFilterBox.setItems(FXCollections.observableArrayList("ALL", "ACTIVE", "BLOCKED"));
        roleFilterBox.setValue("ALL");
        statusFilterBox.setValue("ALL");

        userTable.setItems(filteredUsers);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        roleFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        setupActionColumn();

        loadUsers();
    }
    @FXML
    public void handleRefresh() {
        loadUsers();
    }
    private void loadUsers() {
        BaseRequest request = new BaseRequest(Action.GET_USERS_LIST, null);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            List<UserDTO> user = (List<UserDTO>) response.getData();
            allUsers.setAll(user);
            applyFilters();
        }
        else {
            System.out.println("Load users failed: " + (response == null ? "No response" : response.getMessage()));
        }
    }
    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String role = roleFilterBox.getValue();
        String status = statusFilterBox.getValue();

        filteredUsers.setPredicate(user -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || String.valueOf(user.getUsername()).toLowerCase().contains(keyword)
                    || String.valueOf(user.getFullname()).toLowerCase().contains(keyword)
                    || String.valueOf(user.getEmail()).toLowerCase().contains(keyword);

            boolean matchesRole = role == null || "ALL".equals(role)
                    || String.valueOf(user.getRole()).equals(role);

            boolean matchesStatus = status == null || "ALL".equals(status)
                    || String.valueOf(user.getStatus()).equals(status);

            return matchesKeyword && matchesRole && matchesStatus;
        });
    }
    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button statusButton = new Button();

            {
                statusButton.setOnAction(event -> {
                    UserDTO user = getTableView().getItems().get(getIndex());
                    toggleUserStatus(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                UserDTO user = getTableView().getItems().get(getIndex());
                boolean active = user.getStatus() == UserStatus.ACTIVE;

                statusButton.setText(active ? "Khóa" : "Mở khóa");
                statusButton.getStyleClass().setAll(active ? "danger-btn" : "success-btn");

                setGraphic(new HBox(8, statusButton));
            }
        });
    }

    private void toggleUserStatus(UserDTO user) {
        Action action = user.getStatus() == UserStatus.ACTIVE
                ? Action.DISABLE_USER
                : Action.ENABLE_USER;

        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(action, user.getId()));

        if (response != null && response.isSuccess()) {
            loadUsers();
        } else {
            showAlert(response != null ? response.getMessage() : "Không kết nối được server");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}