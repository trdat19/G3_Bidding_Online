package client.controller;

import client.service.ClientNetworkService;
import client.util.StageUtils;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import shared.enums.Action;
import shared.enums.UserRole;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.util.HashMap;
import java.util.Map;

public class RegisterFormController {
    @FXML private TextField username;
    @FXML private TextField fullname; // Thêm mới
    @FXML private TextField email;    // Thêm mới
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private ComboBox<UserRole> comboBox;

    @FXML
    public void initialize() {
        comboBox.getItems().addAll(UserRole.values());
        comboBox.setValue(UserRole.BIDDER);
    }

    @FXML
    private void handleRegister() {
        String user = username.getText();
        String name = fullname.getText();
        String mail = email.getText();
        String pass = password.getText();
        String confirm = confirmPassword.getText();
        UserRole role = comboBox.getValue();

        // 1. Kiểm tra không được để trống
        if (user.isEmpty() || name.isEmpty() || mail.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 2. Kiểm tra khớp mật khẩu
        if (!pass.equals(confirm)) {
            showAlert("Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        // 3. Kiểm tra định dạng email
        if (!mail.contains("@") || !mail.contains(".")) {
            showAlert("Lỗi", "Định dạng email không hợp lệ!");
            return;
        }
        // 4.Nếu OK, gọi Service để gửi lên Server
        Map<String, String> data = new HashMap<>();
        data.put("username", user);
        data.put("password", pass);
        data.put("fullname", name);
        data.put("email", mail);
        data.put("role", role.name());

        try {
            BaseRequest request = new BaseRequest(Action.REGISTER, data);
            BaseResponse response = ClientNetworkService.getInstance().sendRequest(request);

            if(response != null && response.isSuccess()) {
                showAlert("Thành công", "Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.");
                goToLogin();
            }
            else {
                // Lấy message từ server gửi về
                String msgFromServer = (response != null) ? response.getMessage() : "Lỗi kết nối server";
                showAlert("Lỗi đăng ký", msgFromServer);
            }
        }
        catch (Exception e){
            showAlert("Lỗi", "Có lỗi xảy ra: " + e.getMessage());
        }

    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); //có thể đổi sang AlertType.ERROR nếu là lỗi
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToLogin() {
        try {
            Stage stage = (Stage) username.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            StageUtils.setMaximizedScene(stage,root);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}