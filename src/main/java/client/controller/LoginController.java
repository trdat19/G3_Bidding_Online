package client.controller;

import client.service.ClientNetworkService;
import client.session.ClientSession;
import client.util.StageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.stage.Stage;
import server.model.user.User;
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
            ClientSession.setCurrentUser(LogginUser);
            UserRole role = LogginUser.getRole();
            if (role == UserRole.BIDDER) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/bidder-dashboard.fxml"));
                    Parent root = loader.load();

                    BidderDashboardController controller = loader.getController();
                    controller.setBidderName(LogginUser.getFullName());

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    StageUtils.setMaximizedScene(stage, root);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(role == UserRole.SELLER)
            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/seller-dashboard.fxml"));
                    Parent root = loader.load();

                    SellerDashboardController controller = loader.getController();
                    controller.setSellerName(LogginUser.getFullName());

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    StageUtils.setMaximizedScene(stage, root);
                    stage.show();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if(role == UserRole.ADMIN)  {
                loadScene("/view/admin/admin-dashboard.fxml", event);
            }
        }
        else {
            //Sửa dòng đầu Dương nhé tại nếu server chưa chạy thì code cũ sẽ bị crash
            String message = response != null ? response.getMessage() : "Không kết nối được server";
            errorLabel.setText(message);
        }
    }
    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
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
