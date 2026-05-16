package client.service;

import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse; // Sửa lại đúng class phản hồi

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientNetworkService {
    // 1. Singleton Pattern
    private static ClientNetworkService instance;
    private Socket clientSocket;
    private final String host = "localhost";
    private final int port = 8888;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // Constructor private để ngăn chặn tạo nhiều đối tượng
    private ClientNetworkService() {
        connect();
    }

    public static synchronized ClientNetworkService getInstance() {
        if (instance == null) {
            instance = new ClientNetworkService();
        }
        return instance;
    }

    private void connect() {
        try {
            this.clientSocket = new Socket(host, port);
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println(">>> Đã kết nối đến server tại " + host + ":" + port + " <<<");
        } catch (Exception e) {
            System.err.println("Lỗi khi kết nối đến server: " + e.getMessage());
        }
    }

    // 2. Sửa lại kiểu trả về là BaseResponse
    public BaseResponse sendRequest(BaseRequest request) {
        try {
//            //tự động reconnect
//            if (out == null) {
//                connect();
//            }

            if (out != null) {
                out.writeObject(request);
                out.flush();
                out.reset();

                // Đợi phản hồi từ Server
                Object response = in.readObject();
                if (response instanceof BaseResponse) {
                    return (BaseResponse) response;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi/nhận dữ liệu: " + e.getMessage());
            // Nếu mất kết nối, có thể thử kết nối lại ở đây
        }
        return null;
    }

    public void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("Đã đóng kết nối.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}