package client.controller;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
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
    private void handleSave() {
        String name = nameField.getText();
        String category = categoryField.getText();
        String description = descriptionField.getText();
        String price = priceField.getText();
    }
    @FXML
    private void handleCancel() {

    }
}
