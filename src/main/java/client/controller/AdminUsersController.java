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

import java.util.List;
public class AdminUsersController {
    @FXML private TableView<UserDTO> userTable;
    @FXML private TableColumn<UserDTO, Long> idColumn;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, String> roleColumn;
    @FXML private TableColumn<UserDTO, String> statusColumn;
    @FXML private TableColumn<UserDTO, String> createAtColumn;
    @FXML private TableColumn<UserDTO, String> ActionColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c-> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        usernameColumn.setCellValueFactory(c-> new SimpleStringProperty(c.getValue().getUsername()));
        roleColumn.setCellValueFactory(c-> new SimpleStringProperty(String.valueOf(c.getValue().getRole())));
        statusColumn.setCellValueFactory(c-> new SimpleStringProperty(String.valueOf(c.getValue().getStatus())));
        //createAtColumn.setCellValueFactory(c-> new SimpleStringProperty(""));

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
            userTable.getItems().setAll(user);
        }
        else {
            System.out.println("Load users failed: " + (response == null ? "No response" : response.getMessage()));
        }
    }

}