package client.controller;

import client.service.ClientNetworkService;
import client.util.StageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.stage.Stage;
import shared.dto.common.UserDTO;
import shared.dto.request.auth.LoginRequest;
import shared.dto.response.BaseResponse;
import shared.dto.response.auth.LoginResponse;
import shared.enums.UserRole;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {

        String user = username.getText();
        String pass = password.getText();

        LoginRequest loginRequestData = new LoginRequest(user, pass);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(loginRequestData);

        if (response != null && response.isSuccess()) {
            if (response instanceof LoginResponse loginResponse)
            {
//                UserRole role = loginResponse.getUser().getRole();
                UserDTO loggedInUser = loginResponse.getUser();
                UserRole role = loggedInUser.getRole();

                if (role == UserRole.BIDDER) {
                    loadScene("/view/bidder-dashboard.fxml", event, loggedInUser);
                } else if (role == UserRole.SELLER) {
                    loadScene("/view/seller-dashboard.fxml", event, loggedInUser);
                } else if (role == UserRole.ADMIN) {
                    loadScene("/view/admin/admin-dashboard.fxml", event, loggedInUser);
                }
            }
        }
        else {
            //Sửa dòng đầu Dương nhé tại nếu server chưa chạy thì code cũ sẽ bị crash
            String message = response != null ? response.getMessage() : "Không kết nối được server";
            errorLabel.setText(message);
        }
    }
    private void loadScene(String fxmlPath, ActionEvent event, UserDTO user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof SellerDashboardController sellerController) {
                sellerController.setCurrentUser(user);
            } else if (controller instanceof BidderDashboardController bidderController) {
                bidderController.setCurrentUser(user);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            StageUtils.setMaximizedScene(stage,root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRegister() {
        try {
            Stage stage = (Stage) username.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/register-form.fxml"));
            StageUtils.setMaximizedScene(stage, root);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
