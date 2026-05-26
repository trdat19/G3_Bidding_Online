package server.network;

import server.scheduler.AuctionScheduler;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private static final int PORT = 8888;
    private static final int MAX_CLIENTS = 50;

    private volatile boolean isRunning = false;
    private ServerSocket serverSocket;
    private final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);


    public void start() {
        boolean schedulerStarted = false;

        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println(">>> Server G3-Bidding đang chạy tại cổng: " + PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

            // Khởi động AuctionScheduler
            AuctionScheduler.getInstance().start();
            schedulerStarted = true;

            acceptLoop();
        }
        catch (BindException e) {
            System.err.println("Không thể khởi động Server: cổng " + PORT + " đang bị sử dụng!");
            System.err.println("Hãy tắt process đang chạy trên cổng " + PORT + " rồi chạy lại SocketServer.");
        }
        catch (IOException e) {
            if (isRunning) {
                System.err.println("Loi khi khoi dong server: " + e.getMessage());
                e.printStackTrace();
            }
        }
        finally {
            if (schedulerStarted) {
                AuctionScheduler.getInstance().stop();
            }
            clientPool.shutdown();
        }
    }

    private void acceptLoop() {
        while (isRunning) {
            try {

                // Đợi Client kết nối
                Socket clientSocket = serverSocket.accept();
                System.out.println(">>> Có kết nối mới từ: " + clientSocket.getInetAddress());

                ClientConnectionHandler handler = new ClientConnectionHandler(clientSocket);
                clientPool.submit(handler);

            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Lỗi khi chấp nhận kết nối: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void stop() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            clientPool.shutdownNow();
            AuctionScheduler.getInstance().stop();

            System.out.println("Server đã dừng.");
        }
        catch (IOException e) {
            System.err.println("Lỗi khi dừng server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketServer server = new SocketServer();
        server.start(); // Gọi hàm start để kích hoạt toàn bộ hệ thống
    }
}
