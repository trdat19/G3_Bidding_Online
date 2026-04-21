package server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketSever {
    private static final int PORT = 8888;
    public boolean isRunning = false;
    public ServerSocket serverSocket;

    private void start()
    {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang lắng nghe tại cổng: " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Chấp nhận kết nối từ Client
                // Chuyển kết nối cho Handler để xử lý đa luồng
                new Thread(new ClientConnectionHandler(clientSocket)).start();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    private void acceptLoop()
    {
        while (isRunning) {
            try {
                // Đợi Client kết nối
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> Có kết nối mới từ: " + clientSocket.getInetAddress());

                // Giao cho Handler chạy trong một Thread riêng
                ClientConnectionHandler handler = new ClientConnectionHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();

            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Lỗi khi chấp nhận kết nối: " + e.getMessage());
                }
            }
        }
    }
    private void stop()
    {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            System.out.println("Server đã dừng.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        SocketSever server = new SocketSever();
        server.start(); // Gọi hàm start để kích hoạt toàn bộ hệ thống
    }
}
