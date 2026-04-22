package client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import shared.request.BaseRequest; // Đảm bảo bạn đã import cái này
import shared.response.BaseResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = username.getText();
        String pass = password.getText();

        try {
            // 1. Kết nối (Nên dùng một lớp ConnectionManager để giữ socket này dùng lâu dài)
            Socket socket = new Socket("localhost", 8888); // Port phải khớp với Server
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // 2. Đóng gói dữ liệu Map để Server bóc được
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", user);
            credentials.put("password", pass);

            // 3. Gửi Request
            BaseRequest loginReq = new BaseRequest("LOGIN", credentials);
            out.writeObject(loginReq);
            out.flush();

            // 4. ĐỢI PHẢN HỒI TỪ SERVER
            BaseResponse response = (BaseResponse) in.readObject();

            if (response.isSuccess()) {
                System.out.println("Server xác nhận: " + response.getMessage());
                // Đăng nhập thành công thì mới chuyển cảnh
                loadScene(event, "/view/auction-list-view.fxml");
            } else {
                errorLabel.setText(response.getMessage()); // Hiển thị lỗi từ Server (ví dụ: "Sai tài khoản")
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Không kết nối được tới Server!");
        }
    }

    // Hàm phụ để code gọn hơn
    private void loadScene(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}