package server.network;

import server.scheduler.AuctionScheduler;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private static final int DEFAULT_PORT = 8888;
    private static final int MAX_CLIENTS = 50;
    private static final String DEFAULT_BIND_HOST = "0.0.0.0";

    private final String bindHost;
    private final int port;
    private volatile boolean isRunning = false;
    private ServerSocket serverSocket;
    private final ExecutorService clientPool = Executors.newFixedThreadPool(MAX_CLIENTS);

    public SocketServer() {
        this.bindHost = readConfig("server.bindHost", "SERVER_BIND_HOST", DEFAULT_BIND_HOST);
        this.port = Integer.parseInt(readConfig("server.port", "SERVER_PORT", String.valueOf(DEFAULT_PORT)));
    }

    public void start() {
        boolean schedulerStarted = false;

        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(bindHost, port));
            isRunning = true;
            System.out.println(">>> Server G3-Bidding đang chạy tại " + bindHost + ":" + port);

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

            // Khởi động AuctionScheduler
            AuctionScheduler.getInstance().start();
            schedulerStarted = true;

            acceptLoop();
        }
        catch (BindException e) {
            System.err.println("Không thể khởi động Server: cổng " + port + " đang bị sử dụng!");
            System.err.println("Hãy tắt process đang chạy trên cổng " + port + " rồi chạy lại SocketServer.");
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

    private String readConfig(String propertyName, String envName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return defaultValue;
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
