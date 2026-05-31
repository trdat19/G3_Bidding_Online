package server.network;

import shared.dto.response.BaseResponse;
import shared.enums.UserRole;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RealtimePushServer {

    // userId -> handler
    private static final Map<Long, ClientConnectionHandler> userRegistry =
            new ConcurrentHashMap<>();

    // auctionId -> set các client đang xem phiên đó
    private static final Map<Long, Set<ClientConnectionHandler>> auctionSubscribers =
            new ConcurrentHashMap<>();

    // các client đang xem danh sách phiên đấu giá, ví dụ BidderDashboard
    private static final Set<ClientConnectionHandler> auctionListSubscribers =
            ConcurrentHashMap.newKeySet();

    private RealtimePushServer() {
        // Utility class, không cho tạo object
    }

    public static void registerUser(Long userId, ClientConnectionHandler handler) {
        if (userId == null || handler == null) {
            return;
        }

        ClientConnectionHandler oldHandler = userRegistry.put(userId, handler);

        // Nếu user login lại bằng connection mới, dọn connection cũ khỏi các room realtime
        if (oldHandler != null && oldHandler != handler) {
            removeFromAllRooms(oldHandler);
        }

        System.out.printf(">>> [Realtime] User ID %d đã online. Tổng online: %d%n",
                userId, userRegistry.size());
    }

    public static void pushToUser(Long userId, BaseResponse event) {
        if (userId == null || event == null) {
            return;
        }

        ClientConnectionHandler handler = userRegistry.get(userId);

        if (handler == null) {
            System.out.println(">>> [Realtime] User ID: " + userId + " không online!");
            return;
        }

        handler.sendResponse(event);

        System.out.println(">>> [Realtime] Đã gửi riêng tới User ID: " + userId
                + " Message: " + event.getMessage());
    }

    public static void pushToRole(UserRole role, BaseResponse event) {
        if (role == null || event == null) {
            return;
        }

        for (ClientConnectionHandler handler : userRegistry.values()) {
            if (handler.getUser() != null && handler.getUser().getRole() == role) {
                handler.sendResponse(event);
            }
        }
    }

    public static void subscribeToAuction(Long auctionId, ClientConnectionHandler handler) {
        if (auctionId == null || handler == null) {
            return;
        }

        Set<ClientConnectionHandler> room = auctionSubscribers.computeIfAbsent(
                auctionId,
                id -> ConcurrentHashMap.newKeySet()
        );

        boolean added = room.add(handler);

        if (added) {
            System.out.printf(">>> [Realtime] Phiên #%d: %d người đang xem%n",
                    auctionId, room.size());
        }
    }

    public static void unsubscribeFromAuction(Long auctionId, ClientConnectionHandler handler) {
        if (auctionId == null || handler == null) {
            return;
        }

        Set<ClientConnectionHandler> room = auctionSubscribers.get(auctionId);

        if (room == null) {
            return;
        }

        room.remove(handler);

        if (room.isEmpty()) {
            auctionSubscribers.remove(auctionId, room);
        }

        System.out.printf(">>> [Realtime] Phiên #%d: còn %d người đang xem%n",
                auctionId, room.size());
    }

    public static void pushToAuctionSubscribers(Long auctionId, BaseResponse event) {
        if (auctionId == null || event == null) {
            return;
        }

        Set<ClientConnectionHandler> subscribers = auctionSubscribers.get(auctionId);

        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        System.out.println(">>> [Realtime] Đẩy tin tới " + subscribers.size()
                + " người đang xem phiên #" + auctionId);

        for (ClientConnectionHandler handler : subscribers) {
            handler.sendResponse(event);
        }
    }

    public static void subscribeToAuctionList(ClientConnectionHandler handler) {
        if (handler == null) {
            return;
        }

        boolean added = auctionListSubscribers.add(handler);

        if (added) {
            System.out.println(">>> [Realtime] Bidder dashboard subscribers: "
                    + auctionListSubscribers.size());
        }
    }

    public static void unsubscribeFromAuctionList(ClientConnectionHandler handler) {
        if (handler == null) {
            return;
        }

        auctionListSubscribers.remove(handler);
    }

    public static void pushToAuctionListSubscribers(BaseResponse event) {
        if (event == null) {
            return;
        }

        for (ClientConnectionHandler handler : auctionListSubscribers) {
            handler.sendResponse(event);
        }
    }

    public static void removeConnection(ClientConnectionHandler handler) {
        if (handler == null) {
            return;
        }

        userRegistry.entrySet().removeIf(entry -> entry.getValue() == handler);

        removeFromAllRooms(handler);

        System.out.printf(">>> [Realtime] Một client ngắt kết nối. Còn %d online.%n",
                userRegistry.size());
    }

    private static void removeFromAllRooms(ClientConnectionHandler handler) {
        auctionSubscribers.forEach((auctionId, room) -> {
            room.remove(handler);

            if (room.isEmpty()) {
                auctionSubscribers.remove(auctionId, room);
            }
        });

        auctionListSubscribers.remove(handler);
    }

    public static int countOnlineUsers() {
        return userRegistry.size();
    }

    public static int countRoomSubscribers(Long auctionId) {
        Set<ClientConnectionHandler> room = auctionSubscribers.get(auctionId);
        return room != null ? room.size() : 0;
    }

    public static Set<Long> getOnlineUsers() {
        return userRegistry.keySet();
    }

    public static void printStatus() {
        System.out.println("=== [Realtime] Trạng thái hiện tại ===");
        System.out.println("Online: " + userRegistry.keySet());

        auctionSubscribers.forEach((id, room) ->
                System.out.printf("  Phiên #%d: %d người xem%n", id, room.size()));

        System.out.println("Bidder dashboard subscribers: " + auctionListSubscribers.size());
        System.out.println("======================================");
    }
}