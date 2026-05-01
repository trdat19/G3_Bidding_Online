package client.controller;

import client.service.ClientNetworkService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import server.model.user.User;
import shared.enums.UserRole;
import shared.request.BaseRequest;

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

        // CODE CUA VANH
//        String user = username.getText();
//        String pass = password.getText();
//
//        if(user.equals("bidder") && pass.equals("bidder123")) {
//            loadScene("/view/bidder-dashboard.fxml", event);
//        }
//        else if(user.equals("seller") && pass.equals("seller123")) {
//            loadScene("/view/seller-dashboard.fxml", event);
//        }
//        else if(user.equals("admin") && pass.equals("admin123"))  {
//            loadScene("/view/admin-dashboard.fxml", event);
//        }
//        else {
//            errorLabel.setText("Wrong password or username");
//        }

        // CODE CUA DUONG
        String user = username.getText();
        String pass = password.getText();
        Map<String, String > loginData = new HashMap<>();
        loginData.put("username" , user);
        loginData.put("password" , pass);
        BaseRequest request = new BaseRequest("LOGIN", loginData);
        shared.response.BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        if(response != null && response.isSuccess())
        {
            User LogginUser = (User) response.getData();
            UserRole role =  LogginUser.getRole();
            if(role == UserRole.BIDDER) {
                loadScene("/view/bidder-dashboard.fxml", event);
            }
            else if(role == UserRole.SELLER) {
                loadScene("/view/seller-dashboard.fxml", event);
            }
            else if(role == UserRole.ADMIN) {
                loadScene("/view/admin-dashboard.fxml", event);
            }
        }
        else
        {
            response.getMessage();
             errorLabel.setText("Sai tài khoản/mật khẩu");
        }




    }
    private void loadScene(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRegister() {
        try {
            Stage stage = (Stage) username.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/register-form.fxml"))
            );
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}