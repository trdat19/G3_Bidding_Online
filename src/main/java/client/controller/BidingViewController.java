package client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.Action;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BidingViewController {

    @FXML private Label lblCurrentPrice; // Nhãn hiển thị giá hiện tại
    @FXML private TextField txtBidAmount; // Ô nhập số tiền muốn đặt
    @FXML private Button btnPlaceBid;    // Nút bấm đặt giá

    // Giả sử Socket đã được khởi tạo ở một lớp quản lý kết nối chung
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void initialize() {
        // 1. KÍCH HOẠT LUỒNG REALTIME NGAY KHI MỞ MÀN HÌNH
        startRealtimeListener();

        // 2. XỬ LÝ SỰ KIỆN BẤM NÚT ĐẶT GIÁ
        btnPlaceBid.setOnAction(event -> {
            sendBidRequest();
        });
    }

    private void startRealtimeListener() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof BaseResponse) {
                        BaseResponse response = (BaseResponse) obj;

                        // Nếu Server báo có giá mới (từ RealtimePushServer đẩy về)
                        if ("NEW_BID_UPDATE".equals(response.getAction())) {
                            Platform.runLater(() -> {
                                // Cập nhật số tiền mới lên màn hình
                                lblCurrentPrice.setText(response.getData().toString());
                                System.out.println(">>> Realtime: Đã cập nhật giá mới từ Server!");
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Mất kết nối Realtime.");
            }
        }).start();
    }

    private void sendBidRequest() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            // Gửi lệnh PLACE_BID lên Server
            BaseRequest req = new BaseRequest(Action.PLACE_BID, amount);
            out.writeObject(req);
            out.flush();
        } catch (Exception e) {
            System.out.println("Lỗi gửi lệnh đặt giá.");
        }
    }
}