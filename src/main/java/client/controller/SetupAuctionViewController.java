package client.controller;

import client.service.ClientNetworkService;
import shared.dto.common.ItemDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
    @FXML private Spinner<Integer> minIncrementSpinner;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private ComboBox<Integer> startHourBox;
    @FXML private ComboBox<Integer> startMinuteBox;
    @FXML private ComboBox<Integer> endHourBox;
    @FXML private ComboBox<Integer> endMinuteBox;

    @FXML private Label errorLabel;

    private ItemDTO item;
    private SellerDashboardController sellerDashboardController;

    public void setData(ItemDTO item, SellerDashboardController sellerDashboardController) {
        this.item = item;
        this.sellerDashboardController = sellerDashboardController;

        productNameLabel.setText(item.getName());
        productCategoryLabel.setText(item.getCategory().name());
    }

    @FXML
    private void initialize() {
        errorLabel.setText("");

        for (int i = 0; i < 24; i++) {
            startHourBox.getItems().add(i);
            endHourBox.getItems().add(i);
        }

        for (int i = 0; i < 60; i += 5) {
            startMinuteBox.getItems().add(i);
            endMinuteBox.getItems().add(i);
        }

        startHourBox.setValue(19);
        startMinuteBox.setValue(0);
        endHourBox.setValue(20);
        endMinuteBox.setValue(0);

        minIncrementSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1)
        );

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

        if (startPriceText.isEmpty()
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

        try {
            startPrice = Double.parseDouble(startPriceText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Giá khởi điểm phải là số");
            return;
        }

        if (startPrice <= 0) {
            errorLabel.setText("Giá khởi điểm phải lớn hơn 0");
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
        data.put("minIncrement", BigDecimal.valueOf(minIncrementSpinner.getValue()));
        data.put("buyNowPrice", null);
        data.put("startTime", startDateTime);
        data.put("endTime", endDateTime);

        BaseRequest request = new BaseRequest(Action.SEND_CREATE_AUCTION_REQUEST, data);
        BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            if (sellerDashboardController != null) {
                sellerDashboardController.refreshProducts();
            }
            closeWindow();
        } else {
            errorLabel.setText(response != null ? response.getMessage() : "Không gửi được yêu cầu đấu giá");
        }
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