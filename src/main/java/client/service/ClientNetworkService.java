package client.service;

import shared.config.AppConfig;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClientNetworkService {
    private static ClientNetworkService instance;

    private static final int DEFAULT_PORT = AppConfig.getServerPort();
    private static final String DEFAULT_HOST = AppConfig.getServerHost();

    private final String host;
    private final int port;

    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private volatile boolean running = false;

    private final BlockingQueue<BaseResponse> responseQueue = new LinkedBlockingQueue<>();
    private final List<Consumer<BaseResponse>> eventListeners = new CopyOnWriteArrayList<>();

    private ClientNetworkService() {
        this.host = System.getProperty("server.host", DEFAULT_HOST);
        this.port = Integer.parseInt(System.getProperty("server.port", String.valueOf(DEFAULT_PORT)));
        connect();
    }

    public static ClientNetworkService getInstance() {
        if (instance == null) {
            synchronized (ClientNetworkService.class) {
                if (instance == null) {
                    instance = new ClientNetworkService();
                }
            }
        }
        return instance;
    }

    private void connect() {
        try {
            this.clientSocket = new Socket(host, port);

            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.out.flush();

            this.in = new ObjectInputStream(clientSocket.getInputStream());

            this.running = true;
            startListening();

            System.out.println(">>> Đã kết nối đến server tại " + host + ":" + port + " <<<");

        } catch (Exception e) {
            System.err.println("Lỗi khi kết nối đến server " + host + ":" + port);
            System.err.println("Chi tiết lỗi: " + e.getMessage());
        }
    }

    // Responses do not include a request id, so requests must wait in order.
    public synchronized BaseResponse sendRequest(BaseRequest request) {
        if (!isConnected()) {
            System.err.println("Chưa kết nối tới server.");
            return null;
        }

        try {
            synchronized (out) {
                out.writeObject(request);
                out.flush();
                out.reset();
            }

            BaseResponse response = responseQueue.poll(10, TimeUnit.SECONDS);

            if (response == null) {
                System.err.println("Server không phản hồi trong 10 giây.");
            }

            return response;

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi request: " + e.getMessage());
            closeConnection();
            return null;
        }
    }

    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            while (running && isConnected()) {
                try {
                    Object obj = in.readObject();

                    if (obj instanceof BaseResponse response) {
                        handleResponse(response);
                    }

                } catch (Exception e) {
                    if (running) {
                        System.err.println("Realtime listener stopped: " + e.getMessage());
                    }
                    break;
                }
            }
        });

        listenerThread.setName("Client-Network-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleResponse(BaseResponse response) {
        /*
         * Nếu response có action thì coi là realtime event.
         * Nếu response không có action thì coi là response trả về cho sendRequest().
         */
        if (response.getAction() != null && !response.getAction().isBlank()) {
            for (Consumer<BaseResponse> listener : eventListeners) {
                listener.accept(response);
            }
        } else {
            responseQueue.offer(response);
        }
    }

    public void addEventListener(Consumer<BaseResponse> listener) {
        if (listener != null) {
            eventListeners.add(listener);
        }
    }

    public void removeEventListener(Consumer<BaseResponse> listener) {
        eventListeners.remove(listener);
    }

    public boolean isConnected() {
        return clientSocket != null
                && clientSocket.isConnected()
                && !clientSocket.isClosed();
    }

    public void closeConnection() {
        running = false;

        try {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }

            System.out.println("Đã đóng kết nối.");

        } catch (Exception e) {
            System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
        }
    }
}
