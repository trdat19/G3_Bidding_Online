package client.controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import client.model.Item;

public class AdminDashboardController {
    @FXML private TableView<Item> auctionTable;
    @FXML private TableColumn<Item, String> productColumn;
    @FXML private TableColumn<Item, String> sellerColumn;
    @FXML private TableColumn<Item, Double> currentPriceColumn;
    @FXML private TableColumn<Item, String> statusColumn;
    @FXML private TableColumn<Item, String> endTimeColumn;

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void showDashboard() {

    }
    @FXML
    private void showUsers() {

    }
    @FXML
    private void showAuctions() {

    }
    @FXML
    private void showProducts() {

    }
    @FXML
    private void showReports() {

    }
    @FXML
    private void showSettings() {

    }
}
