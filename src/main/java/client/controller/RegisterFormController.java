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

    @FXML
    private void handleRegister() {

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
