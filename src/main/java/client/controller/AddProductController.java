package client.controller;
import client.model.Item;
import client.service.ClientNetworkService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import shared.dto.request.item.CreateItemRequest;
import shared.dto.response.BaseResponse;
import shared.enums.ItemCategory;

import java.math.BigDecimal;
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
    private void handleSave()
    {
        String title = nameField.getText().trim();
        String categoryText = categoryField.getText().trim();
        String description = descriptionField.getText().trim();
        String priceText = priceField.getText().trim();

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (title.isEmpty() || categoryText.isEmpty() || description.isEmpty()
                || priceText.isEmpty() || startDate == null || endDate == null) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        try {
            double startPrice = Double.parseDouble(priceText);

            LocalDateTime startDateTime = LocalDateTime.of(
                    startDate,
                    LocalTime.of(startHourBox.getValue(), startMinuteBox.getValue())
            );

            LocalDateTime endDateTime = LocalDateTime.of(
                    endDate,
                    LocalTime.of(endHourBox.getValue(), endMinuteBox.getValue())
            );

            if (startDateTime.isAfter(endDateTime)) {
                messageLabel.setText("Thời gian bắt đầu phải trước thời gian kết thúc");
                return;
            }

            CreateItemRequest request = new CreateItemRequest(
                    title,
                    description,
                    ItemCategory.valueOf(categoryText.toUpperCase()),
                    BigDecimal.valueOf(startPrice)
            );

            BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

            if (response != null && response.isSuccess()) {
                Item newItem = new Item(
                        title,
                        categoryText,
                        description,
                        startPrice,
                        startPrice,
                        "None",
                        startDateTime,
                        endDateTime,
                        "PENDING",
                        0
                );

                if (sellerDashboardController != null) {
                    sellerDashboardController.addNewProduct(newItem);
                }

                closeWindow();
            } else {
                errorLabel.setText(response != null ? response.getMessage() : "Không kết nối được server");
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Giá khởi điểm phải là số");
        } catch (IllegalArgumentException e) {
            errorLabel.setText("Danh mục phải là ELECTRONICS, ART hoặc VEHICLE");
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
