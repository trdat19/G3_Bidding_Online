package client.controller;
import client.model.Item;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProductController {
    @FXML
    private TextField nameField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField priceField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label errorLabel;
    private SellerDashboardController sellerDashboardController;
    public void setSellerDashboardController(SellerDashboardController sellerDashboardController) {
        this.sellerDashboardController = sellerDashboardController;
    }
    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String description = descriptionField.getText().trim();
        String price = priceField.getText().trim();

        Item newItem = new Item(name, category, description, price, "OPEN");

        if (name.isEmpty() || category.isEmpty() || description.isEmpty() || price.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin");
        }
        if (sellerDashboardController != null) {
            sellerDashboardController.addNewProduct(newItem);
        }
        closeWindow();
    }
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
