package client.controller;

import client.model.Item;
import client.service.ClientNetworkService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/*Đây laf màn hình khi admin đã duyệt sản phầm
  Seller sẽ nhập startPrice, startTime, endTime, bidCount
 */

public class SetupAuctionViewController {

    @FXML private Label productNameLabel;
    @FXML private Label productCategoryLabel;

    @FXML private TextField startPriceField;
    @FXML private TextField minIncrementField;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private ComboBox<Integer> startHourBox;
    @FXML private ComboBox<Integer> startMinuteBox;
    @FXML private ComboBox<Integer> endHourBox;
    @FXML private ComboBox<Integer> endMinuteBox;

    @FXML private Label errorLabel;

    private Item item;
    private SellerDashboardController sellerDashboardController;

    public void setData(Item item, SellerDashboardController sellerDashboardController) {
        this.item = item;
        this.sellerDashboardController = sellerDashboardController;

        productNameLabel.setText(item.getTitle());
        productCategoryLabel.setText(item.getCategory());
    }

    @FXML
    private void initialize() {
        errorLabel.setText("");

        for (int i = 0; i < 24; i++) {
            startHourBox.getItems().add(i);
            endHourBox.getItems().add(i);
        }

        for (int i = 0; i < 60; i ++) {
            startMinuteBox.getItems().add(i);
            endMinuteBox.getItems().add(i);
        }

        startHourBox.setValue(19);
        startMinuteBox.setValue(0);
        endHourBox.setValue(20);
        endMinuteBox.setValue(0);

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        if (item == null) {
            errorLabel.setText("Không tìm thấy sản phẩm cần thiết lập");
            return;
        }

        String startPriceText = startPriceField.getText().trim();
        String minIncrementText = minIncrementField.getText().trim();

        if (startPriceText.isEmpty()
                || minIncrementText.isEmpty()
                || startDatePicker.getValue() == null
                || endDatePicker.getValue() == null
                || startHourBox.getValue() == null
                || startMinuteBox.getValue() == null
                || endHourBox.getValue() == null
                || endMinuteBox.getValue() == null) {

            errorLabel.setText("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        double startPrice;
        double minIncrement;

        try {
            startPrice = Double.parseDouble(startPriceText);
            minIncrement = Double.parseDouble(minIncrementText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Giá khởi điểm và bước nhảy giá phải là số");
            return;
        }

        if (startPrice <= 0) {
            errorLabel.setText("Giá khởi điểm phải lớn hơn 0");
            return;
        }

        if (minIncrement <= 0) {
            errorLabel.setText("Bước nhảy giá phải lớn hơn 0");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        LocalTime startTime = LocalTime.of(
                startHourBox.getValue(),
                startMinuteBox.getValue()
        );

        LocalTime endTime = LocalTime.of(
                endHourBox.getValue(),
                endMinuteBox.getValue()
        );

        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        if (!startDateTime.isBefore(endDateTime)) {
            errorLabel.setText("Thời gian bắt đầu phải trước thời gian kết thúc");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("itemId", item.getId());
        data.put("startPrice", BigDecimal.valueOf(startPrice));
        data.put("minIncrement", BigDecimal.valueOf(minIncrement));
        data.put("buyNowPrice", BigDecimal.valueOf(startPrice * 10));
        data.put("startTime", startDateTime);
        data.put("endTime", endDateTime);

        BaseResponse response = ClientNetworkService.getInstance()
                .sendRequest(new BaseRequest(Action.SEND_CREATE_AUCTION_REQUEST, data));

        if (response == null || !response.isSuccess()) {
            errorLabel.setText(response != null ? response.getMessage() : "Khong ket noi duoc server");
            return;
        }

        if (sellerDashboardController != null) {
            sellerDashboardController.refreshProducts();
        }

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) startPriceField.getScene().getWindow();
        stage.close();
    }
}