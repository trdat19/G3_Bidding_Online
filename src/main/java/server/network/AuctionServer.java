package server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AuctionServer {
    private static final int PORT = 8080; // Cổng để Client gọi vào

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(">>> SERVER ĐẤU GIÁ ĐANG CHẠY TẠI CỔNG: " + PORT);

            while (true) {
                // Khi có 1 Client kết nối, dòng này sẽ được kích hoạt
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> Có người mới kết nối: " + clientSocket.getInetAddress());

                // Giao khách hàng này cho một "nhân viên" (ClientHandler) xử lý riêng
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start(); // Chạy đa luồng để không ai phải đợi ai
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}