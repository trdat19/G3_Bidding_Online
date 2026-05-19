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
import server.model.user.User;
import shared.dto.common.UserDTO;
import shared.dto.response.BaseResponse;
import shared.enums.UserRole;
import shared.dto.request.BaseRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label errorLabel;

    private BidderDashboardController bidderController;
    private SellerDashboardController sellerController;

    @FXML
    private void handleLogin(ActionEvent event) {

        String user = username.getText();
        String pass = password.getText();
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", user);
        loginData.put("password", pass);
        BaseRequest request = new BaseRequest("LOGIN", loginData);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            User LogginUser = (User) response.getData();
            UserRole role = LogginUser.getRole();
            String fullName = LogginUser.getFullName();
            if (role == UserRole.BIDDER) {
                loadScene("/view/bidder-dashboard.fxml", event, fullName);

            }
            else if(role == UserRole.SELLER) {
                loadScene("/view/seller-dashboard.fxml", event, fullName);
            }
            else if(role == UserRole.ADMIN)  {
                loadScene("/view/admin/admin-dashboard.fxml", event, fullName);
            }
        }
        else {
            //Sửa dòng đầu Dương nhé tại nếu server chưa chạy thì code cũ sẽ bị crash
            String message = response != null ? response.getMessage() : "Không kết nối được server";
            errorLabel.setText(message);
        }
    }
    private void loadScene(String fxmlPath, ActionEvent event, String fullname) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BidderDashboardController) {
                bidderController = (BidderDashboardController) controller;
                bidderController.setFullName(fullname);
            }
            else if (controller instanceof SellerDashboardController) {
                sellerController = (SellerDashboardController) controller;
                sellerController.setFullName(fullname);
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
