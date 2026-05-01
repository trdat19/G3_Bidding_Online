package client.controller;
import client.model.Item;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

public class AddProductController {
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Integer> startHourBox;
    @FXML private ComboBox<Integer> startMinuteBox;
    @FXML private ComboBox<Integer> endHourBox;
    @FXML private ComboBox<Integer> endMinuteBox;
    @FXML private Label errorLabel;
    @FXML private Label messageLabel;


    private SellerDashboardController sellerDashboardController;
    public void setSellerDashboardController(SellerDashboardController sellerDashboardController) {
        this.sellerDashboardController = sellerDashboardController;
    }
    @FXML
    private void initialize() {     //tao lua chon ngay gio cho nguoi dung
        for (int i = 0; i < 24; i++) {
            startHourBox.getItems().add(i);
            endHourBox.getItems().add(i);
        }
        for (int i = 0; i < 60; i += 5) {
            startMinuteBox.getItems().add(i);
            endMinuteBox.getItems().add(i);
        }
        startHourBox.setValue(19); //gia tri mac dinh
        startMinuteBox.setValue(0);

        endHourBox.setValue(20);
        endMinuteBox.setValue(0);
    }
    @FXML
    private void handleSave() {
        String title = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String description = descriptionField.getText().trim();
        Double startPrice = Double.parseDouble(priceField.getText().trim());

        LocalDate startDate = startDatePicker.getValue();
        LocalTime startTime = LocalTime.of(startHourBox.getValue(), startMinuteBox.getValue());
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);

        LocalDate endDate = endDatePicker.getValue();
        LocalTime endTime = LocalTime.of(endHourBox.getValue(), endMinuteBox.getValue());
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        Item newItem = new Item(title,category,description, startPrice, startPrice, "None", startDateTime, endDateTime, "OPEN", 0);

        if (title.isEmpty() || category.isEmpty() || description.isEmpty() || startPrice == null || startDate == null || endDate == null) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin");
        }
        if (sellerDashboardController != null) {
            sellerDashboardController.addNewProduct(newItem);
        }
        closeWindow();
        if (startDateTime.isAfter(endDateTime)) {
            messageLabel.setText("Thời gian bắt đầu phải trước thời gian kết thúc");
            return;
        }
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
