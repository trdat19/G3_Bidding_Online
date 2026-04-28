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
import shared.enums.UserRole;
public class RegisterFormController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private ComboBox<UserRole> comboBox;
    @FXML
    public void initialize() {
        comboBox.getItems().addAll(UserRole.values()); //Su dung enum trong share de tranh sai String
        comboBox.setValue(UserRole.BIDDER); //gia tri mac dinh
    }
    @FXML
    private void handleRegister() {
        String usernameInput = username.getText();
        String passwordInput = password.getText();
        UserRole role = comboBox.getValue();
    }
    @FXML
    private void goToLogin() {
        try {
            Stage stage = (Stage) username.getScene().getWindow();
            Scene scene = new Scene(
                    FXMLLoader.load(getClass().getResource("/view/login.fxml"))
            );
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
