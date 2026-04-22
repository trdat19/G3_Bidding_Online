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
            // 1. Kết nối
            Socket socket = new Socket("localhost", 8888);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // 2. Gửi Request
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", user);
            credentials.put("password", pass);

            BaseRequest loginReq = new BaseRequest("LOGIN", credentials);
            out.writeObject(loginReq);
            out.flush();

            // 3. Đợi phản hồi
            BaseResponse response = (BaseResponse) in.readObject();

            if (response.isSuccess()) {
                // --- PHẦN THAY ĐỔI Ở ĐÂY ---

                // Giả sử Server trả về thông tin User trong field Data dưới dạng Map
                // Hoặc nếu Server trả về một Object User, bạn hãy ép kiểu tương ứng
                Map<String, Object> userData = (Map<String, Object>) response.getData();
                String role = (String) userData.get("role");

                String fxmlPath = "";

                // Kiểm tra Role để chọn file FXML phù hợp
                // Lưu ý: Chuỗi so sánh phải khớp hoàn toàn với Enum hoặc String ở Database/Server
                switch (role) {
                    case "ADMIN":
                        fxmlPath = "/view/admin-dashboard.fxml";
                        break;
                    case "SELLER":
                        fxmlPath = "/view/seller-dashboard.fxml";
                        break;
                    case "BIDDER":
                        fxmlPath = "/view/auction-list-view.fxml";
                        break;
                    default:
                        errorLabel.setText("Vai trò người dùng không hợp lệ!");
                        return;
                }

                System.out.println("Đăng nhập thành công với quyền: " + role);
                loadScene(event, fxmlPath);

                // ---------------------------
            } else {
                errorLabel.setText(response.getMessage());
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