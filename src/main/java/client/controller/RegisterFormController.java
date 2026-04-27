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

public class RegisterFormController {
    @FXML
    private TextField username;
    private PasswordField password;
    private PasswordField confirmPassword;

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }

    @FXML
    private void handleRegister() {
        String user = username.getText();
        String pass = password.getText();
        String confirm = confirmPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Không được để trống!");
        }

        if (!pass.equals(confirm)) {
            showAlert("Mật khẩu xác nhận không khớp!");
        }

        //bắt đầu gửi dl

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
