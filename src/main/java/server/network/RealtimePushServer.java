package server.network;


import shared.response.BaseResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RealtimePushServer {
    // Để gửi riêng
    private static final Map<String, ClientConnectionHandler> userRegistry = new ConcurrentHashMap<>();
    // Để gửi theo phòng
    private static final Map<Integer, List<ClientConnectionHandler>> auctionSubscribers = new ConcurrentHashMap<>();


    // ---PHƯƠNG THỨC CHÍNH---
    // Gửi tin nhắn cho 1 người
    public static void pushToUser(String userId, BaseResponse event) {
        ClientConnectionHandler handler = userRegistry.get(userId);
        if (handler != null) {
            handler.sendResponse(event);
        }
    }
    // Gửi tin nhắn cho cả phòng
    public static void pushToAuctionSubscribers(int auctionId, BaseResponse event) {
        List<ClientConnectionHandler> subscribers = auctionSubscribers.get(auctionId);
        if (subscribers != null && !subscribers.isEmpty()) {
            System.out.println(">>> Đang đẩy tin Realtime tới " + subscribers.size() + " người xem phiên #" + auctionId);
            // Duyệt danh sách và bảo từng Handler đẩy dữ liệu xuống
            for (ClientConnectionHandler handler : subscribers) {
                handler.sendResponse(event);
            }
        }
    }

    // --- HÀM QUẢN LÝ (GỌI TỪ CONTROLLER/SERVICE ) ---

    public static void registerUser(String userId, ClientConnectionHandler handler) {
        userRegistry.put(userId, handler);
    }

    public static void subscribeToAuction(int auctionId, ClientConnectionHandler handler) {
        // Nếu phòng chưa có ai thì tạo mới, có rồi thì thêm vào danh sách
        auctionSubscribers.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public static void removeConnection(ClientConnectionHandler handler) {
        // Dọn dẹp danh bạ khi có người ngắt kết nối
        userRegistry.values().remove(handler);
        auctionSubscribers.values().forEach(list -> list.remove(handler));
    }
}
