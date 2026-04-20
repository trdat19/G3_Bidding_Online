package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

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

        if(user.equals("bidder") && pass.equals("bidder123")) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/auction-list-view.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(user.equals("seller") && pass.equals("seller123")) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/seller-dashboard.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Wrong password or username");
        }
    }
}