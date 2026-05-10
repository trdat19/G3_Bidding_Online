package server.network;

import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import java.io.*;
import java.net.Socket;

public class ClientConnectionHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private server.model.user.User user;

    public ClientConnectionHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("Đã thiết lập Stream cho: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            System.err.println("Lỗi khởi tạo Stream: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {

            RealtimePushServer.registerUser(clientSocket.getInetAddress().toString(), this);

            while (!clientSocket.isClosed()) {
                Object obj = in.readObject(); // Đọc trực tiếp ở đây hoặc dùng readRequest
                if (obj instanceof BaseRequest) {
                    dispatch((BaseRequest) obj);
                }
            }
        } catch (EOFException e) {
            System.out.println("Client đã ngắt kết nối (EOF).");
        } catch (Exception e) {
            System.out.println("Lỗi kết nối: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public void dispatch(BaseRequest request) {
        // Truyền 'this' để Router/Controller có thể dùng handler này đăng ký Realtime
        BaseResponse response = RequestRouter.route(request, this);
        sendResponse(response);
    }

    public void sendResponse(Object response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset(); // Quan trọng: Tránh cache object cũ khi gửi nhiều lần
            }
        } catch (IOException e) {
            System.err.println("Lỗi gửi response: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            RealtimePushServer.removeConnection(this);
            if (clientSocket != null) clientSocket.close();
            System.out.println(">>> Đã đóng kết nối.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public server.model.user.User getUser()
    {
        return user;
    }

    public void setUsetr(server.model.user.User user)
    {
        this.user = user;
    }

}
