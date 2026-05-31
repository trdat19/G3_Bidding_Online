package client.controller;

import client.service.ClientNetworkService;
import client.session.ClientSession;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import shared.dto.common.UserDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;
import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminUsersController implements AdminPageLifecycle {
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
    private final FilteredList<UserDTO> filteredUsers =
            new FilteredList<>(allUsers, user -> true);
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Admin-Users-Request");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean loading = new AtomicBoolean();
    private volatile boolean pageVisible;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));

        usernameColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getUsername()));

        roleColumn.setCellValueFactory(
                c -> new SimpleStringProperty(String.valueOf(c.getValue().getRole())));

        statusColumn.setCellValueFactory(
                c -> new SimpleStringProperty(String.valueOf(c.getValue().getStatus())));

        createAtColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null
                        ? c.getValue().getCreatedAt().format(dateTimeFormatter)
                        : ""));

        fullnameColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getFullname()));

        emailColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getEmail()));

        roleFilterBox.setItems(FXCollections.observableArrayList("ALL", "ADMIN", "SELLER", "BIDDER"));
        statusFilterBox.setItems(FXCollections.observableArrayList("ALL", "ACTIVE", "BLOCKED"));
        roleFilterBox.setValue("ALL");
        statusFilterBox.setValue("ALL");
        userTable.setItems(filteredUsers);
        userTable.setPlaceholder(new Label("Đang tải dữ liệu..."));

        searchField.textProperty().addListener(
                (obs, oldValue, newValue) -> applyFilters());
        roleFilterBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> applyFilters());
        statusFilterBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> applyFilters());
        setupActionColumn();
    }

    @Override
    public void onPageShown() {
        pageVisible = true;
        loadUsers();
    }

    @Override
    public void onPageHidden() {
        pageVisible = false;
    }

    @FXML
    public void handleRefresh() {
        loadUsers();
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        if (!pageVisible || !loading.compareAndSet(false, true)) {
            return;
        }

        userTable.setPlaceholder(new Label("Đang tải dữ liệu..."));
        requestExecutor.execute(() -> {
            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(Action.GET_USERS_LIST, null));

            Platform.runLater(() -> {
                loading.set(false);
                if (!pageVisible) {
                    return;
                }

                if (response != null && response.isSuccess()) {
                    allUsers.setAll((List<UserDTO>) response.getData());
                    applyFilters();
                    userTable.setPlaceholder(new Label("Không có người dùng."));
                } else {
                    userTable.setPlaceholder(new Label("Không tải được dữ liệu."));
                }
            });
        });
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase().trim();
        String role = roleFilterBox.getValue();
        String status = statusFilterBox.getValue();

        filteredUsers.setPredicate(user -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || String.valueOf(user.getId()).contains(keyword)
                    || normalize(user.getUsername()).contains(keyword)
                    || normalize(user.getFullname()).contains(keyword)
                    || normalize(user.getEmail()).contains(keyword)
                    || String.valueOf(user.getRole()).toLowerCase().contains(keyword)
                    || String.valueOf(user.getStatus()).toLowerCase().contains(keyword);
            boolean matchesRole = role == null || "ALL".equals(role)
                    || String.valueOf(user.getRole()).equals(role);
            boolean matchesStatus = status == null || "ALL".equals(status)
                    || String.valueOf(user.getStatus()).equals(status);
            return matchesKeyword && matchesRole && matchesStatus;
        });
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
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
                Long currentUserId = ClientSession.getCurrentUserId();

                if (user.getId().equals(currentUserId)) {
                    statusButton.setDisable(true);
                    statusButton.setText("Tài khoản hiện tại");
                } else if (user.getRole() == UserRole.ADMIN && active) {
                    statusButton.setDisable(true);
                    statusButton.setText("Không thể khóa Admin");
                } else {
                    statusButton.setDisable(false);
                    statusButton.setText(active ? "Khóa" : "Mở khóa");
                }
                setGraphic(new HBox(8, statusButton));
            }
        });
    }

    private void toggleUserStatus(UserDTO user) {
        Action action = user.getStatus() == UserStatus.ACTIVE
                ? Action.DISABLE_USER
                : Action.ENABLE_USER;

        userTable.setDisable(true);
        requestExecutor.execute(() -> {
            BaseResponse response = ClientNetworkService.getInstance()
                    .sendRequest(new BaseRequest(action, user.getId()));

            Platform.runLater(() -> {
                userTable.setDisable(false);
                if (!pageVisible) {
                    return;
                }

                if (response != null && response.isSuccess()) {
                    loadUsers();
                } else {
                    showAlert(response != null ? response.getMessage() : "Không kết nối được server");
                }
            });
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
