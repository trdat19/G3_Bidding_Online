package client.service;

import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse; // Sửa lại đúng class phản hồi
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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

    private final BlockingQueue<BaseResponse> responseQueue = new LinkedBlockingQueue<>();
    private final List<Consumer<BaseResponse>> eventListeners = new CopyOnWriteArrayList<>();

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
            startListening();
            System.out.println(">>> Đã kết nối đến server tại " + host + ":" + port + " <<<");
        } catch (Exception e) {
            System.err.println("Lỗi khi kết nối đến server: " + e.getMessage());
        }
    }

    // 2. Sửa lại kiểu trả về là BaseResponse
    public BaseResponse sendRequest(BaseRequest request) {
        try {
            if (out != null)
            {
                synchronized (out)
                {
                    out.writeObject(request);
                    out.flush();
                    out.reset();
                }
                    return responseQueue.poll(10, TimeUnit.SECONDS);

            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi request : " + e.getMessage());
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

    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (clientSocket != null && !clientSocket.isClosed()) {
                    Object obj = in.readObject();

                    if (obj instanceof BaseResponse response) {
                        if ("NEW_BID".equals(response.getAction())) {
                            for (Consumer<BaseResponse> listener : eventListeners) {
                                listener.accept(response);
                            }
                        } else {
                            responseQueue.offer(response);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Realtime listener stopped: " + e.getMessage());
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void addEventListener(Consumer<BaseResponse> listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(Consumer<BaseResponse> listener) {
        eventListeners.remove(listener);
    }
}