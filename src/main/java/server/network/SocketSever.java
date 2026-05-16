package server.network;

import server.scheduler.AuctionScheduler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketSever {
    private static final int PORT = 8888;
    public boolean isRunning = false;
    public ServerSocket serverSocket;


    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println(">>> Server G3-Bidding đang chạy tại cổng: " + PORT);

            // Khởi động AuctionScheduler
            AuctionScheduler.getInstance().start();

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> Kết nối mới từ: " + clientSocket.getInetAddress());

                new Thread(new ClientConnectionHandler(clientSocket)).start();
            }
        }
        catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        }
        finally {
            AuctionScheduler.getInstance().stop();
        }
    }
    private void acceptLoop()
    {
        while (isRunning) {
            try {
                // Đợi Client kết nối
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> Có kết nối mới từ: " + clientSocket.getInetAddress());

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
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        SocketSever server = new SocketSever();
        server.start(); // Gọi hàm start để kích hoạt toàn bộ hệ thống
    }
}
